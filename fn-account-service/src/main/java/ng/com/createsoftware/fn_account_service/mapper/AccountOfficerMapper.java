package ng.com.createsoftware.fn_account_service.mapper;

import ng.com.createsoftware.fn_account_service.dto.response.AccountOfficerResponse;
import ng.com.createsoftware.fn_account_service.model.AccountOfficer;

public class AccountOfficerMapper {
    public static AccountOfficerResponse accountOfficerToAccountOfficerResponse(AccountOfficer accountOfficer){
        AccountOfficerResponse response = new AccountOfficerResponse();
        response.setId(accountOfficer.getId());
        response.setCode(accountOfficer.getCode());
        response.setFirstName(accountOfficer.getFirstName());
        response.setLastName(accountOfficer.getLastName());
        response.setPhone(accountOfficer.getPhone());
        response.setEmail(accountOfficer.getEmail());
        response.setBranchCode(accountOfficer.getBranchCode());
        response.setStatus(accountOfficer.getStatus().name());
        return response;
    }
}
