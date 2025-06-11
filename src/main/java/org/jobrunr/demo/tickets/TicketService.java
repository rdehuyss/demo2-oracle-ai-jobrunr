package org.jobrunr.demo.tickets;

import org.jobrunr.demo.tickets.model.SimilarTicketResult;
import org.jobrunr.demo.tickets.model.Ticket;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.jobs.annotations.Recurring;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.scheduling.cron.Cron;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.UUID;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final JobScheduler jobScheduler;

    public TicketService(TicketRepository ticketRepository, JobScheduler jobScheduler) {
        this.ticketRepository = ticketRepository;
        this.jobScheduler = jobScheduler;
    }

    public List<SimilarTicketResult> findSimilarTickets(Ticket ticket, int amount) {
        double[] embedding = getEmbeddingForTicket(ticket);
        return ticketRepository.findTopKByEmbedding(embedding, amount);
    }

    public Ticket closeTicket(UUID ticketId, String resolution) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        Ticket resolvedTicket = ticket.closeTicket(resolution);
        ticketRepository.save(resolvedTicket);

        // Enqueue a background job to update the embeddings
        jobScheduler.enqueue(() -> computeAndStoreEmbedding(ticketId));

        return ticket;
    }

    @Job(name = "Send daily report of created and resolved tickets")
    @Recurring(cron = "50 23 * * *")
    public void sendDailyReport() {
        Instant since = LocalDateTime.now().withHour(0).withMinute(0).atZone(ZoneId.systemDefault()).toInstant();
        List<Ticket> ticketsOfToday = ticketRepository.findTicketsByCreatedAtAfter(since);
        System.out.println(ticketsOfToday);
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
