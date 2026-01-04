package com.example.springaitutorial.chat.provider;

import org.springframework.ai.chat.model.ChatResponse;

public interface ChatProvider {

	ProviderType type();

	String defaultModel();

	boolean supports(String model);

	ChatResponse chat(String systemMessage, String userMessage, String model);
}
