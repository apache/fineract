package ng.com.createsoftware.fn_accounting_service.service;

import ng.com.createsoftware.fn_accounting_service.dto.request.CapitalRequest;
import ng.com.createsoftware.fn_accounting_service.dto.response.CapitalResponse;

import java.util.List;

public interface CapitalService {
    List<CapitalResponse> getCapitals();
    CapitalResponse addCapital(CapitalRequest request);
}
