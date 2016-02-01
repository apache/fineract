/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.jobs.service;

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