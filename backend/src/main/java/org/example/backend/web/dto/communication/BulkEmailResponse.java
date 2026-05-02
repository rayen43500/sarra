package org.example.backend.web.dto.communication;

import java.util.List;

public record BulkEmailResponse(
        int total,
        int sent,
        int failed,
        List<String> details
) {
}
