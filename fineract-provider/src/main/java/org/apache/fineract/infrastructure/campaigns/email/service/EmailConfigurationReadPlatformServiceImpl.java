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

import com.querydsl.core.Tuple;
import com.querydsl.sql.SQLQueryFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.campaigns.email.data.EmailConfigurationData;
import org.apache.fineract.infrastructure.campaigns.email.exception.EmailConfigurationNotFoundException;
import org.apache.fineract.query.QScheduledEmailConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailConfigurationReadPlatformServiceImpl implements EmailConfigurationReadPlatformService {

    private final SQLQueryFactory queryFactory;
    private final QScheduledEmailConfiguration cnf = new QScheduledEmailConfiguration("cnf");

    @Override
    public Collection<EmailConfigurationData> retrieveAll() {
        List<Tuple> rows = queryFactory.select(cnf.id, cnf.name, cnf.value).from(cnf).fetch();
        List<EmailConfigurationData> emailConfigurationData = new ArrayList<>(rows.size());
        for (Tuple row : rows) {
            Integer id = row.get(cnf.id);
            String name = row.get(cnf.name);
            String value = row.get(cnf.value);
            emailConfigurationData.add(EmailConfigurationData.instance(id != null ? id.longValue() : null, name, value));
        }
        return emailConfigurationData;
    }

    @Override
    public EmailConfigurationData retrieveOne(String name) {
        Tuple row = queryFactory.select(cnf.id, cnf.name, cnf.value).from(cnf).where(cnf.name.eq(name)).fetchOne();

        if (row == null) {
            throw new EmailConfigurationNotFoundException(name);
        }

        Integer id = row.get(cnf.id);
        String configName = row.get(cnf.name);
        String value = row.get(cnf.value);
        return EmailConfigurationData.instance(id != null ? id.longValue() : null, configName, value);
    }

}
