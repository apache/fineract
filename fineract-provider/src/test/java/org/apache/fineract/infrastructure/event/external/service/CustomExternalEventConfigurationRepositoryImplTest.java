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
package org.apache.fineract.infrastructure.event.external.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.EntityManager;
import org.apache.fineract.infrastructure.event.external.exception.ExternalEventConfigurationNotFoundException;
import org.apache.fineract.infrastructure.event.external.repository.CustomExternalEventConfigurationRepositoryImpl;
import org.apache.fineract.infrastructure.event.external.repository.domain.ExternalEventConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressFBWarnings(value = "RV_EXCEPTION_NOT_THROWN", justification = "False positive")
public class CustomExternalEventConfigurationRepositoryImplTest {

    @Mock
    private EntityManager entityManager;
    private CustomExternalEventConfigurationRepositoryImpl underTest;

    @BeforeEach
    public void setUp() {
        underTest = new CustomExternalEventConfigurationRepositoryImpl(entityManager);
    }

    @Test
    public void givenConfigurationExistsThenReturnConfiguration() {
        // given
        ExternalEventConfiguration configuration = new ExternalEventConfiguration("aType", true);
        when(entityManager.find(Mockito.any(), Mockito.anyString())).thenReturn(configuration);
        // when
        ExternalEventConfiguration actualConfiguration = underTest.findExternalEventConfigurationByTypeWithNotFoundDetection("aType");
        // then
        assertThat(actualConfiguration.getType(), equalTo(configuration.getType()));
        assertThat(actualConfiguration.isEnabled(), equalTo(configuration.isEnabled()));

    }

    @Test
    public void givenConfigurationDoesNotExistsThenThrowExternalEventConfigurationNotFoundException() {
        // given
        when(entityManager.find(Mockito.any(), Mockito.anyString())).thenReturn(null);
        // then
        assertThrows(ExternalEventConfigurationNotFoundException.class,
                () -> underTest.findExternalEventConfigurationByTypeWithNotFoundDetection("aType"));
    }

}
