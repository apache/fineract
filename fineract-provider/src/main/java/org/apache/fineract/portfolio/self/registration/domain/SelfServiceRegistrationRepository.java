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
package org.apache.fineract.portfolio.self.registration.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SelfServiceRegistrationRepository extends JpaRepository<SelfServiceRegistration, Long>,
        JpaSpecificationExecutor<SelfServiceRegistration> {

    public static final String FIND_BY_REQUEST_AND_AUTHENTICATION_TOKEN = "select request from SelfServiceRegistration request where request.id = :id and "
            + "request.authenticationToken = :authenticationToken";

    @Query(FIND_BY_REQUEST_AND_AUTHENTICATION_TOKEN)
    SelfServiceRegistration getRequestByIdAndAuthenticationToken(@Param("id") Long id,
            @Param("authenticationToken") String authenticationToken);

}
