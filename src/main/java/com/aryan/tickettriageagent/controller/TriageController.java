package com.aryan.tickettriageagent.controller;

import com.aryan.tickettriageagent.api.BatchTriageRequest;
import com.aryan.tickettriageagent.api.BatchTriageResponse;
import com.aryan.tickettriageagent.service.TriageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/triage") @RequiredArgsConstructor
public class TriageController {
    private final TriageService triageService;

    @PostMapping("/batch")
    public BatchTriageResponse batch(@Valid @RequestBody BatchTriageRequest request) { return triageService.batch(request.limit()); }
}
