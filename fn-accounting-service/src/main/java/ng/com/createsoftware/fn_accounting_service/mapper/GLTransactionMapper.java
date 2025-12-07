package ng.com.createsoftware.fn_accounting_service.mapper;

import ng.com.createsoftware.fn_accounting_service.dto.response.GLTransactionResponse;
import ng.com.createsoftware.fn_accounting_service.dto.response.GeneralLedgerResponse;
import ng.com.createsoftware.fn_accounting_service.model.GLTransaction;
import ng.com.createsoftware.fn_accounting_service.model.GeneralLedger;
import ng.com.createsoftware.fn_accounting_service.model.TransactionType;

public class GLTransactionMapper {
    public static GLTransactionResponse gLTransactionToGLTransactionResponse(GLTransaction glTransaction){
         GLTransactionResponse response = new GLTransactionResponse();
         response.setId(glTransaction.getId());
         response.setLedgerId(glTransaction.getLedger().getId());
         response.setAmount(glTransaction.getAmount());
         response.setType(glTransaction.getType().name());
         response.setReference(glTransaction.getReference());
         response.setNarration(glTransaction.getNarration());
         response.setTimestamp(glTransaction.getTimestamp());
         return response;
    }
    public static GLTransaction gLTransactionResponseToGLTransaction(GLTransactionResponse response){
         GLTransaction transaction = new GLTransaction();
         transaction.setId(response.getId());
//         transaction.setLedger(response.getLedgerId());
         transaction.setAmount(response.getAmount());
         transaction.setType(TransactionType.valueOf(response.getType()));
         transaction.setReference(response.getReference());
         transaction.setNarration(response.getNarration());
         transaction.setTimestamp(response.getTimestamp());
         return transaction;
    }
}
