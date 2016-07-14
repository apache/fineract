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
package org.apache.fineract.organisation.staff.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StaffRepository extends JpaRepository<Staff, Long>, JpaSpecificationExecutor<Staff> {

    public final static String FIND_BY_OFFICE_QUERY = "select s from Staff s where s.id = :id AND s.office.id = :officeId";

    public final static String IS_ANY_ACTIVE_ENTITY_ASSOCIATED_WITH_STAFF_QUERY = "select " +
            "case when (sum(total) > 0) then true else false end " +
            "from (" +
            "(select count(mg.id) as total " +
            "from m_staff ms " +
            "join m_group mg on mg.staff_id = ms.id " +
            "where ms.id = :staffId " +
            "and mg.status_enum in (100,300)) " +
            "union all " +
            "(select count(ml.id) as total " +
            "from m_loan ml " +
            "join m_staff ms on ml.loan_officer_id = ms.id " +
            "where ms.id = :staffId " +
            "and ml.loan_status_id in (100,200,300,700)) " +
            "union all " +
            "(select count(msa.id) as total " +
            "from m_savings_account msa " +
            "join m_staff ms on msa.field_officer_id = ms.id " +
            "where ms.id = :staffId and " +
            "msa.status_enum in (100,300))) as t1 ";
    
    /**
     * Find staff by officeid.
     */
    @Query(FIND_BY_OFFICE_QUERY)
    public Staff findByOffice(@Param("id") Long id, @Param("officeId") Long officeId);

    /**
     * Query finds if a staff is attached to active, pending group or loans or savings
     */
    @Query(value=IS_ANY_ACTIVE_ENTITY_ASSOCIATED_WITH_STAFF_QUERY, nativeQuery = true)
    public boolean isAnyActiveEntityAssociatedWithStaff(@Param("staffId") Long staffId);
}
