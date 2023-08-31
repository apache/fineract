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
package org.apache.fineract.portfolio.collectionsheet.starter;

import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstanceRepository;
import org.apache.fineract.portfolio.calendar.domain.CalendarRepositoryWrapper;
import org.apache.fineract.portfolio.calendar.service.CalendarReadPlatformService;
import org.apache.fineract.portfolio.collectionsheet.data.CollectionSheetTransactionDataValidator;
import org.apache.fineract.portfolio.collectionsheet.serialization.CollectionSheetBulkDisbursalCommandFromApiJsonDeserializer;
import org.apache.fineract.portfolio.collectionsheet.serialization.CollectionSheetBulkRepaymentCommandFromApiJsonDeserializer;
import org.apache.fineract.portfolio.collectionsheet.serialization.CollectionSheetGenerateCommandFromApiJsonDeserializer;
import org.apache.fineract.portfolio.collectionsheet.service.CollectionSheetReadPlatformService;
import org.apache.fineract.portfolio.collectionsheet.service.CollectionSheetReadPlatformServiceImpl;
import org.apache.fineract.portfolio.collectionsheet.service.CollectionSheetWritePlatformService;
import org.apache.fineract.portfolio.collectionsheet.service.CollectionSheetWritePlatformServiceJpaRepositoryImpl;
import org.apache.fineract.portfolio.group.service.CenterReadPlatformService;
import org.apache.fineract.portfolio.group.service.GroupReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanWritePlatformService;
import org.apache.fineract.portfolio.meeting.attendance.service.AttendanceDropdownReadPlatformService;
import org.apache.fineract.portfolio.meeting.service.MeetingWritePlatformService;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetailAssembler;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadPlatformService;
import org.apache.fineract.portfolio.savings.domain.DepositAccountAssembler;
import org.apache.fineract.portfolio.savings.service.DepositAccountWritePlatformService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
public class CollectionSheetConfiguration {

    @Bean
    @ConditionalOnMissingBean(CollectionSheetReadPlatformService.class)
    public CollectionSheetReadPlatformService collectionSheetReadPlatformService(PlatformSecurityContext context,
            NamedParameterJdbcTemplate namedParameterJdbcTemplate, CenterReadPlatformService centerReadPlatformService,
            GroupReadPlatformService groupReadPlatformService,
            CollectionSheetGenerateCommandFromApiJsonDeserializer collectionSheetGenerateCommandFromApiJsonDeserializer,
            CalendarRepositoryWrapper calendarRepositoryWrapper,
            AttendanceDropdownReadPlatformService attendanceDropdownReadPlatformService,
            CodeValueReadPlatformService codeValueReadPlatformService, PaymentTypeReadPlatformService paymentTypeReadPlatformService,
            CalendarReadPlatformService calendarReadPlatformService, ConfigurationDomainService configurationDomainService,
            CalendarInstanceRepository calendarInstanceRepository, DatabaseSpecificSQLGenerator sqlGenerator) {
        return new CollectionSheetReadPlatformServiceImpl(context, namedParameterJdbcTemplate, centerReadPlatformService,
                groupReadPlatformService, collectionSheetGenerateCommandFromApiJsonDeserializer, calendarRepositoryWrapper,
                attendanceDropdownReadPlatformService, codeValueReadPlatformService, paymentTypeReadPlatformService,
                calendarReadPlatformService, configurationDomainService, calendarInstanceRepository, sqlGenerator);
    }

    @Bean
    @ConditionalOnMissingBean(CollectionSheetWritePlatformService.class)
    public CollectionSheetWritePlatformService collectionSheetWritePlatformService(LoanWritePlatformService loanWritePlatformService,
            CollectionSheetBulkRepaymentCommandFromApiJsonDeserializer bulkRepaymentCommandFromApiJsonDeserializer,
            CollectionSheetBulkDisbursalCommandFromApiJsonDeserializer bulkDisbursalCommandFromApiJsonDeserializer,
            CollectionSheetTransactionDataValidator transactionDataValidator, MeetingWritePlatformService meetingWritePlatformService,
            DepositAccountAssembler accountAssembler, DepositAccountWritePlatformService accountWritePlatformService,
            PaymentDetailAssembler paymentDetailAssembler, PaymentDetailWritePlatformService paymentDetailWritePlatformService) {
        return new CollectionSheetWritePlatformServiceJpaRepositoryImpl(loanWritePlatformService,
                bulkRepaymentCommandFromApiJsonDeserializer, bulkDisbursalCommandFromApiJsonDeserializer, transactionDataValidator,
                meetingWritePlatformService, accountAssembler, accountWritePlatformService, paymentDetailAssembler,
                paymentDetailWritePlatformService);
    }
}
