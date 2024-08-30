package org.apache.fineract.portfolio.savings.service;

import org.pheesdk.transfer.Services.TransferService;
import org.pheesdk.transfer.Utils.SdkApiException;
import org.pheesdk.transfer.Utils.SdkValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SdkWithdrawalServiceImpl implements SdkWithdrawalService {

    private  TransferService transferService;
    private static final Logger logger =  LoggerFactory.getLogger(SdkWithdrawalServiceImpl.class);

    public SdkWithdrawalServiceImpl() {
        TransferService transferService = new TransferService();
        this.transferService = transferService;
    }

    @Override
    public String processWithdrawal(String payerType, String payerId, String payeeType, String payeeId, String amount, String currencyCode) throws SdkValidationException, SdkApiException {
        String id = null;
        transferService.setPlatformTenantId("gorilla");
        transferService.setBaseUrl("http://localhost:1111");
        try {
            id = transferService.processPayment(payerType, payerId, payeeType, payeeId, amount, currencyCode);
        } catch (SdkApiException e) {
            logger.info(String.valueOf(e.getStatusCode()));
            logger.info(e.getResponseBody());
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        return id;
    }

}
