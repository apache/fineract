package ng.com.createsoftware.fn_postings_service.service;

import ng.com.createsoftware.fn_postings_service.dto.request.AccountTransferRequest;
import ng.com.createsoftware.fn_postings_service.model.PostingTransaction;

public interface TransferService {
    PostingTransaction transfer(AccountTransferRequest request);
}
