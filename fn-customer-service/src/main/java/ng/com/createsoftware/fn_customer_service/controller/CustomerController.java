package ng.com.createsoftware.fn_customer_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ng.com.createsoftware.fn_customer_service.dto.request.AddCustomerAccountRequest;
import ng.com.createsoftware.fn_customer_service.dto.request.AddCustomerRequest;
import ng.com.createsoftware.fn_customer_service.dto.response.CustomerAccountResponse;
import ng.com.createsoftware.fn_customer_service.dto.response.CustomerResponse;
import ng.com.createsoftware.fn_customer_service.model.Status;
import ng.com.createsoftware.fn_customer_service.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customers")
public class CustomerController {
    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerResponse> addCustomerHandler(@Valid @RequestBody AddCustomerRequest request){
        return new ResponseEntity<>(customerService.addCustomer(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getCustomersByStatusHandler(@RequestParam(value="status", required = false) Status status){
        return new ResponseEntity<>(customerService.listCustomersByStatus(status), HttpStatus.OK);
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> getCustomerHandler(@PathVariable Long customerId){
        return new ResponseEntity<>(customerService.getCustomer(customerId), HttpStatus.OK);
    }

    @PostMapping("/{customerId}/status")
    public ResponseEntity<CustomerResponse> changeStatusHandler(@PathVariable Long customerId, @RequestParam Status status){
        return new ResponseEntity<>(customerService.changeCustomerStatus(customerId, status), HttpStatus.OK);
    }

    @PostMapping("/{customerId}/verify-bvn")
    public ResponseEntity<CustomerResponse> verifyBvnResponse(@PathVariable Long customerId){
        return new ResponseEntity<>(customerService.verifyBvn(customerId), HttpStatus.CREATED);
    }

    @PostMapping("/{customerId}/accounts")
    public ResponseEntity<CustomerAccountResponse> addAccountHandler(@PathVariable Long customerId, @Valid @RequestBody AddCustomerAccountRequest request){
        return new ResponseEntity<>(customerService.addAccount(customerId, request), HttpStatus.CREATED);
    }

    @GetMapping("/{customerId}/accounts")
    public ResponseEntity<List<CustomerAccountResponse>> ListAccountHandler(@PathVariable Long customerId){
        return new ResponseEntity<>(customerService.listAccounts(customerId), HttpStatus.OK);
    }
}
