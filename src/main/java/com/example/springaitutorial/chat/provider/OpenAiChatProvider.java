package com.example.springaitutorial.chat.provider;

import java.util.List;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;

@Component
public class OpenAiChatProvider implements ChatProvider {

	private final OpenAiChatModel openAiChatModel;

	public OpenAiChatProvider(OpenAiChatModel openAiChatModel) {
		this.openAiChatModel = openAiChatModel;
	}

	@Override
	public ProviderType type() {
		return ProviderType.OPEN_AI;
	}

	@Override
	public String defaultModel() {
		return type().defaultModel();
	}

	@Override
	public boolean supports(String model) {
		return type().supportsModel(model);
	}

	@Override
	public ChatResponse chat(String systemMessage, String userMessage, String model) {
		List<Message> messages = List.of(
			new SystemMessage(systemMessage),
			new UserMessage(userMessage)
		);

		OpenAiChatOptions options = OpenAiChatOptions.builder()
			.model(model)
			.build();

		Prompt prompt = new Prompt(messages, options);
		return openAiChatModel.call(prompt);
	}
}
