package com.aryan.tickettriageagent.api;

import java.util.List;

public record BatchTriageResponse(int requested, int processed, int succeeded, int failed, List<Long> succeededTicketIds, List<BatchFailure> failures) {
    public record BatchFailure(Long ticketId, String message) { }
}
