package com.example.springaitutorial.rag.indexing;

import java.io.File;
import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class UploadTempFileService {

	public File toTempPdf(MultipartFile file) throws IOException {
		File temp = File.createTempFile("upload_", ".pdf");
		file.transferTo(temp);
		return temp;
	}

	public void deleteQuietly(File file) {
		if (file == null) {
			return;
		}
		if (file.exists()) {
			file.delete();
		}
	}
}
