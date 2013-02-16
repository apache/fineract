/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.staff.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StaffRepository extends JpaRepository<Staff, Long>,
		JpaSpecificationExecutor<Staff> {
    
    public final static String FIND_BY_OFFICE_QUERY = "select s from Staff s where s.id = :id AND s.office.id = :officeId";

    /**
     * Find staff by officeid.
     */
    @Query(FIND_BY_OFFICE_QUERY)
    public Staff findByOffice(@Param("id") Long id, @Param("officeId") Long officeId);
   
    
    
}