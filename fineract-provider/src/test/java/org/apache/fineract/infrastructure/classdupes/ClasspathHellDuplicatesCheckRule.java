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
import junit.framework.AssertionFailedError;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * JUnit Rule to run detect duplicate entries on the classpath. Usage:
 *
 * <pre>public static {@literal @}ClassRule ClasspathHellDuplicatesCheckRule
 *     dupes = new ClasspathHellDuplicatesCheckRule();</pre>
 *
 * <p>NB that the basepom/duplicate-finder-maven-plugin already runs as part of odlparent.
 * It has a similar purpose, but covers build time instead of runtime testing.  This JUnit Rule class is
 * thus recommended to be used in particular in tests which previously ran into JAR Hell issues, and for
 * which non-regression with a clear failure message in case of future similar problems is important.
 * (This provides more details at runtime than duplicate-finder-maven-plugin does at build time.)
 *
 * @author Michael Vorburger.ch
 */
public class ClasspathHellDuplicatesCheckRule implements TestRule {

  private final ClasspathHellDuplicatesChecker checker;

  public ClasspathHellDuplicatesCheckRule(ClasspathHellDuplicatesChecker checker) {
    this.checker = checker;
  }

  public ClasspathHellDuplicatesCheckRule() {
    this(ClasspathHellDuplicatesChecker.INSTANCE);
  }

  @Override
  public Statement apply(Statement base, Description description) {
    checkClasspath();
    return base;
  }

  protected void checkClasspath() {
    Map<String, List<String>> dupes = checker.getDuplicates();
    if (!dupes.isEmpty()) {
      throw new AssertionFailedError(
          dupes.size() + " Classpath duplicates detected:\n" + checker.toString(dupes));
    }
  }
}
