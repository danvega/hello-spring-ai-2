package dev.danvega.sai2.anthropic;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.Citation;
import org.springframework.ai.anthropic.SkillsResponseHelper;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.anthropic.api.CitationDocument;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * NEW IN SPRING AI 2.0: Anthropic Claude Advanced Features
 *
 * Demonstrates two new Claude capabilities:
 * - Citations API: Get source references in responses
 * - Skills/Files API: Generate documents (Excel, PowerPoint, Word, PDF)
 *
 * https://docs.spring.io/spring-ai/reference/api/chat/anthropic-chat.html#_citations
 */
@RestController
@RequestMapping("/api/anthropic")
public class AnthropicController {

    private final AnthropicChatModel chatModel;
    private final ChatClient chatClient;
    private final AnthropicApi anthropicApi;

    public AnthropicController(AnthropicChatModel chatModel, AnthropicApi anthropicApi) {
        this.chatModel = chatModel;
        this.chatClient = ChatClient.builder(chatModel).build();
        this.anthropicApi = anthropicApi;
    }

    /**
     * NEW IN 2.0: Citations API - Claude references specific parts of your document.
     * Useful for fact-checking and source verification.
     */
    @PostMapping("/citations")
    public CitationsResponse citations(@RequestBody CitationRequest request) {
        CitationDocument document = CitationDocument.builder()
                .plainText(request.document())
                .title(request.title())
                .citationsEnabled(true)
                .build();

        ChatResponse response = chatClient.prompt()
                .user(request.question())
                .options(AnthropicChatOptions.builder()
                        .model("claude-opus-4-5")
                        .citationDocuments(document)
                        .build())
                .call()
                .chatResponse();

        return new CitationsResponse(
                response.getResult().getOutput().getText(),
                extractCitations(response)
        );
    }

    /**
     * NEW IN 2.0: Skills API - Claude generates downloadable documents.
     * Supports: excel, powerpoint, word, pdf
     */
    @PostMapping("/skills/{type}")
    public SkillResponse generateDocument(
            @PathVariable String type,
            @RequestBody SkillRequest request
    ) {
        AnthropicApi.AnthropicSkill skill = switch (type.toLowerCase()) {
            case "excel" -> AnthropicApi.AnthropicSkill.XLSX;
            case "powerpoint" -> AnthropicApi.AnthropicSkill.PPTX;
            case "word" -> AnthropicApi.AnthropicSkill.DOCX;
            case "pdf" -> AnthropicApi.AnthropicSkill.PDF;
            default -> throw new IllegalArgumentException("Supported types: excel, powerpoint, word, pdf");
        };

        ChatResponse response = chatModel.call(
                new Prompt(
                        request.prompt(),
                        AnthropicChatOptions.builder()
                                .model("claude-sonnet-4-5")
                                .maxTokens(16384)
                                .anthropicSkill(skill)
                                .build()
                )
        );

        return new SkillResponse(
                response.getResult().getOutput().getText(),
                extractFileIds(response),
                type
        );
    }

    @GetMapping("/files/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileId) {
        AnthropicApi.FileMetadata metadata = anthropicApi.getFileMetadata(fileId);
        byte[] content = anthropicApi.downloadFile(fileId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.filename() + "\"")
                .contentType(MediaType.parseMediaType(metadata.mimeType()))
                .body(content);
    }

    @SuppressWarnings("unchecked")
    private List<Citation> extractCitations(ChatResponse response) {
        Object citations = response.getMetadata().get("citations");
        return citations instanceof List ? (List<Citation>) citations : List.of();
    }

    private List<String> extractFileIds(ChatResponse response) {
        return SkillsResponseHelper.extractFileIds(response);
    }

    public record CitationRequest(String document, String title, String question) {}
    public record CitationsResponse(String response, List<Citation> citations) {}
    public record SkillRequest(String prompt) {}
    public record SkillResponse(String response, List<String> fileIds, String type) {}
}
