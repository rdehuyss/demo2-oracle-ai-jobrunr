package org.jobrunr.demo.tickets;

import org.jobrunr.demo.tickets.model.SimilarTicketResult;
import org.jobrunr.demo.tickets.model.Ticket;
import org.jobrunr.demo.tickets.model.TicketStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final TicketRepository ticketRepository;

    public TicketController(TicketService ticketService, TicketRepository ticketRepository) {
        this.ticketService = ticketService;
        this.ticketRepository = ticketRepository;
    }

    @GetMapping("/")
    public ResponseEntity<List<Ticket>> listTickets(@RequestParam(required = false, defaultValue = "OPEN") TicketStatus status) {
        List<Ticket> ticketsByStatus = ticketRepository.findByStatus(status);
        return ResponseEntity.ok(ticketsByStatus);
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketView> getTicket(@PathVariable UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        List<SimilarTicketResult> similarTickets = ticketService.findSimilarTickets(ticket, 10);
        return ResponseEntity.ok(new TicketView(ticket, similarTickets));
    }

    @PostMapping("/")
    public ResponseEntity<Ticket> submitTicket(String subject, String description) {
        Ticket ticket = Ticket.openTicket(subject, description);
        ticketRepository.save(ticket);
        return ResponseEntity.ok(ticket);
    }

    @PostMapping("/{ticketId}/resolve")
    public ResponseEntity<Ticket> resolveTicket(@PathVariable UUID ticketId, String resolution) {
        Ticket closedTicket = ticketService.closeTicket(ticketId, resolution);
        return ResponseEntity.ok(closedTicket);
    }

    public record TicketView(Ticket ticket, List<SimilarTicketResult> similarTickets) {}
}
