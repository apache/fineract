package ng.com.createsoftware.fn_accounting_service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ng.com.createsoftware.fn_accounting_service.dto.request.AccountTypeRequest;
import ng.com.createsoftware.fn_accounting_service.dto.response.AccountTypeResponse;
import ng.com.createsoftware.fn_accounting_service.mapper.AccountTypeMapper;
import ng.com.createsoftware.fn_accounting_service.model.AccountType;
import ng.com.createsoftware.fn_accounting_service.repository.AccountTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountTypeServiceImpl implements  AccountTypeService{

    private final AccountTypeRepository accountTypeRepository;

    @Override
    public List<AccountTypeResponse> getAccountTypes() {
        return accountTypeRepository.findAll()
                .stream()
                .map(AccountTypeMapper::accountTypeToAccountTypeResponse)
                .toList();
    }

    @Override
    public AccountTypeResponse getAccountType(Long accountTypeId) {
        return accountTypeRepository.findById(accountTypeId)
                .map(AccountTypeMapper::accountTypeToAccountTypeResponse)
                .orElse(null);
    }

    @Transactional
    @Override
    public AccountTypeResponse addAccountType(AccountTypeRequest request) {
       AccountType accountType = AccountType.builder()
               .name(request.getName())
               .description(request.getDescription())
               .build();
       accountType = accountTypeRepository.save(accountType);
       return AccountTypeMapper.accountTypeToAccountTypeResponse(accountType);
    }


}
