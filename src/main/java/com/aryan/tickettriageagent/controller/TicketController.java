package com.aryan.tickettriageagent.controller;

import com.aryan.tickettriageagent.api.*;
import com.aryan.tickettriageagent.service.TicketService;
import com.aryan.tickettriageagent.service.TriageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/tickets") @RequiredArgsConstructor
public class TicketController {
    private final TicketService ticketService;
    private final TriageService triageService;

    @GetMapping public Page<TicketResponse> list(@PageableDefault(size = 20) Pageable pageable) { return ticketService.list(pageable); }
    @GetMapping("/{id}") public TicketResponse get(@PathVariable Long id) { return ticketService.get(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public TicketResponse create(@Valid @RequestBody TicketCreateRequest request) { return ticketService.create(request); }
    @GetMapping("/{id}/triage") public TriageResultResponse latestTriage(@PathVariable Long id) { return ticketService.latestTriage(id); }
    @PostMapping("/{id}/triage") public TriageResultResponse triage(@PathVariable Long id) { return triageService.triage(id); }
}
