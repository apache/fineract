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
package org.apache.fineract.portfolio.group.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class GroupRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private static Set<String> paths(Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream().map(v -> v.getPropertyPath().toString()).collect(Collectors.toSet());
    }

    @Test
    void createRequiresNameAndOfficeIdWhenNoCenter() {
        Set<ConstraintViolation<GroupCreateRequest>> violations = validator.validate(GroupCreateRequest.builder().build());
        assertEquals(Set.of("name", "officeId"), paths(violations));
    }

    @Test
    void createAcceptsCenterIdInsteadOfOfficeId() {
        GroupCreateRequest request = GroupCreateRequest.builder().name("g").centerId(3L).build();
        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void createRequiresActivationDateWhenActive() {
        GroupCreateRequest request = GroupCreateRequest.builder().name("g").officeId(1L).active(true).build();
        assertEquals(Set.of("activationDate"), paths(validator.validate(request)));
    }

    @Test
    void createRejectsNameOver100Chars() {
        GroupCreateRequest request = GroupCreateRequest.builder().name("x".repeat(101)).officeId(1L).build();
        assertEquals(Set.of("name"), paths(validator.validate(request)));
    }

    @Test
    void activateRequiresActivationDate() {
        assertEquals(Set.of("activationDate"), paths(validator.validate(GroupActivateRequest.builder().id(1L).build())));
    }

    @Test
    void closeRequiresDateAndReason() {
        assertEquals(Set.of("closureDate", "closureReasonId"), paths(validator.validate(GroupCloseRequest.builder().id(1L).build())));
    }

    @Test
    void associateClientsRequiresNonEmptyMembers() {
        assertEquals(Set.of("clientMembers"),
                paths(validator.validate(GroupAssociateClientsRequest.builder().id(1L).clientMembers(List.of()).build())));
    }

    @Test
    void assignRoleRequiresClientAndRole() {
        assertEquals(Set.of("clientId", "role"), paths(validator.validate(GroupAssignRoleRequest.builder().groupId(1L).build())));
    }

    @Test
    void transferRequiresDestinationAndClients() {
        assertEquals(Set.of("destinationGroupId", "clients"),
                paths(validator.validate(GroupTransferClientsRequest.builder().id(1L).build())));
    }

    @Test
    void saveCollectionSheetRequiresCalendarAndTransactionDate() {
        assertEquals(Set.of("calendarId", "transactionDate"),
                paths(validator.validate(GroupSaveCollectionSheetRequest.builder().id(1L).build())));
    }

    @Test
    void updateRequiresName() {
        assertEquals(Set.of("name"), paths(validator.validate(GroupUpdateRequest.builder().id(1L).build())));
    }
}
