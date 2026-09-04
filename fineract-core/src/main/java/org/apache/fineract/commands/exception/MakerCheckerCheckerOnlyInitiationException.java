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
 * Thrown when a user who only holds the checker (`_CHECKER`) permission for a maker-checker enabled task - without the
 * base permission - attempts to initiate that task directly, instead of approving an existing pending submission.
 */
public class MakerCheckerCheckerOnlyInitiationException extends AbstractPlatformException {

    public MakerCheckerCheckerOnlyInitiationException(final String taskPermissionName) {
        super("error.msg.maker.checker.checker.only.cannot.initiate",
                "You have checker-only permission for this action. You cannot initiate it. Use maker-checker approval flow to approve a pending submission.",
                new Object[] { taskPermissionName });
    }
}
