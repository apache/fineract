package ng.com.createsoftware.fn_accounting_service.mapper;

import ng.com.createsoftware.fn_accounting_service.dto.response.AccountTypeResponse;
import ng.com.createsoftware.fn_accounting_service.dto.response.GeneralLedgerResponse;
import ng.com.createsoftware.fn_accounting_service.model.AccountType;
import ng.com.createsoftware.fn_accounting_service.model.GeneralLedger;

public class GeneralLedgerMapper {
    public static GeneralLedgerResponse generalLedgerToGeneralLedgerResponse(GeneralLedger generalLedger){
        Long catId = generalLedger.getCategory() != null ? generalLedger.getId() : null;
        GeneralLedgerResponse ledger = new GeneralLedgerResponse();
        ledger.setId((generalLedger.getId()));
        ledger.setCode(generalLedger.getCode());
        ledger.setName(generalLedger.getName());
        ledger.setCategoryId(catId);
        ledger.setBalance(generalLedger.getBalance());
        return ledger;
    }
}
