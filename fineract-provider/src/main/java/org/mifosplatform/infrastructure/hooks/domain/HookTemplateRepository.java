/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.hooks.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HookTemplateRepository extends
		JpaRepository<HookTemplate, Long>,
		JpaSpecificationExecutor<HookTemplate> {

	@Query("from HookTemplate template where template.name = :name")
	HookTemplate findOne(@Param("name") String name);
}
