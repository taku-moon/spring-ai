package com.example.springaitutorial.chat.provider;

import java.util.List;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.stereotype.Component;

@Component
public class GoogleGenAiChatProvider implements ChatProvider {

	private final GoogleGenAiChatModel chatModel;

	public GoogleGenAiChatProvider(GoogleGenAiChatModel chatModel) {
		this.chatModel = chatModel;
	}

	@Override
	public String name() {
		return "Google_Gen_Ai_Chat";
	}

	@Override
	public boolean supports(String model) {
		if (model == null) {
			return false;
		}
		String modelName = model.trim().toLowerCase();
		return modelName.startsWith("gemini");
	}

	@Override
	public String defaultModel() {
		return "gemini-2.5-flash";
	}

	@Override
	public ChatResponse chat(String systemMessage, String userMessage, String model) {
		List<Message> messages = List.of(
			new SystemMessage(systemMessage),
			new UserMessage(userMessage)
		);

		GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder()
			.model(model)
			.build();

		Prompt prompt = new Prompt(messages, options);
		return chatModel.call(prompt);
	}
}
