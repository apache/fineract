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
package org.apache.fineract.portfolio.savings.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.charge.service.ChargeDropdownReadPlatformService;
import org.apache.fineract.portfolio.common.service.DropdownReadPlatformService;
import org.apache.fineract.portfolio.savings.data.SavingsChargeTransactionData;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@ExtendWith(MockitoExtension.class)
class SavingsAccountChargeReadPlatformServiceImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private PlatformSecurityContext context;
    @Mock
    private ChargeDropdownReadPlatformService chargeDropdownReadPlatformService;
    @Mock
    private DropdownReadPlatformService dropdownReadPlatformService;
    @Mock
    private DatabaseSpecificSQLGenerator sqlGenerator;

    private SavingsAccountChargeReadPlatformServiceImpl underTest;

    @BeforeEach
    void setUp() {
        underTest = new SavingsAccountChargeReadPlatformServiceImpl(context, chargeDropdownReadPlatformService, jdbcTemplate,
                dropdownReadPlatformService, sqlGenerator);
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    void retrieveChargeTransactionsScopesOrdersAndMapsAllocationHistory() throws Exception {
        Long savingsAccountChargeId = 22L;
        Long savingsAccountId = 11L;
        AppUser user = mock(AppUser.class);
        when(context.authenticatedUser()).thenReturn(user);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Object[].class))).thenReturn(List.of());

        underTest.retrieveChargeTransactions(savingsAccountChargeId, savingsAccountId);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<RowMapper<SavingsChargeTransactionData>> mapperCaptor = ArgumentCaptor.forClass(RowMapper.class);
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> parametersCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(jdbcTemplate).query(sqlCaptor.capture(), mapperCaptor.capture(), parametersCaptor.capture());
        assertThat(sqlCaptor.getValue()).contains("charge.id = ? and charge.savings_account_id = ? and tr.savings_account_id = ?")
                .contains("order by tr.transaction_date desc, tr.id desc, allocation.id desc");
        assertThat(parametersCaptor.getValue()).containsExactly(savingsAccountChargeId, savingsAccountId, savingsAccountId);

        ResultSet resultSet = resultSet(true);
        SavingsChargeTransactionData transaction = mapperCaptor.getValue().mapRow(resultSet, 0);

        assertThat(transaction.getTransactionId()).isEqualTo(42L);
        assertThat(transaction.getAllocationId()).isEqualTo(7L);
        assertThat(transaction.getDate()).isEqualTo(LocalDate.of(2026, 9, 20));
        assertThat(transaction.getTransactionType().getId()).isEqualTo(15L);
        assertThat(transaction.getAmount()).isEqualByComparingTo("100.00");
        assertThat(transaction.getAmountAllocated()).isEqualByComparingTo("25.00");
        assertThat(transaction.isReversed()).isTrue();
        assertThat(transaction.getPaymentDetailData().getPaymentType().getName()).isEqualTo("Bank transfer");
        assertThat(transaction.getPaymentDetailData().getReceiptNumber()).isEqualTo("receipt-123");

        SavingsChargeTransactionData withoutPaymentDetails = mapperCaptor.getValue().mapRow(resultSet(false), 1);
        assertThat(withoutPaymentDetails.getPaymentDetailData()).isNull();
    }

    private ResultSet resultSet(boolean withPaymentDetails) throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getLong("transactionId")).thenReturn(42L);
        when(resultSet.getLong("allocationId")).thenReturn(7L);
        when(resultSet.findColumn("paymentDetailId")).thenReturn(1);
        when(resultSet.getLong(1)).thenReturn(withPaymentDetails ? 9L : 0L);
        when(resultSet.wasNull()).thenReturn(!withPaymentDetails);
        if (withPaymentDetails) {
            when(resultSet.findColumn("paymentTypeId")).thenReturn(2);
            when(resultSet.getLong(2)).thenReturn(3L);
        }
        when(resultSet.getDate("transactionDate")).thenReturn(Date.valueOf(LocalDate.of(2026, 9, 20)));
        when(resultSet.getInt("transactionType")).thenReturn(15);
        when(resultSet.getBigDecimal("transactionAmount")).thenReturn(new BigDecimal("100.00"));
        when(resultSet.getBigDecimal("allocatedAmount")).thenReturn(new BigDecimal("25.00"));
        when(resultSet.getBoolean("reversed")).thenReturn(true);
        if (withPaymentDetails) {
            when(resultSet.getString("paymentTypeName")).thenReturn("Bank transfer");
            when(resultSet.getString("accountNumber")).thenReturn("account-1");
            when(resultSet.getString("checkNumber")).thenReturn("check-1");
            when(resultSet.getString("routingCode")).thenReturn("routing-1");
            when(resultSet.getString("receiptNumber")).thenReturn("receipt-123");
            when(resultSet.getString("bankNumber")).thenReturn("bank-1");
        }
        return resultSet;
    }
}
