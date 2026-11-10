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
package org.apache.fineract.accounting.glaccount.domain;

import static org.apache.fineract.query.QAccGlAccount.accGlAccount;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.MappingProjection;
import java.io.Serial;
import org.apache.fineract.accounting.glaccount.data.GLAccountDataForLookup;

public final class GLAccountLookupProjection extends MappingProjection<GLAccountDataForLookup> {

    @Serial
    private static final long serialVersionUID = 1L;

    public GLAccountLookupProjection() {
        super(GLAccountDataForLookup.class, accGlAccount.id, accGlAccount.name, accGlAccount.glCode);
    }

    @Override
    protected GLAccountDataForLookup map(final Tuple row) {
        return new GLAccountDataForLookup().setId(row.get(accGlAccount.id)).setName(row.get(accGlAccount.name))
                .setGlCode(row.get(accGlAccount.glCode));
    }
}
