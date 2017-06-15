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
package org.apache.fineract.adhocquery.service;

import java.util.Collection;

import org.apache.fineract.adhocquery.data.AdHocData;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.annotation.CronTarget;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service(value = "adHocScheduledJobRunnerService")
public class AdHocScheduledJobRunnerServiceImpl implements AdHocScheduledJobRunnerService {

    private final static Logger logger = LoggerFactory.getLogger(AdHocScheduledJobRunnerServiceImpl.class);
    private final AdHocReadPlatformService adHocReadPlatformService;
    private final JdbcTemplate jdbcTemplate;
    
    @Autowired
    public AdHocScheduledJobRunnerServiceImpl(final RoutingDataSource dataSource,
    		final AdHocReadPlatformService adHocReadPlatformService
            ) {
    	this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.adHocReadPlatformService = adHocReadPlatformService;
       
    }
    @Transactional
    @Override
    @CronTarget(jobName = JobName.GENERATE_ADHOCCLIENT_SCEHDULE)
    public void generateClientSchedule() {
    	final Collection<AdHocData> adhocs = this.adHocReadPlatformService.retrieveAllActiveAdHocQuery();
        if(adhocs.size()>0){
        	adhocs.forEach(adhoc->{
            	jdbcTemplate.execute("truncate table "+adhoc.getTableName());
            	final StringBuilder insertSqlBuilder = new StringBuilder(900);
                insertSqlBuilder
                        .append("INSERT INTO ")
                        .append(adhoc.getTableName()+"(")
                		.append(adhoc.getTableFields()+") ")
                		.append(adhoc.getQuery());
                if (insertSqlBuilder.length() > 0) {
                	final int result = this.jdbcTemplate.update(insertSqlBuilder.toString());
                	logger.info(ThreadLocalContextUtil.getTenant().getName() + ": Results affected by inserted: " + result);
                }
            });	
        }else{
        	logger.info(ThreadLocalContextUtil.getTenant().getName() + "Nothing to update ");
        }
        
        
   
    }

}
