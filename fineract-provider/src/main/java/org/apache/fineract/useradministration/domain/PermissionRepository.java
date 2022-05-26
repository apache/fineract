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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    // It's important to use the same case for equality check because there are cases when the codes are not capitalized
    // the same way. (UNDOTRANSACTION_CLIENT vs UNDOTRANSACTION_client)
    // Also, trimming leading and trailing spaces is critical ("CREATE_STANDINGINSTRUCTION" vs
    // "CREATE_STANDINGINSTRUCTION ").
    @Query("SELECT p FROM Permission p WHERE LOWER(TRIM(BOTH FROM p.code)) = LOWER(TRIM(BOTH FROM ?1))")
    Permission findOneByCode(String code);
}
