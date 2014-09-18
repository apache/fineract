/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.hooks.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HookRepository extends JpaRepository<Hook, Long>,
		JpaSpecificationExecutor<Hook> {

	@Query("select hook from Hook hook inner join hook.events event where event.entityName = :entityName and event.actionName = :actionName and hook.isActive = 1")
	List<Hook> findAllHooksListeningToEvent(
			@Param("entityName") String entityName,
			@Param("actionName") String actionName);

	@Query("from Hook hook where hook.template.id = :templateId ")
	Hook findOneByTemplateId(@Param("templateId") Long templateId);

}
