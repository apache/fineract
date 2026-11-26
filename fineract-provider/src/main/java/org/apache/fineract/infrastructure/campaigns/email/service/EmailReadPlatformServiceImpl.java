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
package org.apache.fineract.infrastructure.campaigns.email.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.campaigns.email.data.EmailData;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailMessageEnumerations;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailMessageStatusType;
import org.apache.fineract.infrastructure.campaigns.email.exception.EmailNotFoundException;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.query.QScheduledEmailMessagesOutbound;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailReadPlatformServiceImpl implements EmailReadPlatformService {

    private final SQLQueryFactory queryFactory;
    private final QScheduledEmailMessagesOutbound emo = new QScheduledEmailMessagesOutbound("emo");
    private final NumberPath<Long> externalId = Expressions.numberPath(Long.class, emo, "external_id");

    @Override
    public Collection<EmailData> retrieveAll() {
        List<Tuple> rows = queryFactory.select(emo.id, emo.groupId, emo.clientId, emo.staffId, emo.campaignName, emo.statusEnum,
                emo.emailAddress, emo.submittedonDate, emo.emailSubject, emo.message, emo.errorMessage).from(emo).fetch();
        List<EmailData> result = new ArrayList<>(rows.size());
        for (Tuple row : rows) {
            result.add(mapRow(row));
        }
        return result;
    }

    private EmailData mapRow(Tuple row) {
        Long id = row.get(emo.id);
        Long groupId = row.get(emo.groupId);
        Long clientId = row.get(emo.clientId);
        Long staffId = row.get(emo.staffId);
        String campaignName = row.get(emo.campaignName);
        Integer statusId = row.get(emo.statusEnum);
        String emailAddress = row.get(emo.emailAddress);
        Date submittedonDate = row.get(emo.submittedonDate);
        LocalDate sentDate = submittedonDate != null ? submittedonDate.toLocalDate() : null;
        String emailSubject = row.get(emo.emailSubject);
        String message = row.get(emo.message);
        String errorMessage = row.get(emo.errorMessage);

        EnumOptionData status = statusId != null ? EmailMessageEnumerations.status(statusId) : null;

        return EmailData.instance(id, groupId, clientId, staffId, status, emailAddress, emailSubject, message, null, null, null, null, null,
                campaignName, sentDate, errorMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public EmailData retrieveOne(final Long resourceId) {
        final Tuple row = queryFactory
                .select(emo.id, emo.groupId, emo.clientId, emo.staffId, emo.campaignName, emo.statusEnum, emo.emailAddress,
                        emo.submittedonDate, emo.emailSubject, emo.message, emo.errorMessage)
                .from(emo).where(emo.id.eq(resourceId)).fetchOne();
        if (row == null) {
            throw new EmailNotFoundException(resourceId);
        }
        return mapRow(row);
    }

    @Override
    public Collection<EmailData> retrieveAllPending(final SearchParameters searchParameters) {
        return retrieveEmailByStatus(EmailMessageStatusType.PENDING.getValue(), searchParameters.getLimit());
    }

    @Override
    public Collection<EmailData> retrieveAllSent(final SearchParameters searchParameters) {
        return retrieveEmailByStatus(EmailMessageStatusType.SENT.getValue(), searchParameters.getLimit());
    }

    @Override
    public List<Long> retrieveExternalIdsOfAllSent(final Integer limit) {
        SQLQuery<Long> query = queryFactory.select(externalId).from(emo).where(emo.statusEnum.eq(EmailMessageStatusType.SENT.getValue()));
        if (limit != null && limit > 0) {
            query.limit(limit);
        }
        return query.fetch();
    }

    @Override
    public Collection<EmailData> retrieveAllDelivered(final Integer limit) {
        return retrieveEmailByStatus(EmailMessageStatusType.DELIVERED.getValue(), limit);
    }

    @Override
    public Collection<EmailData> retrieveAllFailed(final SearchParameters searchParameters) {
        return retrieveEmailByStatus(EmailMessageStatusType.FAILED.getValue(), searchParameters.getLimit());
    }

    @Override
    public Page<EmailData> retrieveEmailByStatus(final Integer limit, final Integer status, final LocalDate dateFrom,
            final LocalDate dateTo) {
        final BooleanBuilder where = new BooleanBuilder();
        if (status != null) {
            where.and(emo.statusEnum.eq(status));
        }
        if (dateFrom != null && dateTo != null) {
            where.and(emo.submittedonDate.goe(Date.valueOf(dateFrom)));
            where.and(emo.submittedonDate.loe(Date.valueOf(dateTo)));
        }

        final Long totalFilteredRecords = queryFactory.select(emo.id.count()).from(emo).where(where).fetchOne();

        final SQLQuery<Tuple> query = queryFactory.select(emo.id, emo.groupId, emo.clientId, emo.staffId, emo.campaignName, emo.statusEnum,
                emo.emailAddress, emo.submittedonDate, emo.emailSubject, emo.message, emo.errorMessage).from(emo).where(where);
        if (limit != null && limit > 0) {
            query.limit(limit);
        }

        final List<Tuple> rows = query.fetch();
        final List<EmailData> items = new ArrayList<>(rows.size());
        for (Tuple row : rows) {
            items.add(mapRow(row));
        }

        return new Page<>(items, Math.toIntExact(totalFilteredRecords != null ? totalFilteredRecords : 0));
    }

    private Collection<EmailData> retrieveEmailByStatus(final Integer status, final Integer limit) {
        final SQLQuery<Tuple> query = queryFactory.select(emo.id, emo.groupId, emo.clientId, emo.staffId, emo.campaignName, emo.statusEnum,
                emo.emailAddress, emo.submittedonDate, emo.emailSubject, emo.message, emo.errorMessage).from(emo)
                .where(emo.statusEnum.eq(status));
        if (limit != null && limit > 0) {
            query.limit(limit);
        }

        final List<Tuple> rows = query.fetch();
        final List<EmailData> result = new ArrayList<>(rows.size());
        for (Tuple row : rows) {
            result.add(mapRow(row));
        }
        return result;
    }
}
