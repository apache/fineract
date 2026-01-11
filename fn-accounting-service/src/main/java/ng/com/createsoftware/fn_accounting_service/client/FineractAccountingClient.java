package ng.com.createsoftware.fn_accounting_service.client;

import ng.com.createsoftware.fn_accounting_service.dto.request.CreateGLRequest;
import ng.com.createsoftware.fn_accounting_service.dto.request.JournalEntryRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="fineract", url="${fineract.base.url}", configuration = FineractFeignConfig.class)
public interface FineractAccountingClient {

    @PostMapping("/v1/glaccounts")
    void createGL(@RequestBody CreateGLRequest request);

    @PostMapping("/v1/journalentries")
    void postJournal(@RequestBody JournalEntryRequest request);

    @PostMapping("/v1/journalentries/{id}")
    void reverse(@PathVariable Long id, @RequestParam("command") String comand);
}
