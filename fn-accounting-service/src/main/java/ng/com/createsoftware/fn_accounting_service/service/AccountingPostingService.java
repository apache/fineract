package ng.com.createsoftware.fn_accounting_service.service;

import ng.com.createsoftware.fn_accounting_service.dto.request.CustomerToGlRequest;
import ng.com.createsoftware.fn_accounting_service.dto.request.GlToCustomerRequest;
import ng.com.createsoftware.fn_accounting_service.dto.request.GlToGlRequest;
import ng.com.createsoftware.fn_accounting_service.dto.request.ReversalRequest;

import java.util.Map;

public interface AccountingPostingService {
    Map<String, Object> postGlToGl(GlToGlRequest request);
    Map<String, Object> postGlToCustomer(GlToCustomerRequest request);
    Map<String, Object> postCustomerToGl(CustomerToGlRequest request);
    Map<String, Object> reverseTransaction(ReversalRequest request);
}
