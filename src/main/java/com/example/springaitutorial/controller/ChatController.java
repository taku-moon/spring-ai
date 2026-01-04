package com.example.springaitutorial.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.springaitutorial.chat.application.ChatFacade;
import com.example.springaitutorial.chat.application.ChatResult;
import com.example.springaitutorial.dto.ApiResponse;
import com.example.springaitutorial.dto.ChatQueryRequest;

@RestController
@RequestMapping("/api")
public class ChatController {

	private final ChatFacade chatFacade;

	public ChatController(ChatFacade chatFacade) {
		this.chatFacade = chatFacade;
	}

	@PostMapping("/chat-query")
	public ResponseEntity<ApiResponse<Map<String, Object>>> sendMessage(@RequestBody ChatQueryRequest request) {
		if (request.getUserMessage() == null || request.getUserMessage().isBlank()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(ApiResponse.fail("입력 값이 없습니다."));
		}

		String systemMessage = "You are a helpful AI assistant.";

		try {
			ChatResult result = chatFacade.chat(systemMessage, request.getUserMessage(), request.getModel());

			Map<String, Object> data = Map.of(
				"provider", result.getProvider(),
				"model", result.getModel(),
				"answer", result.getAnswer()
			);

			return ResponseEntity.ok(ApiResponse.ok(data));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(ApiResponse.fail(e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponse.fail("LLM 응답 생성 중 오류가 발생했습니다."));
		}
	}
}
