/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.useradministration.domain;

import org.mifosplatform.infrastructure.security.domain.PlatformUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AppUserPreviewPasswordRepository extends JpaRepository<AppUserPreviewPassword, Long>, JpaSpecificationExecutor<AppUserPreviewPassword> {

    // no behaviour added
   // @Query("SELECT upp FROM AppUserPreviewPassword upp WHERE upp.userId = :userId order by upp.removalDate ASC ")
    public List<AppUserPreviewPassword> findByUserId(Long userId,Pageable pageable);
}
