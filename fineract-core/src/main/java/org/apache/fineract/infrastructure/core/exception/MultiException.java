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

import com.google.common.io.CharStreams;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Exception with multiple root causes.
 *
 * Intended to be used in places where N operations are performed in a loop over something, each of which could fail,
 * but where we don't want to fail immediately but continue, and then fail at end.
 *
 * <p>
 * The failures should typically also each be logged within the loop, as they occur; this exception is only thrown to
 * propagate the failure, and the caller may or may not log this with the details.
 *
 * <p>
 * Originally inspired by org.junit.runners.model.MultipleFailureException
 * </p>
 * .
 *
 * @author Michael Vorburger.ch <mike@vorburger.ch>
 */
@Slf4j
public class MultiException extends Exception {

    private final List<Throwable> throwables;

    @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
    public MultiException(String message, List<Throwable> problems) {
        super(message);
        if (problems.isEmpty()) {
            throw new IllegalArgumentException("List of Throwables must not be empty");
        }
        this.throwables = new ArrayList<>(problems);
    }

    @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
    public MultiException(List<Throwable> problems) {
        this("MultiException with " + problems.size() + " contained causes (details available)", problems);
    }

    public List<Throwable> getCauses() {
        return Collections.unmodifiableList(throwables);
    }

    @Override
    @SuppressWarnings("RegexpSinglelineJava")
    public String getMessage() {
        int i = 0;
        StringBuilder sb = new StringBuilder(super.getMessage());
        for (Throwable e : throwables) {
            sb.append("\n    ");
            sb.append(++i);
            sb.append(". ");
            Writer w = CharStreams.asWriter(sb);
            e.printStackTrace(new PrintWriter(w, true));
        }
        sb.append("\n  which was itself thrown..");
        return sb.toString();
    }

    @Override
    @SuppressWarnings("RegexpSinglelineJava")
    @SuppressFBWarnings("SLF4J_SIGN_ONLY_FORMAT")
    public void printStackTrace() {
        log.error("{}", super.getMessage());
        int i = 0;
        for (Throwable e : throwables) {
            log.error("{}.", ++i);
            e.printStackTrace();
        }
    }

    @Override
    @SuppressWarnings("RegexpSinglelineJava")
    public void printStackTrace(PrintStream s) {
        s.println(super.getMessage());
        int i = 0;
        for (Throwable e : throwables) {
            s.print(++i + ".");
            e.printStackTrace(s);
        }
    }

    @Override
    @SuppressWarnings("RegexpSinglelineJava")
    public void printStackTrace(PrintWriter s) {
        s.println(super.getMessage());
        int i = 0;
        for (Throwable e : throwables) {
            s.print(++i + ".");
            e.printStackTrace(s);
        }
    }
}
