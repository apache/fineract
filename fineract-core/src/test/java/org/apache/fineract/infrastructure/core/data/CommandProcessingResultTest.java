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
package org.apache.fineract.infrastructure.core.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.junit.jupiter.api.Test;

class CommandProcessingResultTest {

    private static final ExternalId SUB_EXTERNAL_ID = new ExternalId("sub");
    private static final ExternalId FIRST_EXTERNAL_ID = new ExternalId("first");
    private static final ExternalId SECOND_EXTERNAL_ID = new ExternalId("second");

    @Test
    void subResourceFieldsComeFromTheBuilderWithoutSubResources() {
        final CommandProcessingResult result = new CommandProcessingResultBuilder().withEntityId(1L).withSubEntityId(2L)
                .withSubEntityExternalId(SUB_EXTERNAL_ID).build();

        assertEquals(2L, result.getSubResourceId());
        assertEquals(SUB_EXTERNAL_ID, result.getSubResourceExternalId());
        assertNull(result.getSubResources());
    }

    @Test
    void subResourceFieldsComeFromTheFirstSubResource() {
        final List<CommandProcessingSubResourceResult> subResources = List.of(new CommandProcessingSubResourceResult(3L, FIRST_EXTERNAL_ID),
                new CommandProcessingSubResourceResult(4L, SECOND_EXTERNAL_ID));

        final CommandProcessingResult result = new CommandProcessingResultBuilder().withEntityId(1L).withSubEntityId(2L)
                .withSubResources(subResources).build();

        assertEquals(3L, result.getSubResourceId());
        assertEquals(FIRST_EXTERNAL_ID, result.getSubResourceExternalId());
        assertEquals(subResources, result.getSubResources());
    }

    @Test
    void emptySubResourcesAreTreatedAsNone() {
        final CommandProcessingResult result = new CommandProcessingResultBuilder().withSubEntityId(2L).withSubResources(List.of()).build();

        assertEquals(2L, result.getSubResourceId());
        assertNull(result.getSubResources());
    }

    @Test
    void copyKeepsTheSubResources() {
        final List<CommandProcessingSubResourceResult> subResources = List
                .of(new CommandProcessingSubResourceResult(3L, FIRST_EXTERNAL_ID));
        final CommandProcessingResult result = new CommandProcessingResultBuilder().withEntityId(1L).withSubResources(subResources).build();

        final CommandProcessingResult copy = CommandProcessingResult.fromCommandProcessingResult(result, 9L);

        assertEquals(9L, copy.getResourceId());
        assertEquals(3L, copy.getSubResourceId());
        assertEquals(subResources, copy.getSubResources());
    }
}
