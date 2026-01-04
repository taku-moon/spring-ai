package com.example.springaitutorial.chat.application.policy;

public final class ChatSystemMessagePolicy {

	private ChatSystemMessagePolicy() {
	}

	private static final String ROLE = "너는 도움이 되는 AI 어시스턴트다.";
	private static final String STYLE = "답변은 명확하고 간결하게 작성한다.";
	private static final String HONESTY = "확실하지 않으면 모른다고 답한다.";
	private static final String FORMAT = "불필요한 장황한 설명을 피하고 핵심을 먼저 말한다.";

	public static String build() {
		return ROLE + "\n" + STYLE + "\n" + HONESTY + "\n" + FORMAT;
	}
}
