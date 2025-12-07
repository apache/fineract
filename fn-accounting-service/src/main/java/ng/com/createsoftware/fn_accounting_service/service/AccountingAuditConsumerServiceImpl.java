package ng.com.createsoftware.fn_accounting_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ng.com.createsoftware.fn_accounting_service.config.RabbitConfig;
import ng.com.createsoftware.fn_accounting_service.dto.response.AuditPayload;
import ng.com.createsoftware.fn_accounting_service.model.AuditLog;
import ng.com.createsoftware.fn_accounting_service.repository.AuditLogRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountingAuditConsumerServiceImpl implements AccountingAuditConsumerService{

    private final AuditLogRepository auditLogRepository;

//    private final String auditExchange = "audit.exchange";
//    private final String auditRoutingKey = "audit.accounting";
//    @Value("${rabbitmq.queue.name: audit.accounting.queue}")
//    private String auditQueue;

    @Override
    @RabbitListener(queues = "audit.accounting.queue")
    public void listen(AuditPayload payload) {
        AuditLog auditLog = AuditLog.builder()
                .eventType(payload.getEventType())
                .details(payload.getDetails())
                .timestamp(payload.getTimestamp())
                .build();

        auditLogRepository.save(auditLog);
        log.info("Audit Log saved: " + payload.getDetails());
    }
}
