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
package org.apache.fineract.infrastructure.entityaccess.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityToEntityMappingIdsView;
import org.junit.jupiter.api.Test;

class FineractOfficeEntityAccessDataTest {

    private static final Long BRANCH_OFFICE = 3L;
    private static final Long SIBLING_OFFICE = 4L;

    @Test
    void unmappedEntityIsVisible() {
        FineractOfficeEntityAccessData access = FineractOfficeEntityAccessData.from(List.of(mapping(SIBLING_OFFICE, 10L)),
                Set.of(BRANCH_OFFICE));

        assertThat(access.isVisible(11L)).isTrue();
    }

    @Test
    void entityMappedToOfficeInScopeIsVisible() {
        FineractOfficeEntityAccessData access = FineractOfficeEntityAccessData
                .from(List.of(mapping(BRANCH_OFFICE, 10L), mapping(SIBLING_OFFICE, 10L)), Set.of(BRANCH_OFFICE));

        assertThat(access.isVisible(10L)).isTrue();
    }

    @Test
    void entityMappedOnlyToOtherOfficeIsHidden() {
        FineractOfficeEntityAccessData access = FineractOfficeEntityAccessData.from(List.of(mapping(SIBLING_OFFICE, 10L)),
                Set.of(BRANCH_OFFICE));

        assertThat(access.isVisible(10L)).isFalse();
    }

    @Test
    void allMappingForOfficeInScopeGrantsEveryEntity() {
        FineractOfficeEntityAccessData access = FineractOfficeEntityAccessData
                .from(List.of(mapping(SIBLING_OFFICE, 10L), mapping(BRANCH_OFFICE, 0L)), Set.of(BRANCH_OFFICE));

        assertThat(access.isVisible(10L)).isTrue();
    }

    @Test
    void allMappingForOfficeOutOfScopeGrantsNothing() {
        FineractOfficeEntityAccessData access = FineractOfficeEntityAccessData
                .from(List.of(mapping(SIBLING_OFFICE, 10L), mapping(SIBLING_OFFICE, 0L)), Set.of(BRANCH_OFFICE));

        assertThat(access.isVisible(10L)).isFalse();
    }

    @Test
    void entityMappedToAllOfficesIsVisible() {
        FineractOfficeEntityAccessData access = FineractOfficeEntityAccessData.from(List.of(mapping(SIBLING_OFFICE, 10L), mapping(0L, 10L)),
                Set.of(BRANCH_OFFICE));

        assertThat(access.isVisible(10L)).isTrue();
    }

    private static FineractEntityToEntityMappingIdsView mapping(Long fromId, Long toId) {
        return new FineractEntityToEntityMappingIdsView() {

            @Override
            public Long getFromId() {
                return fromId;
            }

            @Override
            public Long getToId() {
                return toId;
            }
        };
    }
}
