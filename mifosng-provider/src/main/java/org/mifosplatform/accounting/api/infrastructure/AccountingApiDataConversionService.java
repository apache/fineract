package org.mifosplatform.accounting.api.infrastructure;

import org.mifosplatform.accounting.api.commands.GLAccountCommand;
import org.mifosplatform.accounting.api.commands.GLClosureCommand;

public interface AccountingApiDataConversionService {

    GLAccountCommand convertJsonToGLAccountCommand(Long resourceIdentifier, String json);

    GLClosureCommand convertJsonToGLClosureCommand(Long resourceIdentifier, String json);

}