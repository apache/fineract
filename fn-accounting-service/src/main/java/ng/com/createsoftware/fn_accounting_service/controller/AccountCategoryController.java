package ng.com.createsoftware.fn_accounting_service.controller;

import lombok.RequiredArgsConstructor;
import ng.com.createsoftware.fn_accounting_service.dto.request.AccountCategoryRequest;
import ng.com.createsoftware.fn_accounting_service.dto.response.AccountCategoryResponse;
import ng.com.createsoftware.fn_accounting_service.service.AccountCategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/account-categories")
@RequiredArgsConstructor
public class AccountCategoryController {
    private final AccountCategoryService accountCategoryService;

    @GetMapping
    public ResponseEntity<List<AccountCategoryResponse>>getAccountCategoriesHandler(){
        return new ResponseEntity<>(accountCategoryService.getAccountCategories(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<AccountCategoryResponse>addAccountCategoryHandler(@RequestBody AccountCategoryRequest request){
        return new ResponseEntity<>(accountCategoryService.addAccountCategory(request), HttpStatus.OK);
    }

}
