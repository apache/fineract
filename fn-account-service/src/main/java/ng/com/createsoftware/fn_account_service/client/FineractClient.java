package ng.com.createsoftware.fn_account_service.client;

import ng.com.createsoftware.fn_account_service.config.FineractFeignConfig;
import ng.com.createsoftware.fn_account_service.dto.request.AccountOfficerRequest;
import ng.com.createsoftware.fn_account_service.dto.request.JournalEntryRequest;
import ng.com.createsoftware.fn_account_service.dto.request.LoanApplicationRequest;
import ng.com.createsoftware.fn_account_service.dto.response.AccountOfficerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(
        name="fineract",
        url="${fineract.base-url}",
       configuration =  FineractFeignConfig.class
)
public interface FineractClient {

    @PostMapping("/fineract-provider/v1/journalentries")
    void postJournalEntry(@RequestBody JournalEntryRequest request);

    @PostMapping("/fineract-provider/v1/loans")
    Map<String, Object> applyLoan(@RequestBody LoanApplicationRequest request);

    @PostMapping("/fineract-provider/v1/staff")
    void createStaff(@RequestBody AccountOfficerRequest request);

    @GetMapping("/fineract-provider/api/v1/staff")
    List<AccountOfficerResponse> allStaff();

    @PutMapping("/fineract-provider/v1/staff{staffId}")
    void updateStaff(@PathVariable Long staffId, @RequestBody AccountOfficerRequest request);
}
