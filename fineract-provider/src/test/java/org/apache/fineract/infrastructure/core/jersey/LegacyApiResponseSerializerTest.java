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
package org.apache.fineract.infrastructure.core.jersey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.infrastructure.core.annotation.LegacyApiResponse;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.domain.TenantOidcConfig;
import org.apache.fineract.infrastructure.core.exception.UnsupportedParameterException;
import org.apache.fineract.infrastructure.core.jersey.serializer.legacy.LegacyApiResponseSerializer;
import org.apache.fineract.infrastructure.core.serialization.GoogleGsonSerializerHelper;
import org.apache.fineract.infrastructure.security.data.AccessTokenData;
import org.apache.fineract.infrastructure.security.data.AuthenticatedOauthUserData;
import org.apache.fineract.infrastructure.security.data.AuthenticatedUserData;
import org.apache.fineract.infrastructure.security.data.OTPDeliveryMethod;
import org.apache.fineract.infrastructure.security.data.OTPMetadata;
import org.apache.fineract.infrastructure.security.data.TenantOidcConfigData;
import org.apache.fineract.infrastructure.security.domain.OidcFederationType;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.useradministration.data.PasswordValidationPolicyData;
import org.apache.fineract.useradministration.data.PermissionData;
import org.apache.fineract.useradministration.data.RoleData;
import org.junit.jupiter.api.Test;

class LegacyApiResponseSerializerTest {

    private final LegacyApiResponseSerializer serializer = new LegacyApiResponseSerializer(
            SecurityApiSerializationTestSupport.newObjectMapper());
    private final Gson gson = GoogleGsonSerializerHelper.createSimpleGson();

    @LegacyApiResponse
    public void unfiltered() {}

    @LegacyApiResponse({ "id", "name", "description", "permissionUsageData" })
    public void filtered() {}

    @Test
    void preservesOfficeDateFieldsAndOmitsTransientImportMetadata() throws Exception {
        OfficeData office = OfficeData.importInstance("Branch", 1L, LocalDate.of(2026, 11, 9), ExternalId.empty());
        office.setImportFields(99, "en", "yyyy-MM-dd");
        assertLegacyJson(office);
        String json = write(office, "unfiltered", Set.of());
        assertTrue(json.contains("\"openingDate\":[2026,11,9]"));
        assertFalse(json.contains("rowIndex"));
        assertFalse(json.contains("externalId"));
    }

    @Test
    void preservesRolesPermissionsAndPasswordPolicyFields() throws Exception {
        RoleData role = new RoleData(7L, "Officer", "Office role", false);
        PermissionData permission = PermissionData.instance("administration", "READ_USER", "USER", "READ", true);
        assertLegacyJson(role);
        assertLegacyJson(List.of(role));
        assertLegacyJson(permission);
        assertLegacyJson(List.of(permission));
        assertLegacyJson(role.toRolePermissionData(List.of(permission)));
        assertLegacyJson(new PasswordValidationPolicyData(1L, true, "Strong", "password-policy"));
    }

    @Test
    void preservesAuthenticationBooleanNamesAndNestedRoles() throws Exception {
        List<RoleData> roles = List.of(new RoleData(7L, "Officer", "Office role", false));
        AuthenticatedUserData basic = new AuthenticatedUserData().setUsername("officer").setUserId(12L).setAuthenticated(true)
                .setTwoFactorAuthenticationRequired(true).setRoles(roles).setPermissions(List.of("READ_USER"))
                .setOrganisationalRole(new EnumOptionData(1L, "role", "Officer"));
        AuthenticatedOauthUserData oauth = new AuthenticatedOauthUserData().setUsername("officer").setAuthenticated(true)
                .setTwoFactorAuthenticationRequired(true).setAccessToken("access-token").setRoles(roles);
        assertLegacyJson(basic);
        assertLegacyJson(oauth);
        String json = write(basic, "unfiltered", Set.of());
        assertTrue(json.contains("\"isTwoFactorAuthenticationRequired\":true"));
        assertFalse(json.contains("\"twoFactorAuthenticationRequired\""));
    }

    @Test
    void preservesTwoFactorEpochMilliseconds() throws Exception {
        ZonedDateTime time = ZonedDateTime.of(2026, 11, 9, 10, 11, 12, 123000000, ZoneId.of("Asia/Kolkata"));
        OTPDeliveryMethod delivery = new OTPDeliveryMethod().setName("email").setTarget("o***@example.org");
        OTPMetadata metadata = new OTPMetadata().setDeliveryMethod(delivery).setRequestTime(time).setTokenLiveTimeInSec(300)
                .setExtendedAccessToken(true);
        AccessTokenData token = new AccessTokenData().setToken("token").setValidFrom(time).setValidTo(time.plusMinutes(5));
        assertLegacyJson(List.of(delivery));
        assertLegacyJson(metadata);
        assertLegacyJson(token);
        assertTrue(write(token, "unfiltered", Set.of()).contains(Long.toString(time.toInstant().toEpochMilli())));
    }

    @Test
    void preservesCommandResultsEmptyChangesAndNumericPrecision() throws Exception {
        assertLegacyJson(CommandProcessingResult.empty());
        assertLegacyJson(CommandProcessingResult.resourceResult(12L));
        assertLegacyJson(CommandProcessingResult.withChanges(12L, Map.of()));
        assertLegacyJson(CommandProcessingResult.withChanges(12L,
                Map.of("amount", new BigDecimal("12345678901234567890.1200"), "enabled", false, "count", 4L)));
        String json = write(CommandProcessingResult.withChanges(12L, Map.of()), "unfiltered", Set.of());
        assertTrue(json.contains("\"changes\":{}"));
        assertFalse(json.contains("rollbackTransaction"));
        assertTrue(write(CommandProcessingResult.withChanges(12L, Map.of("amount", new BigDecimal("12345678901234567890.1200"))),
                "unfiltered", Set.of()).contains("12345678901234567890.1200"));
    }

    @Test
    void preservesConfigurationMapsAndEmptyDeletionResponse() throws Exception {
        Map<String, Object> configuration = new HashMap<>();
        configuration.put("enabled", true);
        configuration.put("limit", 10L);
        configuration.put("absent", null);
        assertLegacyJson(configuration);
        assertLegacyJson(Map.of("externalId", ExternalId.empty()));
        assertLegacyJson(Map.of());
        assertLegacyJson(123L);
        assertEquals("{}", write(Map.of(), "unfiltered", Set.of()));
        assertLegacyJson(TenantOidcConfigData.builder().tenantId("default").providerType(OidcFederationType.GENERIC)
                .issuerUri("https://issuer.example.org").clientId("client").enabled(true).build());
        TenantOidcConfig configurationWithSecret = TenantOidcConfig.builder().tenantId("default").clientSecret("encrypted-secret")
                .providerType(OidcFederationType.GENERIC).issuerUri("https://issuer.example.org").enabled(true).build();
        String oidcJson = write(TenantOidcConfigData.from(configurationWithSecret), "unfiltered", Set.of());
        assertFalse(oidcJson.contains("clientSecret"));
        assertFalse(oidcJson.contains("encrypted-secret"));
    }

    @Test
    void preservesDateFormatsInsideCommandChanges() throws Exception {
        Map<String, Object> changes = Map.of("date", LocalDate.of(2026, 11, 9), "datetime",
                LocalDateTime.of(2026, 11, 9, 10, 11, 12, 123456789), "time", LocalTime.of(10, 11, 12, 123456789), "monthDay",
                MonthDay.of(11, 9), "offsetDateTime", OffsetDateTime.parse("2026-11-09T10:11:12+05:30"), "offsetTime",
                OffsetTime.parse("10:11:12+05:30"), "instant", Date.from(java.time.Instant.parse("2026-11-09T10:11:12Z")));
        assertLegacyJson(CommandProcessingResult.withChanges(12L, changes));
    }

    @Test
    void preservesFieldsFilteringIncludingNestedNamesAndUnlistedFields() throws Exception {
        RoleData role = new RoleData(7L, "Officer", "Office role", false);
        Object data = role.toRolePermissionData(List.of(PermissionData.instance("administration", "READ_USER", "USER", "READ", true)));
        Set<String> fields = Set.of("name", "permissionUsageData");
        LegacyApiResponse response = annotation("filtered");
        Gson filteredGson = new GoogleGsonSerializerHelper()
                .createGsonBuilderWithParameterExclusionSerializationStrategy(Set.of(response.value()), fields);
        assertEquals(JsonParser.parseString(filteredGson.toJson(data)), JsonParser.parseString(write(data, "filtered", fields)));
        assertTrue(write(data, "filtered", fields).contains("\"disabled\":false"));
        assertFalse(write(data, "filtered", fields).contains("description"));
        assertEquals(write(data, "unfiltered", Set.of()), write(data, "filtered", Set.of()));
    }

    @Test
    void rejectsUnsupportedFieldsOnlyOnEndpointsThatPreviouslyFiltered() throws Exception {
        UnsupportedParameterException exception = assertThrows(UnsupportedParameterException.class,
                () -> serializer.writer(annotation("filtered"), Set.of("unknown")));
        assertEquals(List.of("unknown"), exception.getUnsupportedParameters());
        assertEquals(write(Map.of("key", true), "unfiltered", Set.of()), write(Map.of("key", true), "unfiltered", Set.of("unknown")));
    }

    @Test
    void honorsApplicationMixinsModulesAndPropertyDateFormats() throws Exception {
        ObjectMapper mapper = SecurityApiSerializationTestSupport.newObjectMapper();
        mapper.addMixIn(ConfiguredResponse.class, ConfiguredResponseMixin.class);
        SimpleModule configuredModule = new SimpleModule();
        configuredModule.addSerializer(ConfiguredLeaf.class, new ConfiguredLeafSerializer());
        configuredModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ISO_LOCAL_DATE));
        mapper.registerModule(configuredModule);
        LegacyApiResponseSerializer sharedWriter = new LegacyApiResponseSerializer(mapper);
        ConfiguredResponse data = new ConfiguredResponse();
        String json = sharedWriter.writer(annotation("unfiltered"), Set.of()).writeValueAsString(data);
        assertEquals(JsonParser.parseString("{\"renamed\":\"value\",\"date\":\"09/11/2026\",\"leaf\":\"configured\"}"),
                JsonParser.parseString(json));
        assertEquals(JsonParser.parseString(mapper.writeValueAsString(data)), JsonParser.parseString(json));
    }

    @Test
    void sharedSerializerCachesDoNotLeakLegacyDatesOrFieldSelection() throws Exception {
        ObjectMapper mapper = SecurityApiSerializationTestSupport.newObjectMapper();
        LegacyApiResponseSerializer sharedWriter = new LegacyApiResponseSerializer(mapper);
        OfficeData office = OfficeData.importInstance("Branch", 1L, LocalDate.of(2026, 11, 9), ExternalId.empty());
        String ordinary = mapper.writeValueAsString(office);
        String legacy = sharedWriter.writer(annotation("unfiltered"), Set.of()).writeValueAsString(office);
        assertTrue(ordinary.contains("\"openingDate\":\"2026-11-09\""));
        assertTrue(legacy.contains("\"openingDate\":[2026,11,9]"));
        sharedWriter.writer(annotation("filtered"), Set.of("name")).writeValueAsString(office);
        assertEquals(ordinary, mapper.writeValueAsString(office));
        assertEquals(legacy, sharedWriter.writer(annotation("unfiltered"), Set.of()).writeValueAsString(office));
    }

    @Test
    void keepsCommandGetterNormalizationOutsideMigratedResponses() throws Exception {
        ObjectMapper mapper = SecurityApiSerializationTestSupport.newObjectMapper();
        LegacyApiResponseSerializer sharedWriter = new LegacyApiResponseSerializer(mapper);
        CommandProcessingResult result = CommandProcessingResult.withChanges(12L, Map.of());
        String ordinary = mapper.writeValueAsString(result);
        assertFalse(ordinary.contains("\"changes\""));
        String legacy = sharedWriter.writer(annotation("unfiltered"), Set.of()).writeValueAsString(result);
        assertTrue(legacy.contains("\"changes\":{}"));
        assertFalse(legacy.contains("rollbackTransaction"));
        assertEquals(ordinary, mapper.writeValueAsString(result));
    }

    static final class ConfiguredResponse {

        public String getRaw() {
            return "value";
        }

        public LocalDate getDate() {
            return LocalDate.of(2026, 11, 9);
        }

        public ConfiguredLeaf getLeaf() {
            return new ConfiguredLeaf();
        }
    }

    abstract static class ConfiguredResponseMixin {

        @JsonProperty("renamed")
        abstract String getRaw();

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
        abstract LocalDate getDate();
    }

    static final class ConfiguredLeaf {}

    private static final class ConfiguredLeafSerializer extends StdSerializer<ConfiguredLeaf> {

        private ConfiguredLeafSerializer() {
            super(ConfiguredLeaf.class);
        }

        @Override
        public void serialize(ConfiguredLeaf value, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeString("configured");
        }
    }

    private LegacyApiResponse annotation(String method) throws NoSuchMethodException {
        return getClass().getMethod(method).getAnnotation(LegacyApiResponse.class);
    }

    private String write(Object data, String method, Set<String> fields) throws Exception {
        return serializer.writer(annotation(method), fields).writeValueAsString(data);
    }

    private void assertLegacyJson(Object data) throws Exception {
        assertEquals(JsonParser.parseString(gson.toJson(data)), JsonParser.parseString(write(data, "unfiltered", Set.of())));
    }
}
