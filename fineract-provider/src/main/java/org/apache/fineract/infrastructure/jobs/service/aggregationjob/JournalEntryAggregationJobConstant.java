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
package org.apache.fineract.infrastructure.jobs.service.aggregationjob;

public final class JournalEntryAggregationJobConstant {

    public static final String CONTINUE_JOB_EXECUTION = "CONTINUE_JOB_EXECUTION";
    public static final String NO_OP_EXECUTION = "NO_OP_EXECUTION";
    public static final String JOURNAL_ENTRY_AGGREGATION_JOB_NAME = "JOURNAL_ENTRY_AGGREGATION";
    public static final String JOB_SUMMARY_STEP_NAME = "JournalEntryAggregation Summary Insert - Step";
    public static final String JOB_TRACKING_STEP_NAME = "JournalEntryAggregation Tracking Insert - Step";
    public static final String AGGREGATED_ON_DATE = "aggregatedOnDate";
    public static final String AGGREGATED_ON_DATE_FROM = "aggregatedOnDateFrom";
    public static final String AGGREGATED_ON_DATE_TO = "aggregatedOnDateTo";
    public static final String LAST_AGGREGATED_ON_DATE = "lastAggregatedOnDate";

    private JournalEntryAggregationJobConstant() {}
}
