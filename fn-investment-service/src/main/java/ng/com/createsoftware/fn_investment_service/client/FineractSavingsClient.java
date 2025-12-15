package ng.com.createsoftware.fn_investment_service.client;

import ng.com.createsoftware.fn_investment_service.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;


    @FeignClient(name = "fineract", url="${fineract.url}", configuration = FeignConfig.class)
    public interface FineractSavingsClient{
        @PostMapping("/savings-products")
        Map<String, Object> createProduct(@RequestBody Map<String, Object> body);

        @PostMapping("/savings-accounts")
        Map<String, Object> createAccount(@RequestBody Map<String, Object> body);

        @PostMapping("/savings-accounts/{accountId}")
        void closeAccount(
                @PathVariable Long accountId,
                @RequestParam String command,
                @RequestBody Map<String, Object> body);
    }

