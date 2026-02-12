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
package org.apache.fineract.portfolio.workingcapitalloanproduct.repository;

import java.util.List;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkingCapitalLoanProductRepository
        extends JpaRepository<WorkingCapitalLoanProduct, Long>, JpaSpecificationExecutor<WorkingCapitalLoanProduct> {

    boolean existsByExternalId(ExternalId externalId);

    boolean existsByName(String name);

    boolean existsByShortName(String shortName);

    @Query("""
            SELECT wclp FROM WorkingCapitalLoanProduct wclp
            LEFT JOIN FETCH wclp.fund
            LEFT JOIN FETCH wclp.delinquencyBucket
            ORDER BY wclp.name
            """)
    List<WorkingCapitalLoanProduct> findAllWithFund();

    @Query("""
            SELECT wclp FROM WorkingCapitalLoanProduct wclp
            LEFT JOIN FETCH wclp.fund
            LEFT JOIN FETCH wclp.delinquencyBucket
            LEFT JOIN FETCH wclp.paymentAllocationRules
            LEFT JOIN FETCH wclp.configurableAttributes
            WHERE wclp.id = :id
            """)
    Optional<WorkingCapitalLoanProduct> findByIdWithDetails(@Param("id") Long id);

    @Query("""
            SELECT wclp FROM WorkingCapitalLoanProduct wclp
            LEFT JOIN FETCH wclp.fund
            LEFT JOIN FETCH wclp.delinquencyBucket
            LEFT JOIN FETCH wclp.paymentAllocationRules
            LEFT JOIN FETCH wclp.configurableAttributes
            WHERE wclp.externalId = :externalId
            """)
    Optional<WorkingCapitalLoanProduct> findByExternalIdWithDetails(@Param("externalId") ExternalId externalId);

    // TODO: Check if product is used in any loans (for deletion validation)
    // This will be implemented when Working Capital Loan entity is created
    // @Query("SELECT CASE WHEN COUNT(l)>0 THEN TRUE ELSE FALSE END FROM WorkingCapitalLoan l WHERE l.wcpProduct.id =
    // :productId")
    // boolean isProductInUse(@Param("productId") Long productId);
}
