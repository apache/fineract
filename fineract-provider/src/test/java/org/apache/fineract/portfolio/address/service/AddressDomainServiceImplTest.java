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
package org.apache.fineract.portfolio.address.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepository;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.UnsupportedParameterException;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.address.data.AddressCreateRequest;
import org.apache.fineract.portfolio.address.data.AddressCreateResponse;
import org.apache.fineract.portfolio.address.data.FieldConfigurationData;
import org.apache.fineract.portfolio.address.domain.Address;
import org.apache.fineract.portfolio.address.domain.AddressRepository;
import org.apache.fineract.portfolio.address.mapper.AddressMapperImpl;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientAddressRepository;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AddressDomainServiceImplTest {

    private static final long CLIENT_ID = 1L;
    private static final long ADDRESS_TYPE_ID = 2L;

    @Mock
    private Validator validator;
    @Mock
    private FieldConfigurationReadPlatformService fieldConfigurationReadPlatformService;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private ClientAddressRepository clientAddressRepository;
    @Mock
    private ClientRepositoryWrapper clientRepositoryWrapper;
    @Mock
    private CodeValueRepository codeValueRepository;

    private AddressDomainServiceImpl underTest;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        underTest = new AddressDomainServiceImpl(validator, fieldConfigurationReadPlatformService, addressRepository,
                clientAddressRepository, clientRepositoryWrapper, codeValueRepository, new AddressMapperImpl());

        final Client client = mock(Client.class);
        when(client.getId()).thenReturn(CLIENT_ID);
        when(clientRepositoryWrapper.findOneWithNotFoundDetection(CLIENT_ID)).thenReturn(client);
        when(validator.validate(any(AddressCreateRequest.class))).thenReturn(Set.of());
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void createStoresMissingFieldsAsTheLegacyPathDid() {
        givenFieldConfiguration(field("city", true, false, ""));

        final AddressCreateResponse response = underTest.create(request().city("Pune").build());

        final ArgumentCaptor<Address> address = ArgumentCaptor.forClass(Address.class);
        verify(addressRepository).save(address.capture());
        assertThat(address.getValue().getCity()).isEqualTo("Pune");
        assertThat(address.getValue().getStreet()).isEmpty();
        assertThat(address.getValue().getPostalCode()).isEmpty();
        assertThat(address.getValue().getLatitude()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(address.getValue().getCreatedOn()).isNotNull();
        assertThat(response.getClientId()).isEqualTo(CLIENT_ID);
    }

    @Test
    void createRejectsMissingMandatoryField() {
        givenFieldConfiguration(field("postalCode", true, true, ""));

        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> underTest.create(request().city("Pune").build()));

        assertThat(exception.getErrors()).extracting(ApiParameterError::getParameterName).containsExactly("postalCode");
        verifyNoInteractions(addressRepository, clientAddressRepository);
    }

    @Test
    void createRejectsValueNotMatchingTheConfiguredRegex() {
        givenFieldConfiguration(field("postalCode", true, false, "^\\d{6}$"));

        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> underTest.create(request().postalCode("ABC").build()));

        assertThat(exception.getErrors()).extracting(ApiParameterError::getParameterName).containsExactly("postalCode");
        verify(addressRepository, never()).save(any());
    }

    @Test
    void createAppliesTheAddressTypeRegexToAddressTypeId() {
        givenFieldConfiguration(field("addressType", true, false, "^1$"));

        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> underTest.create(request().build()));

        assertThat(exception.getErrors()).extracting(ApiParameterError::getParameterName).containsExactly("addressTypeId");
    }

    @Test
    void createRejectsFieldThatIsDisabled() {
        givenFieldConfiguration(field("latitude", false, false, ""));

        final UnsupportedParameterException exception = assertThrows(UnsupportedParameterException.class,
                () -> underTest.create(request().latitude(BigDecimal.ONE).build()));

        assertThat(exception.getUnsupportedParameters()).containsExactly("latitude");
        verify(addressRepository, never()).save(any());
    }

    @Test
    void createAcceptsStreetTownVillageAndCountyDistrictEvenWhenDisabled() {
        givenFieldConfiguration(field("street", false, false, ""), field("townVillage", false, false, ""),
                field("countyDistrict", false, false, ""));

        underTest.create(request().street("Main").townVillage("Village").countyDistrict("District").build());

        verify(addressRepository).save(any(Address.class));
    }

    @Test
    void createSkipsFieldConfigurationRulesForFieldsThatAreNotSent() {
        givenFieldConfiguration(field("latitude", false, false, ""), field("city", true, false, "^[A-Z].*"));

        underTest.create(request().build());

        verify(addressRepository).save(any(Address.class));
    }

    @Test
    void createRejectsConstraintViolationsBeforeTouchingTheDatabase() {
        @SuppressWarnings("unchecked")
        final ConstraintViolation<AddressCreateRequest> violation = mock(ConstraintViolation.class);
        when(validator.validate(any(AddressCreateRequest.class))).thenReturn(Set.of(violation));

        assertThrows(ConstraintViolationException.class, () -> underTest.create(request().build()));

        verifyNoInteractions(fieldConfigurationReadPlatformService, clientRepositoryWrapper, addressRepository, clientAddressRepository);
    }

    private static AddressCreateRequest.AddressCreateRequestBuilder request() {
        return AddressCreateRequest.builder().clientId(CLIENT_ID).addressTypeId(ADDRESS_TYPE_ID);
    }

    private void givenFieldConfiguration(final FieldConfigurationData... fields) {
        when(fieldConfigurationReadPlatformService.retrieveFieldConfigurationList("ADDRESS")).thenReturn(List.of(fields));
    }

    private static FieldConfigurationData field(final String name, final boolean enabled, final boolean mandatory, final String regex) {
        return new FieldConfigurationData(1L, "ADDRESS", "CLIENT", name, enabled, mandatory, regex);
    }
}
