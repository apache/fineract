package ng.com.createsoftware.fn_postings_service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ng.com.createsoftware.fn_postings_service.model.PostingTransaction;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostingEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    @Value("${posting.events.exchange}")
    private String postingExchange;
    @Value("${posting.events.routing.key}")
    private String routingKey;

    public void publishPostingEvent(PostingTransaction transaction){
        Map<String, Object> payload = Map.of(
                "transactionId", transaction.getId(),
                "type", transaction.getType(),
                "amount", transaction.getAmount(),
                "accountNumber", transaction.getAccountNumber(),
                "timestamp", LocalDateTime.now().toString()
        );

        rabbitTemplate.convertAndSend(
                 postingExchange,
                routingKey,
                payload
        );
    }
}
