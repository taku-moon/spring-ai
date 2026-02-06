package com.example.springaitutorial.rag.indexing;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

@Component
public class PdfDocumentLoader {

	private static final long STACK_SIZE = 1024 * 1024 * 64; // 64MB

	public List<Document> load(File pdfFile) {
		FutureTask<List<Document>> task = new FutureTask<>(() -> {
			PagePdfDocumentReader reader = new PagePdfDocumentReader(new FileSystemResource(pdfFile));
			return reader.read();
		});

		Thread thread = new Thread(null, task, "pdf-reader", STACK_SIZE);
		thread.start();

		try {
			return task.get();
		} catch (ExecutionException e) {
			throw new RuntimeException("PDF 파일 읽기 실패", e.getCause());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("PDF 파일 읽기가 중단되었습니다.", e);
		}
	}
}
