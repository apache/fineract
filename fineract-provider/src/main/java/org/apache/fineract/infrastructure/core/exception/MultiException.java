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

/**
 * Exception with multiple root causes.
 *
 * Intended to be used in places where N operations are performed in a loop over something,
 * each of which could fail, but where we don't want to fail immediately but continue, and
 * then fail at end.
 *
 * <p>The failures MUST each be logged within the loop, as they occur; this exception is
 * only thrown to propagate the failure, but will not contain and re-log the details.
 *
 * @author Michael Vorburger.ch <mike@vorburger.ch>
 */
public class MultiException extends Exception {

    private final int n;

    public MultiException(int n) {
        super(n + "x failures occured here (details have been previously logged");
        this.n = n;
    }

    public int getCausesSize() {
        return n;
    }
}
