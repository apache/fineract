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
package org.apache.fineract.portfolio.collectionsheet.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.group.data.CollectionSheetAttendanceItem;
import org.apache.fineract.portfolio.group.data.CollectionSheetLoanTransactionItem;
import org.apache.fineract.portfolio.group.data.GroupCommandResponse;
import org.apache.fineract.portfolio.group.data.GroupSaveCollectionSheetRequest;
import org.apache.fineract.portfolio.group.domain.GroupTypes;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * The typed group and center save-collection-sheet request is handed to the legacy {@code updateCollectionSheet} as the
 * same JSON command the legacy GROUP / CENTER SAVECOLLECTIONSHEET commands carried.
 */
class CollectionSheetGroupSaveBridgeTest {

    private final FromJsonHelper fromJsonHelper = new FromJsonHelper();
    private final CollectionSheetWritePlatformServiceJpaRepositoryImpl service = spy(
            new CollectionSheetWritePlatformServiceJpaRepositoryImpl(null, null, null, null, null, null, null, null, fromJsonHelper));

    private JsonCommand save(final GroupTypes groupingType) {
        final ArgumentCaptor<JsonCommand> captor = ArgumentCaptor.forClass(JsonCommand.class);
        doReturn(new CommandProcessingResultBuilder().with(Map.of("note", "paid")).build()).when(service).updateCollectionSheet(any());

        final GroupSaveCollectionSheetRequest request = GroupSaveCollectionSheetRequest.builder().groupId(7L).groupingType(groupingType)
                .calendarId(3L).transactionDate("07 March 2011").transactionDateOnNonMeetingDate(true)
                .clientsAttendance(List.of(CollectionSheetAttendanceItem.builder().clientId(11L).attendanceType(1L).build()))
                .bulkRepaymentTransactions(List
                        .of(CollectionSheetLoanTransactionItem.builder().loanId(21L).transactionAmount(new BigDecimal("12.50")).build()))
                .dateFormat("dd MMMM yyyy").locale("en").build();
        final GroupCommandResponse response = service.saveCollectionSheet(request);

        assertThat(response.getGroupId()).isEqualTo(7L);
        assertThat(response.getResourceId()).isEqualTo(7L);
        assertThat(response.getChanges()).containsEntry("note", "paid");
        verify(service).updateCollectionSheet(captor.capture());
        return captor.getValue();
    }

    @Test
    void groupRequestBecomesTheLegacyGroupCommand() {
        final JsonCommand command = save(GroupTypes.GROUP);

        assertThat(command.getEntityName()).isEqualTo("GROUP");
        assertThat(command.getUrl()).isEqualTo("/groups/7");
        assertThat(command.entityId()).isEqualTo(7L);
        assertThat(command.getGroupId()).isEqualTo(7L);
        assertThat(command.longValueOfParameterNamed("calendarId")).isEqualTo(3L);
        assertThat(command.booleanPrimitiveValueOfParameterNamed("isTransactionDateOnNonMeetingDate")).isTrue();
        assertThat(command.parsedJson().getAsJsonObject().getAsJsonArray("clientsAttendance").get(0).getAsJsonObject().get("clientId")
                .getAsLong()).isEqualTo(11L);
        assertThat(command.parsedJson().getAsJsonObject().getAsJsonArray("bulkRepaymentTransactions").get(0).getAsJsonObject()
                .get("transactionAmount").getAsBigDecimal()).isEqualByComparingTo("12.50");
        // the legacy validator rejects unknown parameters, so the path values must stay out of the JSON
        assertThat(command.parsedJson().getAsJsonObject().has("groupId")).isFalse();
        assertThat(command.parsedJson().getAsJsonObject().has("groupingType")).isFalse();
    }

    @Test
    void centerRequestBecomesTheLegacyCenterCommand() {
        final JsonCommand command = save(GroupTypes.CENTER);

        assertThat(command.getEntityName()).isEqualTo("CENTER");
        assertThat(command.getUrl()).isEqualTo("/centers/7");
    }
}
