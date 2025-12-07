package ng.com.createsoftware.fn_accounting_service.controller;

import lombok.RequiredArgsConstructor;
import ng.com.createsoftware.fn_accounting_service.dto.request.AccountTypeRequest;
import ng.com.createsoftware.fn_accounting_service.dto.response.AccountTypeResponse;
import ng.com.createsoftware.fn_accounting_service.service.AccountTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/account-types")
@RequiredArgsConstructor
public class AccountTypeController {
    private final AccountTypeService accountTypeService;

    @GetMapping
    public ResponseEntity<List<AccountTypeResponse>> getAccountTypesHandler(){
        return new ResponseEntity<>(accountTypeService.getAccountTypes(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<AccountTypeResponse> addAccountTypeHandler(@RequestBody AccountTypeRequest request){
        return  new ResponseEntity<>(accountTypeService.addAccountType(request), HttpStatus.OK);
    }
}
