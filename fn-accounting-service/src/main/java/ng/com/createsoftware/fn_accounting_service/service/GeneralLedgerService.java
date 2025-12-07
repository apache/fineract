package ng.com.createsoftware.fn_accounting_service.service;

import ng.com.createsoftware.fn_accounting_service.dto.request.GeneralLedgerRequest;
import ng.com.createsoftware.fn_accounting_service.dto.response.GeneralLedgerResponse;

import java.math.BigDecimal;
import java.util.List;

public interface GeneralLedgerService {

    List<GeneralLedgerResponse> getGeneralLedgers();
    GeneralLedgerResponse getGeneralLedger(Long generalLedgerId);
    GeneralLedgerResponse addGeneralLedger(GeneralLedgerRequest request);
    GeneralLedgerResponse generalLedgerFund(Long ledgerId, BigDecimal amount, String narration);
}
