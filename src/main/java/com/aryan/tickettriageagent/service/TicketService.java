package com.aryan.tickettriageagent.service;

import com.aryan.tickettriageagent.api.*;
import com.aryan.tickettriageagent.domain.Ticket;
import com.aryan.tickettriageagent.domain.TriageResult;
import com.aryan.tickettriageagent.repository.TicketRepository;
import com.aryan.tickettriageagent.repository.TriageResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service @RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final TriageResultRepository triageResultRepository;

    public Page<TicketResponse> list(Pageable pageable) { return ticketRepository.findAll(pageable).map(this::toResponse); }
    public TicketResponse get(Long id) { return toResponse(findTicket(id)); }
    public TicketResponse create(TicketCreateRequest request) {
        Ticket ticket = new Ticket(); ticket.setTitle(request.title()); ticket.setDescription(request.description());
        ticket.setCategory(request.category()); ticket.setCustomerImpact(request.customerImpact());
        return toResponse(ticketRepository.save(ticket));
    }
    public TriageResultResponse latestTriage(Long ticketId) {
        findTicket(ticketId);
        return triageResultRepository.findFirstByTicketIdOrderByCreatedAtDesc(ticketId)
                .map(this::toResponse).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No triage result exists for ticket " + ticketId));
    }
    private Ticket findTicket(Long id) { return ticketRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found: " + id)); }
    private TicketResponse toResponse(Ticket t) { return new TicketResponse(t.getId(), t.getTitle(), t.getDescription(), t.getCategory(), t.getCustomerImpact(), t.getStatus(), t.getCreatedAt()); }
    private TriageResultResponse toResponse(TriageResult r) { return new TriageResultResponse(r.getId(), r.getTicket().getId(), r.getCategory(), r.getPriority(), r.getAssignedTeam(), r.getSummary(), r.getRecommendedAction(), r.getConfidence(), r.getModelName(), r.getCreatedAt()); }
}
