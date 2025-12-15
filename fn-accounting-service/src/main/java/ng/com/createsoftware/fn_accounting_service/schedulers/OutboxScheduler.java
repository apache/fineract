package ng.com.createsoftware.fn_accounting_service.schedulers;

import lombok.RequiredArgsConstructor;
import ng.com.createsoftware.fn_accounting_service.service.OutboxService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxScheduler {
    private final OutboxService outboxService;

    @Scheduled(fixedDelayString = "PT10S") //every  10 seconds
    public  void run(){
        outboxService.publishPending();
    }
}
