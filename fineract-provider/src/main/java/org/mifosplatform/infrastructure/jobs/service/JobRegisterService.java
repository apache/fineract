/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.jobs.service;

public interface JobRegisterService {

    public void executeJob(Long jobId);

    public void rescheduleJob(Long jobId);

    public void pauseScheduler();

    public void startScheduler();

    public boolean isSchedulerRunning();

    public void stopScheduler(String name);

    public void stopAllSchedulers();

}
