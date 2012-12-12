package org.mifosplatform.accounting.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GLClosureRepository extends JpaRepository<GLClosure, Long>, JpaSpecificationExecutor<GLClosure> {

    @Query("from GLClosure closure where closure.closingDate = (select max(closure1.closingDate) from GLClosure closure1 where closure1.office.id=:officeId)  and closure.office.id= :officeId")
    GLClosure getLatestGLClosureByBranch(@Param("officeId") Long officeId);
}
