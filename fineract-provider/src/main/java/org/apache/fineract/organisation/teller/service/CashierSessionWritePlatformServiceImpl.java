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
package org.apache.fineract.organisation.teller.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.teller.domain.Cashier;
import org.apache.fineract.organisation.teller.domain.CashierRepository;
import org.apache.fineract.organisation.teller.domain.CashierSession;
import org.apache.fineract.organisation.teller.domain.CashierSessionRepository;
import org.apache.fineract.organisation.teller.domain.CashierSessionStatus;
import org.apache.fineract.organisation.teller.domain.Teller;
import org.apache.fineract.organisation.teller.domain.TellerRepositoryWrapper;
import org.apache.fineract.organisation.teller.exception.CashierNotFoundException;
import org.apache.fineract.organisation.teller.exception.CashierSessionAlreadyOpenException;
import org.apache.fineract.organisation.teller.exception.CashierSessionNotFoundException;
import org.apache.fineract.organisation.teller.exception.CashierSessionUnsettledPriorDayException;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class CashierSessionWritePlatformServiceImpl implements CashierSessionWritePlatformService {

    private final PlatformSecurityContext context;
    private final CashierSessionRepository cashierSessionRepository;
    private final CashierRepository cashierRepository;
    private final TellerRepositoryWrapper tellerRepositoryWrapper;

    @Override
    @Transactional
    public CommandProcessingResult openSession(final Long tellerId, final Long cashierId, final String currencyCode) {
        final AppUser currentUser = context.authenticatedUser();

        final Cashier cashier = cashierRepository.findById(cashierId)
                .orElseThrow(() -> new CashierNotFoundException(cashierId));
        final Teller teller = tellerRepositoryWrapper.findOneWithNotFoundDetection(tellerId);

        final LocalDate today = DateUtils.getBusinessLocalDate();

        // Business rule: one OPEN session per cashier per teller per day
        cashierSessionRepository.findOpenSession(cashierId, tellerId, today).ifPresent(s -> {
            throw new CashierSessionAlreadyOpenException(cashierId, tellerId);
        });

        // Business rule: cannot open if prior day has unsettled session
        final List<CashierSession> unsettled = cashierSessionRepository.findUnsettledPriorSessions(currentUser.getId(), today);
        if (!unsettled.isEmpty()) {
            throw new CashierSessionUnsettledPriorDayException();
        }

        final LocalDateTime now = LocalDateTime.now();
        final CashierSession session = new CashierSession()
                .setCashier(cashier)
                .setTeller(teller)
                .setUserId(currentUser.getId())
                .setOffice(teller.getOffice())
                .setSessionDate(today)
                .setOpenedAt(now)
                .setStatus(CashierSessionStatus.OPEN)
                .setCurrencyCode(currencyCode != null ? currencyCode : "")
                .setCreatedBy(currentUser.getId())
                .setCreatedDate(now);

        cashierSessionRepository.save(session);

        return new CommandProcessingResultBuilder()
                .withEntityId(session.getId())
                .withOfficeId(teller.getOffice().getId())
                .build();
    }

    @Override
    @Transactional
    public CommandProcessingResult closeSession(final Long sessionId) {
        context.authenticatedUser();

        final CashierSession session = cashierSessionRepository.findById(sessionId)
                .orElseThrow(() -> new CashierSessionNotFoundException(sessionId));

        session.setStatus(CashierSessionStatus.CLOSED);
        session.setClosedAt(LocalDateTime.now());

        cashierSessionRepository.save(session);

        return new CommandProcessingResultBuilder()
                .withEntityId(sessionId)
                .build();
    }
}
