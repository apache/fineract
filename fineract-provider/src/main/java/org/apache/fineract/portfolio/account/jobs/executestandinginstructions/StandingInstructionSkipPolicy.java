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
package org.apache.fineract.portfolio.account.jobs.executestandinginstructions;

import org.springframework.batch.core.step.skip.SkipPolicy;

/**
 * Skips any instruction whose execution failed, without a limit.
 *
 * <p>
 * An instruction that cannot be paid — most often for want of funds — is a normal outcome of a mandate, recorded
 * against that mandate, and not a reason to abandon the instructions that follow it. A count-based limit would make the
 * job fail on a day when many accounts happen to be short, which is exactly the day it needs to run. Errors are not
 * skipped: those say something is wrong with the run itself rather than with an instruction.
 * </p>
 */
public class StandingInstructionSkipPolicy implements SkipPolicy {

    @Override
    public boolean shouldSkip(final Throwable t, final long skipCount) {
        return t instanceof Exception;
    }
}
