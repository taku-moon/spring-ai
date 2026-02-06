package com.example.springaitutorial.rag.application.policy;

import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;

public final class RagChatSystemMessagePolicy {

	private static final int MAX_CONTEXT_CHARS = 6000;
	private static final int MAX_SOURCE_SNIPPET_CHARS = 300;

	private RagChatSystemMessagePolicy() {
	}

	public static String build(List<Document> contexts) {
		String contextText = buildContext(contexts);

		return """
				너는 지식 기반 Q&A 시스템이다.
				아래 정보(컨텍스트)만 근거로 답변한다.
				컨텍스트에 없는 내용은 추측하지 않는다.
				컨텍스트에 답이 없으면 모른다고 답한다.
			
				답변 형식 규칙:
				1) 답변 본문을 작성한다.
				2) 답변 마지막에 '출처' 섹션을 만든다.
				3) '출처'에는 실제로 근거로 사용한 항목만 포함한다.
				4) 각 출처는 [번호]와 함께 해당 근거 내용을 1~2문장으로 요약하거나 짧게 발췌해서 적는다.
				5) 출처 내용은 항목당 최대 %d자까지만 적는다.
				6) 가능한 경우 파일명(originalFilename)과 청크 번호(chunkIndex)도 함께 적는다.
			
				컨텍스트:
				%s
			""".formatted(MAX_SOURCE_SNIPPET_CHARS, contextText);
	}

	private static String buildContext(List<Document> contexts) {
		if (contexts == null || contexts.isEmpty()) {
			return "(컨텍스트 없음)";
		}

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < contexts.size(); i++) {
			Document doc = contexts.get(i);
			if (doc == null) {
				continue;
			}

			String text = doc.getText();
			if (text == null || text.isBlank()) {
				continue;
			}

			Map<String, Object> metadata = doc.getMetadata();
			String originalFilename = metadata == null ? null : toStringOrNull(metadata.get("originalFilename"));
			String chunkIndex = metadata == null ? null : toStringOrNull(metadata.get("chunkIndex"));

			String header = "[" + (i + 1) + "]";
			if (originalFilename != null || chunkIndex != null) {
				header = header + " (" + nullToDash(originalFilename) + ", chunkIndex=" + nullToDash(chunkIndex) + ")";
			}

			String chunk = header + " " + text.trim() + "\n\n";

			if (sb.length() + chunk.length() > MAX_CONTEXT_CHARS) {
				break;
			}

			sb.append(chunk);
		}

		if (sb.length() == 0) {
			return "(컨텍스트 없음)";
		}

		return sb.toString().trim();
	}

	private static String toStringOrNull(Object value) {
		if (value == null) {
			return null;
		}
		String s = value.toString().trim();
		return s.isEmpty() ? null : s;
	}

	private static String nullToDash(String value) {
		return value == null ? "-" : value;
	}
}
