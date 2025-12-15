package ng.com.createsoftware.fn_loaning_service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ng.com.createsoftware.fn_loaning_service.dto.request.AddLoanProductRequest;
import ng.com.createsoftware.fn_loaning_service.dto.request.AddLoanRequest;
import ng.com.createsoftware.fn_loaning_service.dto.request.RepaymentRequest;
import ng.com.createsoftware.fn_loaning_service.model.Loan;
import ng.com.createsoftware.fn_loaning_service.model.LoanProduct;
import ng.com.createsoftware.fn_loaning_service.model.LoanRepayment;
import ng.com.createsoftware.fn_loaning_service.model.Status;
import ng.com.createsoftware.fn_loaning_service.repository.LoanProductRepository;
import ng.com.createsoftware.fn_loaning_service.repository.LoanRepaymentRepository;
import ng.com.createsoftware.fn_loaning_service.repository.LoanRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanServiceImpl implements LoanService{
    private final LoanProductRepository loanProductRepository;
    private final LoanRepository loanRepository;
    private final LoanRepaymentRepository loanRepaymentRepository;
    private final LoanEventPublisher loanEventPublisher;

    @Override
    public LoanProduct addProduct(AddLoanProductRequest request) {
        var p = new LoanProduct(request.getProductCode(), request.getName(), request.getInterestRate(), request.getMaxTenure(), request.getMaxAmount());
        return loanProductRepository.save(p);
    }

    @Transactional
    @Override
    public Loan addLoan(AddLoanRequest request) {
       var product = loanProductRepository.findByProductCode(request.getProductCode());

       Loan loan = new Loan(request.getCustomerId(), request.getAmount(), request.getTenure(), product.getInterestRate()) ;
       loan = loanRepository.save(loan);

        loanEventPublisher.loanCreated(loan.getId(), request.getCustomerId());
        return loan;
    }

    @Transactional
    @Override
    public Loan approveLoan(Long loanId) {
        var loan = loanRepository.findById(loanId).orElseThrow();
        loan.setStatus(Status.APPROVED);
        loan.setApprovalDate(LocalDate.now());
        loanRepository.save(loan);

        loanEventPublisher.loanApproved(loanId);

        return loan;
    }

    @Transactional
    @Override
    public Loan disburseLoan(Long loanId) {
        var loan = loanRepository.findById(loanId).orElseThrow();
        loan.setStatus(Status.DISBURSED);
        loan.setApprovalDate(LocalDate.now());
        loanRepository.save(loan);

        loanEventPublisher.loanApproved(loanId);

        return loan;
    }

    @Transactional
    @Override
    public LoanRepayment repay(Long loanId, RepaymentRequest request) {
       var r = new LoanRepayment(loanId, request.getAmount());
       r = loanRepaymentRepository.save(r);

       loanEventPublisher.repaymentMade(loanId, request.getAmount());
       return r;
    }
}
