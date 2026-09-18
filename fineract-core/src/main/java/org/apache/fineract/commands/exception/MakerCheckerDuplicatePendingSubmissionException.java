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
package org.apache.fineract.commands.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformException;

/**
 * Thrown when a maker-only user submits a maker-checker enabled action for which a submission is already awaiting
 * checker approval for the same action, entity and resource.
 */
public class MakerCheckerDuplicatePendingSubmissionException extends AbstractPlatformException {

    public MakerCheckerDuplicatePendingSubmissionException(final String actionName, final String entityName) {
        super("error.msg.maker.checker.duplicate.pending.submission",
                "This action is already pending checker approval. Please wait for it to be approved or rejected before resubmitting.",
                new Object[] { actionName, entityName });
    }
}
