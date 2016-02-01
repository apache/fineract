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
package org.apache.fineract.mix.report.service;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.mix.service.XBRLResultServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReadTaxonomyMappingServiceImplTest {

    private XBRLResultServiceImpl readService;

    @Before
    public void setUp() throws Exception {
        final RoutingDataSource dataSource = Mockito.mock(RoutingDataSource.class);
        this.readService = new XBRLResultServiceImpl(dataSource, null, null);

    }

    @Test
    public void shouldCorrectlyGetGLCode() {
        final ArrayList<String> result = this.readService.getGLCodes("{12000}+{11000}");
        assertEquals("12000", result.get(0));
        assertEquals("11000", result.get(1));
    }

}
