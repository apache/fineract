package ng.com.createsoftware.fn_agency_banking_service.controller;

import lombok.RequiredArgsConstructor;
import ng.com.createsoftware.fn_agency_banking_service.dto.request.TillCustomerRequest;
import ng.com.createsoftware.fn_agency_banking_service.dto.request.TillVaultRequest;
import ng.com.createsoftware.fn_agency_banking_service.dto.response.TillTransactionResponse;
import ng.com.createsoftware.fn_agency_banking_service.service.TillVaultService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/agency")
public class AgencyController {
    private final TillVaultService tillVaultService;

    @PostMapping("/pos")
    public ResponseEntity<TillTransactionResponse> posTxnHandler(@RequestBody TillCustomerRequest request){
        return new ResponseEntity<>(tillVaultService.tillToCustomer(request), HttpStatus.CREATED);
    }

    @PostMapping("/ussd")
    public ResponseEntity<TillTransactionResponse> ussdHandler(@RequestBody TillCustomerRequest request){
        return new ResponseEntity<>(tillVaultService.tillToCustomer(request), HttpStatus.CREATED);
    }

    @PostMapping("/atm/withdraw")
    public ResponseEntity<TillTransactionResponse> atmWithdrawalHandler(@RequestBody TillCustomerRequest request){
        return new ResponseEntity<>(tillVaultService.customerToTill(request), HttpStatus.CREATED);
    }

//    @PostMapping("/vault/{vaultId}/to-till/{tillId}")
    @PostMapping("/vault/to-till")
    public ResponseEntity<TillTransactionResponse> vaultToTillHandler(@RequestBody TillVaultRequest request){
        return new ResponseEntity<>(tillVaultService.vaultToTill(request), HttpStatus.CREATED);
    }

    @PostMapping("/till/to-vault")
    public ResponseEntity<TillTransactionResponse> tillToVaultHandler(@RequestBody TillVaultRequest request){
        return new ResponseEntity<>(tillVaultService.tillToVault(request), HttpStatus.CREATED);
    }

    //customer withdraw via till
    @PostMapping("/till/{tillId}/withdraw")
    public ResponseEntity<TillTransactionResponse> tillWithdrawalHandler(@RequestBody TillCustomerRequest request){
        return new ResponseEntity<>(tillVaultService.customerToTill(request), HttpStatus.CREATED);
    }


}
