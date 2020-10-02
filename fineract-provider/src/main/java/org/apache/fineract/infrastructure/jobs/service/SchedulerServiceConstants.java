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

    String JOB_KEY_SEPERATOR = " _ ";
    String TRIGGER_TYPE_CRON = "cron";
    String TRIGGER_TYPE_APPLICATION = "application";
    String TRIGGER_TYPE_REFERENCE = "TRIGGER_TYPE_REFERENCE";
    String SCHEDULER_EXCEPTION = "SchedulerException";
    String JOB_EXECUTION_EXCEPTION = "JobExecutionException";
    String JOB_METHOD_INVOCATION_FAILED_EXCEPTION = "JobMethodInvocationFailedException";
    String STATUS_SUCCESS = "success";
    String STATUS_FAILED = "failed";
    String DEFAULT_LISTENER_NAME = "Global Listener";
    int STACK_TRACE_LEVEL = 7;
    String TENANT_IDENTIFIER = "tenantIdentifier";
    String SCHEDULER = "Scheduler";
    String SCHEDULER_GROUP = "group";
    int DEFAULT_THREAD_COUNT = 7;
    int GROUP_THREAD_COUNT = 1;
    String SCHEDULER_NAME = "schedulerName";

}
