package com.example.springaitutorial.rag.indexing;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;

@Component
public class DocumentSplitter {

	private final TokenTextSplitter splitter;

	public DocumentSplitter() {
		this.splitter = new TokenTextSplitter();
	}

	public List<Document> split(List<Document> documents) {
		return splitter.apply(documents);
	}
}
