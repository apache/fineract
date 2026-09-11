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

    @Test
    void updateParentRecalculatesHierarchyForDescendants() {
        Office root = Office.headOffice("Head Office", LocalDate.of(2024, 1, 1), null);
        Office branch = Office.headOffice("Branch", LocalDate.of(2024, 1, 1), null);
        Office subBranch = Office.headOffice("Sub Branch", LocalDate.of(2024, 1, 1), null);

        root.setId(1L);
        branch.setId(2L);
        subBranch.setId(3L);
        root.getChildren().add(branch);
        branch.getChildren().add(subBranch);
        branch.setParent(root);
        subBranch.setParent(branch);
        root.generateHierarchy();

        branch.update(root);

        assertEquals(".", root.getHierarchy());
        assertEquals(".2.", branch.getHierarchy());
        assertEquals(".2.3.", subBranch.getHierarchy());

        Office newRoot = Office.headOffice("New Root", LocalDate.of(2024, 1, 1), null);
        newRoot.setId(4L);
        newRoot.generateHierarchy();
        branch.update(newRoot);

        assertEquals(".", newRoot.getHierarchy());
        assertEquals(".2.", branch.getHierarchy());
        assertEquals(".2.3.", subBranch.getHierarchy());
    }
}
