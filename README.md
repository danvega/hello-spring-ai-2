# Spring AI 2.0 - What's New

Demonstrates **new features in Spring AI 2.0.0-M1**.

Built on Spring Boot 4.0.1 and Java 25.

## New Features in 2.0

| Feature | What's New |
|---------|------------|
| **OpenAI SDK** | Official Java SDK with Responses API & web search |
| **Anthropic Citations** | Source references in responses |
| **Anthropic Skills** | Generate Excel, PowerPoint, Word, PDF files |
| **Google GenAI** | Gemini integration with safety ratings |
| **Redis Chat Memory** | Persistent conversation storage |

And more, check out the [blog post](https://spring.io/blog/2025/12/11/spring-ai-2-0-0-M1-available-now).

## Quick Start

```bash
export OPENAI_API_KEY=sk-...
export ANTHROPIC_API_KEY=sk-ant-...    # Optional
export GOOGLE_GENAI_API_KEY=...        # Optional

./mvnw spring-boot:run
```

> **Note:** Redis starts automatically via Docker Compose support when available.

> **IntelliJ Users:** This README includes curl commands, but you can also use the included [`api-tests.http`](api-tests.http) file with IntelliJ's HTTP Client for a better experience.

---

## 1. OpenAI SDK Integration

**NEW:** Uses the official OpenAI Java SDK (`spring-ai-starter-model-openai-sdk`)

Key advantages:
- Direct access to OpenAI APIs not yet wrapped by Spring AI
- Native Azure OpenAI & GitHub Models support
- Automatic API updates via SDK releases

### Responses API with Web Search

The Responses API is OpenAI's new primary API with built-in tools. This endpoint demonstrates **web search** - a feature NOT available through Spring AI's ChatClient.

```bash
curl -X POST http://localhost:8080/api/openai/responses/search \
  -H "Content-Type: application/json" \
  -d '{"query": "What are the latest developments in Spring AI?"}'
```

Response includes the answer with source citations:

```json
{
  "answer": "Spring AI 2.0.0-M1 was released with...",
  "citations": [
    {"title": "Spring AI Release Notes", "url": "https://..."},
    {"title": "Spring Blog", "url": "https://..."}
  ]
}
```

### Basic Chat

```bash
curl -X POST http://localhost:8080/api/openai/chat \
  -H "Content-Type: text/plain" \
  -d 'Tell me a joke about Java programming'
```

---

## 2. Anthropic Claude - Citations API

**NEW:** Claude can cite specific parts of documents in its responses.

```bash
curl -X POST http://localhost:8080/api/anthropic/citations \
  -H "Content-Type: application/json" \
  -d '{
    "document": "The Eiffel Tower was completed in 1889. It stands 330 meters tall.",
    "title": "Eiffel Tower Facts",
    "question": "When was it built and how tall is it?"
  }'
```

Response includes `citations` array with document references, cited text, and character positions.

---

## 3. Anthropic Claude - Skills API

**NEW:** Claude can generate downloadable documents directly.

```bash
# Generate Excel spreadsheet
curl -X POST http://localhost:8080/api/anthropic/skills/excel \
  -H "Content-Type: application/json" \
  -d '{"prompt": "Create a budget spreadsheet with income and expenses"}'

# Generate PowerPoint presentation
curl -X POST http://localhost:8080/api/anthropic/skills/powerpoint \
  -H "Content-Type: application/json" \
  -d '{"prompt": "Create a 3-slide presentation about Spring AI"}'

# Generate Word document
curl -X POST http://localhost:8080/api/anthropic/skills/word \
  -H "Content-Type: application/json" \
  -d '{"prompt": "Create a project proposal document"}'

# Generate PDF
curl -X POST http://localhost:8080/api/anthropic/skills/pdf \
  -H "Content-Type: application/json" \
  -d '{"prompt": "Create a quarterly sales report with sample data"}'

# Download generated file (use fileId from response)
curl http://localhost:8080/api/anthropic/files/{fileId} --output document.xlsx
```

> **Note:** Skills API requires extended timeout (configured to 10 minutes) as document generation can take 1-2 minutes.

---

## 4. Google GenAI (Gemini)

**NEW:** Direct Gemini integration with `spring-ai-starter-model-google-genai`.

### Basic Chat

```bash
curl -X POST http://localhost:8080/api/google/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Explain quantum computing in simple terms"}'
```

### Chat with Safety Ratings

```bash
curl -X POST http://localhost:8080/api/google/chat/safe \
  -H "Content-Type: application/json" \
  -d '{"message": "Tell me about AI safety"}'
```

Response includes `safetyRatings` metadata from Gemini.

---

## 5. Redis Chat Memory

**NEW:** Persistent conversation storage with `spring-ai-starter-model-chat-memory-repository-redis`.

### Chat with Memory

```bash
# Start a conversation
curl -X POST http://localhost:8080/api/redis/chat/user123 \
  -H "Content-Type: application/json" \
  -d '{"message": "My name is Dan"}'

# Continue the conversation (remembers context)
curl -X POST http://localhost:8080/api/redis/chat/user123 \
  -H "Content-Type: application/json" \
  -d '{"message": "What is my name?"}'
```

### Get Conversation History

```bash
curl http://localhost:8080/api/redis/history/user123
```

### List All Conversations

```bash
curl http://localhost:8080/api/redis/conversations
```

### Clear Conversation

```bash
curl -X DELETE http://localhost:8080/api/redis/history/user123
```

---

## Configuration

```yaml
spring:
  ai:
    openai-sdk:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o
    anthropic:
      api-key: ${ANTHROPIC_API_KEY:}
      timeout: 600s  # Extended timeout for Skills API
      chat:
        options:
          model: claude-sonnet-4-5
    google:
      genai:
        api-key: ${GOOGLE_GENAI_API_KEY:}
        chat:
          options:
            model: gemini-2.0-flash
    chat:
      memory:
        repository:
          redis:
            key-prefix: "spring-ai-chat:"
            time-to-live: 1h
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
```

## Project Structure

```
src/main/java/dev/danvega/sai2/
├── openai/OpenAiSdkController.java      # Responses API & Web Search
├── anthropic/AnthropicController.java   # Citations & Skills API
├── google/GoogleGenAiController.java    # Gemini with Safety Ratings
└── redis/RedisChatMemoryController.java # Persistent Chat Memory
```

## Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `OPENAI_API_KEY` | Yes | OpenAI API key |
| `ANTHROPIC_API_KEY` | No | Anthropic API key for Citations & Skills |
| `GOOGLE_GENAI_API_KEY` | No | Google AI Studio API key for Gemini |
| `REDIS_HOST` | No | Redis host (default: localhost) |
| `REDIS_PORT` | No | Redis port (default: 6379) |

## Resources

- [Spring AI 2.0.0-M1 Release](https://spring.io/blog/2025/12/11/spring-ai-2-0-0-M1-available-now/)
- [OpenAI SDK Docs](https://docs.spring.io/spring-ai/reference/api/chat/openai-sdk-chat.html)
- [Anthropic Citations & Skills](https://docs.spring.io/spring-ai/reference/api/chat/anthropic-chat.html)
- [Google GenAI Docs](https://docs.spring.io/spring-ai/reference/api/chat/google-genai-chat.html)
- [Chat Memory](https://docs.spring.io/spring-ai/reference/api/chat-memory.html)
