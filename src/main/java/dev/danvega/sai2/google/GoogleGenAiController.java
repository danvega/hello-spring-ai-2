package dev.danvega.sai2.google;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * NEW IN SPRING AI 2.0: Google GenAI (Gemini) Integration
 *
 * Uses spring-ai-starter-model-google-genai for easy Gemini access.
 * Features in 2.0:
 * - Gemini 2.0 Flash model support
 * - Safety ratings in response metadata
 * - Thought signatures for function calling (Gemini 3 Pro)
 */
@RestController
@RequestMapping("/api/google")
public class GoogleGenAiController {

    private final ChatClient chatClient;

    public GoogleGenAiController(GoogleGenAiChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody ChatRequest request) {
        String response = chatClient.prompt()
                .user(request.message())
                .call()
                .content();
        return Map.of("response", response);
    }

    /**
     * Chat with safety ratings included in response.
     * NEW in 2.0: Safety ratings metadata available.
     */
    @PostMapping("/chat/safe")
    public SafeResponse chatWithSafety(@RequestBody ChatRequest request) {
        var response = chatClient.prompt()
                .user(request.message())
                .call()
                .chatResponse();

        String content = response.getResult().getOutput().getText();

        // Extract safety ratings from metadata if available
        @SuppressWarnings("unchecked")
        var safetyRatings = response.getMetadata().get("safetyRatings");

        return new SafeResponse(
                content,
                safetyRatings != null ? safetyRatings : List.of()
        );
    }

    public record ChatRequest(String message) {}
    public record SafeResponse(String response, Object safetyRatings) {}
}
