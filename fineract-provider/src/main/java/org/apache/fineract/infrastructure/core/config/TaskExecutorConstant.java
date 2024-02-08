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
package org.apache.fineract.infrastructure.core.config;

public final class TaskExecutorConstant {

    private TaskExecutorConstant() {

    }

    public static final String DEFAULT_TASK_EXECUTOR_BEAN_NAME = "fineractDefaultThreadPoolTaskExecutor";
    public static final String CONFIGURABLE_TASK_EXECUTOR_BEAN_NAME = "fineractConfigurableThreadPoolTaskExecutor";
    public static final String EVENT_TASK_EXECUTOR_BEAN_NAME = "externalEventJmsProducerExecutor";
    public static final String LOAN_COB_CATCH_UP_TASK_EXECUTOR_BEAN_NAME = "loanCOBCatchUpThreadPoolTaskExecutor";
}
