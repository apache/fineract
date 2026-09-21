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
package org.apache.fineract.portfolio.group.mapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.apache.fineract.portfolio.group.data.GroupActivateRequest;
import org.apache.fineract.portfolio.group.data.GroupCommandRequest;
import org.apache.fineract.portfolio.group.data.GroupTransferClientItem;
import org.apache.fineract.portfolio.group.data.GroupTransferClientsRequest;
import org.apache.fineract.portfolio.group.data.GroupUnassignRoleRequest;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class GroupCommandRequestMapperTest {

    private final GroupCommandRequestMapper mapper = Mappers.getMapper(GroupCommandRequestMapper.class);

    @Test
    void activateTakesIdFromPathAndDateFieldsFromBody() {
        GroupCommandRequest body = GroupCommandRequest.builder().activationDate("01 January 2024").dateFormat("dd MMMM yyyy").locale("en")
                .staffId(99L).build();
        GroupActivateRequest out = mapper.toActivate(body, 5L);
        assertEquals(5L, out.getId());
        assertEquals("01 January 2024", out.getActivationDate());
        assertEquals("dd MMMM yyyy", out.getDateFormat());
        assertEquals("en", out.getLocale());
    }

    @Test
    void transferCopiesNestedClients() {
        GroupCommandRequest body = GroupCommandRequest.builder().destinationGroupId(8L)
                .clients(List.of(GroupTransferClientItem.builder().id(3L).build())).transferActiveLoans(false).build();
        GroupTransferClientsRequest out = mapper.toTransferClients(body, 5L);
        assertEquals(5L, out.getId());
        assertEquals(8L, out.getDestinationGroupId());
        assertEquals(3L, out.getClients().get(0).getId());
        assertEquals(false, out.getTransferActiveLoans());
        assertNull(out.getStaffId());
    }

    @Test
    void unassignRoleTakesBothIdsFromQuery() {
        GroupUnassignRoleRequest out = mapper.toUnassignRole(GroupCommandRequest.builder().build(), 5L, 12L);
        assertEquals(5L, out.getGroupId());
        assertEquals(12L, out.getRoleId());
    }
}
