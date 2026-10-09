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
package org.apache.fineract.infrastructure.dataqueries.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class DatatableEntryRequiredExceptionTest {

    @Test
    void namesTheDatatableTheActionAndTheEntity() {
        DatatableEntryRequiredException exception = new DatatableEntryRequiredException(List.of("dt_loan_details"), "m_loan", 200);

        assertEquals("Please fill in the data table [dt_loan_details] before approving this loan.", exception.getDefaultUserMessage());
        assertEquals("error.msg.entry.required.in.datatable.[dt_loan_details]", exception.getGlobalisationMessageCode());
    }

    @Test
    void usesThePluralForSeveralDatatables() {
        DatatableEntryRequiredException exception = new DatatableEntryRequiredException(List.of("dt_first", "dt_second"), "m_client", 100);

        assertEquals("Please fill in the data tables [dt_first, dt_second] before creating this client.",
                exception.getDefaultUserMessage());
        assertEquals("error.msg.entry.required.in.datatable.[dt_first, dt_second]", exception.getGlobalisationMessageCode());
    }

    @Test
    void describesSavingsAndWriteOffActions() {
        assertEquals("Please fill in the data table [dt_savings_details] before activating this savings account.",
                new DatatableEntryRequiredException(List.of("dt_savings_details"), "m_savings_account", 300).getDefaultUserMessage());
        assertEquals("Please fill in the data table [dt_loan_details] before writing off this loan.",
                new DatatableEntryRequiredException(List.of("dt_loan_details"), "m_loan", 601).getDefaultUserMessage());
    }

    @Test
    void fallsBackToAGenericActionWhenEntityOrStatusIsUnknown() {
        assertEquals("Please fill in the data table [dt_extra] before continuing.",
                new DatatableEntryRequiredException(List.of("dt_extra"), "m_unknown", 100).getDefaultUserMessage());
        assertEquals("Please fill in the data table [dt_extra] before continuing.",
                new DatatableEntryRequiredException(List.of("dt_extra"), "m_loan", 999).getDefaultUserMessage());
    }
}
