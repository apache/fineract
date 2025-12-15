package ng.com.createsoftware.fn_loaning_service.service;

import ng.com.createsoftware.fn_loaning_service.dto.request.AddLoanProductRequest;
import ng.com.createsoftware.fn_loaning_service.dto.request.AddLoanRequest;
import ng.com.createsoftware.fn_loaning_service.dto.request.RepaymentRequest;
import ng.com.createsoftware.fn_loaning_service.model.Loan;
import ng.com.createsoftware.fn_loaning_service.model.LoanProduct;
import ng.com.createsoftware.fn_loaning_service.model.LoanRepayment;

public interface LoanService {
    LoanProduct addProduct(AddLoanProductRequest request);
    Loan addLoan(AddLoanRequest request);
    Loan approveLoan(Long loanId);
    Loan disburseLoan(Long loanId);
    LoanRepayment repay(Long loanId, RepaymentRequest request);
}
