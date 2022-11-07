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
package org.apache.fineract.portfolio.shareaccounts.jobs.postdividentsforshares;

import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.portfolio.shareaccounts.service.ShareAccountDividendReadPlatformService;
import org.apache.fineract.portfolio.shareaccounts.service.ShareAccountSchedularService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PostDividentsForSharesConfig {

    @Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;
    @Autowired
    private ShareAccountDividendReadPlatformService shareAccountDividendReadPlatformService;
    @Autowired
    private ShareAccountSchedularService shareAccountSchedularService;

    @Bean
    protected Step postDividentsForSharesStep() {
        return steps.get(JobName.POST_DIVIDENTS_FOR_SHARES.name()).tasklet(postDividentsForSharesTasklet()).build();
    }

    @Bean
    public Job postDividentsForSharesJob() {
        return jobs.get(JobName.POST_DIVIDENTS_FOR_SHARES.name()).start(postDividentsForSharesStep()).incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public PostDividentsForSharesTasklet postDividentsForSharesTasklet() {
        return new PostDividentsForSharesTasklet(shareAccountDividendReadPlatformService, shareAccountSchedularService);
    }
}
