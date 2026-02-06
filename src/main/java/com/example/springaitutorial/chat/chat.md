# Chat 시스템

## 개요

이 모듈은 Spring AI 기반의 멀티 프로바이더 채팅 시스템으로, 사용자의 메시지를 받아 LLM에 전달하고 응답을 반환합니다. 모델 이름을 기준으로 적절한 프로바이더(OpenAI, Google GenAI)를 자동으로 라우팅하는 구조입니다.

## 전체 아키텍처

```
[클라이언트]
    │
    ▼
ChatController (/api/chat)
    └── POST /messages ──▶ ChatFacade
                               │
                               ▼
                          ChatRouter (모델명 기반 라우팅)
                           ├── OpenAiChatProvider
                           └── GoogleGenAiChatProvider
```

## 패키지 구조

```
chat/
├── application/                       # 비즈니스 로직 (Facade 계층)
│   ├── ChatFacade                     # 채팅 오케스트레이션
│   ├── policy/
│   │   └── ChatSystemMessagePolicy    # 시스템 메시지 생성 정책
│   └── result/
│       └── ChatResult                 # 채팅 결과 DTO
├── controller/
│   ├── ChatController                 # REST API 엔드포인트
│   └── dto/
│       └── ChatMessageRequest         # 채팅 요청 DTO
├── provider/                          # LLM 프로바이더
│   ├── ChatProvider                   # 프로바이더 인터페이스
│   ├── OpenAiChatProvider             # OpenAI 구현체
│   ├── GoogleGenAiChatProvider        # Google GenAI 구현체
│   └── ProviderType                   # 프로바이더 타입 및 지원 모델 정의
└── router/
    └── ChatRouter                     # 모델명 기반 프로바이더 라우팅
```

## 핵심 흐름

### 채팅 흐름 (`POST /api/chat/messages`)

사용자가 메시지와 모델명을 보내면, 해당 모델을 지원하는 프로바이더를 통해 LLM 호출을 수행합니다.

```
ChatMessageRequest { userMessage, model }
    │
    ▼
① ChatFacade.chat()
    │  - 메시지 유효성 검증
    │  - ChatSystemMessagePolicy.build()로 시스템 메시지 생성
    │
    ▼
② ChatRouter.routeByModel()
    │  - 모델명이 없으면 기본 프로바이더(OpenAI) 사용
    │  - 모델명이 있으면 해당 모델을 지원하는 프로바이더 탐색
    │
    ▼
③ ChatProvider.chat()
    │  - SystemMessage + UserMessage로 Prompt 구성
    │  - 프로바이더별 ChatModel.call() 실행
    │
    ▼
④ ChatResult 반환 (provider, model, answer)
```

**요청 예시:**

```json
{
  "userMessage": "Spring AI가 뭐야?",
  "model": "gpt-4.1-mini"
}
```

**응답 예시:**

```json
{
  "success": true,
  "data": {
    "provider": "OPEN_AI",
    "model": "gpt-4.1-mini",
    "answer": "..."
  }
}
```

모델을 지정하지 않으면 기본 프로바이더(OpenAI)의 기본 모델(`gpt-4.1-nano`)이 사용됩니다.

```json
{
  "userMessage": "안녕하세요",
  "model": ""
}
```

## 주요 컴포넌트 상세

### ChatFacade

- 채팅의 전체 흐름을 오케스트레이션하는 Facade 클래스입니다.
- 두 가지 `chat()` 메서드를 제공합니다:
    - `chat(userMessage, model)` — 기본 시스템 메시지를 사용합니다. 일반 채팅에 사용됩니다.
    - `chat(systemMessage, userMessage, model)` — 외부에서 시스템 메시지를 주입받습니다. RAG 모듈에서 사용됩니다.
- 모델명이 비어있으면 기본 프로바이더(OpenAI)와 기본 모델을 자동으로 선택합니다.

### ChatSystemMessagePolicy

기본 채팅에 사용되는 시스템 메시지를 생성하는 정책 클래스입니다. 다음 규칙을 포함합니다:

- 도움이 되는 AI 어시스턴트 역할을 수행합니다.
- 답변은 명확하고 간결하게 작성합니다.
- 확실하지 않으면 모른다고 답합니다.
- 불필요한 장황한 설명을 피하고 핵심을 먼저 말합니다.

### ChatProvider (인터페이스)

모든 LLM 프로바이더가 구현하는 인터페이스입니다. 다음 메서드를 정의합니다:

| 메서드                | 설명                                     |
|--------------------|----------------------------------------|
| `type()`           | 프로바이더 타입(`ProviderType`) 반환            |
| `defaultModel()`   | 기본 모델명 반환                              |
| `supports(model)`  | 해당 모델을 지원하는지 여부 반환                     |
| `chat(systemMessage, userMessage, model)` | 시스템/사용자 메시지로 LLM 호출 |

### OpenAiChatProvider

- `OpenAiChatModel`을 사용하는 OpenAI 프로바이더 구현체입니다.
- `SystemMessage` + `UserMessage`로 `Prompt`를 구성하고, `OpenAiChatOptions`로 모델을 지정하여 호출합니다.

### GoogleGenAiChatProvider

- `GoogleGenAiChatModel`을 사용하는 Google GenAI 프로바이더 구현체입니다.
- OpenAI 프로바이더와 동일한 구조로 `Prompt`를 구성하고, `GoogleGenAiChatOptions`로 모델을 지정하여 호출합니다.

### ProviderType (Enum)

각 프로바이더의 기본 모델과 지원 모델 목록을 정의하는 열거형입니다.

| 프로바이더        | 기본 모델              | 지원 모델                            |
|--------------|--------------------|------------------------------------|
| `OPEN_AI`    | `gpt-4.1-nano`    | `gpt-4.1-nano`, `gpt-4.1-mini`    |
| `GOOGLE_GENAI` | `gemini-2.5-flash` | `gemini-2.5-flash`                |

### ChatRouter

- 모델명을 기반으로 적절한 `ChatProvider`를 선택하는 라우터입니다.
- Spring이 주입한 `List<ChatProvider>`를 순회하며 해당 모델을 지원하는 프로바이더를 찾습니다.
- `defaultProvider()` — 모델명이 없을 때 기본 프로바이더(`OPEN_AI`)를 반환합니다.
- `routeByModel(model)` — 모델명을 지원하는 프로바이더를 반환하며, 없으면 `IllegalArgumentException`을 던집니다.

## 설정

### application.properties 관련 설정

| 속성                             | 설명                    |
|--------------------------------|-----------------------|
| `spring.ai.openai.api-key`    | OpenAI API 키          |
| `spring.ai.google.genai.api-key` | Google GenAI API 키 |

API 키는 `spring.profiles.include=API-KEY`를 통해 별도 프로파일에서 관리됩니다.

## 다른 모듈과의 관계

Chat 모듈은 RAG 모듈의 하위 의존성으로 재사용됩니다. RAG 모듈은 자체적으로 시스템 메시지를 구성한 뒤 `ChatFacade.chat(systemMessage, userMessage, model)` 오버로드를 호출하여 LLM 호출을 위임합니다.

```
rag.RagChatFacade ──▶ chat.ChatFacade ──▶ ChatRouter ──▶ ChatProvider
                           ▲
chat.ChatController ───────┘
```
