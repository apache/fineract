package ng.com.createsoftware.fn_accounting_service.service;

import ng.com.createsoftware.fn_accounting_service.dto.request.AccountTypeRequest;
import ng.com.createsoftware.fn_accounting_service.dto.response.AccountTypeResponse;

import java.util.List;

public interface AccountTypeService {

    List<AccountTypeResponse> getAccountTypes();

    AccountTypeResponse getAccountType(Long accountTypeId);

    AccountTypeResponse addAccountType(AccountTypeRequest request);

}
