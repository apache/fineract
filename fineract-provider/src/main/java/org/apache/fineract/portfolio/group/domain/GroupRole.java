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
package org.apache.fineract.portfolio.group.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.group.api.GroupingTypesApiConstants;

@Entity
@Table(name = "m_group_roles")
public class GroupRole extends AbstractPersistableCustom<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_cv_id")
    private CodeValue role;

    public GroupRole() {
        // TODO Auto-generated constructor stub
    }

    public static final GroupRole createGroupRole(final Group group, final Client client, final CodeValue role) {
        return new GroupRole(group, client, role);
    }

    public GroupRole(final Group group, final Client client, final CodeValue role) {
        this.group = group;
        this.client = client;
        this.role = role;
    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(2);

        if (command.isChangeInLongParameterNamed(GroupingTypesApiConstants.clientIdParamName, this.client.getId())) {
            final Long newValue = command.longValueOfParameterNamed(GroupingTypesApiConstants.clientIdParamName);
            actualChanges.put(GroupingTypesApiConstants.clientIdParamName, newValue);
        }

        if (command.isChangeInLongParameterNamed(GroupingTypesApiConstants.roleParamName, this.role.getId())) {
            final Long newValue = command.longValueOfParameterNamed(GroupingTypesApiConstants.roleParamName);
            actualChanges.put(GroupingTypesApiConstants.roleParamName, newValue);
        }

        return actualChanges;
    }

    public void updateRole(final CodeValue role) {
        this.role = role;
    }

    public void updateClient(final Client client) {
        this.client = client;
    }

}
