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
package org.apache.fineract.portfolio.loanaccount.domain;

import java.math.BigDecimal;
import java.util.List;
import org.apache.fineract.portfolio.loanaccount.data.AmortizationAllocationMappingDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for LoanAmortizationAllocationMapping entity
 */
@Repository
public interface LoanAmortizationAllocationMappingRepository
        extends JpaRepository<LoanAmortizationAllocationMapping, Long>, JpaSpecificationExecutor<LoanAmortizationAllocationMapping> {

    @Query("""
                    SELECT new org.apache.fineract.portfolio.loanaccount.data.AmortizationAllocationMappingDTO(
                        laam.amortizationLoanTransactionId,
                        at.externalId,
                        laam.date,
                        laam.amortizationType,
                        laam.amount
                    ) FROM LoanAmortizationAllocationMapping laam
                        JOIN LoanTransaction at ON laam.amortizationLoanTransactionId = at.id
                    WHERE laam.baseLoanTransactionId = :baseLoanTransactionId AND laam.loanId = :loanId
                    ORDER BY laam.date, laam.amortizationLoanTransactionId
            """)
    List<AmortizationAllocationMappingDTO> findAmortizationMappingsByBaseTransactionAndLoan(
            @Param("baseLoanTransactionId") Long baseLoanTransactionId, @Param("loanId") Long loanId);

    @Query("""
                    SELECT COALESCE(SUM(
                        CASE
                            WHEN laam.amortizationType = org.apache.fineract.portfolio.loanaccount.domain.AmortizationType.AM THEN laam.amount
                            WHEN laam.amortizationType = org.apache.fineract.portfolio.loanaccount.domain.AmortizationType.AM_ADJ THEN -laam.amount
                            ELSE 0
                        END
                    ), 0)
                    FROM LoanAmortizationAllocationMapping laam
                    WHERE laam.baseLoanTransactionId = :baseLoanTransactionId AND laam.loanId = :loanId
            """)
    BigDecimal calculateAlreadyAmortizedAmount(@Param("baseLoanTransactionId") Long baseLoanTransactionId, @Param("loanId") Long loanId);
}
