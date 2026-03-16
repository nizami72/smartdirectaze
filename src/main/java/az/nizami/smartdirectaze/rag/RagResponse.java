package az.nizami.smartdirectaze.rag;

import java.util.List;
import lombok.Builder;

@Builder
public record RagResponse(String answer, List<String> articles, Boolean isLarge) {}

