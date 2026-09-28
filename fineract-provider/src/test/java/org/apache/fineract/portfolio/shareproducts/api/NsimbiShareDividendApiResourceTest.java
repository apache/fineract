/*
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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.fineract.portfolio.shareproducts.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.google.gson.JsonParser;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.exception.NoAuthorizationException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.apache.fineract.portfolio.shareaccounts.data.ShareAccountDividendData;
import org.apache.fineract.portfolio.shareaccounts.service.ShareAccountDividendReadPlatformService;
import org.apache.fineract.portfolio.shareproducts.data.ShareProductDividendPayOutData;
import org.apache.fineract.portfolio.shareproducts.service.ShareProductDividendReadPlatformService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NsimbiShareDividendApiResourceTest {

    @Mock
    private DefaultToApiJsonSerializer<ShareProductDividendPayOutData> toApiJsonSerializer;
    @Mock
    private DefaultToApiJsonSerializer<ShareAccountDividendData> toApiAccountDetailJsonSerializer;
    @Mock
    private PlatformSecurityContext platformSecurityContext;
    @Mock
    private PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    @Mock
    private ShareAccountDividendReadPlatformService shareAccountDividendReadPlatformService;
    @Mock
    private ShareProductDividendReadPlatformService shareProductDividendReadPlatformService;
    @Mock
    private SqlValidator sqlValidator;
    @Mock
    private AppUser user;
    @InjectMocks
    private ShareDividendApiResource resource;

    @Test
    void unauthorizedOperatorCannotSubmitOrRevealValidationDetails() {
        when(platformSecurityContext.authenticatedUser()).thenReturn(user);
        doThrow(new NoAuthorizationException("not permitted")).when(user).validateHasPermissionTo("CREATE_DIVIDEND_SHAREPRODUCT");

        assertThrows(NoAuthorizationException.class, () -> resource.createNsimbiDividend(7L, "{}"));

        verifyNoInteractions(commandsSourceWritePlatformService);
    }

    @Test
    void validRequestUsesTheExistingPermissionAndDividendCommandWithoutAuditingPassword() {
        when(platformSecurityContext.authenticatedUser()).thenReturn(user);
        CommandProcessingResult result = new CommandProcessingResultBuilder().withEntityId(7L).build();
        when(commandsSourceWritePlatformService.logCommandSource(any())).thenReturn(result);
        when(toApiJsonSerializer.serialize(result)).thenReturn("accepted");

        String response = resource.createNsimbiDividend(7L,
                "{\"amount\":50000,\"date\":\"2026-09-20\",\"sharePeriodMonths\":1,\"password\":\"secret\"}");

        assertThat(response).isEqualTo("accepted");
        verify(user).validateHasPermissionTo("CREATE_DIVIDEND_SHAREPRODUCT");
        ArgumentCaptor<CommandWrapper> command = ArgumentCaptor.forClass(CommandWrapper.class);
        verify(commandsSourceWritePlatformService).logCommandSource(command.capture());
        assertThat(command.getValue().getEntityId()).isEqualTo(7L);
        assertThat(command.getValue().taskPermissionName()).isEqualTo("CREATE_DIVIDEND_SHAREPRODUCT");
        assertThat(command.getValue().getJson()).doesNotContain("secret", "password");
        assertThat(JsonParser.parseString(command.getValue().getJson()).getAsJsonObject().get("dividendPeriodStartDate").getAsString())
                .isEqualTo("2026-08-20");
    }
}
