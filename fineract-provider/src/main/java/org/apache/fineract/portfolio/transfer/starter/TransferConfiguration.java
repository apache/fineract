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
package org.apache.fineract.portfolio.transfer.starter;

import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstanceRepository;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.ClientTransferDetailsRepositoryWrapper;
import org.apache.fineract.portfolio.group.domain.GroupRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.service.LoanWritePlatformService;
import org.apache.fineract.portfolio.note.service.NoteWritePlatformService;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.service.SavingsAccountWritePlatformService;
import org.apache.fineract.portfolio.transfer.data.TransfersDataValidator;
import org.apache.fineract.portfolio.transfer.service.TransferWritePlatformService;
import org.apache.fineract.portfolio.transfer.service.TransferWritePlatformServiceJpaRepositoryImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TransferConfiguration {

    @Bean
    @ConditionalOnMissingBean(TransferWritePlatformService.class)
    public TransferWritePlatformService transferWritePlatformService(ClientRepositoryWrapper clientRepositoryWrapper,
            OfficeRepositoryWrapper officeRepository, CalendarInstanceRepository calendarInstanceRepository,
            LoanWritePlatformService loanWritePlatformService, GroupRepositoryWrapper groupRepository,
            LoanRepositoryWrapper loanRepositoryWrapper, TransfersDataValidator transfersDataValidator,
            NoteWritePlatformService noteWritePlatformService, StaffRepositoryWrapper staffRepositoryWrapper,
            SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper,
            SavingsAccountWritePlatformService savingsAccountWritePlatformService,
            ClientTransferDetailsRepositoryWrapper clientTransferDetailsRepositoryWrapper, PlatformSecurityContext context) {
        return new TransferWritePlatformServiceJpaRepositoryImpl(clientRepositoryWrapper, officeRepository, calendarInstanceRepository,
                groupRepository, loanWritePlatformService, savingsAccountWritePlatformService, loanRepositoryWrapper,
                savingsAccountRepositoryWrapper, transfersDataValidator, noteWritePlatformService, staffRepositoryWrapper,
                clientTransferDetailsRepositoryWrapper, context);
    }
}
