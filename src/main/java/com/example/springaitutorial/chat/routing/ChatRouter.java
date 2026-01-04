package com.example.springaitutorial.chat.routing;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.springaitutorial.chat.provider.ChatProvider;

@Service
public class ChatRouter {

	private final List<ChatProvider> providers;

	public ChatRouter(List<ChatProvider> providers) {
		this.providers = providers;
	}

	public ChatProvider defaultProvider() {
		return providers.stream()
			.filter(provider -> "Open_AI_Chat".equalsIgnoreCase(provider.name()))
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("등록된 ChatProvider가 없습니다."));
	}

	public ChatProvider routeByModel(String model) {
		for (ChatProvider provider : providers) {
			if (provider.supports(model)) {
				return provider;
			}
		}
		throw new IllegalArgumentException(model + "은(는) 지원하지 않는 모델입니다.");
	}
}
