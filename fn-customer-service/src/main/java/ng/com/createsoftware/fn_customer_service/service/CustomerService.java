package ng.com.createsoftware.fn_customer_service.service;

import ng.com.createsoftware.fn_customer_service.dto.request.AddCustomerAccountRequest;
import ng.com.createsoftware.fn_customer_service.dto.request.AddCustomerRequest;
import ng.com.createsoftware.fn_customer_service.dto.response.CustomerAccountResponse;
import ng.com.createsoftware.fn_customer_service.dto.response.CustomerResponse;
import ng.com.createsoftware.fn_customer_service.model.Status;

import java.util.List;

public interface CustomerService {
    CustomerResponse addCustomer(AddCustomerRequest request);
    List<CustomerResponse> listCustomersByStatus(Status status);
    List<CustomerResponse> getCustomers();
    CustomerResponse getCustomer(Long customerId);
    CustomerResponse changeCustomerStatus(Long customerId, Status status);
    CustomerResponse verifyBvn(Long customerId);
    CustomerAccountResponse addAccount (Long customerId, AddCustomerAccountRequest request);
    List<CustomerAccountResponse> listAccounts(Long customerId);
}
