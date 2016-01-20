/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.accounts.constants;



public interface ShareAccountApiConstants {

    //Command Strings
    String APPROVE_COMMAND = "approve" ;
    String REJECT_COMMAND = "reject" ;
    String APPLY_ADDITIONALSHARES_COMMAND = "applyadditionalshares" ;
    String APPROVE_ADDITIONSHARES_COMMAND = "approveadditionalshares" ;
    String REJECT_ADDITIONSHARES_COMMAND = "rejectadditionalshares" ;
    
    //
    String id_paramname = "id" ;
    
    String clientid_paramname = "clientId" ;
    
    String productid_paramname = "productId" ;
    
    String submitteddate_paramname = "submittedDate" ;
    
    String approveddate_paramname = "approvedDate" ;
    
    String fieldofferid_paramname = "fieldOfficerId" ;
    
    String externalid_paramname = "externalId" ;
    
    String currency_paramname = "currencyCode" ;
    
    String digitsafterdecimal_paramname = "digitsAfterDecimal" ;
    
    String inmultiplesof_paramname = "inMultiplesOf" ;
    
    String purchasedshares_paramname = "purchasedShares" ;
    
    String additionalshares_paramname = "additionalShares" ;
    
    String suspenseaccount_paramname = "suspenseAccount" ;
    
    String equityaccount_paramname = "equityAccount" ;
    
    String savingsaccountid_paramname = "savingsAccountId" ;
    
    String lockperiod_paramname = "lockPeriod" ;
    
    String minimumactiveperiodfordividends_paramname = "minimumActivePeriodForDividends" ;
    
    String allowdividendcalculationforinactiveclients_paramname = "allowDividendCalculationForInactiveClients" ;
    
    String charges_paramname = "charges" ; 
    
    String purchaseddate_paramname = "purchasedDate" ;
    
    String numberofshares_paramname = "numberOfShares" ;
    
    String purchasedprice_paramname = "purchasedPrice" ;
    
}
