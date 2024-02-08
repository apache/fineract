/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.apache.fineract.portfolio.savings.transfers;

import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.portfolio.account.api.AccountTransfersApiResource;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 *
 * @author daviestobialex
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SavingsAccountTransfersApiTest {

    @Mock
    private ApiRequestParameterHelper parameterHelper;

    @Mock
    private PlatformSecurityContext securityContext;

    @Mock
    private PortfolioCommandSourceWritePlatformService commandWritePlatformService;

    @Mock
    private AccountTransfersReadPlatformService accountTransfersReadPlatformService;

    private ApiRequestJsonSerializationSettings apiRequestJsonSerializationSettings;

    @InjectMocks
    private AccountTransfersApiResource underTest;

    @BeforeEach
    void setUp() throws IOException {
        apiRequestJsonSerializationSettings = new ApiRequestJsonSerializationSettings(false, null, false, false, false);
        given(parameterHelper.process(Mockito.any())).willReturn(apiRequestJsonSerializationSettings);
    }

    @Test
    void accountTransfer_ChargeWithdrawalFeeWithPaymentType_Test() {
        AppUser appUser = Mockito.mock(AppUser.class);
        CommandProcessingResult response = Mockito.mock(CommandProcessingResult.class);
        // given
        Mockito.doNothing().when(appUser).validateHasUpdatePermission("BUSINESS_DATE");
        given(securityContext.authenticatedUser()).willReturn(appUser);
        given(commandWritePlatformService.logCommandSource(Mockito.any())).willReturn(response);

        // when
        underTest.create("{}");

        // then
        verify(commandWritePlatformService, Mockito.times(1)).logCommandSource(Mockito.any());


    }
}
