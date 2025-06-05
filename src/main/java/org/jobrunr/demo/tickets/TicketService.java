package org.jobrunr.demo.tickets;

import org.jobrunr.demo.tickets.model.SimilarTicketResult;
import org.jobrunr.demo.tickets.model.Ticket;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final JobScheduler jobScheduler;

    public TicketService(TicketRepository ticketRepository, JobScheduler jobScheduler) {
        this.ticketRepository = ticketRepository;
        this.jobScheduler = jobScheduler;
    }

    /**
     * {@link TicketService#closeTicket(UUID, String)}
     * @param ticket
     * @param amount
     * @return
     */
    public List<SimilarTicketResult> findSimilarTickets(Ticket ticket, int amount) {
        double[] embedding = getEmbeddingForTicket(ticket);
        return ticketRepository.findTopKByEmbedding(embedding, amount);
    }

    public Ticket closeTicket(UUID ticketId, String resolution) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        Ticket resolvedTicket = ticket.closeTicket(resolution);
        ticketRepository.save(resolvedTicket);

        // Enqueue a background job:
        //jobScheduler.enqueue(() -> computeAndStoreEmbedding(ticketId));

//        double[] embedding = getEmbeddingForTicket(ticket);
//        ticketRepository.updateEmbedding(ticketId, embedding);

        ticketRepository.updateEmbeddingV2(ticketId, getTextForEmbedding(resolvedTicket));

        return ticket;
    }
    
    public void computeAndStoreEmbedding(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        double[] embedding = getEmbeddingForTicket(ticket);
        ticketRepository.updateEmbedding(ticketId, embedding);
    }

    private String getTextForEmbedding(Ticket ticket) {
        return ticket.getSubject() + "\n" + ticket.getDescription();
    }

    private double[] getEmbeddingForTicket(Ticket ticket) {
        return (double[]) ticketRepository.computeEmbedding(getTextForEmbedding(ticket));
    }
}
