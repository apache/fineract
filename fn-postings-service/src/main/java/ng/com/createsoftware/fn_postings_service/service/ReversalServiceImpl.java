package ng.com.createsoftware.fn_postings_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ng.com.createsoftware.fn_postings_service.repository.PostingTransactionRepository;
import ng.com.createsoftware.fn_postings_service.client.SavingsClient;
import ng.com.createsoftware.fn_postings_service.dto.request.ReversalRequest;
import ng.com.createsoftware.fn_postings_service.model.PostingTransaction;
import ng.com.createsoftware.fn_postings_service.model.PostingType;
import ng.com.createsoftware.fn_postings_service.model.Status;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class ReversalServiceImpl implements ReversalService{
    private final PostingTransactionRepository postingTransactionRepository;
    private final SavingsClient savingsClient;

    @Override
    public void reverse(ReversalRequest request) {
        PostingTransaction original = postingTransactionRepository.findById(request.getTransactionId())
                .orElseThrow(() -> new RuntimeException("Transaction not found."));

        if(!original.getStatus().equals(Status.APPROVED))
            throw new RuntimeException("Only approved transactions can be reversed.");

        if(original.getType().equals(PostingType.DEPOSIT))
            savingsClient.debitAccount(original.getAccountNumber(), original.getAmount());

        if(original.getType().equals(PostingType.WITHDRAWAL))
            savingsClient.creditAccount(original.getAccountNumber(), original.getAmount());

        original.setStatus(Status.REVERSED);
        postingTransactionRepository.save(original);
    }
}
