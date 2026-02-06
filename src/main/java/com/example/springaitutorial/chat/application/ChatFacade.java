package com.example.springaitutorial.chat.application;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

import com.example.springaitutorial.chat.application.policy.ChatSystemMessagePolicy;
import com.example.springaitutorial.chat.application.result.ChatResult;
import com.example.springaitutorial.chat.provider.ChatProvider;
import com.example.springaitutorial.chat.router.ChatRouter;

@Service
public class ChatFacade {

	private final ChatRouter chatRouter;

	public ChatFacade(ChatRouter chatRouter) {
		this.chatRouter = chatRouter;
	}

	public ChatResult chat(String userMessage, String model) {
		String systemMessage = ChatSystemMessagePolicy.build();
		return chat(systemMessage, userMessage, model);
	}

	public ChatResult chat(String systemMessage, String userMessage, String model) {
		if (userMessage == null || userMessage.isBlank()) {
			throw new IllegalArgumentException("입력된 메시지가 없습니다.");
		}

		ChatProvider provider;
		String resolvedModel = model;

		if (resolvedModel == null || resolvedModel.isBlank()) {
			provider = chatRouter.defaultProvider();
			resolvedModel = provider.defaultModel();
		} else {
			provider = chatRouter.routeByModel(resolvedModel);
		}

		ChatResponse response = provider.chat(systemMessage, userMessage, resolvedModel);

		return new ChatResult(
			provider.type().name(),
			response.getMetadata().getModel(),
			response.getResult().getOutput().getText()
		);
	}
}
