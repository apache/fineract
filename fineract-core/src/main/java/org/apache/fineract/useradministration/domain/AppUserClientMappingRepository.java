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
package org.apache.fineract.useradministration.domain;

import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AppUserClientMappingRepository extends JpaRepository<AppUserClientMapping, Long> {

    @Modifying(clearAutomatically = true)
    @Transactional    
    @Query(value = "INSERT INTO m_selfservice_user_client_mapping (appuser_id, client_id) VALUES (?1 ,  ?2 ) ", nativeQuery = true)
    public void saveClientUserMapping(@Param("appuserId") Long appuserId, @Param("clientId") Long clientId);

    @Query("select appUserMapping from AppUserClientMapping appUserMapping where appUserMapping.client.id = :clientId")
    @Cacheable(value = "appUserClientFetchByClientId", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#clientId)")
    AppUserClientMapping fetchByClientId(@Param("clientId") Long clientId);

    @Query("select appUserMapping from AppUserClientMapping appUserMapping where appUserMapping.appUser.username = :username")
    @Cacheable(value = "appUserClientFetchByAppuserUsername", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#username)")
    AppUserClientMapping fetchByAppuserUsername(@Param("username") String username);

    @Query("select appUserMapping from AppUserClientMapping appUserMapping WHERE appUserMapping.client.externalId = :externalId AND appUserMapping.appUser.email = :email")
    Optional<AppUserClientMapping> fetchByClientExternalIdAndUserEmail(@Param("externalId") String externalId, @Param("email") String email);

    @Query("select appUserMapping from AppUserClientMapping appUserMapping where appUserMapping.appUser.id = :appUserId")    
    @Cacheable(value = "appUserClientFetchByAppUserId", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#appUserId)")
    AppUserClientMapping fetchByAppUserId(@Param("appUserId") Long appUserId);
}