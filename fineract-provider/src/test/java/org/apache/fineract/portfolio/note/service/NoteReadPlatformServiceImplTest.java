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
package org.apache.fineract.portfolio.note.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.portfolio.note.domain.NoteType;
import org.junit.jupiter.api.Test;

class NoteReadPlatformServiceImplTest {

    @Test
    void getResourceConditionShouldSupportShareAccount() {
        List<Object> paramList = new ArrayList<>(List.of(42L));

        String condition = NoteReadPlatformServiceImpl.getResourceCondition(NoteType.SHARE_ACCOUNT, paramList);

        assertEquals(" n.share_account_id = ? and note_type_enum = ? ", condition);
        assertEquals(List.of(42L, NoteType.SHARE_ACCOUNT.getValue()), paramList);
    }
}
