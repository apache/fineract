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
package org.apache.fineract.infrastructure.campaigns.jobs.getdeliveryreportsfromsmsgateway;

import org.apache.fineract.infrastructure.campaigns.helper.SmsConfigUtils;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.infrastructure.sms.domain.SmsMessageRepository;
import org.apache.fineract.infrastructure.sms.service.SmsReadPlatformService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class GetDeliveryReportsFromSmsGatewayConfig {

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private SmsMessageRepository smsMessageRepository;
    @Autowired
    private SmsReadPlatformService smsReadPlatformService;
    @Autowired
    private SmsConfigUtils smsConfigUtils;

    @Bean
    protected Step getDeliveryReportsFromSmsGatewayStep() {
        return new StepBuilder(JobName.GET_DELIVERY_REPORTS_FROM_SMS_GATEWAY.name(), jobRepository)
                .tasklet(getDeliveryReportsFromSmsGatewayTasklet(), transactionManager).build();
    }

    @Bean
    public Job getDeliveryReportsFromSmsGatewayJob() {
        return new JobBuilder(JobName.GET_DELIVERY_REPORTS_FROM_SMS_GATEWAY.name(), jobRepository)
                .start(getDeliveryReportsFromSmsGatewayStep()).incrementer(new RunIdIncrementer()).build();
    }

    @Bean
    public GetDeliveryReportsFromSmsGatewayTasklet getDeliveryReportsFromSmsGatewayTasklet() {
        return new GetDeliveryReportsFromSmsGatewayTasklet(smsReadPlatformService, smsConfigUtils, smsMessageRepository);
    }
}
