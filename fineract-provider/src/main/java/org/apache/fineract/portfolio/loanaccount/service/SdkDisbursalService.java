package org.apache.fineract.portfolio.loanaccount.service;

import org.pheesdk.transfer.Utils.SdkApiException;
import org.pheesdk.transfer.Utils.SdkValidationException;

public interface SdkDisbursalService {
    String processDisbursal(String payerType, String payerId, String payeeType, String payeeId, String amount, String currencyCode) throws SdkApiException, SdkValidationException;
}
