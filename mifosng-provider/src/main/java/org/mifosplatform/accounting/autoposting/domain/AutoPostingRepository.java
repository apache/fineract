/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.autoposting.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AutoPostingRepository extends JpaRepository<AutoPosting, Long>, JpaSpecificationExecutor<AutoPosting> {

    @Query("from GLClosure closure where closure.closingDate = (select max(closure1.closingDate) from GLClosure closure1 where closure1.office.id=:officeId)  and closure.office.id= :officeId")
    AutoPosting getLatestGLClosureByBranch(@Param("officeId") Long officeId);
}
