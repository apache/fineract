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

public interface HookConfigurationRepository extends
		JpaRepository<HookConfiguration, Long>,
		JpaSpecificationExecutor<HookConfiguration> {

	@Query("select config.fieldValue from HookConfiguration config where config.hook.id = :hookId and config.fieldName = :fieldName")
	String findOneByHookIdAndFieldName(@Param("hookId") Long hookId,
			@Param("fieldName") String fieldName);

}
