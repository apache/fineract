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
package org.apache.fineract.portfolio.loanaccount.guarantor.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.staff.exception.StaffNotFoundException;
import org.apache.fineract.organisation.staff.service.StaffReadService;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.guarantor.data.GuarantorData;
import org.apache.fineract.portfolio.loanaccount.guarantor.data.GuarantorFundingData;
import org.apache.fineract.portfolio.loanaccount.guarantor.domain.GuarantorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;

@ExtendWith(MockitoExtension.class)
class GuarantorReadPlatformServiceImplTest {

    private static final Long LOAN_ID = 10L;

    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private ClientReadPlatformService clientReadPlatformService;
    @Mock
    private StaffReadService staffReadService;
    @Mock
    private LoanRepositoryWrapper loanRepositoryWrapper;
    @Mock
    private PlatformSecurityContext context;

    private GuarantorReadPlatformServiceImpl underTest;

    @BeforeEach
    void setUp() {
        underTest = new GuarantorReadPlatformServiceImpl(jdbcTemplate, clientReadPlatformService, staffReadService, loanRepositoryWrapper,
                context);
    }

    @Test
    void fundedGuarantorWithoutHoldTransactionHasNoTransactions() throws SQLException {
        stubRows(guarantorRow(1L, GuarantorType.EXTERNAL, 5L));

        List<GuarantorData> guarantors = underTest.retrieveGuarantorsForLoan(LOAN_ID);

        assertEquals(1, guarantors.size());
        List<GuarantorFundingData> fundingDetails = new ArrayList<>(guarantors.get(0).getGuarantorFundingDetails());
        assertEquals(1, fundingDetails.size());
        assertTrue(fundingDetails.get(0).getGuarantorTransactions().isEmpty());
    }

    @Test
    void clientGuarantorOutsideOfficeHierarchyDoesNotHideOtherGuarantors() throws SQLException {
        stubRows(guarantorRow(1L, GuarantorType.CUSTOMER, 0L), guarantorRow(2L, GuarantorType.EXTERNAL, 0L));
        stubClientVisibility(0);

        List<GuarantorData> guarantors = underTest.retrieveGuarantorsForLoan(LOAN_ID);

        assertEquals(2, guarantors.size());
        assertEquals(1L, guarantors.get(0).getId());
        assertEquals(100L, guarantors.get(0).getEntityId());
        assertNull(guarantors.get(0).getOfficeName());
        assertEquals(2L, guarantors.get(1).getId());
        verify(clientReadPlatformService, never()).retrieveOne(any());
    }

    @Test
    void clientGuarantorInsideOfficeHierarchyIsMerged() throws SQLException {
        stubRows(guarantorRow(1L, GuarantorType.CUSTOMER, 0L));
        stubClientVisibility(1);
        ClientData clientData = mock(ClientData.class);
        when(clientData.getOfficeName()).thenReturn("Head Office");
        when(clientData.getExternalId()).thenReturn(ExternalId.empty());
        when(clientReadPlatformService.retrieveOne(100L)).thenReturn(clientData);

        List<GuarantorData> guarantors = underTest.retrieveGuarantorsForLoan(LOAN_ID);

        assertEquals(1, guarantors.size());
        assertEquals("Head Office", guarantors.get(0).getOfficeName());
    }

    @Test
    void unresolvableStaffGuarantorIsReturnedUnmerged() throws SQLException {
        stubRows(guarantorRow(1L, GuarantorType.STAFF, 0L));
        when(staffReadService.retrieveStaff(100L)).thenThrow(new StaffNotFoundException(100L));

        List<GuarantorData> guarantors = underTest.retrieveGuarantorsForLoan(LOAN_ID);

        assertEquals(1, guarantors.size());
        assertEquals(1L, guarantors.get(0).getId());
        assertEquals(100L, guarantors.get(0).getEntityId());
    }

    private void stubClientVisibility(int count) {
        when(context.officeHierarchy()).thenReturn(".");
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(100L), eq(".%"), eq(".%"))).thenReturn(count);
    }

    @SuppressWarnings("unchecked")
    private void stubRows(ResultSet... rows) {
        when(jdbcTemplate.query(any(PreparedStatementCreator.class), any(RowMapper.class))).thenAnswer(invocation -> {
            RowMapper<GuarantorData> mapper = invocation.getArgument(1);
            List<GuarantorData> result = new ArrayList<>();
            for (int i = 0; i < rows.length; i++) {
                result.add(mapper.mapRow(rows[i], i));
            }
            return result;
        });
    }

    private static ResultSet guarantorRow(Long id, GuarantorType type, Long fundingDetailId) throws SQLException {
        ResultSet rs = mock(ResultSet.class, withSettings().strictness(Strictness.LENIENT));
        when(rs.getLong("id")).thenReturn(id);
        when(rs.getLong("loanId")).thenReturn(LOAN_ID);
        when(rs.getInt("guarantorType")).thenReturn(type.getValue());
        when(rs.getLong("entityId")).thenReturn(100L);
        when(rs.getLong("gfdId")).thenReturn(fundingDetailId);
        when(rs.getLong("gtId")).thenReturn(0L);
        when(rs.next()).thenReturn(false);
        return rs;
    }
}
