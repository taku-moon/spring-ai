package com.example.springaitutorial.rag.indexing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

@Component
public class DocumentMetadataEnricher {

	public List<Document> enrich(String documentId, String originalFilename, List<Document> documents) {
		List<Document> result = new ArrayList<>();
		for (int i = 0; i < documents.size(); i++) {
			Document document = documents.get(i);

			Map<String, Object> metadata = new HashMap<>(document.getMetadata());
			metadata.put("documentId", documentId);
			metadata.put("originalFilename", originalFilename);
			metadata.put("chunkIndex", i);

			result.add(new Document(document.getText(), metadata));
		}
		return result;
	}
}
