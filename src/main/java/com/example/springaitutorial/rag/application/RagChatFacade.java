package com.example.springaitutorial.rag.application;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import com.example.springaitutorial.chat.application.ChatFacade;
import com.example.springaitutorial.chat.application.result.ChatResult;
import com.example.springaitutorial.rag.application.policy.RagChatSystemMessagePolicy;
import com.example.springaitutorial.rag.application.result.RagChatResult;
import com.example.springaitutorial.rag.storage.InMemoryDocumentVectorStore;

@Service
public class RagChatFacade {

	private static final int TOP_K = 5;

	private final InMemoryDocumentVectorStore documentVectorStore;
	private final ChatFacade chatFacade;

	public RagChatFacade(InMemoryDocumentVectorStore documentVectorStore, ChatFacade chatFacade) {
		this.documentVectorStore = documentVectorStore;
		this.chatFacade = chatFacade;
	}

	public RagChatResult chat(String documentId, String userMessage, String model) {
		validate(documentId, userMessage);

		List<Document> contexts = documentVectorStore.searchSimilar(documentId, userMessage, TOP_K);

		String systemMessage = RagChatSystemMessagePolicy.build(contexts);

		ChatResult chatResult = chatFacade.chat(systemMessage, userMessage, model);

		return new RagChatResult(
			chatResult.getProvider(),
			chatResult.getModel(),
			chatResult.getAnswer(),
			documentId,
			contexts.size()
		);
	}

	private void validate(String documentId, String userMessage) {
		if (documentId == null || documentId.isBlank()) {
			throw new IllegalArgumentException("입력된 documentId가 없습니다.");
		}
		if (userMessage == null || userMessage.isBlank()) {
			throw new IllegalArgumentException("입력된 메시지가 없습니다.");
		}
	}
}
