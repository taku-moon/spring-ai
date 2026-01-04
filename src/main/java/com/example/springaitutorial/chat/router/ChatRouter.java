package com.example.springaitutorial.chat.router;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.springaitutorial.chat.provider.ChatProvider;
import com.example.springaitutorial.chat.provider.ProviderType;

@Service
public class ChatRouter {

	private final List<ChatProvider> chatProviders;

	public ChatRouter(List<ChatProvider> chatProviders) {
		this.chatProviders = chatProviders;
	}

	public ChatProvider defaultProvider() {
		for (ChatProvider provider : chatProviders) {
			if (provider.type() == ProviderType.OPEN_AI) {
				return provider;
			}
		}
		throw new IllegalStateException("기본 ChatProvider(OPEN_AI)를 찾을 수 없습니다.");
	}

	public ChatProvider routeByModel(String model) {
		for (ChatProvider provider : chatProviders) {
			if (provider.supports(model)) {
				return provider;
			}
		}
		throw new IllegalArgumentException(model + "은(는) 지원하지 않는 모델입니다.");
	}
}
