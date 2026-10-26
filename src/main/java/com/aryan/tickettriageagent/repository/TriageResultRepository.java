package com.aryan.tickettriageagent.repository;

import com.aryan.tickettriageagent.domain.TriageResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TriageResultRepository extends JpaRepository<TriageResult, Long> {
    Optional<TriageResult> findFirstByTicketIdOrderByCreatedAtDesc(Long ticketId);
}
