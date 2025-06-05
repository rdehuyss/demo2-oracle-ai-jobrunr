package org.jobrunr.demo.tickets.model;

import java.util.UUID;

/**
 * Projection for a “similar ticket” result:
 *   id, subject, description, resolution, score
 */
public record SimilarTicketResult(
        UUID id,
        String subject,
        String description,
        String resolution,
        double score
) {}
