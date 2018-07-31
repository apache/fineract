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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public interface ShareAccountApiConstants {

	public static final String amountParamName = "amount";
	public static final String dateFormatParamName = "dateFormat";
	public static final String dueAsOfDateParamName = "dueDate";
	public static final String feeIntervalParamName = "feeInterval";
	public static final String feeOnMonthDayParamName = "feeOnMonthDay";
	public static final String localeParamName = "locale";

	// Command Strings
	String APPROVE_COMMAND = "approve";
	String REJECT_COMMAND = "reject";
	String APPLY_ADDITIONALSHARES_COMMAND = "applyadditionalshares";
	String APPROVE_ADDITIONSHARES_COMMAND = "approveadditionalshares";
	String REJECT_ADDITIONSHARES_COMMAND = "rejectadditionalshares";

	//
	String locale_paramname = "locale" ;
	
	String dateformat_paramname = "dateFormat" ;
	
	String id_paramname = "id";

	String clientid_paramname = "clientId";

	String productid_paramname = "productId";

	String submitteddate_paramname = "submittedDate";

	String approveddate_paramname = "approvedDate";

	String activatedate_paramname = "activatedDate" ;
	
	String fieldofferid_paramname = "fieldOfficerId";

	String externalid_paramname = "externalId";

	String currency_paramname = "currencyCode";

	String digitsafterdecimal_paramname = "digitsAfterDecimal";

	String inmultiplesof_paramname = "inMultiplesOf";

	String requestedshares_paramname = "requestedShares";

	String savingsaccountid_paramname = "savingsAccountId";

	String lockinperiod_paramname = "lockinPeriodFrequency";

	String lockperiodfrequencytype_paramname = "lockinPeriodFrequencyType";

	String minimumactiveperiod_paramname = "minimumActivePeriod";

	String minimumactiveperiodfrequencytype_paramname = "minimumActivePeriodFrequencyType";

	String allowdividendcalculationforinactiveclients_paramname = "allowDividendCalculationForInactiveClients";

	String charges_paramname = "charges";

	String applicationdate_param = "applicationDate";

	String purchaseddate_paramname = "purchasedDate";

	String numberofshares_paramname = "numberOfShares";

	String purchasedprice_paramname = "unitPrice";
	
	String note_paramname = "note" ;
	
	public String requesteddate_paramname = "requestedDate" ;
	
	public String additionalshares_paramname = "additionalshares" ;
	
	public String closeddate_paramname = "closedDate";

	public static final String shareEntityType = "share";
	
	Set<String> supportedParameters = new HashSet<>(Arrays.asList(locale_paramname, dateformat_paramname, id_paramname,clientid_paramname, productid_paramname,
	        submitteddate_paramname,approveddate_paramname, externalid_paramname, currency_paramname, digitsafterdecimal_paramname,
	        inmultiplesof_paramname, requestedshares_paramname,savingsaccountid_paramname,lockinperiod_paramname,
	        lockperiodfrequencytype_paramname,minimumactiveperiod_paramname, minimumactiveperiodfrequencytype_paramname,
	        allowdividendcalculationforinactiveclients_paramname, charges_paramname, applicationdate_param,
	        purchaseddate_paramname,numberofshares_paramname,purchasedprice_paramname));

}
