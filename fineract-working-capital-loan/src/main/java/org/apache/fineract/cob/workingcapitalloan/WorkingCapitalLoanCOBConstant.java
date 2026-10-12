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
package org.apache.fineract.cob.workingcapitalloan;

import lombok.NoArgsConstructor;
import org.apache.fineract.cob.COBConstant;

@NoArgsConstructor
public final class WorkingCapitalLoanCOBConstant extends COBConstant {

    // Job Related Constants
    public static final String WORKING_CAPITAL_JOB_NAME = "WC_LOAN_COB";
    public static final String WORKING_CAPITAL_JOB_HUMAN_READABLE_NAME = "Working Capital Loan COB";
    public static final String WORKING_CAPITAL_LOAN_COB_JOB_NAME = "WORKING_CAPITAL_LOAN_CLOSE_OF_BUSINESS";

    // Bean Names
    public static final String WORKING_CAPITAL_LOAN_COB_STEP = "workingCapitalLoanCOBStep";
    public static final String WORKING_CAPITAL_LOAN_COB_BUSINESS_STEP = "workingCapitalLoanCOBBusinessStep";
    public static final String WORKING_CAPITAL_LOAN_COB_PARTITIONER = "workingCapitalLoanCOBPartitioner";

    public static final String WORKING_CAPITAL_LOAN_COB_WORKER_STEP = "workingCapitalLoanCOBWorkerStep";
    public static final String WORKING_CAPITAL_LOAN_COB_FLOW = "workingCapitalLoanCOBFlow";

    public static final String INLINE_WORKING_CAPITAL_LOAN_COB_JOB_NAME = "INLINE_WORKING_CAPITAL_LOAN_COB";
    public static final String WORKING_CAPITAL_LOAN_IDS_PARAMETER_NAME = "LoanIds";

    public static final String WORKING_CAPITAL_LOAN_COB_PARTITIONER_STEP = "Working Capital Loan COB partition - Step";

}
