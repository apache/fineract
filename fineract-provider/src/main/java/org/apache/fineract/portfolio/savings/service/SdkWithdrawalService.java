package org.apache.fineract.portfolio.savings.service;

import org.pheesdk.transfer.Utils.SdkApiException;
import org.pheesdk.transfer.Utils.SdkValidationException;

public interface SdkWithdrawalService {
    String processWithdrawal(String payerType, String payerId, String payeeType, String payeeId, String amount, String currencyCode) throws SdkApiException, SdkValidationException;
}