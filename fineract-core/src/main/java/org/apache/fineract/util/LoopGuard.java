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
package org.apache.fineract.util;

import java.util.function.Predicate;

/**
 * Loop Guard is a utility solution to avoid endless loops
 *
 * Example: LoopGuard.runSafeDoWhileLoop(500, () -> { // loop body }, () -> conditions() );
 */
public final class LoopGuard {

    private LoopGuard() {}

    public interface LoopBody<T extends LoopContext> {

        void execute(T context);
    }

    public static <T extends LoopContext> void runSafeDoWhileLoop(int maxIterations, T context, Predicate<T> condition, LoopBody<T> body) {
        int count = 0;
        do {
            if (++count > maxIterations) {
                throw new IllegalStateException("Loop exceeded " + maxIterations + " iterations. Possible infinite loop.");
            }
            body.execute(context);
        } while (condition.test(context));
    }

    public static <T extends LoopContext> void runSafeWhileLoop(int maxIterations, T context, Predicate<T> condition, LoopBody<T> body) {
        int count = 0;
        while (condition.test(context)) {
            if (++count > maxIterations) {
                throw new IllegalStateException("Loop exceeded " + maxIterations + " iterations. Possible infinite loop.");
            }
            body.execute(context);
        }
    }
}
