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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExternalAssetOwnerTransferRepository
        extends JpaRepository<ExternalAssetOwnerTransfer, Long>, JpaSpecificationExecutor<ExternalAssetOwnerTransfer> {

    Page<ExternalAssetOwnerTransfer> findAllByLoanId(Long loanId, PageRequest pageable);

    Page<ExternalAssetOwnerTransfer> findAllByExternalLoanId(ExternalId externalLoanId, PageRequest pageable);

    Page<ExternalAssetOwnerTransfer> findAllByExternalId(ExternalId externalId, PageRequest pageable);

    @Query("select e from ExternalAssetOwnerTransfer e where e.loanId = :loanId and e.id = (select max(ex.id) from ExternalAssetOwnerTransfer ex where ex.loanId = :loanId)")
    Optional<ExternalAssetOwnerTransfer> findLatestByLoanId(@Param("loanId") Long loanId);

    @Query("select m.ownerTransfer from ExternalAssetOwnerTransferLoanMapping m inner join fetch m.ownerTransfer o where m.loanId = :loanId")
    Optional<ExternalAssetOwnerTransfer> findActiveByLoanId(@Param("loanId") Long loanId);

    @Query("select m.ownerTransfer.owner from ExternalAssetOwnerTransferLoanMapping m where m.loanId = :loanId")
    Optional<ExternalAssetOwner> findActiveOwnerByLoanId(@Param("loanId") Long loanId);

    @Query("SELECT t FROM ExternalAssetOwnerTransfer t WHERE t.loanId = :loanId AND t.effectiveDateTo > :effectiveDate order by t.id desc")
    List<ExternalAssetOwnerTransfer> findEffectiveTransfersOrderByIdDesc(@Param("loanId") Long loanId,
            @Param("effectiveDate") LocalDate effectiveDate);

    Optional<ExternalAssetOwnerTransfer> findFirstByExternalIdOrderByIdAsc(ExternalId externalTransferId);

    @Query("select max(e.id) from ExternalAssetOwnerTransfer e where e.externalId = :externalTransferId")
    Optional<Long> findLastByExternalIdOrderByIdDesc(@Param("externalTransferId") ExternalId externalTransferId);
}
