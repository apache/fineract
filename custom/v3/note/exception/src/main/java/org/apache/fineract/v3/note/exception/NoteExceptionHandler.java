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

package org.apache.fineract.v3.note.exception;

import org.apache.fineract.infrastructure.core.data.ApiGlobalErrorResponse;
import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.apache.fineract.portfolio.group.exception.GroupNotFoundException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanTransactionNotFoundException;
import org.apache.fineract.portfolio.note.exception.NoteNotFoundException;
import org.apache.fineract.portfolio.note.exception.NoteResourceNotSupportedException;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

// TODO: this can be improved and restructured in a suitable way
@ControllerAdvice
public class NoteExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({ NoteResourceNotSupportedException.class, //
            NoteNotFoundException.class, //
            LoanNotFoundException.class, //
            GroupNotFoundException.class, //
            ClientNotFoundException.class, //
            SavingsAccountNotFoundException.class, //
            LoanTransactionNotFoundException.class //
    })
    public ResponseEntity<ApiGlobalErrorResponse> handleResourceNotFoundExceptions(AbstractPlatformResourceNotFoundException ex) {

        ApiGlobalErrorResponse response = ApiGlobalErrorResponse.notFound(//
                ex.getGlobalisationMessageCode(), //
                ex.getDefaultUserMessage(), //
                // With NoteResourceNotSupportedException, there should not be any args
                ex instanceof NoteResourceNotSupportedException ? null : ex.getDefaultUserMessageArgs() //
        );

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
}
