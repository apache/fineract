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
package org.apache.fineract.infrastructure.entityaccess.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.apache.fineract.infrastructure.entityaccess.data.FineractOfficeEntityAccessData;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityAccessType;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityRelation;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityRelationRepositoryWrapper;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityToEntityMappingIdsView;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityToEntityMappingRepository;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepository;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FineractEntityAccessReadServiceImplTest {

    private static final Long HEAD_OFFICE = 1L;
    private static final Long REGION_OFFICE = 2L;
    private static final Long BRANCH_OFFICE = 5L;
    private static final Long SIBLING_OFFICE = 6L;
    private static final Long SUB_BRANCH_OFFICE = 7L;

    @Mock
    private FineractEntityRelationRepositoryWrapper fineractEntityRelationRepository;
    @Mock
    private FineractEntityToEntityMappingRepository fineractEntityToEntityMappingRepository;
    @Mock
    private OfficeRepository officeRepository;
    @Mock
    private OfficeRepositoryWrapper officeRepositoryWrapper;
    @InjectMocks
    private FineractEntityAccessReadServiceImpl service;

    private final FineractEntityRelation relation = new FineractEntityRelation();

    private void givenBranchWithMappings() {
        Office branch = mock(Office.class);
        when(branch.getHierarchy()).thenReturn(".2.5.");
        when(officeRepositoryWrapper.findOneWithNotFoundDetection(BRANCH_OFFICE)).thenReturn(branch);
        when(fineractEntityRelationRepository.findOneByCodeName(FineractEntityAccessType.OFFICE_ACCESS_TO_LOAN_PRODUCTS.getStr()))
                .thenReturn(relation);
        when(officeRepository.findIdsByHierarchyIn(Set.of(".", ".2.", ".2.5.")))
                .thenReturn(List.of(HEAD_OFFICE, REGION_OFFICE, BRANCH_OFFICE));
        when(fineractEntityToEntityMappingRepository.findByRelationId(relation)).thenReturn(List.of(mapping(REGION_OFFICE, 10L),
                mapping(SIBLING_OFFICE, 11L), mapping(SUB_BRANCH_OFFICE, 12L), mapping(BRANCH_OFFICE, 13L)));
    }

    @Test
    void officeSeesEntitiesMappedToItselfAndItsAncestors() {
        givenBranchWithMappings();
        FineractOfficeEntityAccessData access = service.retrieveOfficeEntityAccess(FineractEntityAccessType.OFFICE_ACCESS_TO_LOAN_PRODUCTS,
                BRANCH_OFFICE, false);

        assertThat(access.isVisible(10L)).isTrue();
        assertThat(access.isVisible(11L)).isFalse();
        assertThat(access.isVisible(12L)).isFalse();
        assertThat(access.isVisible(13L)).isTrue();
        assertThat(access.isVisible(99L)).isTrue();
        verify(officeRepository, never()).findIdsByHierarchyLike(anyString());
    }

    @Test
    void includingSubOfficesAddsEntitiesMappedToDescendants() {
        givenBranchWithMappings();
        when(officeRepository.findIdsByHierarchyLike(".2.5.%")).thenReturn(List.of(BRANCH_OFFICE, SUB_BRANCH_OFFICE));

        FineractOfficeEntityAccessData access = service.retrieveOfficeEntityAccess(FineractEntityAccessType.OFFICE_ACCESS_TO_LOAN_PRODUCTS,
                BRANCH_OFFICE, true);

        assertThat(access.isVisible(10L)).isTrue();
        assertThat(access.isVisible(11L)).isFalse();
        assertThat(access.isVisible(12L)).isTrue();
    }

    @Test
    void hierarchyWithAncestorsListsEveryPrefix() {
        assertThat(FineractEntityAccessReadServiceImpl.hierarchyWithAncestors(".")).containsExactly(".");
        assertThat(FineractEntityAccessReadServiceImpl.hierarchyWithAncestors(".2.5.")).containsExactly(".", ".2.", ".2.5.");
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
