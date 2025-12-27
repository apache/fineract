package ng.com.createsoftware.fn_asset_service.client;

import ng.com.createsoftware.fn_asset_service.dto.response.ClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="fineract")
public interface FineractClientFeign {

    @GetMapping("/fineract-provider/v1/clients{clientId}")
    ClientResponse getClient(@PathVariable Long clientId);
}
