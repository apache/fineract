package ng.com.createsoftware.fn_agency_banking_service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ng.com.createsoftware.fn_agency_banking_service.client.PostingClient;
import ng.com.createsoftware.fn_agency_banking_service.dto.request.TillCustomerRequest;
import ng.com.createsoftware.fn_agency_banking_service.dto.request.TillVaultRequest;
import ng.com.createsoftware.fn_agency_banking_service.dto.response.TillTransactionResponse;
import ng.com.createsoftware.fn_agency_banking_service.events.AgencyEventPublisher;
import ng.com.createsoftware.fn_agency_banking_service.mapper.TillTransactionMapper;
import ng.com.createsoftware.fn_agency_banking_service.model.Till;
import ng.com.createsoftware.fn_agency_banking_service.model.TillTransaction;
import ng.com.createsoftware.fn_agency_banking_service.model.TransactionType;
import ng.com.createsoftware.fn_agency_banking_service.model.Vault;
import ng.com.createsoftware.fn_agency_banking_service.repository.TillRepository;
import ng.com.createsoftware.fn_agency_banking_service.repository.TillTransactionRepository;
import ng.com.createsoftware.fn_agency_banking_service.repository.VaultRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TillVaultServiceImpl implements TillVaultService{
    private final VaultRepository vaultRepository;
    private final TillRepository tillRepository;
    private final TillTransactionRepository tillTransactionRepository;
    private final PostingClient postingClient;
    private final AgencyEventPublisher agencyEventPublisher;

    @Transactional
    @Override
    public TillTransactionResponse vaultToTill(TillVaultRequest request) {
        Vault vault  = vaultRepository.findById(request.getVaultId()).orElseThrow();
        Till till = tillRepository.findById(request.getTillId()).orElseThrow();

        if((vault.getBalance()).compareTo(request.getAmount()) > 0) throw new RuntimeException("Vault Insufficient funds.");

        vault.setBalance((vault.getBalance()).subtract(request.getAmount()));
        till.setBalance((till.getBalance()).add(request.getAmount()));
        vaultRepository.save(vault);
        tillRepository.save(till);

        String reference = UUID.randomUUID().toString();
        TillTransaction tx = new TillTransaction(
                reference, TransactionType.VAULT_TO_TILL,
                request.getAmount(), till,
                null, request.getPerformedBy()
        );
        tx = tillTransactionRepository.save(tx);

        //optionally create posting via posting-service (GL -> GL): call postingClient.transfer
        //or deposit as per chart of accounts

        //eg: notify posting-service (We send a transfer request to posting-service)
        try{
//            Map<String, Object> body = Map.of(
//                    "from", "VAULT_GL",
//                    "to", "TILL_GL_"+ till.getId(),
//                    "amount", request.getAmount(),
//                    "narration" + "Vault to till " + reference
//            );
            Map<String, Object> body = Map.of(
                    "from", "VAULT_GL" ,
                    "to", "TILL_GL_"+ till.getId(),
                    "amount", request.getAmount(),
                    "narration", "Vault to till " + reference
            );
            postingClient.transfer(body);
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
        agencyEventPublisher.publishTillTransfer(till.getId(), TransactionType.VAULT_TO_TILL.name(), request.getAmount(), request.getPerformedBy());
        return TillTransactionMapper.tillTransactionToTillTransactionResponse(tx);
    }

    @Transactional
    @Override
    public TillTransactionResponse tillToVault(TillVaultRequest request) {
        Vault vault  = vaultRepository.findById(request.getVaultId()).orElseThrow();
        Till till = tillRepository.findById(request.getTillId()).orElseThrow();

        if((till.getBalance()).compareTo(request.getAmount()) > 0) throw new RuntimeException("Vault Insufficient funds.");

        till.setBalance((till.getBalance()).subtract(request.getAmount()));
        vault.setBalance((vault.getBalance()).add(request.getAmount()));
        vaultRepository.save(vault);
        tillRepository.save(till);

        String reference = UUID.randomUUID().toString();
        TillTransaction tx = new TillTransaction(
                reference, TransactionType.TILL_TO_VAULT,
                request.getAmount(), till,
                null, request.getPerformedBy()
        );
        tx = tillTransactionRepository.save(tx);

        //optionally create posting via posting-service (GL -> GL): call postingClient.transfer
        //or deposit as per chart of accounts

        //eg: notify posting-service (We send a transfer request to posting-service)
        try{
            Map<String, Object> body = Map.of(
                    "from","TILL_GL_"+ till.getId(),
                    "to", "VAULT_GL" ,
                    "amount", request.getAmount(),
                    "narration", "Till to vault " + reference
            );
            postingClient.transfer(body);
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
        agencyEventPublisher.publishTillTransfer(
                till.getId(), TransactionType.TILL_TO_VAULT.name(),
                request.getAmount(), request.getPerformedBy());
        return TillTransactionMapper.tillTransactionToTillTransactionResponse(tx);
    }

    @Override
    public TillTransactionResponse tillToCustomer(TillCustomerRequest request) {
         Till till = tillRepository.findById(request.getTillId()).orElseThrow();
         if((till.getBalance()).compareTo(request.getAmount()) > 0) throw new RuntimeException("Till insufficient funds");

         till.setBalance((till.getBalance()).subtract(request.getAmount()));
         tillRepository.save(till);

         String reference = UUID.randomUUID().toString();
        TillTransaction tx = new TillTransaction(
                reference, TransactionType.TILL_TO_CUSTOMER,
                request.getAmount(), till,
                request.getAccountNumber(), request.getPerformedBy()
        );
        tx = tillTransactionRepository.save(tx);
        try{
            Map<String, Object> body = Map.of(
                    "fromGlCode","TILL_CASH_GL_"+ till.getId(),
                    "customerAccountNumber", request.getAccountNumber(),
                    "amount", request.getAmount(),
                    "narration", "Till deposit" + reference,
                    "requestedBy", request.getPerformedBy()
            );
            postingClient.transfer(body);
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
        agencyEventPublisher.publishTillTransfer(
                till.getId(), TransactionType.TILL_TO_CUSTOMER.name(),
                request.getAmount(), request.getPerformedBy());
        return TillTransactionMapper.tillTransactionToTillTransactionResponse(tx);
    }

    @Override
    public TillTransactionResponse customerToTill(TillCustomerRequest request) {
        Till till = tillRepository.findById(request.getTillId()).orElseThrow();

        try{
            Map<String, Object> body = Map.of(
                    "customerAccountNumber", request.getAccountNumber(),
                    "toGlCode", "TILL_CASH_GL_" + till.getId(),
                    "amount", request.getAmount(),
                    "narration", "Customer withdraw to till",
                    "requestedBy", request.getPerformedBy()
            );
            postingClient.transfer(body);
        }catch (Exception ex){
            System.out.println(ex.getMessage());
            throw new RuntimeException("Posting-service error: " + ex.getMessage());
        }

        till.setBalance((till.getBalance()).add(request.getAmount()));
        tillRepository.save(till);

        String reference = UUID.randomUUID().toString();
        TillTransaction tx = new TillTransaction(
                reference, TransactionType.CUSTOMER_TO_TILL,
                request.getAmount(), till,
                request.getAccountNumber(), request.getPerformedBy()
        );
        tx = tillTransactionRepository.save(tx);

        agencyEventPublisher.publishTillTransfer(
                till.getId(), TransactionType.CUSTOMER_TO_TILL.name(),
                request.getAmount(), request.getPerformedBy());
        return TillTransactionMapper.tillTransactionToTillTransactionResponse(tx);
    }
}
