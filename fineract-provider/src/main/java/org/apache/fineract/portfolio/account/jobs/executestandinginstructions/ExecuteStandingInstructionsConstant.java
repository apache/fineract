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
package org.apache.fineract.portfolio.account.jobs.executestandinginstructions;

import org.apache.fineract.infrastructure.jobs.service.JobName;

public final class ExecuteStandingInstructionsConstant {

    public static final String JOB_NAME = JobName.EXECUTE_STANDING_INSTRUCTIONS.name();

    public static final String PARTITIONER_STEP = "Execute Standing Instructions partition - Step";
    public static final String WORKER_STEP = "executeStandingInstructionsWorkerStep";

    public static final String PARTITION_PREFIX = "partition_";

    /** Inclusive bounds of the partition's source-account key range, written into the partition execution context. */
    public static final String MIN_ACCOUNT_KEY = "minAccountKey";
    public static final String MAX_ACCOUNT_KEY = "maxAccountKey";
    public static final String PARTITION_KEY = "partition";

    private ExecuteStandingInstructionsConstant() {

    }
}
