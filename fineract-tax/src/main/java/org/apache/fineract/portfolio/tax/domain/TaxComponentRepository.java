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
package org.apache.fineract.portfolio.tax.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface TaxComponentRepository extends JpaRepository<TaxComponent, Long>, JpaSpecificationExecutor<TaxComponent> {

    @Query(value = "select case when exists (select 1 from m_tax_group_mappings tgm where tgm.tax_component_id = ?1"
            + " and (exists (select 1 from m_charge c where c.tax_group_id = tgm.tax_group_id)"
            + " or exists (select 1 from m_savings_product sp where sp.tax_group_id = tgm.tax_group_id)"
            + " or exists (select 1 from m_savings_account sa where sa.tax_group_id = tgm.tax_group_id)))"
            + " or exists (select 1 from m_loan_charge_tax_details lctd where lctd.tax_component_id = ?1)"
            + " or exists (select 1 from m_savings_account_transaction_tax_details sattd where sattd.tax_component_id = ?1)"
            + " then 1 else 0 end", nativeQuery = true)
    int linkExists(Long taxComponentId);

    default boolean isLinked(final Long taxComponentId) {
        return linkExists(taxComponentId) > 0;
    }

}
