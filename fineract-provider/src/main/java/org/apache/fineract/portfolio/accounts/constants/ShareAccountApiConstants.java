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
package org.apache.fineract.portfolio.accounts.constants;



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
