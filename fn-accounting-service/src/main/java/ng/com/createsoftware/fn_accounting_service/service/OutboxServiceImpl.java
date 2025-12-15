package ng.com.createsoftware.fn_accounting_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ng.com.createsoftware.fn_accounting_service.dto.request.OutboxRequest;
import ng.com.createsoftware.fn_accounting_service.model.OutboxEvent;
import ng.com.createsoftware.fn_accounting_service.repository.OutboxRepository;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxServiceImpl implements OutboxService{

    private final OutboxRepository outboxRepository;
    private final AmqpTemplate amqpTemplate;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String exchange = "audit.exchange";
    private final String routingKey = "audit.accounting";

    @Transactional
    @Override
    public void saveEvent(OutboxRequest request) throws Exception {
        String payload = mapper.writeValueAsString(request.getPayloadObj());
        OutboxEvent event = new OutboxEvent();
        event.setAggregateType(request.getAggregateType());
        event.setAggregateId(request.getAggregateId());
        event.setEventType(request.getEventType());
        event.setPayload(payload);
        event.setPublished(false);
        event.setCreatedAt(LocalDateTime.now());
        outboxRepository.save(event);
    }

    //scheduled publish
    @Override
    public void publishPending() {
        List<OutboxEvent> outboxEventList = outboxRepository.findTop100ByPublishedFalseOrderByCreatedAtAsc();
        for (OutboxEvent event : outboxEventList){
                 try {
                         amqpTemplate.convertAndSend(exchange, routingKey, event.getPayload());
                         event.setPublished(true);
                         event.setPublishedAt(LocalDateTime.now());
                         outboxRepository.save(event);
                      }catch(Exception ex){
                         System.out.println(ex.getMessage());
                      }
        }
    }
}
