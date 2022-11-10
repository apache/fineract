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
package org.apache.fineract.integrationtests.common;

import com.google.gson.Gson;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ExternalEventConfigurationHelper {

    private static final String EXTERNAL_EVENT_CONFIGURATION = "externalEventConfiguration";
    private static final String EXTERNAL_EVENT_CONFIGURATION_RESPONSE = "externalEventConfigurations";

    protected ExternalEventConfigurationHelper() {}

    private static final String EXTERNAL_EVENT_CONFIGURATION_URL = "/fineract-provider/api/v1/externalevents/configuration?"
            + Utils.TENANT_IDENTIFIER;

    public static ArrayList<Map<String, Object>> getAllExternalEventConfigurations(RequestSpecification requestSpec,
            ResponseSpecification responseSpec) {
        Map<String, ArrayList<Map<String, Object>>> response = Utils.performServerGet(requestSpec, responseSpec,
                EXTERNAL_EVENT_CONFIGURATION_URL, "");
        return response.get(EXTERNAL_EVENT_CONFIGURATION);
    }

    public static ArrayList<Map<String, Object>> getDefaultExternalEventConfigurations() {
        ArrayList<Map<String, Object>> defaults = new ArrayList<>();

        Map<String, Object> centersCreateBusinessEvent = new HashMap<>();
        centersCreateBusinessEvent.put("type", "CentersCreateBusinessEvent");
        centersCreateBusinessEvent.put("enabled", false);
        defaults.add(centersCreateBusinessEvent);

        Map<String, Object> clientActivateBusinessEvent = new HashMap<>();
        clientActivateBusinessEvent.put("type", "ClientActivateBusinessEvent");
        clientActivateBusinessEvent.put("enabled", false);
        defaults.add(clientActivateBusinessEvent);

        Map<String, Object> clientCreateBusinessEvent = new HashMap<>();
        clientCreateBusinessEvent.put("type", "ClientCreateBusinessEvent");
        clientCreateBusinessEvent.put("enabled", false);
        defaults.add(clientCreateBusinessEvent);

        Map<String, Object> clientRejectBusinessEvent = new HashMap<>();
        clientRejectBusinessEvent.put("type", "ClientRejectBusinessEvent");
        clientRejectBusinessEvent.put("enabled", false);
        defaults.add(clientRejectBusinessEvent);

        Map<String, Object> fixedDepositAccountCreateBusinessEvent = new HashMap<>();
        fixedDepositAccountCreateBusinessEvent.put("type", "FixedDepositAccountCreateBusinessEvent");
        fixedDepositAccountCreateBusinessEvent.put("enabled", false);
        defaults.add(fixedDepositAccountCreateBusinessEvent);

        Map<String, Object> groupsCreateBusinessEvent = new HashMap<>();
        groupsCreateBusinessEvent.put("type", "GroupsCreateBusinessEvent");
        groupsCreateBusinessEvent.put("enabled", false);
        defaults.add(groupsCreateBusinessEvent);

        Map<String, Object> loanAcceptTransferBusinessEvent = new HashMap<>();
        loanAcceptTransferBusinessEvent.put("type", "LoanAcceptTransferBusinessEvent");
        loanAcceptTransferBusinessEvent.put("enabled", false);
        defaults.add(loanAcceptTransferBusinessEvent);

        Map<String, Object> loanAddChargeBusinessEvent = new HashMap<>();
        loanAddChargeBusinessEvent.put("type", "LoanAddChargeBusinessEvent");
        loanAddChargeBusinessEvent.put("enabled", false);
        defaults.add(loanAddChargeBusinessEvent);

        Map<String, Object> loanAdjustTransactionBusinessEvent = new HashMap<>();
        loanAdjustTransactionBusinessEvent.put("type", "LoanAdjustTransactionBusinessEvent");
        loanAdjustTransactionBusinessEvent.put("enabled", false);
        defaults.add(loanAdjustTransactionBusinessEvent);

        Map<String, Object> loanApplyOverdueChargeBusinessEvent = new HashMap<>();
        loanApplyOverdueChargeBusinessEvent.put("type", "LoanApplyOverdueChargeBusinessEvent");
        loanApplyOverdueChargeBusinessEvent.put("enabled", false);
        defaults.add(loanApplyOverdueChargeBusinessEvent);

        Map<String, Object> loanApprovedBusinessEvent = new HashMap<>();
        loanApprovedBusinessEvent.put("type", "LoanApprovedBusinessEvent");
        loanApprovedBusinessEvent.put("enabled", false);
        defaults.add(loanApprovedBusinessEvent);

        Map<String, Object> loanBalanceChangedBusinessEvent = new HashMap<>();
        loanBalanceChangedBusinessEvent.put("type", "LoanBalanceChangedBusinessEvent");
        loanBalanceChangedBusinessEvent.put("enabled", false);
        defaults.add(loanBalanceChangedBusinessEvent);

        Map<String, Object> loanChargebackTransactionBusinessEvent = new HashMap<>();
        loanChargebackTransactionBusinessEvent.put("type", "LoanChargebackTransactionBusinessEvent");
        loanChargebackTransactionBusinessEvent.put("enabled", false);
        defaults.add(loanChargebackTransactionBusinessEvent);

        Map<String, Object> loanChargePaymentPostBusinessEvent = new HashMap<>();
        loanChargePaymentPostBusinessEvent.put("type", "LoanChargePaymentPostBusinessEvent");
        loanChargePaymentPostBusinessEvent.put("enabled", false);
        defaults.add(loanChargePaymentPostBusinessEvent);

        Map<String, Object> loanChargePaymentPreBusinessEvent = new HashMap<>();
        loanChargePaymentPreBusinessEvent.put("type", "LoanChargePaymentPreBusinessEvent");
        loanChargePaymentPreBusinessEvent.put("enabled", false);
        defaults.add(loanChargePaymentPreBusinessEvent);

        Map<String, Object> loanChargeRefundBusinessEvent = new HashMap<>();
        loanChargeRefundBusinessEvent.put("type", "LoanChargeRefundBusinessEvent");
        loanChargeRefundBusinessEvent.put("enabled", false);
        defaults.add(loanChargeRefundBusinessEvent);

        Map<String, Object> loanCloseAsRescheduleBusinessEvent = new HashMap<>();
        loanCloseAsRescheduleBusinessEvent.put("type", "LoanCloseAsRescheduleBusinessEvent");
        loanCloseAsRescheduleBusinessEvent.put("enabled", false);
        defaults.add(loanCloseAsRescheduleBusinessEvent);

        Map<String, Object> loanCloseBusinessEvent = new HashMap<>();
        loanCloseBusinessEvent.put("type", "LoanCloseBusinessEvent");
        loanCloseBusinessEvent.put("enabled", false);
        defaults.add(loanCloseBusinessEvent);

        Map<String, Object> loanCreatedBusinessEvent = new HashMap<>();
        loanCreatedBusinessEvent.put("type", "LoanCreatedBusinessEvent");
        loanCreatedBusinessEvent.put("enabled", false);
        defaults.add(loanCreatedBusinessEvent);

        Map<String, Object> loanCreditBalanceRefundPostBusinessEvent = new HashMap<>();
        loanCreditBalanceRefundPostBusinessEvent.put("type", "LoanCreditBalanceRefundPostBusinessEvent");
        loanCreditBalanceRefundPostBusinessEvent.put("enabled", false);
        defaults.add(loanCreditBalanceRefundPostBusinessEvent);

        Map<String, Object> loanCreditBalanceRefundPreBusinessEvent = new HashMap<>();
        loanCreditBalanceRefundPreBusinessEvent.put("type", "LoanCreditBalanceRefundPreBusinessEvent");
        loanCreditBalanceRefundPreBusinessEvent.put("enabled", false);
        defaults.add(loanCreditBalanceRefundPreBusinessEvent);

        Map<String, Object> loanDeleteChargeBusinessEvent = new HashMap<>();
        loanDeleteChargeBusinessEvent.put("type", "LoanDeleteChargeBusinessEvent");
        loanDeleteChargeBusinessEvent.put("enabled", false);
        defaults.add(loanDeleteChargeBusinessEvent);

        Map<String, Object> loanDisbursalBusinessEvent = new HashMap<>();
        loanDisbursalBusinessEvent.put("type", "LoanDisbursalBusinessEvent");
        loanDisbursalBusinessEvent.put("enabled", false);
        defaults.add(loanDisbursalBusinessEvent);

        Map<String, Object> loanDisbursalTransactionBusinessEvent = new HashMap<>();
        loanDisbursalTransactionBusinessEvent.put("type", "LoanDisbursalTransactionBusinessEvent");
        loanDisbursalTransactionBusinessEvent.put("enabled", false);
        defaults.add(loanDisbursalTransactionBusinessEvent);

        Map<String, Object> loanForeClosurePostBusinessEvent = new HashMap<>();
        loanForeClosurePostBusinessEvent.put("type", "LoanForeClosurePostBusinessEvent");
        loanForeClosurePostBusinessEvent.put("enabled", false);
        defaults.add(loanForeClosurePostBusinessEvent);

        Map<String, Object> loanForeClosurePreBusinessEvent = new HashMap<>();
        loanForeClosurePreBusinessEvent.put("type", "LoanForeClosurePreBusinessEvent");
        loanForeClosurePreBusinessEvent.put("enabled", false);
        defaults.add(loanForeClosurePreBusinessEvent);

        Map<String, Object> loanInitiateTransferBusinessEvent = new HashMap<>();
        loanInitiateTransferBusinessEvent.put("type", "LoanInitiateTransferBusinessEvent");
        loanInitiateTransferBusinessEvent.put("enabled", false);
        defaults.add(loanInitiateTransferBusinessEvent);

        Map<String, Object> loanInterestRecalculationBusinessEvent = new HashMap<>();
        loanInterestRecalculationBusinessEvent.put("type", "LoanInterestRecalculationBusinessEvent");
        loanInterestRecalculationBusinessEvent.put("enabled", false);
        defaults.add(loanInterestRecalculationBusinessEvent);

        Map<String, Object> loanProductCreateBusinessEvent = new HashMap<>();
        loanProductCreateBusinessEvent.put("type", "LoanProductCreateBusinessEvent");
        loanProductCreateBusinessEvent.put("enabled", false);
        defaults.add(loanProductCreateBusinessEvent);

        Map<String, Object> loanReassignOfficerBusinessEvent = new HashMap<>();
        loanReassignOfficerBusinessEvent.put("type", "LoanReassignOfficerBusinessEvent");
        loanReassignOfficerBusinessEvent.put("enabled", false);
        defaults.add(loanReassignOfficerBusinessEvent);

        Map<String, Object> loanRefundPostBusinessEvent = new HashMap<>();
        loanRefundPostBusinessEvent.put("type", "LoanRefundPostBusinessEvent");
        loanRefundPostBusinessEvent.put("enabled", false);
        defaults.add(loanRefundPostBusinessEvent);

        Map<String, Object> loanRefundPreBusinessEvent = new HashMap<>();
        loanRefundPreBusinessEvent.put("type", "LoanRefundPreBusinessEvent");
        loanRefundPreBusinessEvent.put("enabled", false);
        defaults.add(loanRefundPreBusinessEvent);

        Map<String, Object> loanRejectedBusinessEvent = new HashMap<>();
        loanRejectedBusinessEvent.put("type", "LoanRejectedBusinessEvent");
        loanRejectedBusinessEvent.put("enabled", false);
        defaults.add(loanRejectedBusinessEvent);

        Map<String, Object> loanRejectTransferBusinessEvent = new HashMap<>();
        loanRejectTransferBusinessEvent.put("type", "LoanRejectTransferBusinessEvent");
        loanRejectTransferBusinessEvent.put("enabled", false);
        defaults.add(loanRejectTransferBusinessEvent);

        Map<String, Object> loanRemoveOfficerBusinessEvent = new HashMap<>();
        loanRemoveOfficerBusinessEvent.put("type", "LoanRemoveOfficerBusinessEvent");
        loanRemoveOfficerBusinessEvent.put("enabled", false);
        defaults.add(loanRemoveOfficerBusinessEvent);

        Map<String, Object> loanRepaymentDueBusinessEvent = new HashMap<>();
        loanRepaymentDueBusinessEvent.put("type", "LoanRepaymentDueBusinessEvent");
        loanRepaymentDueBusinessEvent.put("enabled", false);
        defaults.add(loanRepaymentDueBusinessEvent);

        Map<String, Object> loanRepaymentOverdueBusinessEvent = new HashMap<>();
        loanRepaymentOverdueBusinessEvent.put("type", "LoanRepaymentOverdueBusinessEvent");
        loanRepaymentOverdueBusinessEvent.put("enabled", false);
        defaults.add(loanRepaymentOverdueBusinessEvent);

        Map<String, Object> loanRescheduledDueCalendarChangeBusinessEvent = new HashMap<>();
        loanRescheduledDueCalendarChangeBusinessEvent.put("type", "LoanRescheduledDueCalendarChangeBusinessEvent");
        loanRescheduledDueCalendarChangeBusinessEvent.put("enabled", false);
        defaults.add(loanRescheduledDueCalendarChangeBusinessEvent);

        Map<String, Object> loanRescheduledDueHolidayBusinessEvent = new HashMap<>();
        loanRescheduledDueHolidayBusinessEvent.put("type", "LoanRescheduledDueHolidayBusinessEvent");
        loanRescheduledDueHolidayBusinessEvent.put("enabled", false);
        defaults.add(loanRescheduledDueHolidayBusinessEvent);

        Map<String, Object> loanScheduleVariationsAddedBusinessEvent = new HashMap<>();
        loanScheduleVariationsAddedBusinessEvent.put("type", "LoanScheduleVariationsAddedBusinessEvent");
        loanScheduleVariationsAddedBusinessEvent.put("enabled", false);
        defaults.add(loanScheduleVariationsAddedBusinessEvent);

        Map<String, Object> loanScheduleVariationsDeletedBusinessEvent = new HashMap<>();
        loanScheduleVariationsDeletedBusinessEvent.put("type", "LoanScheduleVariationsDeletedBusinessEvent");
        loanScheduleVariationsDeletedBusinessEvent.put("enabled", false);
        defaults.add(loanScheduleVariationsDeletedBusinessEvent);

        Map<String, Object> loanStatusChangedBusinessEvent = new HashMap<>();
        loanStatusChangedBusinessEvent.put("type", "LoanStatusChangedBusinessEvent");
        loanStatusChangedBusinessEvent.put("enabled", false);
        defaults.add(loanStatusChangedBusinessEvent);

        Map<String, Object> loanTransactionGoodwillCreditPostBusinessEvent = new HashMap<>();
        loanTransactionGoodwillCreditPostBusinessEvent.put("type", "LoanTransactionGoodwillCreditPostBusinessEvent");
        loanTransactionGoodwillCreditPostBusinessEvent.put("enabled", false);
        defaults.add(loanTransactionGoodwillCreditPostBusinessEvent);

        Map<String, Object> loanTransactionGoodwillCreditPreBusinessEvent = new HashMap<>();
        loanTransactionGoodwillCreditPreBusinessEvent.put("type", "LoanTransactionGoodwillCreditPreBusinessEvent");
        loanTransactionGoodwillCreditPreBusinessEvent.put("enabled", false);
        defaults.add(loanTransactionGoodwillCreditPreBusinessEvent);

        Map<String, Object> loanTransactionMakeRepaymentPostBusinessEvent = new HashMap<>();
        loanTransactionMakeRepaymentPostBusinessEvent.put("type", "LoanTransactionMakeRepaymentPostBusinessEvent");
        loanTransactionMakeRepaymentPostBusinessEvent.put("enabled", false);
        defaults.add(loanTransactionMakeRepaymentPostBusinessEvent);

        Map<String, Object> loanTransactionMakeRepaymentPreBusinessEvent = new HashMap<>();
        loanTransactionMakeRepaymentPreBusinessEvent.put("type", "LoanTransactionMakeRepaymentPreBusinessEvent");
        loanTransactionMakeRepaymentPreBusinessEvent.put("enabled", false);
        defaults.add(loanTransactionMakeRepaymentPreBusinessEvent);

        Map<String, Object> loanTransactionMerchantIssuedRefundPostBusinessEvent = new HashMap<>();
        loanTransactionMerchantIssuedRefundPostBusinessEvent.put("type", "LoanTransactionMerchantIssuedRefundPostBusinessEvent");
        loanTransactionMerchantIssuedRefundPostBusinessEvent.put("enabled", false);
        defaults.add(loanTransactionMerchantIssuedRefundPostBusinessEvent);

        Map<String, Object> loanTransactionMerchantIssuedRefundPreBusinessEvent = new HashMap<>();
        loanTransactionMerchantIssuedRefundPreBusinessEvent.put("type", "LoanTransactionMerchantIssuedRefundPreBusinessEvent");
        loanTransactionMerchantIssuedRefundPreBusinessEvent.put("enabled", false);
        defaults.add(loanTransactionMerchantIssuedRefundPreBusinessEvent);

        Map<String, Object> loanTransactionPayoutRefundPostBusinessEvent = new HashMap<>();
        loanTransactionPayoutRefundPostBusinessEvent.put("type", "LoanTransactionPayoutRefundPostBusinessEvent");
        loanTransactionPayoutRefundPostBusinessEvent.put("enabled", false);
        defaults.add(loanTransactionPayoutRefundPostBusinessEvent);

        Map<String, Object> loanTransactionPayoutRefundPreBusinessEvent = new HashMap<>();
        loanTransactionPayoutRefundPreBusinessEvent.put("type", "LoanTransactionPayoutRefundPreBusinessEvent");
        loanTransactionPayoutRefundPreBusinessEvent.put("enabled", false);
        defaults.add(loanTransactionPayoutRefundPreBusinessEvent);

        Map<String, Object> loanTransactionRecoveryPaymentPostBusinessEvent = new HashMap<>();
        loanTransactionRecoveryPaymentPostBusinessEvent.put("type", "LoanTransactionRecoveryPaymentPostBusinessEvent");
        loanTransactionRecoveryPaymentPostBusinessEvent.put("enabled", false);
        defaults.add(loanTransactionRecoveryPaymentPostBusinessEvent);

        Map<String, Object> loanTransactionRecoveryPaymentPreBusinessEvent = new HashMap<>();
        loanTransactionRecoveryPaymentPreBusinessEvent.put("type", "LoanTransactionRecoveryPaymentPreBusinessEvent");
        loanTransactionRecoveryPaymentPreBusinessEvent.put("enabled", false);
        defaults.add(loanTransactionRecoveryPaymentPreBusinessEvent);

        Map<String, Object> loanUndoApprovalBusinessEvent = new HashMap<>();
        loanUndoApprovalBusinessEvent.put("type", "LoanUndoApprovalBusinessEvent");
        loanUndoApprovalBusinessEvent.put("enabled", false);
        defaults.add(loanUndoApprovalBusinessEvent);

        Map<String, Object> loanUndoDisbursalBusinessEvent = new HashMap<>();
        loanUndoDisbursalBusinessEvent.put("type", "LoanUndoDisbursalBusinessEvent");
        loanUndoDisbursalBusinessEvent.put("enabled", false);
        defaults.add(loanUndoDisbursalBusinessEvent);

        Map<String, Object> loanUndoLastDisbursalBusinessEvent = new HashMap<>();
        loanUndoLastDisbursalBusinessEvent.put("type", "LoanUndoLastDisbursalBusinessEvent");
        loanUndoLastDisbursalBusinessEvent.put("enabled", false);
        defaults.add(loanUndoLastDisbursalBusinessEvent);

        Map<String, Object> loanUndoWrittenOffBusinessEvent = new HashMap<>();
        loanUndoWrittenOffBusinessEvent.put("type", "LoanUndoWrittenOffBusinessEvent");
        loanUndoWrittenOffBusinessEvent.put("enabled", false);
        defaults.add(loanUndoWrittenOffBusinessEvent);

        Map<String, Object> loanUpdateChargeBusinessEvent = new HashMap<>();
        loanUpdateChargeBusinessEvent.put("type", "LoanUpdateChargeBusinessEvent");
        loanUpdateChargeBusinessEvent.put("enabled", false);
        defaults.add(loanUpdateChargeBusinessEvent);

        Map<String, Object> loanUpdateDisbursementDataBusinessEvent = new HashMap<>();
        loanUpdateDisbursementDataBusinessEvent.put("type", "LoanUpdateDisbursementDataBusinessEvent");
        loanUpdateDisbursementDataBusinessEvent.put("enabled", false);
        defaults.add(loanUpdateDisbursementDataBusinessEvent);

        Map<String, Object> loanWaiveChargeBusinessEvent = new HashMap<>();
        loanWaiveChargeBusinessEvent.put("type", "LoanWaiveChargeBusinessEvent");
        loanWaiveChargeBusinessEvent.put("enabled", false);
        defaults.add(loanWaiveChargeBusinessEvent);

        Map<String, Object> loanWaiveChargeUndoBusinessEvent = new HashMap<>();
        loanWaiveChargeUndoBusinessEvent.put("type", "LoanWaiveChargeUndoBusinessEvent");
        loanWaiveChargeUndoBusinessEvent.put("enabled", false);
        defaults.add(loanWaiveChargeUndoBusinessEvent);

        Map<String, Object> loanWaiveInterestBusinessEvent = new HashMap<>();
        loanWaiveInterestBusinessEvent.put("type", "LoanWaiveInterestBusinessEvent");
        loanWaiveInterestBusinessEvent.put("enabled", false);
        defaults.add(loanWaiveInterestBusinessEvent);

        Map<String, Object> loanWithdrawTransferBusinessEvent = new HashMap<>();
        loanWithdrawTransferBusinessEvent.put("type", "LoanWithdrawTransferBusinessEvent");
        loanWithdrawTransferBusinessEvent.put("enabled", false);
        defaults.add(loanWithdrawTransferBusinessEvent);

        Map<String, Object> loanWrittenOffPostBusinessEvent = new HashMap<>();
        loanWrittenOffPostBusinessEvent.put("type", "LoanWrittenOffPostBusinessEvent");
        loanWrittenOffPostBusinessEvent.put("enabled", false);
        defaults.add(loanWrittenOffPostBusinessEvent);

        Map<String, Object> loanWrittenOffPreBusinessEvent = new HashMap<>();
        loanWrittenOffPreBusinessEvent.put("type", "LoanWrittenOffPreBusinessEvent");
        loanWrittenOffPreBusinessEvent.put("enabled", false);
        defaults.add(loanWrittenOffPreBusinessEvent);

        Map<String, Object> recurringDepositAccountCreateBusinessEvent = new HashMap<>();
        recurringDepositAccountCreateBusinessEvent.put("type", "RecurringDepositAccountCreateBusinessEvent");
        recurringDepositAccountCreateBusinessEvent.put("enabled", false);
        defaults.add(recurringDepositAccountCreateBusinessEvent);

        Map<String, Object> savingsActivateBusinessEvent = new HashMap<>();
        savingsActivateBusinessEvent.put("type", "SavingsActivateBusinessEvent");
        savingsActivateBusinessEvent.put("enabled", false);
        defaults.add(savingsActivateBusinessEvent);

        Map<String, Object> savingsApproveBusinessEvent = new HashMap<>();
        savingsApproveBusinessEvent.put("type", "SavingsApproveBusinessEvent");
        savingsApproveBusinessEvent.put("enabled", false);
        defaults.add(savingsApproveBusinessEvent);

        Map<String, Object> savingsCloseBusinessEvent = new HashMap<>();
        savingsCloseBusinessEvent.put("type", "SavingsCloseBusinessEvent");
        savingsCloseBusinessEvent.put("enabled", false);
        defaults.add(savingsCloseBusinessEvent);

        Map<String, Object> savingsCreateBusinessEvent = new HashMap<>();
        savingsCreateBusinessEvent.put("type", "SavingsCreateBusinessEvent");
        savingsCreateBusinessEvent.put("enabled", false);
        defaults.add(savingsCreateBusinessEvent);

        Map<String, Object> savingsDepositBusinessEvent = new HashMap<>();
        savingsDepositBusinessEvent.put("type", "SavingsDepositBusinessEvent");
        savingsDepositBusinessEvent.put("enabled", false);
        defaults.add(savingsDepositBusinessEvent);

        Map<String, Object> savingsPostInterestBusinessEvent = new HashMap<>();
        savingsPostInterestBusinessEvent.put("type", "SavingsPostInterestBusinessEvent");
        savingsPostInterestBusinessEvent.put("enabled", false);
        defaults.add(savingsPostInterestBusinessEvent);

        Map<String, Object> savingsRejectBusinessEvent = new HashMap<>();
        savingsRejectBusinessEvent.put("type", "SavingsRejectBusinessEvent");
        savingsRejectBusinessEvent.put("enabled", false);
        defaults.add(savingsRejectBusinessEvent);

        Map<String, Object> savingsWithdrawalBusinessEvent = new HashMap<>();
        savingsWithdrawalBusinessEvent.put("type", "SavingsWithdrawalBusinessEvent");
        savingsWithdrawalBusinessEvent.put("enabled", false);
        defaults.add(savingsWithdrawalBusinessEvent);

        Map<String, Object> shareAccountApproveBusinessEvent = new HashMap<>();
        shareAccountApproveBusinessEvent.put("type", "ShareAccountApproveBusinessEvent");
        shareAccountApproveBusinessEvent.put("enabled", false);
        defaults.add(shareAccountApproveBusinessEvent);

        Map<String, Object> shareAccountCreateBusinessEvent = new HashMap<>();
        shareAccountCreateBusinessEvent.put("type", "ShareAccountCreateBusinessEvent");
        shareAccountCreateBusinessEvent.put("enabled", false);
        defaults.add(shareAccountCreateBusinessEvent);

        Map<String, Object> shareProductDividentsCreateBusinessEvent = new HashMap<>();
        shareProductDividentsCreateBusinessEvent.put("type", "ShareProductDividentsCreateBusinessEvent");
        shareProductDividentsCreateBusinessEvent.put("enabled", false);
        defaults.add(shareProductDividentsCreateBusinessEvent);

        Map<String, Object> loanChargeAdjustmentPostBusinessEvent = new HashMap<>();
        loanChargeAdjustmentPostBusinessEvent.put("type", "LoanChargeAdjustmentPostBusinessEvent");
        loanChargeAdjustmentPostBusinessEvent.put("enabled", false);
        defaults.add(loanChargeAdjustmentPostBusinessEvent);

        Map<String, Object> loanChargeAdjustmentPreBusinessEvent = new HashMap<>();
        loanChargeAdjustmentPreBusinessEvent.put("type", "LoanChargeAdjustmentPreBusinessEvent");
        loanChargeAdjustmentPreBusinessEvent.put("enabled", false);
        defaults.add(loanChargeAdjustmentPreBusinessEvent);

        Map<String, Object> loanDelinquencyRangeChangeBusinessEvent = new HashMap<>();
        loanDelinquencyRangeChangeBusinessEvent.put("type", "LoanDelinquencyRangeChangeBusinessEvent");
        loanDelinquencyRangeChangeBusinessEvent.put("enabled", false);
        defaults.add(loanDelinquencyRangeChangeBusinessEvent);

        return defaults;

    }

    public static String getExternalEventConfigurationsForUpdateJSON() {

        Map<String, Map<String, Boolean>> configurationsForUpdate = new HashMap<>();

        Map<String, Boolean> configurations = new HashMap<>();

        configurations.put("CentersCreateBusinessEvent", true);
        configurations.put("ClientActivateBusinessEvent", true);

        configurationsForUpdate.put("externalEventConfigurations", configurations);

        return new Gson().toJson(configurationsForUpdate);

    }

    public static Map<String, Boolean> updateExternalEventConfigurations(RequestSpecification requestSpec,
            ResponseSpecification responseSpec, String json) {
        Map<String, Map<String, Boolean>> response = Utils.performServerPut(requestSpec, responseSpec, EXTERNAL_EVENT_CONFIGURATION_URL,
                json, "changes");
        return response.get(EXTERNAL_EVENT_CONFIGURATION_RESPONSE);
    }

    public static void resetDefaultConfigurations(RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        Map<String, Map<String, Boolean>> configurationsForReset = new HashMap<>();

        Map<String, Boolean> configurations = new HashMap<>();

        configurations.put("CentersCreateBusinessEvent", false);
        configurations.put("ClientActivateBusinessEvent", false);

        configurationsForReset.put("externalEventConfigurations", configurations);

        String jsonForResettingConfigurations = new Gson().toJson(configurationsForReset);
        Utils.performServerPut(requestSpec, responseSpec, EXTERNAL_EVENT_CONFIGURATION_URL, jsonForResettingConfigurations, "");

    }
}
