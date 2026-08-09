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
package org.apache.fineract.test.messaging.event.workingcapitalloan.journalentry;

import java.util.function.Function;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanJournalEntryDataV1;
import org.apache.fineract.test.messaging.event.Event;

public abstract class AbstractWorkingCapitalLoanJournalEntryEvent implements Event<WorkingCapitalLoanJournalEntryDataV1> {

    @Override
    public Class<WorkingCapitalLoanJournalEntryDataV1> getDataClass() {
        return WorkingCapitalLoanJournalEntryDataV1.class;
    }

    @Override
    public Function<WorkingCapitalLoanJournalEntryDataV1, Long> getIdExtractor() {
        return WorkingCapitalLoanJournalEntryDataV1::getLoanId;
    }
}
