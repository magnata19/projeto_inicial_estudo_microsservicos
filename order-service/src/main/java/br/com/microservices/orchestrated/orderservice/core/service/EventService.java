package br.com.microservices.orchestrated.orderservice.core.service;

import br.com.microservices.orchestrated.orderservice.config.exception.ValidationException;
import br.com.microservices.orchestrated.orderservice.core.document.Event;
import br.com.microservices.orchestrated.orderservice.core.dto.EventFilters;
import br.com.microservices.orchestrated.orderservice.core.repository.EventRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;


import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public void notifyEnding(Event event) {
        event.setId(event.getOrderId());
        event.setCreatedAt(LocalDateTime.now());
        save(event);
        log.info("Order {} with saga notified! TransactionID: {}", event.getOrderId(), event.getTransactionId());
    }

    public List<Event> findAll() {
        return eventRepository.findAllOrderByCreatedAtDesc();
    }

    public Event findByFilters(EventFilters filters) {
        validateEmptyFilters(filters);
        if(!ObjectUtils.isEmpty(filters)) {
            return findByOrderId(filters.getOrderId());
        } else {
            return findByTransactionId(filters.getTransactionId());
        }
    }

    public Event findByOrderId(String orderId) {
        return eventRepository.findTop1OrderIdOrderByCreatedAtDesc(orderId).orElseThrow(
                () -> new ValidationException("Event not found by orderId")
        );
    }

    public Event findByTransactionId (String transactionId) {
        return eventRepository.findTop1TransactionIdOrderByCreatedAtDesc(transactionId).
                orElseThrow(() -> new ValidationException("Event not found by TransactionId"));
    }

    private Event validateEmptyFilters(EventFilters filters) {
        if (ObjectUtils.isEmpty(filters.getOrderId()) && ObjectUtils.isEmpty(filters.getTransactionId())) {
            throw new ValidationException("OrderID and TransactionId must be informed.");
        }
            return null;
    }

    public Event save (Event event) {
        return eventRepository.save(event);
    }
}
