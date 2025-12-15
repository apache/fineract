package ng.com.createsoftware.fn_customer_service.repository;

import ng.com.createsoftware.fn_customer_service.model.Customer;
import ng.com.createsoftware.fn_customer_service.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerReposiitory extends JpaRepository<Customer, Long> {
    List<Customer> findByStatus(Status status);
    Optional<Customer> findByBvn(String bvn);
}
