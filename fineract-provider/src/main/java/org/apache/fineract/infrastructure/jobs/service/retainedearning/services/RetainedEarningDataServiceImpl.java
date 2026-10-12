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
package org.apache.fineract.infrastructure.jobs.service.retainedearning.services;

import static org.apache.fineract.infrastructure.jobs.service.retainedearning.RetainedEarningJobConstant.END_DATE_QUERY_PARAM;
import static org.apache.fineract.infrastructure.jobs.service.retainedearning.RetainedEarningJobConstant.OFFICE_ID_QUERY_PARAM;

import com.google.common.base.Splitter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.fineract.accounting.retainedearning.domain.AccountGLJournalEntryAnnualSummary;
import org.apache.fineract.accounting.retainedearning.domain.AccountGLJournalEntryAnnualSummaryRepository;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.dataqueries.service.DatatableExportTargetParameter;
import org.apache.fineract.infrastructure.jobs.service.retainedearning.RetainedEarningConfigurationService;
import org.apache.fineract.infrastructure.jobs.service.retainedearning.data.AccountGLJournalEntryAnnualSummaryData;
import org.apache.fineract.infrastructure.jobs.service.retainedearning.helper.DataParser;
import org.apache.fineract.infrastructure.report.service.ReportingProcessService;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;
import org.springframework.stereotype.Component;

/**
 * Retained earning data service implementation. Handles data fetching, processing, and persistence.
 */
@Component
@AllArgsConstructor
@Slf4j
public class RetainedEarningDataServiceImpl implements RetainedEarningDataService {

    private final ReportingProcessService reportingProcessService;

    private final DataParser dataParser;

    private final AccountGLJournalEntryAnnualSummaryRepository retainedEarningSummaryRepository;

    private final LoanProductRepository loanProductRepository;

    private final RetainedEarningConfigurationService retainedEarningConfigurationService;

    private record ProductOwnerKey(String productName, ExternalId ownerExternalId) {
    }

    @Override
    public void insertRetainedEarningSummaryBatch(final List<AccountGLJournalEntryAnnualSummaryData> retainedEarningSummaries) {
        if (retainedEarningSummaries == null || retainedEarningSummaries.isEmpty()) {
            log.warn("No retained earning summaries provided for insertion, skipping batch save.");
            return;
        }
        List<AccountGLJournalEntryAnnualSummary> entities = retainedEarningSummaries.stream().map(this::convertToRetainedEarningSummary)
                .toList();
        retainedEarningSummaryRepository.saveAll(entities);
    }

    private AccountGLJournalEntryAnnualSummary convertToRetainedEarningSummary(final AccountGLJournalEntryAnnualSummaryData summaryDTO) {
        AccountGLJournalEntryAnnualSummary entrySummary = new AccountGLJournalEntryAnnualSummary();
        entrySummary.setProductId(summaryDTO.getProductId());
        entrySummary.setGlCode(String.valueOf(summaryDTO.getGlAccountCode()));
        entrySummary.setOfficeId(summaryDTO.getOfficeId());
        entrySummary.setOwnerExternalId(summaryDTO.getOwnerExternalId());
        entrySummary.setOpeningBalanceAmount(summaryDTO.getOpeningBalanceAmount());
        entrySummary.setYearEndDate(summaryDTO.getYearEndDate());
        entrySummary.setCurrencyCode(summaryDTO.getCurrencyCode());
        return entrySummary;
    }

    @Override
    public List<AccountGLJournalEntryAnnualSummaryData> fetchTrialBalanceData(String reportName, LocalDate fiscalYearEnd) {
        MultivaluedMap<String, String> queryParams = buildQueryParams(fiscalYearEnd);
        Response response = reportingProcessService.processRequest(reportName, queryParams);
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            throw new IllegalStateException("Trial balance report returned HTTP " + response.getStatus() + " for report: " + reportName);
        }
        String jsonResponse = (String) response.getEntity();
        return parseJsonResponse(jsonResponse);
    }

    private MultivaluedMap<String, String> buildQueryParams(final LocalDate lastDayOfPreviousFiscalYear) {
        final MultivaluedMap<String, String> queryParams = new MultivaluedStringMap();
        queryParams.add(DatatableExportTargetParameter.PRETTY_JSON.getValue(), BooleanUtils.TRUE);
        queryParams.add(END_DATE_QUERY_PARAM, lastDayOfPreviousFiscalYear.toString());
        queryParams.add(OFFICE_ID_QUERY_PARAM, String.valueOf(retainedEarningConfigurationService.getOfficeId()));
        return queryParams;
    }

    @Override
    public List<AccountGLJournalEntryAnnualSummaryData> processTrialBalanceData(List<AccountGLJournalEntryAnnualSummaryData> rawData,
            LocalDate lastDayOfPreviousFiscalYear) {

        if (rawData == null || rawData.isEmpty()) {
            log.warn("No data to process");
            return Collections.emptyList();
        }

        final String incomeAndExpenseGlAccounts = retainedEarningConfigurationService.getIncomeExpenseGlAccounts();
        final Predicate<String> glAccountMatcher = buildGlAccountMatcher(incomeAndExpenseGlAccounts);

        final List<AccountGLJournalEntryAnnualSummaryData> incomeExpenseRecords = rawData.stream().filter(
                r -> r != null && r.getGlAccountCode() != null && r.getOwnerExternalId() != null && !r.getOwnerExternalId().isEmpty())
                .filter(r -> glAccountMatcher.test(r.getGlAccountCode())).collect(Collectors.toList());

        final Set<String> distinctGlCodes = incomeExpenseRecords.stream().map(r -> String.valueOf(r.getGlAccountCode()))
                .collect(Collectors.toSet());
        final Set<ExternalId> distinctOwners = incomeExpenseRecords.stream().map(AccountGLJournalEntryAnnualSummaryData::getOwnerExternalId)
                .collect(Collectors.toSet());

        log.info(
                "Retained earning validation: totalTrialBalanceRecords={}, matchedIncomeExpenseRecords={}, distinctGlAccounts={}, distinctAssetOwners={}, fiscalYearEnd={}",
                rawData.size(), incomeExpenseRecords.size(), distinctGlCodes.size(), distinctOwners.size(), lastDayOfPreviousFiscalYear);

        if (incomeExpenseRecords.isEmpty()) {
            log.info("No income/expense account records found hence skipping retained earning creation");
            return Collections.emptyList();
        }

        final Set<String> distinctProductNamesLower = incomeExpenseRecords.stream()
                .map(AccountGLJournalEntryAnnualSummaryData::getProductName).filter(name -> name != null && !name.isBlank())
                .map(String::toLowerCase).collect(Collectors.toSet());
        final Map<String, LoanProduct> productByName = loanProductRepository.findAllByNameIgnoreCase(distinctProductNamesLower).stream()
                .collect(Collectors.toMap(p -> p.getName().toLowerCase(), p -> p, (a, b) -> a));

        final Map<ProductOwnerKey, BigDecimal> retainedByProductAndOwner = incomeExpenseRecords.stream()
                .collect(Collectors.toMap(r -> new ProductOwnerKey(r.getProductName(), r.getOwnerExternalId()),
                        r -> Optional.ofNullable(r.getEndingBalanceAmount()).orElse(BigDecimal.ZERO), BigDecimal::add));

        final List<AccountGLJournalEntryAnnualSummaryData> retainedEarningRecords = createRetainedEarningRecords(retainedByProductAndOwner,
                incomeExpenseRecords, lastDayOfPreviousFiscalYear);

        final List<AccountGLJournalEntryAnnualSummaryData> allRecords = Stream
                .concat(incomeExpenseRecords.stream(), retainedEarningRecords.stream()).map(data -> {
                    LoanProduct loanProduct = data.getProductName() != null ? productByName.get(data.getProductName().toLowerCase()) : null;
                    if (loanProduct == null) {
                        return data;
                    }
                    return data.toBuilder().productId(loanProduct.getId()).currencyCode(loanProduct.getCurrency().getCode()).build();
                }).collect(Collectors.toList());

        log.info(
                "Retained earning processing complete: incomeExpenseOffsetRecords={}, retainedEarningRecords={}, totalRecordsToWrite={}, assetOwners={}",
                incomeExpenseRecords.size(), retainedEarningRecords.size(), allRecords.size(), distinctOwners);

        return allRecords;
    }

    private Predicate<String> buildGlAccountMatcher(String incomeAndExpenseGlAccounts) {
        if (incomeAndExpenseGlAccounts == null || incomeAndExpenseGlAccounts.isBlank()) {
            return code -> false;
        }
        List<Predicate<String>> predicates = new ArrayList<>();
        for (String token : Splitter.on(',').split(incomeAndExpenseGlAccounts)) {
            predicates.add(buildPredicate(token));
        }
        return predicates.stream().reduce(code -> false, Predicate::or);
    }

    private Predicate<String> buildPredicate(String glAccountCode) {
        String trimmed = glAccountCode.trim();
        if (trimmed.matches("\\d+-\\d+")) {
            String[] bounds = trimmed.split("-", 2);
            int from = Integer.parseInt(bounds[0].trim());
            int to = Integer.parseInt(bounds[1].trim());
            return code -> {
                try {
                    int codeInt = Integer.parseInt(code);
                    return codeInt >= from && codeInt <= to;
                } catch (NumberFormatException e) {
                    return false;
                }
            };
        } else {
            return trimmed::equals;
        }
    }

    private List<AccountGLJournalEntryAnnualSummaryData> createRetainedEarningRecords(
            Map<ProductOwnerKey, BigDecimal> retainedByProductAndOwner, List<AccountGLJournalEntryAnnualSummaryData> originalData,
            LocalDate lastDayOfPreviousFiscalYear) {

        final String retainedEarningGlAccountCode = retainedEarningConfigurationService.getRetainedEarningGlAccount();

        final Map<ProductOwnerKey, AccountGLJournalEntryAnnualSummaryData> firstRecordByProductAndOwner = originalData.stream()
                .filter(r -> r.getOwnerExternalId() != null && !r.getOwnerExternalId().isEmpty() && r.getProductName() != null)
                .collect(Collectors.toMap(r -> new ProductOwnerKey(r.getProductName(), r.getOwnerExternalId()), r -> r,
                        (first, second) -> first));

        Long defaultOfficeId = retainedEarningConfigurationService.getOfficeId();
        return retainedByProductAndOwner.entrySet().stream().filter(e -> e.getValue().compareTo(BigDecimal.ZERO) != 0).map(e -> {
            final ProductOwnerKey key = e.getKey();
            AccountGLJournalEntryAnnualSummaryData template = firstRecordByProductAndOwner.get(key);
            return AccountGLJournalEntryAnnualSummaryData.builder().productName(key.productName())
                    .glAccountCode(retainedEarningGlAccountCode).officeId(template != null ? template.getOfficeId() : defaultOfficeId)
                    .ownerExternalId(key.ownerExternalId()).openingBalanceAmount(e.getValue()).endingBalanceAmount(e.getValue())
                    .yearEndDate(lastDayOfPreviousFiscalYear).manualEntry(false).build();
        }).collect(Collectors.toList());
    }

    private List<AccountGLJournalEntryAnnualSummaryData> parseJsonResponse(String jsonResponse) {
        try {
            return dataParser.parse(jsonResponse).stream()
                    .map(record -> AccountGLJournalEntryAnnualSummaryData.builder().glAccountCode(record.getGlAcct())
                            .productName(record.getProduct()).officeId(retainedEarningConfigurationService.getOfficeId())
                            .ownerExternalId(record.getAssetOwner()).openingBalanceAmount(record.getEndingBalance().negate())
                            .endingBalanceAmount(record.getEndingBalance()).yearEndDate(LocalDate.parse(record.getPostingDate()))
                            .manualEntry(false).build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse trial balance data: " + e.getMessage(), e);
        }
    }

}
