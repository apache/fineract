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
package org.apache.fineract.infrastructure.dataqueries.domain;

import org.apache.fineract.infrastructure.dataqueries.exception.ReportNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** 
 *  A wrapper class for the ReportRepository that provides a method that returns a Report entity if it exists, 
 *  else throws "ReportNotFoundException" exception if the Report does not exist
 **/
@Service
public class ReportRepositoryWrapper {
    private final ReportRepository reportRepository;
    
    @Autowired
    public ReportRepositoryWrapper(final ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }
    
    /**
     * Retrieves an entity by its id
     * 
     * @param id must not be null
     * @throws ReportNotFoundException if entity not found
     * @return {@link Report} object
     */
    public Report findOneThrowExceptionIfNotFound(final Long id) {
        final Report report = this.reportRepository.findOne(id);
        
        if (report == null) {
            throw new ReportNotFoundException(id);
        }
        
        return report;
    }
}
