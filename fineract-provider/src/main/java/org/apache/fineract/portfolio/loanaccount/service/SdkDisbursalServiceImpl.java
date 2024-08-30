/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.loanaccount.service;

import org.apache.fineract.portfolio.savings.service.SdkWithdrawalServiceImpl;
import org.pheesdk.transfer.Services.TransferService;
import org.pheesdk.transfer.Utils.SdkApiException;
import org.pheesdk.transfer.Utils.SdkValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SdkDisbursalServiceImpl implements SdkDisbursalService{

    private static final Logger logger = LoggerFactory.getLogger(SdkWithdrawalServiceImpl.class);

    private TransferService transferService;

    public SdkDisbursalServiceImpl(){
        TransferService transferService = new TransferService();
        this.transferService = transferService;

    }

    @Override
    public String processDisbursal(String payerType, String payerId, String payeeType, String payeeId, String amount, String currencyCode) throws SdkValidationException, SdkApiException{
        transferService.setBaseUrl("http://localhost:1111");
        transferService.setPlatformTenantId("gorilla");
        String id = null;
        try {
            id = transferService.processPayment(payerType, payerId, payeeType, payeeId, amount, currencyCode);
            logger.info(id);
        } catch (SdkApiException e) {
            logger.error("Error status code: "+e.getStatusCode());
            logger.error("Error status code: "+e.getResponseBody());
            System.out.println(e.getResponseBody());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return id;
    }


}
