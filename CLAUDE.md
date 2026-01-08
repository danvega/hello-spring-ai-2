# CLAUDE.md

Development guidance for Claude Code when working with this repository.

## Project Overview

Spring Boot 4.0.1 demo showcasing **new features in Spring AI 2.0.0-M1**. Uses Java 25.

See [README.md](README.md) for feature documentation and examples.

## Build and Run

```bash
./mvnw clean install    # Build
./mvnw spring-boot:run  # Run
./mvnw test             # Test
```

> Redis starts automatically via Docker Compose support.

## Environment

Required: `OPENAI_API_KEY`
Optional: `ANTHROPIC_API_KEY`, `GOOGLE_GENAI_API_KEY`

## Project Structure

```
src/main/java/dev/danvega/sai2/
├── openai/OpenAiSdkController.java      # Responses API & Web Search (NEW in 2.0)
├── anthropic/AnthropicController.java   # Citations & Skills API (NEW in 2.0)
├── google/GoogleGenAiController.java    # Gemini with Safety Ratings (NEW in 2.0)
└── redis/RedisChatMemoryController.java # Persistent Chat Memory (NEW in 2.0)
```

## Key Dependencies

- `spring-ai-starter-model-openai-sdk` - Official OpenAI Java SDK
- `spring-ai-starter-model-anthropic` - Anthropic Claude (Citations & Skills)
- `spring-ai-starter-model-google-genai` - Google Gemini
- `spring-ai-starter-model-chat-memory-repository-redis` - Redis Chat Memory
- `spring-ai-bom` (v2.0.0-M1)

## Notes

- Uses `@ConditionalOnBean` for graceful feature degradation
- Anthropic Skills API requires extended timeout (10 min) - configured in `AnthropicConfig.java`
- Redis auto-starts via `spring-boot-docker-compose` dependency
