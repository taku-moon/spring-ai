package com.example.springaitutorial.chat.provider;

import java.util.Set;

public enum ProviderType {

	OPEN_AI(
		"gpt-4.1-nano",
		Set.of("gpt-4.1-nano", "gpt-4.1-mini")
	),
	GOOGLE_GENAI(
		"gemini-2.5-flash",
		Set.of("gemini-2.5-flash")
	);

	private final String defaultModel;
	private final Set<String> supportedModels;

	ProviderType(String defaultModel, Set<String> supportedModels) {
		this.defaultModel = defaultModel;
		this.supportedModels = supportedModels;
	}

	public String defaultModel() {
		return defaultModel;
	}

	public boolean supportsModel(String model) {
		if (model == null || model.isBlank()) {
			return false;
		}
		return supportedModels.contains(model.trim().toLowerCase());
	}
}
