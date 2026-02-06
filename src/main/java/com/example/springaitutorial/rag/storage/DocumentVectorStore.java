package com.example.springaitutorial.rag.storage;

import java.util.List;

import org.springframework.ai.document.Document;

public interface DocumentVectorStore {

	void saveAll(List<Document> documents);

	List<Document> searchSimilar(String documentId, String query, int topK);
}
