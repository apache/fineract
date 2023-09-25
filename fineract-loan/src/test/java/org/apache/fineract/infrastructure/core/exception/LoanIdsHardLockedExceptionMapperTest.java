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
package org.apache.fineract.infrastructure.core.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.fineract.infrastructure.jobs.exception.LoanIdsHardLockedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LoanIdsHardLockedExceptionMapperTest {

    @Test
    public void testExceptionMapper() {
        LoanIdsHardLockedExceptionMapper exceptionMapper = new LoanIdsHardLockedExceptionMapper();
        Response response = exceptionMapper.toResponse(new LoanIdsHardLockedException(123L));
        Assertions.assertEquals(4090, exceptionMapper.errorCode());

        Assertions.assertEquals("{\"developerMessage\":\"Loan is locked by the COB job. Loan ID: 123\"," + "\"httpStatusCode\":\"409\","
                + "\"defaultUserMessage\":\"Loan is locked by the COB job. Loan ID: 123\","
                + "\"userMessageGlobalisationCode\":\"error.msg.loan.locked\"}", response.getEntity());
        Assertions.assertEquals(409, response.getStatus());
        Assertions.assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
    }
}
