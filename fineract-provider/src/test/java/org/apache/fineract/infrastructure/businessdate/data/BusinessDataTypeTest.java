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
package org.apache.fineract.infrastructure.businessdate.data;

import static org.junit.Assert.assertEquals;

import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.junit.Test;

public class BusinessDataTypeTest {

    @Test
    public void typoCheck() {
        for (BusinessDateType businessDateType : BusinessDateType.values()) {
            switch (businessDateType) {
                case BUSINESS_DATE -> assertEquals("Business Date", businessDateType.getDescription());
                case COB_DATE -> assertEquals("Close of Business Date", businessDateType.getDescription());
            }
        }
    }
}
