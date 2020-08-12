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
package org.apache.fineract.infrastructure.classdupes;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.opentest4j.AssertionFailedError;

/**
 * JUnit Rule to run detect duplicate entries on the classpath. Usage:
 *
 * <pre>
 * public static {@literal @}ClassRule ClasspathHellDuplicatesCheckRule
 *     dupes = new ClasspathHellDuplicatesCheckRule();
 * </pre>
 *
 * <p>
 * NB that the basepom/duplicate-finder-maven-plugin already runs as part of odlparent. It has a similar purpose, but
 * covers build time instead of runtime testing. This JUnit Rule class is thus recommended to be used in particular in
 * tests which previously ran into JAR Hell issues, and for which non-regression with a clear failure message in case of
 * future similar problems is important. (This provides more details at runtime than duplicate-finder-maven-plugin does
 * at build time.)
 *
 * @author Michael Vorburger.ch
 */
public class ClasspathHellDuplicatesCheckExtension implements AfterEachCallback {

    private final ClasspathHellDuplicatesChecker checker;

    public ClasspathHellDuplicatesCheckExtension(ClasspathHellDuplicatesChecker checker) {
        this.checker = checker;
    }

    public ClasspathHellDuplicatesCheckExtension() {
        this(ClasspathHellDuplicatesChecker.INSTANCE);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        checkClasspath();
    }

    protected void checkClasspath() {
        Map<String, List<String>> dupes = checker.getDuplicates();
        if (!dupes.isEmpty()) {
            throw new AssertionFailedError(dupes.size() + " Classpath duplicates detected:\n" + checker.toString(dupes));
        }
    }
}
