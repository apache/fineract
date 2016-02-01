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
package org.apache.fineract.portfolio.accounts.data;

import java.util.Collection;
import java.util.Date;

import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;

public class ShareAccountData implements AccountData{

    private Long id;
    
    private String accountNo;
    
    private String externalId;

    private Long productId;
    
    private String productName;
    
    private String loanProductDescription;
    
    private Long fieldOfficerId;
    
    private String fieldOfficerName;

    private Long clientId;
    
    private String clientName;

    private Long clientOfficeId;

    private CurrencyData currency;

    private Date submittedDate;

    private Date approvedDate;

    private Collection<PurchasedSharesData> purchasedShares;

    private GLAccountData suspenseAccount;

    private GLAccountData equityAccount;

    private Long savingsAccountId;

    private EnumOptionData lockPeriod;

    private EnumOptionData minimumActivePeriodForDividends;
    
    private Boolean allowDividendCalculationForInactiveClients;
    
    private Collection<ShareChargeData> charges ;
    
    private String status ;
    
    public ShareAccountData(final Long id, final String accountNo, final Long clientId, final String clientName, final Long productId, final String productName,
            final Long fieldOfficerId, final String externalId, final Date submittedDate, final Collection<PurchasedSharesData> purchasedShares, final GLAccountData suspenseAccount,
            final GLAccountData equityAccount, final EnumOptionData lockPeriod, final EnumOptionData minimumActivePeriodForDividends,
            final Boolean allowDividendCalculationForInactiveClients, final Collection<ShareChargeData> charges, final String status) {
        this.id = id ;
        this.accountNo = accountNo ;
        this.clientId = clientId ;
        this.clientName = clientName ;
        this.productId = productId ;
        this.productName = productName ;
        this.fieldOfficerId = fieldOfficerId ;
        this.externalId = externalId ;
        this.submittedDate = submittedDate ;
        this.purchasedShares = purchasedShares ;
        this.suspenseAccount = suspenseAccount ;
        this.equityAccount = equityAccount ;
        this.lockPeriod = lockPeriod ;
        this.minimumActivePeriodForDividends = minimumActivePeriodForDividends ;
        this.allowDividendCalculationForInactiveClients = allowDividendCalculationForInactiveClients ;
        this.charges = charges ;
        this.status = status ;
        
    }
}
