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

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class CenterRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private static Set<String> paths(Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream().map(v -> v.getPropertyPath().toString()).collect(Collectors.toSet());
    }

    @Test
    void createRequiresNameAndOfficeId() {
        assertEquals(Set.of("name", "officeId"), paths(validator.validate(CenterCreateRequest.builder().build())));
    }

    @Test
    void createRequiresActivationDateWhenActive() {
        CenterCreateRequest request = CenterCreateRequest.builder().name("c").officeId(1L).active(true).build();
        assertEquals(Set.of("activationDate"), paths(validator.validate(request)));
    }

    @Test
    void associateGroupsRequiresMembers() {
        assertEquals(Set.of("groupMembers"),
                paths(validator.validate(CenterAssociateGroupsRequest.builder().id(1L).groupMembers(List.of()).build())));
    }

    @Test
    void pageResponseExposesPageItemsAndTotal() {
        GroupsPageResponse page = new GroupsPageResponse(List.of(), 0);
        assertEquals(0, page.getTotalFilteredRecords());
        assertEquals(0, page.getPageItems().size());
    }

    @Test
    void updateRequiresName() {
        assertEquals(Set.of("name"), paths(validator.validate(CenterUpdateRequest.builder().id(1L).build())));
    }
}
