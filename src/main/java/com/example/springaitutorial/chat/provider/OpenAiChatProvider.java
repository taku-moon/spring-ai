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

	private final OpenAiChatModel chatModel;

	public OpenAiChatProvider(OpenAiChatModel chatModel) {
		this.chatModel = chatModel;
	}

	@Override
	public String name() {
		return "Open_AI_Chat";
	}

	@Override
	public boolean supports(String model) {
		if (model == null) {
			return false;
		}
		String modelName = model.trim().toLowerCase();
		return modelName.startsWith("gpt-") || modelName.startsWith("chatgpt-") || modelName.matches("^o\\d.*");
	}

	@Override
	public String defaultModel() {
		return "gpt-4.1-nano";
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
		return chatModel.call(prompt);
	}
}
