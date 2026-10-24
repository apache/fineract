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

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {

    @Query("SELECT COUNT(a) FROM AppUser a JOIN a.roles r WHERE r.id = :roleId AND a.deleted = false")
    Integer getCountOfRolesAssociatedWithUsers(@Param("roleId") Long roleId);

    @Query("SELECT role FROM Role role WHERE LOWER(role.name) = LOWER(:name)")
    Role getRoleByName(@Param("name") String name);

    // Lets a caller unassign a permission from every role holding it before deleting the permission itself -
    // m_role_permission has a FK onto m_permission that a plain permissionRepository.delete(...) doesn't clean
    // up, since JPA only maintains this @ManyToMany join table from the Role (owning) side.
    @Query("SELECT role FROM Role role JOIN role.permissions p WHERE p.id = :permissionId")
    List<Role> findByPermissionId(@Param("permissionId") Long permissionId);

}
