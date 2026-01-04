package com.example.springaitutorial.chat.application;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

import com.example.springaitutorial.chat.provider.ChatProvider;
import com.example.springaitutorial.chat.routing.ChatRouter;

@Service
public class ChatFacade {

	private final ChatRouter router;

	public ChatFacade(ChatRouter router) {
		this.router = router;
	}

	public ChatResult chat(String systemMessage, String userMessage, String model) {
		ChatProvider provider;
		String resolvedModel = model;

		if (resolvedModel == null || resolvedModel.isBlank()) {
			provider = router.defaultProvider();
			resolvedModel = provider.defaultModel();
		} else {
			provider = router.routeByModel(resolvedModel);
		}

		ChatResponse response = provider.chat(systemMessage, userMessage, resolvedModel);

		return new ChatResult(
			provider.name(),
			response.getMetadata().getModel(),
			response.getResult().getOutput().getText()
		);
	}
}
