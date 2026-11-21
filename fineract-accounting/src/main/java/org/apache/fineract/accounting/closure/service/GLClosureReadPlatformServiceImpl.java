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
package org.apache.fineract.accounting.closure.service;

import static org.apache.fineract.query.QAccGlClosure.accGlClosure;
import static org.apache.fineract.query.QMOffice.mOffice;

import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.closure.data.GLClosureData;
import org.apache.fineract.accounting.closure.domain.GLClosureProjection;
import org.apache.fineract.accounting.closure.exception.GLClosureNotFoundException;
import org.apache.fineract.query.QMAppuser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GLClosureReadPlatformServiceImpl implements GLClosureReadPlatformService {

    private final SQLQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = true)
    public List<GLClosureData> retrieveAllGLClosures(final Long officeId) {
        final SQLQuery<GLClosureData> query = baseQuery().where(accGlClosure.isDeleted.isFalse());
        if (officeId != null && officeId != 0) {
            query.where(accGlClosure.officeId.eq(officeId));
        }
        return query.orderBy(accGlClosure.closingDate.desc()).fetch();
    }

    @Override
    @Transactional(readOnly = true)
    public GLClosureData retrieveGLClosureById(final long glClosureId) {
        final GLClosureData closure = baseQuery().where(accGlClosure.id.eq(glClosureId)).fetchOne();
        return Optional.ofNullable(closure).orElseThrow(() -> new GLClosureNotFoundException(glClosureId));
    }

    private SQLQuery<GLClosureData> baseQuery() {
        final QMAppuser creatingUser = new QMAppuser("creatingUser");
        final QMAppuser updatingUser = new QMAppuser("updatingUser");
        return queryFactory.select(new GLClosureProjection(creatingUser, updatingUser)).from(accGlClosure).join(mOffice)
                .on(mOffice.id.eq(accGlClosure.officeId)).join(creatingUser).on(creatingUser.id.eq(accGlClosure.createdbyId))
                .join(updatingUser).on(updatingUser.id.eq(accGlClosure.lastmodifiedbyId));
    }

}
