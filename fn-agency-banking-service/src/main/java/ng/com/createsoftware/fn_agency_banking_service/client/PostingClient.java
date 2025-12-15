package ng.com.createsoftware.fn_agency_banking_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name="posting-service", url="${posting.service.url}")
public interface PostingClient {

    @PostMapping("/api/v1/posting/deposit")
    Map<String, Object> deposit(@RequestBody Map<String, Object> body);

    @PostMapping("/api/v1/posting/withdraw")
    Map<String, Object> withdraw(@RequestBody Map<String, Object> body);

    @PostMapping("/api/v1/posting/transfer")
    Map<String, Object> transfer(@RequestBody Map<String, Object> body);

    @PostMapping("/api/v1/posting/reverse")
    Map<String, Object> reverse(@RequestBody Map<String, Object> body);
}
