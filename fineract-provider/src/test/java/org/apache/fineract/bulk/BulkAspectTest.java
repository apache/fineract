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
package org.apache.fineract.bulk;

import org.apache.fineract.AbstractSpringTest;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

public class BulkAspectTest extends AbstractSpringTest {

    @Autowired
    private BulkTestEvent testEvent;
    @SpyBean
    private BusinessEventNotifierService notifier;

    @Test
    public void testEventOnFail() {

        Assertions.assertThrows(IllegalStateException.class, () -> testEvent.processCommand(null));
        Mockito.verify(notifier, Mockito.times(1)).startExternalEventRecording();
        Mockito.verify(notifier, Mockito.times(0)).stopExternalEventRecording();
        Mockito.verify(notifier, Mockito.times(1)).resetEventRecording();
    }
}
