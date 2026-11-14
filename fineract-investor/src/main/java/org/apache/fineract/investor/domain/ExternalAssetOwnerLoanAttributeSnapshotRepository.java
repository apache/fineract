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
package org.apache.fineract.investor.domain;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.apache.fineract.investor.data.ExternalAssetOwnerLoanAttributeValueData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExternalAssetOwnerLoanAttributeSnapshotRepository extends JpaRepository<ExternalAssetOwnerLoanAttributeSnapshot, Long> {

    boolean existsByLoanId(Long loanId);

    Optional<ExternalAssetOwnerLoanAttributeSnapshot> findByLoanId(Long loanId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT snapshot FROM ExternalAssetOwnerLoanAttributeSnapshot snapshot WHERE snapshot.loanId = :loanId")
    Optional<ExternalAssetOwnerLoanAttributeSnapshot> findByLoanIdForUpdate(@Param("loanId") Long loanId);

    /**
     * Empty when the loan has no loan-level copy; otherwise one row whose value is null when the copy lacks the key.
     */
    @Query("SELECT new org.apache.fineract.investor.data.ExternalAssetOwnerLoanAttributeValueData(snapshot.id, attribute.attributeValue)"
            + " FROM ExternalAssetOwnerLoanAttributeSnapshot snapshot"
            + " LEFT JOIN snapshot.attributes attribute ON attribute.attributeKey = :attributeKey WHERE snapshot.loanId = :loanId")
    Optional<ExternalAssetOwnerLoanAttributeValueData> findLoanLevelAttributeValue(@Param("loanId") Long loanId,
            @Param("attributeKey") String attributeKey);
}
