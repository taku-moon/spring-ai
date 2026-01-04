package com.example.springaitutorial.chat.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.springaitutorial.chat.application.ChatFacade;
import com.example.springaitutorial.chat.application.result.ChatResult;
import com.example.springaitutorial.chat.controller.dto.ChatMessageRequest;
import com.example.springaitutorial.common.ApiResponse;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

	private final ChatFacade chatFacade;

	public ChatController(ChatFacade chatFacade) {
		this.chatFacade = chatFacade;
	}

	@PostMapping("/messages")
	public ResponseEntity<ApiResponse<Map<String, Object>>> createMessage(@RequestBody ChatMessageRequest request) {
		ChatResult result = chatFacade.chat(request.getUserMessage(), request.getModel());

		Map<String, Object> data = Map.of(
			"provider", result.getProvider(),
			"model", result.getModel(),
			"answer", result.getAnswer()
		);

		return ResponseEntity.ok(ApiResponse.ok(data));
	}
}
