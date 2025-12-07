package ng.com.createsoftware.fn_wallet_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditPublisherService {
    private final AmqpTemplate amqpTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    public void publishWalletTopup(Long clientId, BigDecimal amount, String phone){
             try {
                     Map<String, Object> payload = Map.of(
                             "eventType", "WALLET_TOPUP",
                             "details", "clientId="+clientId+",amount=" + amount+",phone="+ phone,
                             "timestamp", Instant.now().toString()
                     );
                     String json = mapper.writeValueAsString(payload);
                     amqpTemplate.convertAndSend(exchangeName, routingKey, json);
                  }catch(Exception ex){
                     System.out.println(ex.getMessage());
                  }
    }
}
