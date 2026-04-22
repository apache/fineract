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
package org.apache.fineract.portfolio.loanorigination.domain;

import java.util.List;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanOriginatorRepository extends JpaRepository<LoanOriginator, Long>, JpaSpecificationExecutor<LoanOriginator> {

    Optional<LoanOriginator> findByExternalId(ExternalId externalId);

    boolean existsByExternalId(ExternalId externalId);

    List<LoanOriginator> findByStatus(LoanOriginatorStatus status);

    @Query("SELECT lo FROM LoanOriginator lo LEFT JOIN FETCH lo.originatorType LEFT JOIN FETCH lo.channelType")
    List<LoanOriginator> findAllWithCodeValues();

    @Query("SELECT lo FROM LoanOriginator lo LEFT JOIN FETCH lo.originatorType LEFT JOIN FETCH lo.channelType WHERE lo.id = :id")
    Optional<LoanOriginator> findByIdWithCodeValues(@Param("id") Long id);

    @Query("SELECT lo FROM LoanOriginator lo LEFT JOIN FETCH lo.originatorType LEFT JOIN FETCH lo.channelType WHERE lo.externalId = :externalId")
    Optional<LoanOriginator> findByExternalIdWithCodeValues(@Param("externalId") ExternalId externalId);
}
