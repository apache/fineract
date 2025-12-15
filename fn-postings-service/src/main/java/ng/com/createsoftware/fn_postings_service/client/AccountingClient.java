package ng.com.createsoftware.fn_postings_service.client;

import ng.com.createsoftware.fn_postings_service.dto.request.GlPostingRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="accounting-service", url="http://localhost:9007")
public interface AccountingClient {
    @PostMapping("/api/v1/gl/posting")
    void glPosting(@RequestBody GlPostingRequest request);
}
