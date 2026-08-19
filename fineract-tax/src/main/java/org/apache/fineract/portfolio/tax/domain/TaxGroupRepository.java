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

public interface TaxGroupRepository extends JpaRepository<TaxGroup, Long>, JpaSpecificationExecutor<TaxGroup> {

    @Query(value = "select count(*) from (select tax_group_id from m_charge union all select tax_group_id from m_savings_product"
            + " union all select tax_group_id from m_savings_account) links where links.tax_group_id = ?1", nativeQuery = true)
    long countLinks(Long taxGroupId);

    default boolean isLinked(final Long taxGroupId) {
        return countLinks(taxGroupId) > 0;
    }

}
