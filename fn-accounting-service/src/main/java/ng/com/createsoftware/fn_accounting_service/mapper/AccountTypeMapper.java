package ng.com.createsoftware.fn_accounting_service.mapper;

import ng.com.createsoftware.fn_accounting_service.dto.response.AccountTypeResponse;
import ng.com.createsoftware.fn_accounting_service.model.AccountType;

public class AccountTypeMapper {
    public static AccountTypeResponse accountTypeToAccountTypeResponse(AccountType accountType){
        AccountTypeResponse response = new AccountTypeResponse();
        response.setId((accountType.getId()));
        response.setName(accountType.getName());
        response.setDescription(accountType.getDescription());
        return response;
    }
}
