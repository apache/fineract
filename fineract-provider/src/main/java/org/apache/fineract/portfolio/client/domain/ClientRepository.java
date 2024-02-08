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
package org.apache.fineract.portfolio.client.domain;

import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.portfolio.client.domain.search.SearchingClientRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClientRepository extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client>, SearchingClientRepository {

    String FIND_CLIENT_BY_ACCOUNT_NUMBER = "select client from Client client where client.accountNumber = :accountNumber";

    @Query(FIND_CLIENT_BY_ACCOUNT_NUMBER)
    Client getClientByAccountNumber(@Param("accountNumber") String accountNumber);

    @Query("""
            SELECT client
            FROM Client client
            JOIN client.office office
            LEFT JOIN client.transferToOffice transferToOffice
            WHERE client.id = :clientId
            AND (office.hierarchy LIKE :officeHierarchy OR transferToOffice.hierarchy LIKE :transferToOfficeHierarchy)
                """)
    Client fetchByClientIdAndHierarchy(@Param("clientId") Long clientId, @Param("officeHierarchy") String officeHierarchy,
            @Param("transferToOfficeHierarchy") String transferToOfficeHierarchy);

    @Query("SELECT c.id FROM Client c WHERE c.externalId = :externalId")
    Long findIdByExternalId(@Param("externalId") ExternalId externalId);

}
