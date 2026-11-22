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
package org.apache.fineract.organisation.office.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class OfficeTest {

    private static final LocalDate OPENING_DATE = LocalDate.of(2024, 1, 1);

    @Test
    void updateParentRegeneratesHierarchyOfAllDescendants() {
        Office headOffice = office(1L, null);
        Office region = office(2L, headOffice);
        Office branch = office(3L, headOffice);
        Office subBranch = office(4L, branch);
        Office agency = office(5L, subBranch);

        assertEquals(".3.", branch.getHierarchy());
        assertEquals(".3.4.", subBranch.getHierarchy());
        assertEquals(".3.4.5.", agency.getHierarchy());

        branch.update(region);

        assertEquals(".2.3.", branch.getHierarchy());
        assertEquals(".2.3.4.", subBranch.getHierarchy());
        assertEquals(".2.3.4.5.", agency.getHierarchy());
    }

    private static Office office(Long id, Office parent) {
        Office office = Office.headOffice("Office " + id, OPENING_DATE, null);
        office.setId(id);
        if (parent != null) {
            office.setParent(parent);
            parent.getChildren().add(office);
        }
        office.generateHierarchy();
        return office;
    }
}
