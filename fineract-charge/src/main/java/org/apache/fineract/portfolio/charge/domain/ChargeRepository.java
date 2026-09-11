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
package org.apache.fineract.portfolio.charge.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChargeRepository extends JpaRepository<Charge, Long>, JpaSpecificationExecutor<Charge> {

    @Query("select lc.id from WorkingCapitalLoanCharge lc where lc.charge.id = :chargeId and lc.active = true")
    Optional<Long> isAnyWorkingCapitalLoansAssociateWithThisCharge(@Param("chargeId") Long chargeId);

    /**
     * Checks if any Charge exists that references a TaxGroup containing the specified TaxComponent. This is used to
     * determine if a TaxComponent is "in use" (linked to charges via tax groups).
     *
     * @param taxComponentId
     *            the ID of the TaxComponent to check
     * @return true if at least one Charge exists with a TaxGroup that contains this TaxComponent, false otherwise
     */
    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM m_charge c
                INNER JOIN m_tax_group_mappings tgm
                    ON tgm.tax_group_id = c.tax_group_id
                WHERE c.tax_group_id IS NOT NULL
                AND tgm.tax_component_id = ?1
            )
            """, nativeQuery = true)
    boolean existsByTaxGroupContainingTaxComponent(Long taxComponentId);
}
