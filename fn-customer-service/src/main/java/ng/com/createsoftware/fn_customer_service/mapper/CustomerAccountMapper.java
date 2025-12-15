package ng.com.createsoftware.fn_customer_service.mapper;

import ng.com.createsoftware.fn_customer_service.dto.response.CustomerAccountResponse;
import ng.com.createsoftware.fn_customer_service.model.CustomerAccount;

public class CustomerAccountMapper {
    public static CustomerAccountResponse customerAccountToCustomerAccountResponse(CustomerAccount account){
        CustomerAccountResponse response = new CustomerAccountResponse();
        response.setId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setProductCode(account.getProductCode());
        response.setCurrency(account.getCurrency());
        response.setBalance(account.getBalance());
        response.setStatus(account.getStatus().name());
        response.setCustomerFirstName(account.getCustomer().getFirstName());
        return response;
    }
}
