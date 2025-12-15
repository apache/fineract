package ng.com.createsoftware.fn_loaning_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name="fineract-loan", url="${fineract.url}")
public interface FineractLoanClient {
    @PostMapping("/loans")
    Map<String, Object> apply(@RequestBody Map<String, Object> body);

    @PostMapping("/loans/{id}")
    void command(@PathVariable Long id, @RequestParam String command, @RequestBody Map<String, Object> body);

    @PostMapping("/loans/calculate")
    Map<String, Object> calculate(@RequestBody Map<String, Object> body);
}
