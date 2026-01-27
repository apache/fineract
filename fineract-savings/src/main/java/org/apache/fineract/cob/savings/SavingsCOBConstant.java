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
package org.apache.fineract.cob.savings;

import org.apache.fineract.cob.COBConstant;

public final class SavingsCOBConstant extends COBConstant {

    public static final String JOB_NAME = "SAVINGS_COB";
    public static final String JOB_HUMAN_READABLE_NAME = "Savings COB";
    public static final String SAVINGS_COB_JOB_NAME = "SAVINGS_CLOSE_OF_BUSINESS";
    public static final String SAVINGS_COB_PARAMETER = "savingsCobParameter";
    public static final String SAVINGS_COB_WORKER_STEP = "savingsCOBWorkerStep";

    public static final String INLINE_SAVINGS_COB_JOB_NAME = "INLINE_SAVINGS_COB";
    public static final String SAVINGS_IDS_PARAMETER_NAME = "SavingsIds";

    public static final String SAVINGS_COB_PARTITIONER_STEP = "Savings COB partition - Step";
    public static final String PARTITION_KEY = "partition";

    private SavingsCOBConstant() {

    }
}
