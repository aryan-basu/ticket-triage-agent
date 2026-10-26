package com.aryan.tickettriageagent.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TicketCreateRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank String description,
        @Size(max = 50) String category,
        @Size(max = 50) String customerImpact) { }
