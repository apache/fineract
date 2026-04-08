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
package org.apache.fineract.infrastructure.jobs.service.aggregationjob.services;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.jobs.service.aggregationjob.data.JournalEntryAggregationSummaryData;
import org.apache.fineract.infrastructure.jobs.service.aggregationjob.data.JournalEntryAggregationTrackingData;
import org.apache.fineract.infrastructure.jobs.service.aggregationjob.domain.JournalEntryAggregationTracking;
import org.apache.fineract.infrastructure.jobs.service.aggregationjob.domain.JournalEntryAggregationTrackingRepository;
import org.apache.fineract.infrastructure.jobs.service.aggregationjob.domain.JournalEntrySummary;
import org.apache.fineract.infrastructure.jobs.service.aggregationjob.domain.JournalEntrySummaryRepository;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class JournalEntryAggregationWriterServiceImpl implements JournalEntryAggregationWriterService {

    private JournalEntrySummaryRepository journalSummaryRepository;
    private JournalEntryAggregationTrackingRepository journalEntryAggregationTrackingRepository;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public void insertJournalEntrySummaryBatch(final List<JournalEntryAggregationSummaryData> journalEntrySummaries) {
        List<JournalEntrySummary> entities = journalEntrySummaries.stream().map(this::convertToJournalEntrySummary).toList();
        journalSummaryRepository.saveAll(entities);
    }

    @Override
    public void insertJournalEntryTracking(JournalEntryAggregationTrackingData journalEntrySummaryTrackingDTO) throws Exception {
        log.info("Inserting journal entry tracking");
        saveJournalEntryTracking(journalEntrySummaryTrackingDTO);
    }

    @Override
    public void rollbackJournalEntrySummary(final Long jobExecutionId) {
        log.info("Rolling back journal entry summary for jobExecutionId={}", jobExecutionId);
        journalSummaryRepository.deleteByJobExecutionId(jobExecutionId);
    }

    @Override
    public void rollbackJournalEntryTracking(final Long jobExecutionId) {
        log.info("Rolling back journal entry summary tracking for jobExecutionId={}", jobExecutionId);
        journalEntryAggregationTrackingRepository.deleteByJobExecutionId(jobExecutionId);
    }

    private void saveJournalEntryTracking(final JournalEntryAggregationTrackingData journalEntrySummaryTrackingDTO) {
        JournalEntryAggregationTracking journalEntryAggregationTracking = new JournalEntryAggregationTracking();
        journalEntryAggregationTracking.setSubmittedOnDate(journalEntrySummaryTrackingDTO.getSubmittedOnDate());
        journalEntryAggregationTracking.setAggregatedOnDateFrom(journalEntrySummaryTrackingDTO.getAggregatedOnDateFrom());
        journalEntryAggregationTracking.setAggregatedOnDateTo(journalEntrySummaryTrackingDTO.getAggregatedOnDateTo());
        journalEntryAggregationTracking.setJobExecutionId(journalEntrySummaryTrackingDTO.getJobExecutionId());
        journalEntryAggregationTrackingRepository.save(journalEntryAggregationTracking);
    }

    private JournalEntrySummary convertToJournalEntrySummary(final JournalEntryAggregationSummaryData summaryDTO) {
        JournalEntrySummary entrySummary = new JournalEntrySummary();
        entrySummary.setProduct(summaryDTO.getProductId());
        entrySummary.setGlAccountId(summaryDTO.getGlAccountId());
        entrySummary.setOffice(summaryDTO.getOffice());
        entrySummary.setEntityTypeEnum(summaryDTO.getEntityTypeEnum());
        entrySummary.setSubmittedOnDate(summaryDTO.getSubmittedOnDate());
        entrySummary.setDebitAmount(summaryDTO.getDebitAmount());
        entrySummary.setCreditAmount(summaryDTO.getCreditAmount());
        entrySummary.setExternalOwnerId(summaryDTO.getExternalOwnerId());
        entrySummary.setAggregatedOnDate(summaryDTO.getAggregatedOnDate());
        entrySummary.setJobExecutionId(summaryDTO.getJobExecutionId());
        return entrySummary;
    }

}
