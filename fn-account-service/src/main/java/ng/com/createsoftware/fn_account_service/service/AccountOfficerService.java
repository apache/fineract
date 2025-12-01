package ng.com.createsoftware.fn_account_service.service;


import ng.com.createsoftware.fn_account_service.dto.request.AccountOfficerRequest;
import ng.com.createsoftware.fn_account_service.dto.response.AccountOfficerResponse;

import java.util.List;

public interface AccountOfficerService {
    List<AccountOfficerResponse> getAllAccountOfficers();

    List<AccountOfficerResponse> findAccountOfficerByBranchCode(String branchCode);

    AccountOfficerResponse getAccountOfficerById(Long accountOfficerId);

    AccountOfficerResponse addAccountOfficer(AccountOfficerRequest request);

    AccountOfficerResponse updateAccountOfficer(Long accountOfficerId, AccountOfficerRequest request);

    boolean deactivateAccountOfficer(Long accountOfficerId);
}
