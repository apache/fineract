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
package org.apache.fineract.portfolio.loanaccount.rescheduleloan.domain;

import java.util.List;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.portfolio.loanaccount.data.LoanTermVariationsData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTermVariations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanTermVariationsRepository
        extends JpaRepository<LoanTermVariations, Long>, JpaSpecificationExecutor<LoanTermVariations> {

    @Query("""
            select new org.apache.fineract.portfolio.loanaccount.data.LoanTermVariationsData(
                ltv.id, ltv.termType, ltv.termApplicableFrom, ltv.decimalValue, ltv.dateValue, ltv.isSpecificToInstallment
            )
            from LoanTermVariations ltv
            where ltv.loan.id = :loanId
            order by ltv.termApplicableFrom
            """)
    List<LoanTermVariationsData> findLoanTermVariationsByLoanId(@Param("loanId") long loanId);

    @Query("""
            select new org.apache.fineract.portfolio.loanaccount.data.LoanTermVariationsData(
                ltv.id, ltv.termType, ltv.termApplicableFrom, ltv.decimalValue, ltv.dateValue, ltv.isSpecificToInstallment
            )
            from LoanTermVariations ltv
            where ltv.loan.id = :loanId and ltv.termType = :termType
            order by ltv.termApplicableFrom
            """)
    List<LoanTermVariationsData> findLoanTermVariationsByLoanIdAndTermType(@Param("loanId") long loanId, @Param("termType") int termType);

    @Query("""
            select new org.apache.fineract.portfolio.loanaccount.data.LoanTermVariationsData(
                ltv.id, ltv.termType, ltv.termApplicableFrom, ltv.decimalValue, ltv.dateValue, ltv.isSpecificToInstallment
            )
            from LoanTermVariations ltv
            where ltv.loan.externalId = :loanExternalId and ltv.termType = :termType
            order by ltv.termApplicableFrom
            """)
    List<LoanTermVariationsData> findLoanTermVariationsByExternalLoanIdAndTermType(@Param("loanExternalId") ExternalId loanExternalId,
            @Param("termType") int termType);
}
