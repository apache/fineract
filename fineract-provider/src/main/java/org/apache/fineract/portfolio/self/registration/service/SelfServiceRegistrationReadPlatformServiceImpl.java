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
package org.apache.fineract.portfolio.self.registration.service;

import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SelfServiceRegistrationReadPlatformServiceImpl implements SelfServiceRegistrationReadPlatformService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public SelfServiceRegistrationReadPlatformServiceImpl(final RoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public boolean isClientExist(String accountNumber, String firstName, String lastName, String mobileNumber,
            boolean isEmailAuthenticationMode) {
        String sql = "select count(*) from m_client where account_no = ? and firstname = ? and lastname = ?";
        Object[] params = new Object[] { accountNumber, firstName, lastName };
        if (!isEmailAuthenticationMode) {
            sql = sql + " and mobile_no = ?";
            params = new Object[] { accountNumber, firstName, lastName, mobileNumber };
        }
        Integer count = this.jdbcTemplate.queryForObject(sql, params, Integer.class);
        if (count == 0) { return false; }
        return true;
    }

}
