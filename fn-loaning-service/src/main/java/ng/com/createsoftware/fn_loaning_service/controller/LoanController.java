package ng.com.createsoftware.fn_loaning_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ng.com.createsoftware.fn_loaning_service.dto.request.AddLoanProductRequest;
import ng.com.createsoftware.fn_loaning_service.dto.request.AddLoanRequest;
import ng.com.createsoftware.fn_loaning_service.dto.request.RepaymentRequest;
import ng.com.createsoftware.fn_loaning_service.model.Loan;
import ng.com.createsoftware.fn_loaning_service.model.LoanProduct;
import ng.com.createsoftware.fn_loaning_service.model.LoanRepayment;
import ng.com.createsoftware.fn_loaning_service.service.LoanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
public class LoanController {
    private final LoanService loanService;

    @PostMapping("/products")
    public ResponseEntity<LoanProduct> addProductHandler(@RequestBody AddLoanProductRequest request){
        return new ResponseEntity<>(loanService.addProduct(request), HttpStatus.CREATED);
    }

    @PostMapping
    public ResponseEntity<Loan> addLoanHandler(@Valid @RequestBody AddLoanRequest request){
        return new ResponseEntity<>(loanService.addLoan(request), HttpStatus.CREATED);
    }

    @PostMapping("/{loanId}/approve")
    public ResponseEntity<Loan> approveHandler(@PathVariable Long loanId){
        return new ResponseEntity<>(loanService.approveLoan(loanId), HttpStatus.CREATED);
    }

    @PostMapping("/{loanId}/disburse")
    public ResponseEntity<Loan> disburseHandler(@PathVariable Long loanId){
        return new ResponseEntity<>(loanService.disburseLoan(loanId), HttpStatus.CREATED);
    }

    @PostMapping("/{loanId}/repay")
    public ResponseEntity<LoanRepayment> repayHandler(@PathVariable Long loanId, @RequestBody RepaymentRequest request){
        return new ResponseEntity<>(loanService.repay(loanId, request), HttpStatus.CREATED);
    }


}
