package ng.com.createsoftware.fn_agency_banking_service.mapper;

import ng.com.createsoftware.fn_agency_banking_service.dto.response.TillTransactionResponse;
import ng.com.createsoftware.fn_agency_banking_service.model.TillTransaction;

public class TillTransactionMapper {
    public static TillTransactionResponse tillTransactionToTillTransactionResponse(TillTransaction transaction){
        TillTransactionResponse response = new TillTransactionResponse();
        response.setId(transaction.getId());
        response.setReference(transaction.getReference());
        response.setType(transaction.getType());
        response.setAmount(transaction.getAmount());
        response.setAccountNumber(transaction.getAccountNumber());
        response.setPerformedBy(transaction.getPerformedBy());
        response.setTillName(transaction.getTill().getName());
        return response;
    }
}
