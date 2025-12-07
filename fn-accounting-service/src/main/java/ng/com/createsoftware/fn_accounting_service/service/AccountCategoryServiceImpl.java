package ng.com.createsoftware.fn_accounting_service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ng.com.createsoftware.fn_accounting_service.dto.request.AccountCategoryRequest;
import ng.com.createsoftware.fn_accounting_service.dto.response.AccountCategoryResponse;
import ng.com.createsoftware.fn_accounting_service.mapper.AccountCategoryMapper;
import ng.com.createsoftware.fn_accounting_service.model.AccountCategory;
import ng.com.createsoftware.fn_accounting_service.model.AccountType;
import ng.com.createsoftware.fn_accounting_service.repository.AccountCategoryRepository;
import ng.com.createsoftware.fn_accounting_service.repository.AccountTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountCategoryServiceImpl implements AccountCategoryService{
    private final AccountCategoryRepository accountCategoryRepository;
    private final AccountTypeRepository accountTypeRepository;
    @Override
    public List<AccountCategoryResponse> getAccountCategories() {
        return accountCategoryRepository.findAll().stream()
                .map(AccountCategoryMapper::accountCategoryToAccountCategoryResponse)
                .toList();
    }

    @Transactional
    @Override
    public AccountCategoryResponse addAccountCategory(AccountCategoryRequest request) {
        AccountType accountType = null;
        if(request.getAccountTypeId() != null){
            accountType = accountTypeRepository.findById(request.getAccountTypeId())
                    .orElse(null);
        }
        AccountCategory accountCategory = AccountCategory.builder()
                .name(request.getName())
                .description(request.getDescription())
                .accountType(accountType)
                .build();
        accountCategory = accountCategoryRepository.save(accountCategory);
        return AccountCategoryMapper.accountCategoryToAccountCategoryResponse(accountCategory);
    }
}
