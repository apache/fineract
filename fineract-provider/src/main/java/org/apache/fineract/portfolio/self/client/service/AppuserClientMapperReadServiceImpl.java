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
package org.apache.fineract.portfolio.self.client.service;

import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AppuserClientMapperReadServiceImpl implements
		AppuserClientMapperReadService {

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;

	@Autowired
	public AppuserClientMapperReadServiceImpl(final RoutingDataSource dataSource, final PlatformSecurityContext context) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.context = context;
	}

	@Override
	public Boolean isClientMappedToUser(Long clientId, Long appUserId) {
		return this.jdbcTemplate
				.queryForObject(
						"select case when (count(*) > 0) then true else false end "
								+ " from m_selfservice_user_client_mapping where client_id = ? and appuser_id = ?",
						new Object[] { clientId, appUserId }, Boolean.class);
	}
	
	@Override
	public void validateAppuserClientsMapping(final Long clientId) {
		AppUser user = this.context.authenticatedUser();
		if (clientId != null) {
			final boolean mappedClientId = isClientMappedToUser(clientId, user.getId());
			if (!mappedClientId) {
				throw new ClientNotFoundException(clientId);
			}
		} else
			throw new ClientNotFoundException(clientId);

	}

}
