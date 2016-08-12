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
package org.apache.fineract.infrastructure.reportmailingjob.domain;

import org.apache.fineract.infrastructure.reportmailingjob.exception.ReportMailingJobNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportMailingJobRepositoryWrapper {
    private final ReportMailingJobRepository reportMailingJobRepository;
    
    @Autowired
    public ReportMailingJobRepositoryWrapper(final ReportMailingJobRepository reportMailingJobRepository) {
        this.reportMailingJobRepository = reportMailingJobRepository;
    }
    
    /** 
     * find report mailing job by ID, throw a "entity not found" exception if the job does not exist
     * 
     * @param id -- the identifier of the report mailing job to be found
     * @return ReportMailingJob object
     **/
    public ReportMailingJob findOneThrowExceptionIfNotFound(final Long id) {
        final ReportMailingJob reportMailingJob = this.reportMailingJobRepository.findOne(id);
        
        if (reportMailingJob == null || reportMailingJob.isDeleted()) {
            throw new ReportMailingJobNotFoundException(id);
        }
        
        return reportMailingJob;
    }
    
    /** 
     * @return ReportMailingJobRepository Jpa Repository object
     **/
    public ReportMailingJobRepository getReportMailingJobRepository() {
        return this.reportMailingJobRepository;
    }
}
