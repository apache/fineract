package ng.com.createsoftware.fn_accounting_service.service;

import ng.com.createsoftware.fn_accounting_service.dto.request.AccountCategoryRequest;
import ng.com.createsoftware.fn_accounting_service.dto.response.AccountCategoryResponse;
import ng.com.createsoftware.fn_accounting_service.model.AccountCategory;

import java.util.List;

public interface AccountCategoryService {

    List<AccountCategoryResponse> getAccountCategories();
    AccountCategoryResponse addAccountCategory(AccountCategoryRequest request);
}
