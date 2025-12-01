package ng.com.createsoftware.fn_account_service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ng.com.createsoftware.fn_account_service.dto.request.AccountOfficerRequest;
import ng.com.createsoftware.fn_account_service.dto.response.AccountOfficerResponse;
import ng.com.createsoftware.fn_account_service.mapper.AccountOfficerMapper;
import ng.com.createsoftware.fn_account_service.model.AccountOfficer;
import ng.com.createsoftware.fn_account_service.model.Status;
import ng.com.createsoftware.fn_account_service.repository.AccountOfficerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountOfficerServiceImpl implements AccountOfficerService{

    private final AccountOfficerRepository accountOfficerRepository;

    @Override
    public List<AccountOfficerResponse> getAllAccountOfficers() {
        return accountOfficerRepository.findAll()
                .stream()
                .map(AccountOfficerMapper::accountOfficerToAccountOfficerResponse)
                .toList();
    }

    @Override
    public List<AccountOfficerResponse> findAccountOfficerByBranchCode(String branchCode) {
        return accountOfficerRepository.findByBranchCode(branchCode)
                .stream()
                .map(AccountOfficerMapper::accountOfficerToAccountOfficerResponse)
                .toList();
    }

    @Override
    public AccountOfficerResponse getAccountOfficerById(Long accountOfficerId) {
        return accountOfficerRepository.findById(accountOfficerId)
                .map(AccountOfficerMapper::accountOfficerToAccountOfficerResponse)
                .orElse(null);
    }

    @Transactional
    @Override
    public AccountOfficerResponse addAccountOfficer(AccountOfficerRequest request) {
        AccountOfficer officer = new AccountOfficer();
        officer.setCode(request.getCode());
        officer.setFirstName(request.getFirstName());
        officer.setLastName(request.getLastName());
        officer.setPhone(request.getPhone());
        officer.setEmail(request.getEmail());
        officer.setBranchCode(request.getBranchCode());
        if(request.getStatus() != null)
            officer.setStatus(Status.valueOf(request.getStatus()));

        AccountOfficer savedOfficer = accountOfficerRepository.save(officer);
        return AccountOfficerMapper.accountOfficerToAccountOfficerResponse(savedOfficer);
    }

    @Transactional
    @Override
    public AccountOfficerResponse updateAccountOfficer(Long accountOfficerId, AccountOfficerRequest request) {
        return accountOfficerRepository.findById(accountOfficerId)
                .map(officer -> {
                    if(request.getFirstName() != null) officer.setFirstName(request.getFirstName());
                    if(request.getLastName() != null) officer.setLastName(request.getLastName());
                    if(request.getPhone() != null) officer.setPhone(request.getPhone());
                    if(request.getEmail() != null) officer.setEmail(request.getEmail());
                    if(request.getBranchCode() != null) officer.setBranchCode(request.getBranchCode());
                    if(request.getStatus() != null) officer.setStatus(Status.valueOf(request.getStatus()));
                    AccountOfficer updatedOfficer = accountOfficerRepository.save(officer);
                    return AccountOfficerMapper.accountOfficerToAccountOfficerResponse(updatedOfficer);
                }).orElse(null);
    }

    @Override
    public boolean deactivateAccountOfficer(Long accountOfficerId) {
        return accountOfficerRepository.findById(accountOfficerId)
                .map(officer -> {
                    officer.setStatus(Status.INACTIVE);
                    accountOfficerRepository.save(officer);
                    return true;
                }).orElse(false);
    }
}
