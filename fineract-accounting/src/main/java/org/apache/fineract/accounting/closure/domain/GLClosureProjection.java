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
package org.apache.fineract.accounting.closure.domain;

import static org.apache.fineract.query.QAccGlClosure.accGlClosure;
import static org.apache.fineract.query.QMOffice.mOffice;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.MappingProjection;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.Expressions;
import java.sql.Date;
import java.time.LocalDate;
import org.apache.fineract.accounting.closure.data.GLClosureData;
import org.apache.fineract.query.QMAppuser;

public final class GLClosureProjection extends MappingProjection<GLClosureData> {

    private static final DateTemplate<Date> CREATED_DATE = Expressions.dateTemplate(Date.class, "{0}", accGlClosure.createdDate);
    private static final DateTemplate<Date> LAST_UPDATED_DATE = Expressions.dateTemplate(Date.class, "{0}", accGlClosure.lastmodifiedDate);

    private final QMAppuser creatingUser;
    private final QMAppuser updatingUser;

    public GLClosureProjection(final QMAppuser creatingUser, final QMAppuser updatingUser) {
        super(GLClosureData.class, accGlClosure.id, accGlClosure.officeId, mOffice.name, accGlClosure.closingDate, accGlClosure.isDeleted,
                CREATED_DATE, LAST_UPDATED_DATE, creatingUser.id, creatingUser.username, updatingUser.id, updatingUser.username,
                accGlClosure.comments);
        this.creatingUser = creatingUser;
        this.updatingUser = updatingUser;
    }

    @Override
    protected GLClosureData map(final Tuple row) {
        return new GLClosureData(row.get(accGlClosure.id), row.get(accGlClosure.officeId), row.get(mOffice.name),
                toLocalDate(row.get(accGlClosure.closingDate)), Boolean.TRUE.equals(row.get(accGlClosure.isDeleted)),
                toLocalDate(row.get(CREATED_DATE)), toLocalDate(row.get(LAST_UPDATED_DATE)), row.get(creatingUser.id),
                row.get(creatingUser.username), row.get(updatingUser.id), row.get(updatingUser.username), row.get(accGlClosure.comments));
    }

    private static LocalDate toLocalDate(final Date date) {
        return date == null ? null : date.toLocalDate();
    }
}
