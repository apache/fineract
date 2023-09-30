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
package org.apache.fineract.cob.domain;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CustomLoanAccountLockRepositoryImpl implements CustomLoanAccountLockRepository {

    private final EntityManager entityManager;
    private final DatabaseSpecificSQLGenerator databaseSpecificSQLGenerator;

    @Override
    public void updateLoanFromAccountLocks() {
        String sql = "UPDATE m_loan SET last_closed_business_date = (select "
                + databaseSpecificSQLGenerator.subDate("lck.lock_placed_on_cob_business_date", "1", "DAY")
                + """
                                                             from m_loan_account_locks lck
                                                             where lck.loan_id = id
                                                               and lck.lock_placed_on_cob_business_date is not null
                                                               and lck.error is not null
                                                               and lck.lock_owner in ('LOAN_COB_CHUNK_PROCESSING','LOAN_INLINE_COB_PROCESSING'))
                        where last_closed_business_date is null and exists  (select lck.loan_id
                                      from m_loan_account_locks lck  where lck.loan_id = id
                                        and lck.lock_placed_on_cob_business_date is not null and lck.error is not null
                                        and lck.lock_owner in ('LOAN_COB_CHUNK_PROCESSING','LOAN_INLINE_COB_PROCESSING'))
                            """;

        entityManager.createNativeQuery(sql).executeUpdate();
        entityManager.flush();
    }
}
