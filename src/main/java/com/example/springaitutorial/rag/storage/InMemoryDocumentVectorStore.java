package com.example.springaitutorial.rag.storage;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter.Expression;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Component;

@Component
public class InMemoryDocumentVectorStore implements DocumentVectorStore {

	private final VectorStore vectorStore;

	public InMemoryDocumentVectorStore(VectorStore vectorStore) {
		this.vectorStore = vectorStore;
	}

	@Override
	public void saveAll(List<Document> documents) {
		vectorStore.add(documents);
	}

	@Override
	public List<Document> searchSimilar(String documentId, String userMessage, int topK) {
		FilterExpressionBuilder builder = new FilterExpressionBuilder();
		Expression filter = builder.eq("documentId", documentId).build();

		SearchRequest request = SearchRequest.builder()
			.filterExpression(filter)
			.query(userMessage)
			.topK(topK)
			.build();

		return vectorStore.similaritySearch(request);
	}
}
