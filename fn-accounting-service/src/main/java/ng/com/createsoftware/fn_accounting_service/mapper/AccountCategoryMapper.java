package ng.com.createsoftware.fn_accounting_service.mapper;


import ng.com.createsoftware.fn_accounting_service.dto.response.AccountCategoryResponse;
import ng.com.createsoftware.fn_accounting_service.model.AccountCategory;

public class AccountCategoryMapper {

    public static AccountCategoryResponse accountCategoryToAccountCategoryResponse(AccountCategory accountCategory){
        AccountCategoryResponse response = new AccountCategoryResponse();
        response.setId((accountCategory.getId()));
        response.setName(accountCategory.getName());
        response.setDescription(accountCategory.getDescription());
        return response;
    }
}
