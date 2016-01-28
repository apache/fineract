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
package org.apache.fineract.infrastructure.jobs.service;

public interface SchedulerServiceConstants {

    public static final String JOB_KEY_SEPERATOR = " _ ";
    public static final String TRIGGER_TYPE_CRON = "cron";
    public static final String TRIGGER_TYPE_APPLICATION = "application";
    public static final String TRIGGER_TYPE_REFERENCE = "TRIGGER_TYPE_REFERENCE";
    public static final String SCHEDULER_EXCEPTION = "SchedulerException";
    public static final String JOB_EXECUTION_EXCEPTION = "JobExecutionException";
    public static final String JOB_METHOD_INVOCATION_FAILED_EXCEPTION = "JobMethodInvocationFailedException";
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_FAILED = "failed";
    public static final String DEFAULT_LISTENER_NAME = "Global Listner";
    public static final int STACK_TRACE_LEVEL = 7;
    public static final String TENANT_IDENTIFIER = "tenantIdentifier";
    public static final String SCHEDULER = "Scheduler";
    public static final String SCHEDULER_GROUP = "group";
    public static final int DEFAULT_THREAD_COUNT = 7;
    public static final int GROUP_THREAD_COUNT = 1;
    public static final String SCHEDULER_NAME = "schedulerName";

}