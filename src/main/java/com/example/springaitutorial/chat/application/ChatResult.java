package com.example.springaitutorial.chat.application;

public final class ChatResult {

	private final String provider;
	private final String model;
	private final String answer;

	public ChatResult(String provider, String model, String answer) {
		this.provider = provider;
		this.model = model;
		this.answer = answer;
	}

	public String getProvider() {
		return provider;
	}

	public String getModel() {
		return model;
	}

	public String getAnswer() {
		return answer;
	}
}
