package org.apache.fineract.portfolio.fund.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.fund.domain.Fund;
import org.apache.fineract.portfolio.fund.domain.FundRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class FundWritePlatformServiceImplTest {
    @Mock
    private FundRepository fundRepository;

    @InjectMocks
    private FundWritePlatformServiceJpaRepositoryImpl fundWritePlatformService;

    private JsonCommand createTestJsonCommandForFund(String name, String externalId, boolean isActive) {
        FromJsonHelper fromJsonHelper = new FromJsonHelper();

        String json = String.format("""
                                    {
                                        "name": "%s",
                                        "externalId": "%s",
                                        "isActive": %s
                                    }
                                    """,
                                    name, externalId, isActive);

        JsonObject parsedJson = JsonParser.parseString(json).getAsJsonObject();

        return JsonCommand.from(
                json,        // first: raw JSON string
                parsedJson,  // second: parsed JSON
                fromJsonHelper,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null, null
        );
    }

    @Test
    void shouldArchiveFundSuccessfully() {
        JsonCommand command = createTestJsonCommandForFund("Test Fund", "EXT123", true);

        Fund fund = Fund.fromJson(command);

        fund.setArchived();

        assertFalse(fund.isActive());

    }
}
