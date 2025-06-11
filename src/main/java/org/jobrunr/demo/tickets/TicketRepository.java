package org.jobrunr.demo.tickets;

import org.jobrunr.demo.tickets.model.SimilarTicketResult;
import org.jobrunr.demo.tickets.model.Ticket;
import org.jobrunr.demo.tickets.model.TicketStatus;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Basic CRUD repository for TICKETS (without embedding).
 */
@Repository
public interface TicketRepository extends CrudRepository<Ticket, UUID> {

    List<Ticket> findByStatus(TicketStatus status);

    @Query("SELECT VECTOR_EMBEDDING(ALL_MINILM_L12_V2 USING :text AS DATA) AS EMB FROM DUAL")
    Object computeEmbedding(@Param("text") String text);


    @Query("""
        SELECT ID, SUBJECT, DESCRIPTION, RESOLUTION, (1 - VECTOR_DISTANCE(EMBEDDING, :vector, COSINE)) AS SCORE
        FROM TICKETS
        WHERE STATUS = 'CLOSED'
        ORDER BY SCORE DESC
        FETCH FIRST :topK ROWS ONLY
        """)
    List<SimilarTicketResult> findTopKByEmbedding(@Param("vector") double[] vector, @Param("topK") int topK);

    @Modifying
    @Query("UPDATE TICKETS SET EMBEDDING = :vector WHERE ID = :ticketId")
    void updateEmbedding(@Param("ticketId") UUID ticketId, @Param("vector") double[] vector);

    List<Ticket> findTicketsByCreatedAtAfter(Instant createdAtAfter);
}
