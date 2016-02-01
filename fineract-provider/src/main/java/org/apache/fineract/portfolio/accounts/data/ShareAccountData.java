/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.accounts.data;

import java.util.Collection;
import java.util.Date;

import org.mifosplatform.accounting.glaccount.data.GLAccountData;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.monetary.data.CurrencyData;

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
