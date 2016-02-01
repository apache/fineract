/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.self.client.service;

import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AppuserClientMapperReadServiceImpl implements
		AppuserClientMapperReadService {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public AppuserClientMapperReadServiceImpl(final RoutingDataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public Boolean isClientMappedToUser(Long clientId, Long appUserId) {
		return this.jdbcTemplate
				.queryForObject(
						"select case when (count(*) > 0) then true else false end "
								+ " from m_selfservice_user_client_mapping where client_id = ? and appuser_id = ?",
						new Object[] { clientId, appUserId }, Boolean.class);
	}

}
