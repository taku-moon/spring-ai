package com.example.springaitutorial.rag.controller;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.springaitutorial.common.ApiResponse;
import com.example.springaitutorial.rag.application.DocumentIndexingFacade;
import com.example.springaitutorial.rag.application.RagChatFacade;
import com.example.springaitutorial.rag.application.result.DocumentIndexingResult;
import com.example.springaitutorial.rag.application.result.RagChatResult;
import com.example.springaitutorial.rag.controller.dto.RagMessageRequest;

@RestController
@RequestMapping("/api/rag")
public class RagController {

	private final DocumentIndexingFacade documentIndexingFacade;
	private final RagChatFacade ragFacade;

	public RagController(DocumentIndexingFacade documentIndexingFacade, RagChatFacade ragFacade) {
		this.documentIndexingFacade = documentIndexingFacade;
		this.ragFacade = ragFacade;
	}

	@PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponse<Map<String, Object>>> uploadDocument(
		@RequestParam("file") MultipartFile file
	) {
		DocumentIndexingResult result = documentIndexingFacade.indexPdfDocument(file);

		Map<String, Object> data = Map.of(
			"documentId", result.getDocumentId(),
			"chunkCount", result.getChunkCount()
		);

		return ResponseEntity.ok(ApiResponse.ok(data));
	}

	@PostMapping("/messages")
	public ResponseEntity<ApiResponse<Map<String, Object>>> createMessage(@RequestBody RagMessageRequest request) {
		RagChatResult result = ragFacade.chat(request.getDocumentId(), request.getUserMessage(), request.getModel());

		Map<String, Object> data = Map.of(
			"provider", result.getProvider(),
			"model", result.getModel(),
			"answer", result.getAnswer(),
			"retrievedCount", result.getRetrievedCount()
		);

		return ResponseEntity.ok(ApiResponse.ok(data));
	}
}
