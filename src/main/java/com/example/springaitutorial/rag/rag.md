# RAG (Retrieval-Augmented Generation) 시스템

## 개요

이 모듈은 Spring AI 기반의 RAG 시스템으로, 사용자가 업로드한 PDF 문서를 벡터화하여 저장한 뒤, 질문에 대해 문서 내용을 근거로 답변을 생성하는 지식 기반 Q&A 시스템입니다.

## 전체 아키텍처

```
[클라이언트]
    │
    ▼
RagController (/api/rag)
    ├── POST /documents        ──▶ DocumentIndexingFacade (문서 인덱싱)
    └── POST /messages         ──▶ RagChatFacade (RAG 채팅)
                                        │
                                        ▼
                                   ChatFacade (chat 모듈 재사용)
```

## 패키지 구조

```
rag/
├── application/                        # 비즈니스 로직 (Facade 계층)
│   ├── DocumentIndexingFacade          # 문서 인덱싱 오케스트레이션
│   ├── RagChatFacade                   # RAG 채팅 오케스트레이션
│   ├── policy/
│   │   └── RagChatSystemMessagePolicy  # 시스템 메시지 생성 정책
│   └── result/
│       ├── DocumentIndexingResult      # 인덱싱 결과 DTO
│       └── RagChatResult               # 채팅 결과 DTO
├── config/
│   └── RagVectorStoreConfig            # VectorStore 빈 설정
├── controller/
│   ├── RagController                   # REST API 엔드포인트
│   └── dto/
│       └── RagMessageRequest           # 채팅 요청 DTO
├── indexing/                           # 문서 처리 파이프라인
│   ├── PdfDocumentLoader               # PDF 파일 로딩
│   ├── DocumentSplitter                # 문서 청크 분할
│   ├── DocumentMetadataEnricher        # 메타데이터 부여
│   └── UploadTempFileService           # 임시 파일 관리
└── storage/                            # 벡터 저장소
    ├── DocumentVectorStore             # 저장소 인터페이스
    └── InMemoryDocumentVectorStore     # 인메모리 구현체
```

## 핵심 흐름

### 1. 문서 인덱싱 흐름 (`POST /api/rag/documents`)

사용자가 PDF 파일을 업로드하면 다음 파이프라인을 거쳐 벡터 저장소에 저장됩니다.

```
MultipartFile (PDF)
    │
    ▼
① UploadTempFileService.toTempPdf()     임시 파일로 변환
    │
    ▼
② PdfDocumentLoader.load()              PDF → Document 리스트 (페이지 단위)
    │
    ▼
③ DocumentSplitter.split()              Document → 청크 단위로 분할 (TokenTextSplitter)
    │
    ▼
④ DocumentMetadataEnricher.enrich()     각 청크에 documentId, originalFilename, chunkIndex 메타데이터 부여
    │
    ▼
⑤ InMemoryDocumentVectorStore.saveAll() 벡터 저장소에 임베딩 후 저장
    │
    ▼
⑥ 임시 파일 삭제 (finally)
```

**응답 예시:**

```json
{
  "success": true,
  "data": {
    "documentId": "550e8400-e29b-41d4-a716-446655440000",
    "chunkCount": 42
  }
}
```

### 2. RAG 채팅 흐름 (`POST /api/rag/messages`)

사용자가 `documentId`와 질문을 보내면, 해당 문서에서 관련 청크를 검색하여 컨텍스트로 활용해 답변을 생성합니다.

```
RagMessageRequest { documentId, userMessage, model }
    │
    ▼
① InMemoryDocumentVectorStore.searchSimilar()    documentId로 필터링 + 유사도 검색 (Top 5)
    │
    ▼
② RagChatSystemMessagePolicy.build()             검색된 청크로 시스템 메시지 구성
    │
    ▼
③ ChatFacade.chat()                              시스템 메시지 + 사용자 메시지로 LLM 호출
    │
    ▼
④ RagChatResult 반환
```

**요청 예시:**

```json
{
  "documentId": "550e8400-e29b-41d4-a716-446655440000",
  "userMessage": "이 문서의 핵심 내용을 요약해줘",
  "model": "gpt-4o-mini"
}
```

**응답 예시:**

```json
{
  "success": true,
  "data": {
    "provider": "OPENAI",
    "model": "gpt-4o-mini",
    "answer": "...",
    "retrievedCount": 5
  }
}
```

## 주요 컴포넌트 상세

### DocumentIndexingFacade

- 문서 인덱싱의 전체 과정을 오케스트레이션하는 Facade 클래스입니다.
- PDF 유효성 검증, 임시 파일 생성, 문서 로딩, 청크 분할, 메타데이터 부여, 벡터 저장까지의 전체 파이프라인을 관리합니다.
- 처리 완료 후 임시 파일은 `finally` 블록에서 반드시 삭제됩니다.

### RagChatFacade

- RAG 채팅의 핵심 로직을 담당하는 Facade 클래스입니다.
- 벡터 저장소에서 유사 문서를 검색(Top 5)하고, 검색 결과를 시스템 메시지로 구성한 뒤, 기존 `chat` 모듈의 `ChatFacade`를 재사용하여 LLM 호출을 수행합니다.

### RagChatSystemMessagePolicy

검색된 문서 청크를 기반으로 LLM 시스템 메시지를 생성하는 정책 클래스입니다.

- **컨텍스트 최대 길이:** 6,000자 (초과 시 이후 청크는 생략됩니다)
- **출처 스니펫 최대 길이:** 300자
- **시스템 메시지 규칙:**
    - 제공된 컨텍스트만 근거로 답변합니다.
    - 컨텍스트에 없는 내용은 추측하지 않습니다.
    - 답변 마지막에 출처 섹션을 포함합니다. (파일명, 청크 번호 표기)

### PdfDocumentLoader

- Spring AI의 `PagePdfDocumentReader`를 사용하여 PDF 파일을 페이지 단위 `Document` 리스트로 변환합니다.
- PDF 파싱 시 `StackOverflowError`를 방지하기 위해 64MB 스택 크기를 가진 별도 스레드에서 실행됩니다.

### DocumentSplitter

- Spring AI의 `TokenTextSplitter`를 사용하여 문서를 토큰 기반으로 청크 단위로 분할합니다.
- 기본 설정을 사용합니다.

### DocumentMetadataEnricher

- 분할된 각 청크에 다음 메타데이터를 부여합니다.

| 메타데이터 키            | 설명                 |
|--------------------|--------------------|
| `documentId`       | 문서 고유 식별자 (UUID)   |
| `originalFilename` | 원본 PDF 파일명         |
| `chunkIndex`       | 청크 순서 인덱스 (0부터 시작) |

### InMemoryDocumentVectorStore

- Spring AI의 `SimpleVectorStore`를 래핑한 인메모리 벡터 저장소 구현체입니다.
- 문서 저장 시 OpenAI 임베딩 모델을 통해 벡터화되며, 검색 시 `documentId` 필터와 유사도 검색을 결합하여 특정 문서 내에서만 관련 청크를 조회합니다.

## 설정

### VectorStore 설정 (RagVectorStoreConfig)

- `OpenAiEmbeddingModel`을 사용하는 `SimpleVectorStore`를 빈으로 등록합니다.
- 임베딩은 OpenAI API를 통해 수행됩니다.

### application.properties 관련 설정

| 속성                                       | 값      | 설명               |
|------------------------------------------|--------|------------------|
| `spring.servlet.multipart.max-file-size` | `10MB` | 업로드 가능한 최대 파일 크기 |

## 의존 관계

- RAG 모듈은 기존 `chat` 모듈의 `ChatFacade`를 재사용하여 LLM 호출을 수행합니다.
- 이를 통해 모델 라우팅(OpenAI, Google GenAI) 로직을 중복 없이 활용합니다.

```
rag.RagChatFacade ──depends on──▶ chat.ChatFacade ──▶ ChatRouter ──▶ ChatProvider (OpenAI / Google GenAI)
```

## 트러블슈팅

### EmbeddingModel 빈 주입 실패 (NoUniqueBeanDefinitionException)

**문제 상황**

`RagVectorStoreConfig`에서 `VectorStore` 빈을 생성할 때, `EmbeddingModel` 주입에 실패하며 애플리케이션이 기동되지 않았습니다.

```java
// 문제가 발생한 코드
@Bean
public VectorStore vectorStore(EmbeddingModel embeddingModel) {
    return SimpleVectorStore.builder(embeddingModel).build();
}
```

**원인**

이 프로젝트는 두 개의 AI 모델 스타터를 동시에 사용하고 있습니다.

```groovy
implementation 'org.springframework.ai:spring-ai-starter-model-openai'
implementation 'org.springframework.ai:spring-ai-starter-model-google-genai'
```

각 스타터가 자동 설정을 통해 `EmbeddingModel` 구현체를 각각 빈으로 등록합니다.

| 스타터                                | 등록되는 빈                       |
|------------------------------------|------------------------------|
| `spring-ai-starter-model-openai`   | `OpenAiEmbeddingModel`       |
| `spring-ai-starter-model-google-genai` | `GoogleGenAiEmbeddingModel` |

`EmbeddingModel` 인터페이스 타입으로 주입받으면 Spring이 두 개의 후보 빈 중 어떤 것을 선택해야 할지 결정할 수 없어 `NoUniqueBeanDefinitionException`이 발생합니다.

`application.properties`의 `spring.ai.model.embedding` 설정은 모델 이름을 지정하는 것이지, 빈 선택을 해결하지는 않습니다.

```properties
# 이 설정은 사용할 모델 이름만 지정할 뿐, 어떤 EmbeddingModel 빈을 주입할지는 결정하지 않습니다.
spring.ai.model.embedding=text-embedding-3-small
```

**해결 방법**

임베딩에 OpenAI의 `text-embedding-3-small`을 사용할 것이므로, 인터페이스 대신 구체 타입(`OpenAiEmbeddingModel`)으로 주입받도록 변경했습니다.

```java
// 해결: 구체 타입으로 주입
@Bean
public VectorStore vectorStore(OpenAiEmbeddingModel embeddingModel) {
    return SimpleVectorStore.builder(embeddingModel).build();
}
```

### PDF 파싱 시 StackOverflowError

**문제 상황**

PDF 문서를 업로드하면 `StackOverflowError`가 발생하며 인덱싱에 실패했습니다.

**원인**

`PdfDocumentLoader`가 사용하는 `PagePdfDocumentReader` 내부의 Apache PDFBox 라이브러리가 PDF 콘텐츠 스트림을 재귀적으로 파싱하는데, 복잡한 구조의 PDF 파일의 경우 재귀 깊이가 매우 깊어져 Java의 기본 스택 크기(~512KB)를 초과합니다.

**1차 시도: JVM 전역 스택 크기 증가 — 실패**

`build.gradle`의 `bootRun` 태스크에 `-Xss4m` 옵션을 추가하여 모든 스레드의 기본 스택 크기를 4MB로 늘렸습니다.

```groovy
tasks.named('bootRun') {
    jvmArgs = ['-Xss4m']
}
```

그러나 복잡한 PDF에는 4MB도 부족하여 여전히 `StackOverflowError`가 발생했습니다. 전역 스택 크기를 더 높이면 Tomcat 요청 처리 스레드 등 모든 스레드가 불필요하게 큰 메모리를 사용하게 되므로 적절한 해결책이 아니었습니다.

**2차 시도: PDF 파싱 전용 스레드에 64MB 스택 할당 — 해결**

PDF 파싱 작업만 별도 스레드에서 실행하면서, 해당 스레드에만 64MB의 큰 스택을 할당하는 방식으로 변경했습니다.

```java
// 변경 전: 요청 스레드에서 직접 실행 (기본 스택 크기 사용)
public List<Document> load(File pdfFile) {
    PagePdfDocumentReader reader = new PagePdfDocumentReader(new FileSystemResource(pdfFile));
    return reader.read();
}
```

```java
// 변경 후: 64MB 스택의 전용 스레드에서 실행
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
```

이 방식의 장점은 다음과 같습니다:
- PDF 파싱 스레드만 큰 스택(64MB)을 사용하고, 다른 모든 스레드(Tomcat 요청 처리 등)는 기본 스택 크기를 유지합니다.
- `build.gradle`의 `-Xss4m` 전역 설정은 불필요해졌으므로 제거했습니다.
