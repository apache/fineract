package ng.com.createsoftware.fn_postings_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name="customer-service", url="http://localhost:9097")
public interface CustomerClient {

    @GetMapping("/api/v1/customers/{id}/balance")
    BigDecimal getBalance(@PathVariable String id);

    @PutMapping("/api/v1/customers/{id}/balance/debit")
    void debit(@PathVariable String id, @RequestParam BigDecimal amount);

    @PutMapping("/api/v1/customers/{id}/balance/credit")
    void credit(@PathVariable String id, @RequestParam BigDecimal amount);
}
