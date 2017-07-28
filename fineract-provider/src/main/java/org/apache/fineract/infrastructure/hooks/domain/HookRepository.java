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
package org.apache.fineract.infrastructure.hooks.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HookRepository extends JpaRepository<Hook, Long>,
		JpaSpecificationExecutor<Hook> {

	@Query("select hook from Hook hook inner join hook.events event where event.entityName = :entityName and event.actionName = :actionName and hook.isActive = true")
	List<Hook> findAllHooksListeningToEvent(
			@Param("entityName") String entityName,
			@Param("actionName") String actionName);

	@Query("select hook from Hook hook where hook.template.id = :templateId ")
	Hook findOneByTemplateId(@Param("templateId") Long templateId);

}
