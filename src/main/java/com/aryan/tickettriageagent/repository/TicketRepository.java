package com.aryan.tickettriageagent.repository;

import com.aryan.tickettriageagent.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.aryan.tickettriageagent.domain.TicketStatus;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Page<Ticket> findByStatusOrderByCreatedAtAsc(TicketStatus status, Pageable pageable);
}
