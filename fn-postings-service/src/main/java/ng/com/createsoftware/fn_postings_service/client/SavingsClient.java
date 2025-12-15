package ng.com.createsoftware.fn_postings_service.client;

import ng.com.createsoftware.fn_postings_service.dto.response.SavingsAccountResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name="savings-service", url="http://localhost:9009")
public interface SavingsClient {

    @GetMapping("/api/v1/savings/{accountNumber}")
    SavingsAccountResponse getAccount(@PathVariable String accountNumber);

    @PutMapping("/api/v1/savings/{accountNumber}/credit")
    void creditAccount(@PathVariable String accountNumber, @RequestParam BigDecimal amount);

    @PutMapping("/api/v1/savings/{accountNumber}/debit")
    void debitAccount(@PathVariable String accountNumber, @RequestParam BigDecimal amount);


}
