/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.api.infrastructure;

import org.mifosplatform.accounting.api.commands.GLAccountCommand;
import org.mifosplatform.accounting.api.commands.GLClosureCommand;
import org.mifosplatform.accounting.api.commands.GLJournalEntryCommand;

public interface AccountingApiDataConversionService {

    GLAccountCommand convertJsonToGLAccountCommand(Long resourceIdentifier, String json);

    GLClosureCommand convertJsonToGLClosureCommand(Long resourceIdentifier, String json);

    GLJournalEntryCommand convertJsonToGLJournalEntryCommand(String json);

}