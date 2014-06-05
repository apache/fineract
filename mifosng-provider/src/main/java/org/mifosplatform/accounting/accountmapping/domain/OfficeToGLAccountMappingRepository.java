/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.accountmapping.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OfficeToGLAccountMappingRepository extends JpaRepository<OfficeToGLAccountMapping, Long>,
        JpaSpecificationExecutor<OfficeToGLAccountMapping> {

    @Query("from OfficeToGLAccountMapping otga where otga.office.id = :officeId and otga.financialAccountType = :financialAccountType")
    OfficeToGLAccountMapping findByOfficeAndFinancialAccountType(@Param("officeId")Long officeId,@Param("financialAccountType") int financialAccountType);

    @Query("from OfficeToGLAccountMapping otga where otga.office.id =:officeId")
    List<OfficeToGLAccountMapping> findByOffice(@Param("officeId")Long officeId);
}
