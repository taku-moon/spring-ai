package com.example.springaitutorial.service;

import java.util.List;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

	private final OpenAiApi openAiApi;

	public ChatService(OpenAiApi openAiApi) {
		this.openAiApi = openAiApi;
	}

	public ChatResponse openAiChat(String systemMessage, String userMessage, String model) {
		try {
			List<Message> messages = List.of(
				new SystemMessage(systemMessage),
				new UserMessage(userMessage)
			);

			ChatOptions chatOptions = ChatOptions.builder()
				.model(model)
				//.temperature(1.0)
				.build();

			Prompt prompt = new Prompt(messages, chatOptions);

			OpenAiChatModel chatModel = OpenAiChatModel.builder()
				.openAiApi(openAiApi)
				.build();

			return chatModel.call(prompt);
		} catch (RuntimeException e) {
			return null;
		}
	}
}
