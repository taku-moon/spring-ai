package com.example.springaitutorial.controller;

import java.util.Map;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.springaitutorial.dto.ApiResponseDto;
import com.example.springaitutorial.dto.ChatRequestDto;
import com.example.springaitutorial.service.ChatService;

@RestController
@RequestMapping("/api")
public class ChatController {

	private final ChatService chatService;

	public ChatController(ChatService chatService) {
		this.chatService = chatService;
	}

	@PostMapping("/query")
	public ResponseEntity<ApiResponseDto<Map<String, Object>>> sendMessage(@RequestBody ChatRequestDto request) {
		if (request.getUserMessage() == null || request.getUserMessage().isBlank()) {
			return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ApiResponseDto.fail("입력 값이 없습니다."));
		}

		String systemMessage = "You are a helpful AI assistant.";

		try {
			ChatResponse response = chatService.openAiChat(systemMessage, request.getUserMessage(), request.getModel());

			if (response == null) {
				return ResponseEntity
					.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponseDto.fail("LLM 응답 생성 중 오류가 발생했습니다."));
			}

			String answer = response.getResult().getOutput().getText();
			return ResponseEntity.ok(ApiResponseDto.ok(Map.of("answer", answer)));
		} catch (RuntimeException e) {
			return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(
					ApiResponseDto.fail(e.getMessage() != null ? e.getMessage() : "알 수 없는 오류가 발생했습니다.")
				);
		}
	}
}
