package ng.com.createsoftware.fn_account_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ng.com.createsoftware.fn_account_service.dto.request.AccountOfficerRequest;
import ng.com.createsoftware.fn_account_service.dto.response.AccountOfficerResponse;
import ng.com.createsoftware.fn_account_service.service.AccountOfficerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/account-officers")
@RequiredArgsConstructor
public class AccountOfficerController {
    private final AccountOfficerService accountOfficerService;

    @GetMapping
    public ResponseEntity<List<AccountOfficerResponse>> getAccountOfficersHandler(@RequestParam(value="branch", required=false) String branch){
        if(branch != null)
            return new ResponseEntity<>(accountOfficerService.findAccountOfficerByBranchCode(branch), HttpStatus.OK);
        return new ResponseEntity<>(accountOfficerService.getAllAccountOfficers(), HttpStatus.OK);
    }

    @GetMapping("/{accountOfficerId}")
    public ResponseEntity<AccountOfficerResponse> getAccountOfficersByIdHandler(@PathVariable Long accountOfficerId){
        AccountOfficerResponse response = accountOfficerService.getAccountOfficerById(accountOfficerId);
        if(response == null)
            return ResponseEntity.notFound().build();
        return  ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<AccountOfficerResponse> addAccountOfficerHandler(@Valid @RequestBody AccountOfficerRequest request){
        return new ResponseEntity<>(accountOfficerService.addAccountOfficer(request), HttpStatus.CREATED);
    }

    @PutMapping("/{accountOfficerId}")
    public ResponseEntity<AccountOfficerResponse> updateAccountOfficerHandler(@PathVariable Long accountOfficerId, @Valid @RequestBody AccountOfficerRequest request){
        AccountOfficerResponse updatedAccountOfficer = accountOfficerService.updateAccountOfficer(accountOfficerId, request);
        if(updatedAccountOfficer == null)
            return ResponseEntity.notFound().build();
        return new ResponseEntity<>(accountOfficerService.updateAccountOfficer(accountOfficerId,request), HttpStatus.OK);
    }

    @DeleteMapping("/{accountOfficerId}")
    public ResponseEntity<Void> deactivateAccountOfficerHandler(@PathVariable Long accountOfficerId){
        boolean ok = accountOfficerService.deactivateAccountOfficer(accountOfficerId);
        if(!ok)
            return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }
}
