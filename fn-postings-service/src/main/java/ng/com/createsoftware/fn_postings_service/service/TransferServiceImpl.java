package ng.com.createsoftware.fn_postings_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ng.com.createsoftware.fn_postings_service.repository.PostingTransactionRepository;
import ng.com.createsoftware.fn_postings_service.client.SavingsClient;
import ng.com.createsoftware.fn_postings_service.dto.request.AccountTransferRequest;
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
public class TransferServiceImpl implements TransferService{
    private final PostingTransactionRepository postingTransactionRepository;
    private final SavingsClient savingsClient;
    private final PostingEventPublisher postingEventPublisher;

    @Override
    public PostingTransaction transfer(AccountTransferRequest request) {

        //debit source account
        savingsClient.debitAccount(request.getFromAccountNumber(), request.getAmount());

        //credit destination account
        savingsClient.creditAccount(request.getToAccountNumber(), request.getAmount());

        PostingTransaction transaction = postingTransactionRepository.save(
                PostingTransaction.builder()
                        .customerId(request.getFromCustomerId())
                        .accountNumber(request.getFromAccountNumber())
                        .amount(request.getAmount())
                        .type(PostingType.TRANSFER)
                        .status(Status.APPROVED)
                        .createdAt(LocalDateTime.now())
                        .reference(UUID.randomUUID().toString())
                        .build()
        );

        postingEventPublisher.publishPostingEvent(transaction);
        return  transaction;
    }
}
