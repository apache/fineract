/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.self.loanaccount.service;

import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AppuserLoansMapperReadServiceImpl implements
		AppuserLoansMapperReadService {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public AppuserLoansMapperReadServiceImpl(final RoutingDataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public Boolean isLoanMappedToUser(Long loanId, Long appUserId) {
		return this.jdbcTemplate
				.queryForObject(
						"select case when (count(*) > 0) then true else false end "
								+ " from m_selfservice_user_client_mapping as m "
								+ " left join m_loan as l on l.client_id = m.client_id "
								+ " where l.id = ? and m.appuser_id = ? ",
						new Object[] { loanId, appUserId }, Boolean.class);
	}

}
