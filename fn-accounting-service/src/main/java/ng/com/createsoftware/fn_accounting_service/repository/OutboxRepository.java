package ng.com.createsoftware.fn_accounting_service.repository;

import ng.com.createsoftware.fn_accounting_service.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findTop100ByPublishedFalseOrderByCreatedAtAsc();
}
