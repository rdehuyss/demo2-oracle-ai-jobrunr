package org.jobrunr.demo.tickets.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import static org.jobrunr.demo.tickets.model.TicketStatus.CLOSED;
import static org.jobrunr.demo.tickets.model.TicketStatus.OPEN;

/**
 * A support ticket entity.
 */
@Table("TICKETS")
public class Ticket {
    @Id
    private final UUID id;
    @Version
    private final int version;
    private final String subject;
    private final String description;
    private String resolution;
    private TicketStatus status;

    @PersistenceCreator
    Ticket(UUID id, int version, String subject, String description, String resolution, TicketStatus status) {
        this.id = id;
        this.version = version;
        this.subject = subject;
        this.description = description;
        this.resolution = resolution;
        this.status = status;
    }

    public static Ticket openTicket(String subject, String description) {
        return new Ticket(UUID.randomUUID(), 0, subject, description, null, OPEN);
    }

    public UUID getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public String getDescription() {
        return description;
    }

    public String getResolution() {
        return resolution;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public Ticket closeTicket(String resolution) {
        this.resolution = resolution;
        this.status = CLOSED;
        return this;
    }
}
