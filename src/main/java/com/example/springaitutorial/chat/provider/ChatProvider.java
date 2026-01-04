package com.example.springaitutorial.chat.provider;

import org.springframework.ai.chat.model.ChatResponse;

public interface ChatProvider {

	String name();

	boolean supports(String model);

	String defaultModel();

	ChatResponse chat(String systemMessage, String userMessage, String model);
}
