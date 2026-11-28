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
package org.apache.fineract.accounting.glaccount.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.junit.jupiter.api.Test;

class GLAccountProjectionTest {

    private final GLAccountProjection projection = new GLAccountProjection(null);

    @Test
    void accountWithoutTagHasNoTagId() {
        final GLAccountData account = projection.newInstance(1L, "Cash", null, "100", false, true, GLAccountType.ASSET.getValue(),
                GLAccountUsage.DETAIL.getValue(), "description", "Cash", null, null);

        assertNull(account.getTagId());
    }

    @Test
    void accountWithTagHasTagId() {
        final GLAccountData account = projection.newInstance(1L, "Cash", null, "100", false, true, GLAccountType.ASSET.getValue(),
                GLAccountUsage.DETAIL.getValue(), "description", "Cash", 7, "Tag");

        final CodeValueData tag = account.getTagId();
        assertNotNull(tag);
        assertEquals(7L, tag.getId());
        assertEquals("Tag", tag.getName());
    }
}
