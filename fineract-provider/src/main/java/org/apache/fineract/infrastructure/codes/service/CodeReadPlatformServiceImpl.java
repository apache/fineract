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
package org.apache.fineract.infrastructure.codes.service;

import static org.apache.fineract.query.QMCode.mCode;

import com.querydsl.core.Tuple;
import com.querydsl.sql.SQLQueryFactory;
import java.util.Collection;
import org.apache.fineract.infrastructure.codes.data.CodeData;
import org.apache.fineract.infrastructure.codes.exception.CodeNotFoundException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CodeReadPlatformServiceImpl implements CodeReadPlatformService {

    private final PlatformSecurityContext context;
    private final SQLQueryFactory queryFactory;

    @Autowired
    public CodeReadPlatformServiceImpl(final PlatformSecurityContext context, final SQLQueryFactory queryFactory) {
        this.context = context;
        this.queryFactory = queryFactory;
    }

    @Override
    @Cacheable(value = "codes", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('CD')")
    public Collection<CodeData> retrieveAllCodes() {
        this.context.authenticatedUser();

        return this.queryFactory.select(mCode.id, mCode.codeName, mCode.isSystemDefined) //
                .from(mCode) //
                .orderBy(mCode.codeName.asc()) //
                .fetch() //
                .stream() //
                .map(row -> CodeData.instance(row.get(mCode.id).longValue(), row.get(mCode.codeName),
                        Boolean.TRUE.equals(row.get(mCode.isSystemDefined)))) //
                .toList();
    }

    @Override
    public CodeData retrieveCode(final Long codeId) {
        this.context.authenticatedUser();

        final Tuple row = this.queryFactory.select(mCode.id, mCode.codeName, mCode.isSystemDefined) //
                .from(mCode) //
                .where(mCode.id.eq(codeId.intValue())) //
                .fetchOne();

        if (row == null) {
            throw new CodeNotFoundException(codeId);
        }

        return CodeData.instance(row.get(mCode.id).longValue(), row.get(mCode.codeName),
                Boolean.TRUE.equals(row.get(mCode.isSystemDefined)));
    }

    @Override
    public CodeData retrieveCode(final String codeName) {
        this.context.authenticatedUser();
        final Tuple row = this.queryFactory.select(mCode.id, mCode.codeName, mCode.isSystemDefined)//
                .from(mCode)//
                .where(mCode.codeName.eq(codeName))//
                .fetchOne();
        if (row == null) {
            throw new CodeNotFoundException(codeName);
        }
        return CodeData.instance(row.get(mCode.id).longValue(), row.get(mCode.codeName),
                Boolean.TRUE.equals(row.get(mCode.isSystemDefined)));
    }
}
