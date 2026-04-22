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
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanOriginatorMappingRepository
        extends JpaRepository<LoanOriginatorMapping, Long>, JpaSpecificationExecutor<LoanOriginatorMapping> {

    List<LoanOriginatorMapping> findByLoanId(Long loanId);

    @Query("""
            SELECT m FROM LoanOriginatorMapping m
            JOIN FETCH m.originator o
            LEFT JOIN FETCH o.originatorType
            LEFT JOIN FETCH o.channelType
            WHERE m.loanId = :loanId
            """)
    List<LoanOriginatorMapping> findByLoanIdWithOriginatorDetails(@Param("loanId") Long loanId);

    boolean existsByLoanId(Long loanId);

    boolean existsByOriginatorId(Long originatorId);

    List<LoanOriginatorMapping> findByOriginatorId(Long originatorId);

    Optional<LoanOriginatorMapping> findByLoanIdAndOriginatorId(Long loanId, Long originatorId);

    boolean existsByLoanIdAndOriginatorId(Long loanId, Long originatorId);

    void deleteByLoanIdAndOriginatorId(Long loanId, Long originatorId);

    @org.springframework.data.jpa.repository.Query("""
            SELECT m FROM LoanOriginatorMapping m
            JOIN FETCH m.originator o
            LEFT JOIN FETCH o.originatorType
            LEFT JOIN FETCH o.channelType
            WHERE m.loanId = :loanId
            """)
    List<LoanOriginatorMapping> findByLoanIdWithOriginator(@org.springframework.data.repository.query.Param("loanId") Long loanId);
}
