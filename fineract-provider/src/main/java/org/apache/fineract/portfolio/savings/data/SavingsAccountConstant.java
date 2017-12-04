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
package org.apache.fineract.portfolio.savings.data;

import org.apache.fineract.portfolio.savings.SavingsApiConstants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SavingsAccountConstant extends SavingsApiConstants {

	/**
	 * These parameters will match the class level parameters of
	 * {@link SavingsProductData}. Where possible, we try to get response
	 * parameters to match those of request parameters.
	 */
	protected static final Set<String> SAVINGS_ACCOUNT_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(
			localeParamName, dateFormatParamName, monthDayFormatParamName, staffIdParamName, accountNoParamName,
			externalIdParamName, clientIdParamName, groupIdParamName, productIdParamName, fieldOfficerIdParamName,
			submittedOnDateParamName, nominalAnnualInterestRateParamName, interestCompoundingPeriodTypeParamName,
			interestPostingPeriodTypeParamName, interestCalculationTypeParamName,
			interestCalculationDaysInYearTypeParamName, minRequiredOpeningBalanceParamName,
			lockinPeriodFrequencyParamName, lockinPeriodFrequencyTypeParamName,
			// withdrawalFeeAmountParamName, withdrawalFeeTypeParamName,
			withdrawalFeeForTransfersParamName, feeAmountParamName, feeOnMonthDayParamName, chargesParamName,
			allowOverdraftParamName, overdraftLimitParamName, minRequiredBalanceParamName,
			enforceMinRequiredBalanceParamName, nominalAnnualInterestRateOverdraftParamName,
			minOverdraftForInterestCalculationParamName, withHoldTaxParamName, datatables));

	/**
	 * These parameters will match the class level parameters of
	 * {@link SavingsAccountData}. Where possible, we try to get response
	 * parameters to match those of request parameters.
	 */

	protected static final Set<String> SAVINGS_ACCOUNT_TRANSACTION_REQUEST_DATA_PARAMETERS = new HashSet<>(
			Arrays.asList(localeParamName, dateFormatParamName, transactionDateParamName, transactionAmountParamName,
					paymentTypeIdParamName, transactionAccountNumberParamName, checkNumberParamName,
					routingCodeParamName, receiptNumberParamName, bankNumberParamName, noteParamName));

	protected static final Set<String> SAVINGS_ACCOUNT_TRANSACTION_RESPONSE_DATA_PARAMETERS = new HashSet<>(
			Arrays.asList(idParamName, accountNoParamName));

	protected static final Set<String> SAVINGS_ACCOUNT_ACTIVATION_REQUEST_DATA_PARAMETERS = new HashSet<>(
			Arrays.asList(localeParamName, dateFormatParamName, activatedOnDateParamName));

	protected static final Set<String> SAVINGS_ACCOUNT_CLOSE_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(
			localeParamName, dateFormatParamName, closedOnDateParamName, noteParamName, paymentTypeIdParamName,
			withdrawBalanceParamName, transactionAccountNumberParamName, checkNumberParamName, routingCodeParamName,
			receiptNumberParamName, bankNumberParamName, postInterestValidationOnClosure));

	protected static final Set<String> SAVINGS_ACCOUNT_CHARGES_ADD_REQUEST_DATA_PARAMETERS = new HashSet<>(
			Arrays.asList(chargeIdParamName, amountParamName, dueAsOfDateParamName, dateFormatParamName,
					localeParamName, feeOnMonthDayParamName, monthDayFormatParamName, feeIntervalParamName));

	protected static final Set<String> SAVINGS_ACCOUNT_CHARGES_PAY_CHARGE_REQUEST_DATA_PARAMETERS = new HashSet<>(
			Arrays.asList(amountParamName, dueAsOfDateParamName, dateFormatParamName, localeParamName));

}
