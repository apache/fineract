package ng.com.createsoftware.fn_postings_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ng.com.createsoftware.fn_postings_service.repository.PostingTransactionRepository;
import ng.com.createsoftware.fn_postings_service.client.SavingsClient;
import ng.com.createsoftware.fn_postings_service.dto.request.DepositRequest;
import ng.com.createsoftware.fn_postings_service.dto.request.WithdrawalRequest;
import ng.com.createsoftware.fn_postings_service.dto.response.SavingsAccountResponse;
import ng.com.createsoftware.fn_postings_service.event.PostingEventPublisher;
import ng.com.createsoftware.fn_postings_service.model.PostingTransaction;
import ng.com.createsoftware.fn_postings_service.model.PostingType;
import ng.com.createsoftware.fn_postings_service.model.Status;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostingServiceImpl implements PostingService{

    private final PostingTransactionRepository postingTransactionRepository;
    private final SavingsClient savingsClient;
    private final PostingEventPublisher postingEventPublisher;

    @Override
    public PostingTransaction deposit(DepositRequest request) {
       //save pending posting transaction
        PostingTransaction transaction = postingTransactionRepository.save(
                PostingTransaction.builder()
                        .customerId(request.getCustomerId())
                        .accountNumber(request.getAccountNumber())
                        .amount(request.getAmount())
                        .type(PostingType.DEPOSIT)
                        .status(Status.PENDING)
                        .createdAt(LocalDateTime.now())
                        .reference(UUID.randomUUID().toString())
                        .build()
        );

        //apply to saving service
        savingsClient.creditAccount(request.getAccountNumber(), request.getAmount());

        //mark as approved
        transaction.setStatus(Status.APPROVED);
        transaction.setApprovedAt(LocalDateTime.now());
        postingTransactionRepository.save(transaction);

        //publish posting event
        postingEventPublisher.publishPostingEvent(transaction);

        return transaction;
    }

    @Override
    //withdrawal posting -> check balance
    public PostingTransaction withdraw(WithdrawalRequest request){
        SavingsAccountResponse acct = savingsClient.getAccount(request.getAccountNumber());
        if((acct.getBalance()).compareTo(request.getAmount()) < 0)
            throw new RuntimeException("Insufficient funds");

        PostingTransaction transaction = postingTransactionRepository.save(
                PostingTransaction.builder()
                        .customerId(request.getCustomerId())
                        .accountNumber(request.getAccountNumber())
                        .amount(request.getAmount())
                        .type(PostingType.WITHDRAWAL)
                        .status(Status.PENDING)
                        .createdAt(LocalDateTime.now())
                        .reference(UUID.randomUUID().toString())
                        .build()
        );

        savingsClient.debitAccount(request.getAccountNumber(), request.getAmount());

        transaction.setStatus(Status.APPROVED);
        transaction.setApprovedAt(LocalDateTime.now());
        postingTransactionRepository.save(transaction);

        postingEventPublisher.publishPostingEvent(transaction);

        return transaction;
    }

}
