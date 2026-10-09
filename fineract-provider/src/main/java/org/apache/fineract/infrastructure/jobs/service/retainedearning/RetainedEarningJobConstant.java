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
package org.apache.fineract.infrastructure.jobs.service.retainedearning;

/**
 * Retained Earning job constant
 */
public final class RetainedEarningJobConstant {

    /**
     * Constructor
     */
    private RetainedEarningJobConstant() {}

    /**
     * Retained earning job name
     */
    public static final String RETAINED_EARNING_JOB_NAME = "RETAINED_EARNING";

    /**
     * Summary step name
     */
    public static final String JOB_SUMMARY_STEP_NAME = "RetainedEarning Summary Insert - Step";

    /**
     * Query parameter - end date
     */
    public static final String END_DATE_QUERY_PARAM = "R_endDate";

    /**
     * Report type Trial Balance Summary with asset owner
     */
    public static final String TRIAL_BALANCE_SUMMARY_WITH_ASSET_OWNER = "Trial Balance Summary Report with Asset Owner";

    /**
     * Report type Trial Balance Summary for working capital loans
     */
    public static final String TRIAL_BALANCE_SUMMARY_FOR_WORKING_CAPITAL_LOANS = "Trial Balance Summary Report for Working Capital Loans";

    /**
     * Query parameter - office id
     */
    public static final String OFFICE_ID_QUERY_PARAM = "R_officeId";

}
