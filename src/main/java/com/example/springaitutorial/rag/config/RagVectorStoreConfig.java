package com.example.springaitutorial.rag.config;

import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RagVectorStoreConfig {

	@Bean
	public VectorStore vectorStore(OpenAiEmbeddingModel embeddingModel) {
		return SimpleVectorStore.builder(embeddingModel).build();
	}
}
