package ng.com.createsoftware.fn_postings_service.service;

import ng.com.createsoftware.fn_postings_service.dto.request.DepositRequest;
import ng.com.createsoftware.fn_postings_service.dto.request.WithdrawalRequest;
import ng.com.createsoftware.fn_postings_service.model.PostingTransaction;

public interface PostingService {
    PostingTransaction deposit(DepositRequest request);
    PostingTransaction withdraw(WithdrawalRequest request);
}
