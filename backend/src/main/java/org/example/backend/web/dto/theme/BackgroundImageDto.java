package org.example.backend.web.dto.theme;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BackgroundImageDto {
    private Long id;
    private String url;
    private boolean active;
}
