/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.paymenttype.handler;

import javax.transaction.Transactional;

import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.portfolio.paymenttype.service.PaymentTypeWriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreatePaymentTypeCommandHandler implements NewCommandSourceHandler {

    private final PaymentTypeWriteService paymentTypeWriteService;

    @Autowired
    public CreatePaymentTypeCommandHandler(final PaymentTypeWriteService paymentTypeWriteService) {
        this.paymentTypeWriteService = paymentTypeWriteService;
    }

    @Override
    @Transactional
    public CommandProcessingResult processCommand(JsonCommand command) {
        return this.paymentTypeWriteService.createPaymentType(command);
    }
}
