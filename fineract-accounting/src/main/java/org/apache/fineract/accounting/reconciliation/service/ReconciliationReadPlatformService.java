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
package org.apache.fineract.accounting.reconciliation.service;

import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.accounting.reconciliation.data.BankStatementImportData;
import org.apache.fineract.accounting.reconciliation.data.BankStatementTransactionData;
import org.apache.fineract.accounting.reconciliation.data.ReconciliationAdjustmentData;
import org.apache.fineract.accounting.reconciliation.data.ReconciliationMatchData;
import org.apache.fineract.accounting.reconciliation.data.ReconciliationRuleData;
import org.apache.fineract.accounting.reconciliation.data.ReconciliationSummaryData;
import org.apache.fineract.accounting.reconciliation.data.UnreconciledGLEntryData;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;

public interface ReconciliationReadPlatformService {

    Page<BankStatementImportData> retrieveAll(Long glAccountId, LocalDate fromDate, LocalDate toDate, String status,
            SearchParameters searchParameters);

    BankStatementImportData retrieveOne(Long importId);

    List<BankStatementTransactionData> retrieveTransactions(Long importId);

    List<ReconciliationMatchData> retrieveMatches(Long importId);

    List<ReconciliationAdjustmentData> retrieveAdjustments(Long importId);

    ReconciliationSummaryData retrieveSummary(Long importId);

    List<UnreconciledGLEntryData> retrieveUnreconciledGLEntries(Long glAccountId, LocalDate fromDate, LocalDate toDate);

    List<ReconciliationRuleData> retrieveAllRules(Long glAccountId);

    ReconciliationRuleData retrieveRule(Long ruleId);
}
