// ── Tab Navigation ──
document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
        document.querySelectorAll('.tab-panel').forEach(p => p.classList.remove('active'));
        btn.classList.add('active');
        document.getElementById('panel-' + btn.dataset.tab).classList.add('active');
    });
});

// ── Auto-resize Textarea ──
document.querySelectorAll('textarea').forEach(ta => {
    ta.addEventListener('input', () => {
        ta.style.height = 'auto';
        ta.style.height = Math.min(ta.scrollHeight, 120) + 'px';
    });
});

// ── Helpers ──
function appendMessage(containerId, type, text, meta) {
    const container = document.getElementById(containerId);
    const empty = container.querySelector('.empty-state');
    if (empty) empty.remove();

    const div = document.createElement('div');
    div.className = 'msg ' + type;
    div.textContent = text;

    if (meta) {
        const metaDiv = document.createElement('div');
        metaDiv.className = 'meta';
        metaDiv.textContent = meta;
        div.appendChild(metaDiv);
    }

    container.appendChild(div);
    container.scrollTop = container.scrollHeight;
}

function setTyping(id, show) {
    document.getElementById(id).classList.toggle('show', show);
}

// ══════════════════════════════
//  CHAT
// ══════════════════════════════
const chatInput = document.getElementById('chat-input');
const chatSend = document.getElementById('chat-send');
const chatModel = document.getElementById('chat-model');
let chatBusy = false;

async function sendChat() {
    const text = chatInput.value.trim();
    if (!text || chatBusy) return;

    chatBusy = true;
    chatSend.disabled = true;
    appendMessage('chat-messages', 'user', text);
    chatInput.value = '';
    chatInput.style.height = 'auto';
    setTyping('chat-typing', true);

    try {
        const res = await fetch('/api/chat/messages', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({userMessage: text, model: chatModel.value})
        });
        const json = await res.json();

        if (json.success) {
            appendMessage('chat-messages', 'assistant', json.data.answer, json.data.provider + ' / ' + json.data.model);
        } else {
            appendMessage('chat-messages', 'error', json.error || 'Unknown error');
        }
    } catch (e) {
        appendMessage('chat-messages', 'error', 'Request failed: ' + e.message);
    } finally {
        chatBusy = false;
        chatSend.disabled = false;
        setTyping('chat-typing', false);
        chatInput.focus();
    }
}

chatSend.addEventListener('click', sendChat);
chatInput.addEventListener('keydown', e => {
    if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        sendChat();
    }
});

// ══════════════════════════════
//  RAG
// ══════════════════════════════
const ragInput = document.getElementById('rag-input');
const ragSend = document.getElementById('rag-send');
const ragModel = document.getElementById('rag-model');
const fileInput = document.getElementById('file-input');
const uploadBox = document.getElementById('upload-box');
const uploadProgress = document.getElementById('upload-progress');
const docInfo = document.getElementById('doc-info');

let ragBusy = false;
let currentDocId = null;

// Drag & Drop
uploadBox.addEventListener('dragover', e => {
    e.preventDefault();
    uploadBox.classList.add('dragover');
});
uploadBox.addEventListener('dragleave', () => uploadBox.classList.remove('dragover'));
uploadBox.addEventListener('drop', e => {
    e.preventDefault();
    uploadBox.classList.remove('dragover');
    const file = e.dataTransfer.files[0];
    if (file && file.name.endsWith('.pdf')) uploadFile(file);
});

fileInput.addEventListener('change', () => {
    if (fileInput.files[0]) uploadFile(fileInput.files[0]);
});

async function uploadFile(file) {
    uploadBox.style.display = 'none';
    uploadProgress.classList.add('show');
    docInfo.classList.remove('show');

    const formData = new FormData();
    formData.append('file', file);

    try {
        const res = await fetch('/api/rag/documents', {method: 'POST', body: formData});
        const json = await res.json();

        if (json.success) {
            currentDocId = json.data.documentId;
            document.getElementById('doc-filename').textContent = file.name;
            document.getElementById('doc-id-display').textContent = currentDocId.substring(0, 8) + '...';
            document.getElementById('doc-chunks').textContent = json.data.chunkCount;

            docInfo.classList.add('show');
            ragInput.disabled = false;
            ragSend.disabled = false;

            appendMessage('rag-messages', 'system', 'Document uploaded: ' + file.name + ' (' + json.data.chunkCount + ' chunks)');
        } else {
            appendMessage('rag-messages', 'error', json.error || 'Upload failed');
            uploadBox.style.display = '';
        }
    } catch (e) {
        appendMessage('rag-messages', 'error', 'Upload failed: ' + e.message);
        uploadBox.style.display = '';
    } finally {
        uploadProgress.classList.remove('show');
    }
}

document.getElementById('doc-reset').addEventListener('click', () => {
    currentDocId = null;
    docInfo.classList.remove('show');
    uploadBox.style.display = '';
    fileInput.value = '';
    ragInput.disabled = true;
    ragSend.disabled = true;

    const container = document.getElementById('rag-messages');
    container.innerHTML = '<div class="empty-state">Upload a PDF document, then ask questions about it.</div>';
});

async function sendRag() {
    const text = ragInput.value.trim();
    if (!text || ragBusy || !currentDocId) return;

    ragBusy = true;
    ragSend.disabled = true;
    appendMessage('rag-messages', 'user', text);
    ragInput.value = '';
    ragInput.style.height = 'auto';
    setTyping('rag-typing', true);

    try {
        const res = await fetch('/api/rag/messages', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({documentId: currentDocId, userMessage: text, model: ragModel.value})
        });
        const json = await res.json();

        if (json.success) {
            appendMessage(
                'rag-messages',
                'assistant',
                json.data.answer,
                json.data.provider + ' / ' + json.data.model + ' | retrieved: ' + json.data.retrievedCount
            );
        } else {
            appendMessage('rag-messages', 'error', json.error || 'Unknown error');
        }
    } catch (e) {
        appendMessage('rag-messages', 'error', 'Request failed: ' + e.message);
    } finally {
        ragBusy = false;
        ragSend.disabled = false;
        setTyping('rag-typing', false);
        ragInput.focus();
    }
}

ragSend.addEventListener('click', sendRag);
ragInput.addEventListener('keydown', e => {
    if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        sendRag();
    }
});
