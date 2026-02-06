package com.example.springaitutorial.rag.application;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.springaitutorial.rag.application.result.DocumentIndexingResult;
import com.example.springaitutorial.rag.indexing.DocumentMetadataEnricher;
import com.example.springaitutorial.rag.indexing.DocumentSplitter;
import com.example.springaitutorial.rag.indexing.PdfDocumentLoader;
import com.example.springaitutorial.rag.indexing.UploadTempFileService;
import com.example.springaitutorial.rag.storage.InMemoryDocumentVectorStore;

@Service
public class DocumentIndexingFacade {

	private final PdfDocumentLoader documentLoader;
	private final DocumentSplitter documentSplitter;
	private final DocumentMetadataEnricher metadataEnricher;
	private final UploadTempFileService uploadTempFileService;
	private final InMemoryDocumentVectorStore documentVectorStore;

	public DocumentIndexingFacade(
		PdfDocumentLoader documentLoader,
		DocumentSplitter documentSplitter,
		DocumentMetadataEnricher metadataEnricher,
		UploadTempFileService uploadTempFileService,
		InMemoryDocumentVectorStore documentVectorStore
	) {
		this.documentLoader = documentLoader;
		this.documentSplitter = documentSplitter;
		this.metadataEnricher = metadataEnricher;
		this.uploadTempFileService = uploadTempFileService;
		this.documentVectorStore = documentVectorStore;
	}

	public DocumentIndexingResult indexPdfDocument(MultipartFile file) {
		validatePdf(file);

		String documentId = UUID.randomUUID().toString();
		File tempFile = null;

		try {
			tempFile = uploadTempFileService.toTempPdf(file);

			List<Document> pages = documentLoader.load(tempFile);
			List<Document> chunks = documentSplitter.split(pages);
			List<Document> enriched = metadataEnricher.enrich(documentId, file.getOriginalFilename(), chunks);

			documentVectorStore.saveAll(enriched);

			return new DocumentIndexingResult(documentId, enriched.size());
		} catch (IOException e) {
			throw new IllegalStateException("파일 처리 중 오류가 발생했습니다.", e);
		} finally {
			uploadTempFileService.deleteQuietly(tempFile);
		}
	}

	private void validatePdf(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("파일이 존재하지 않습니다.");
		}
		String name = file.getOriginalFilename();
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("파일명이 존재하지 않습니다.");
		}
		if (!name.toLowerCase().endsWith(".pdf")) {
			throw new IllegalArgumentException("PDF 파일만 업로드 가능합니다.");
		}
	}
}
