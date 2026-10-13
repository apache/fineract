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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.infrastructure.entityaccess.data.FineractEntityToEntityMappingData;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityRelation;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityRelationRepository;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityRelationRepositoryWrapper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepository;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FineractEntityAccessReadServiceImplTest {

    private static final Long REL_ID = 1L;

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private FineractEntityRelationRepositoryWrapper relationRepositoryWrapper;
    @Mock
    private FineractEntityRelationRepository relationRepository;
    @Mock
    private OfficeRepository officeRepository;
    @Mock
    private AppUser user;
    @Mock
    private Office office;

    private FineractEntityAccessReadServiceImpl underTest;

    @BeforeEach
    void setUp() {
        underTest = new FineractEntityAccessReadServiceImpl(context, jdbcTemplate, relationRepositoryWrapper, relationRepository,
                officeRepository);
        when(context.authenticatedUser()).thenReturn(user);
        when(user.getOffice()).thenReturn(office);
        when(office.getHierarchy()).thenReturn(".2.");
        when(officeRepository.findIdsByHierarchyLike(".2.%")).thenReturn(List.of(2L, 5L));
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Object[].class)))
                .thenReturn(List.of(mapping(2L), mapping(3L), mapping(5L)));
    }

    @Test
    void officeMappingsAreScopedToTheUserOfficeHierarchy() {
        givenRelation("office_access_to_loan_products");

        Collection<FineractEntityToEntityMappingData> result = underTest.retrieveEntityToEntityMappings(REL_ID, 0L, 0L);

        assertThat(result).extracting(FineractEntityToEntityMappingData::getFromId).containsExactly(2L, 5L);
    }

    @Test
    void roleMappingsAreNotScopedByOffice() {
        givenRelation("role_access_to_loan_products");

        Collection<FineractEntityToEntityMappingData> result = underTest.retrieveEntityToEntityMappings(REL_ID, 0L, 0L);

        assertThat(result).extracting(FineractEntityToEntityMappingData::getFromId).containsExactly(2L, 3L, 5L);
        verifyNoInteractions(officeRepository);
    }

    private void givenRelation(final String codeName) {
        when(relationRepository.findById(REL_ID)).thenReturn(Optional.of(new FineractEntityRelation().setCodeName(codeName)));
    }

    private static FineractEntityToEntityMappingData mapping(final Long fromId) {
        return new FineractEntityToEntityMappingData().setRelationId(REL_ID).setFromId(fromId);
    }
}
