# Spring AI Tutorial — RAG 챗봇 만들기

Spring AI의 고수준 추상화(`ChatClient`, `Advisor` 등)에 의존하지 않고, 저수준 빌딩 블록을 조합하여 **멀티 프로바이더 채팅**과 **PDF 기반 RAG(
Retrieval-Augmented Generation)** 흐름을 직접 구현한 프로젝트입니다.
<img width="2014" height="1523" alt="스크린샷 2026-02-06 171615" src="https://github.com/user-attachments/assets/89ade45a-c61d-49f4-8192-ff528b2ae81c" />
<img width="2011" height="1517" alt="스크린샷 2026-02-06 171815" src="https://github.com/user-attachments/assets/fe33f03a-aaa0-4c32-9c90-ad2d3f86d8b0" />

## 설계 방향

Spring AI는 `ChatClient`, `RetrievalAugmentationAdvisor` 등 RAG 흐름을 자동화하는 고수준 API를 제공합니다. 이 프로젝트는 그 대신 Spring AI가 제공하는 저수준
컴포넌트들을 직접 조합하여, 각 단계가 어떻게 동작하는지 명시적으로 드러나도록 구성했습니다.

```
// Spring AI 고수준 추상화 (이 프로젝트에서는 사용하지 않음)
ChatClient.builder(chatModel)
    .defaultAdvisors(new RetrievalAugmentationAdvisor(vectorStore))
    .build()
    .prompt().user("질문").call();

// 이 프로젝트의 방식 — 각 단계를 직접 제어
List<Document> contexts = vectorStore.searchSimilar(documentId, userMessage, TOP_K);
String systemMessage = RagChatSystemMessagePolicy.build(contexts);
ChatResult result = chatFacade.chat(systemMessage, userMessage, model);
```

**사용한 Spring AI 컴포넌트 (저수준)**

| 영역       | 클래스                                       |
|----------|-------------------------------------------|
| PDF 로딩   | `PagePdfDocumentReader`                   |
| 청크 분할    | `TokenTextSplitter`                       |
| 벡터 저장/검색 | `SimpleVectorStore`, `SearchRequest`      |
| 임베딩      | `OpenAiEmbeddingModel`                    |
| LLM 호출   | `OpenAiChatModel`, `GoogleGenAiChatModel` |
| 메시지/프롬프트 | `SystemMessage`, `UserMessage`, `Prompt`  |

**사용하지 않은 Spring AI 추상화 (고수준)**

| 추상화                                          | 대신 직접 구현한 것                                     |
|----------------------------------------------|-------------------------------------------------|
| `ChatClient`                                 | `ChatFacade` + `ChatRouter` + `ChatProvider`    |
| `Advisor` (`RetrievalAugmentationAdvisor` 등) | `RagChatFacade` + `RagChatSystemMessagePolicy`  |
| ETL 파이프라인 자동화                                | `DocumentIndexingFacade` (로딩 → 분할 → 메타데이터 → 저장) |

## 기술 스택

| 항목          | 버전/내용                                                                      |
|-------------|----------------------------------------------------------------------------|
| Java        | 17                                                                         |
| Spring Boot | 3.5.9                                                                      |
| Spring AI   | 1.1.2                                                                      |
| LLM 프로바이더   | OpenAI (`gpt-4.1-nano`, `gpt-4.1-mini`), Google GenAI (`gemini-2.5-flash`) |
| 임베딩         | OpenAI `text-embedding-3-small`                                            |
| 벡터 저장소      | `SimpleVectorStore` (인메모리)                                                 |
| 빌드 도구       | Gradle                                                                     |

## 프로젝트 구조

```
src/main/
├── java/.../springaitutorial/
│   ├── chat/                  # 멀티 프로바이더 채팅 모듈
│   │   ├── application/       #   Facade, 시스템 메시지 정책, 결과 DTO
│   │   ├── controller/        #   REST API, 요청 DTO
│   │   ├── provider/          #   ChatProvider 인터페이스 및 구현체 (OpenAI, Google)
│   │   └── router/            #   모델명 기반 프로바이더 라우팅
│   ├── rag/                   # PDF 기반 RAG 모듈
│   │   ├── application/       #   문서 인덱싱/RAG 채팅 Facade, 시스템 메시지 정책, 결과 DTO
│   │   ├── config/            #   VectorStore 빈 설정
│   │   ├── controller/        #   REST API, 요청 DTO
│   │   ├── indexing/          #   PDF 로딩, 청크 분할, 메타데이터 부여, 임시 파일 관리
│   │   └── storage/           #   벡터 저장소 인터페이스 및 인메모리 구현체
│   └── common/                # 공통 (ApiResponse, GlobalExceptionHandler)
└── resources/
    ├── static/                # 웹 UI (index.html, app.js, styles.css)
    └── application.properties
```

## 주요 기능

### 1. 멀티 프로바이더 채팅

모델명을 기준으로 OpenAI 또는 Google GenAI 프로바이더를 자동 라우팅합니다.

```
POST /api/chat/messages
```

```json
{
  "userMessage": "Spring AI가 뭐야?",
  "model": "gpt-4.1-mini"
}
```

모델을 지정하지 않으면 기본 프로바이더(OpenAI)의 기본 모델(`gpt-4.1-nano`)이 사용됩니다.

### 2. PDF 문서 인덱싱

PDF 파일을 업로드하면 로딩 → 청크 분할 → 메타데이터 부여 → 벡터 저장소 임베딩의 파이프라인이 실행됩니다.

```
POST /api/rag/documents  (multipart/form-data)
```

### 3. RAG 채팅

인덱싱된 문서를 대상으로 유사도 검색(Top 5)을 수행하고, 검색된 청크를 컨텍스트로 활용하여 답변을 생성합니다.

```
POST /api/rag/messages
```

```json
{
  "documentId": "...",
  "userMessage": "이 문서의 핵심 내용을 요약해줘",
  "model": "gpt-4.1-mini"
}
```

### 4. 웹 UI

`http://localhost:8080`에서 Chat 탭과 RAG 탭을 가진 웹 UI를 제공합니다.

- **Chat 탭** — 모델을 선택하고 메시지를 주고받는 채팅 인터페이스
- **RAG 탭** — PDF 드래그 앤 드롭 업로드 후 문서 기반 Q&A

## 실행 방법

### 1. API 키 설정

`src/main/resources/application-API-KEY.properties` 파일을 생성하고 API 키를 입력합니다.

```properties
OPENAI_API_KEY=sk-...
GEMINI_API_KEY=AI...
```

### 2. 애플리케이션 실행

```bash
./gradlew bootRun
```

### 3. 접속

브라우저에서 `http://localhost:8080`으로 접속합니다.

## 모듈별 상세 문서

각 모듈의 상세 설명, 컴포넌트 분석, 트러블슈팅은 별도 문서를 참고해 주세요.

- [chat.md](src/main/java/com/example/springaitutorial/chat/chat.md) — Chat 모듈 상세 (프로바이더 라우팅, 시스템 메시지 정책 등)
- [rag.md](src/main/java/com/example/springaitutorial/rag/rag.md) — RAG 모듈 상세 (인덱싱 파이프라인, 벡터 검색, 트러블슈팅 등)
