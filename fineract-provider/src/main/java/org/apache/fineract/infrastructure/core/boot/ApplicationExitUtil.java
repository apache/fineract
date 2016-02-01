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
package org.apache.fineract.infrastructure.core.boot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.context.ConfigurableApplicationContext;

public abstract class ApplicationExitUtil {

    private ApplicationExitUtil() {}

    public static void waitForKeyPressToCleanlyExit(ConfigurableApplicationContext ctx) throws IOException {

        // NOTE: In Eclipse, the Shutdown Hooks are not invoked on exit (red
        // button).. In the case of MariaDB4j that's a problem because then the
        // mysqld won't be stopped, so:
        // (@see https://bugs.eclipse.org/bugs/show_bug.cgi?id=38016)
        System.out.println("\nHit Enter to quit...");
        // NOTE: In Eclipse, System.console() is not available.. so:
        // (@see https://bugs.eclipse.org/bugs/show_bug.cgi?id=122429)
        BufferedReader d = new BufferedReader(new InputStreamReader(System.in));
        d.readLine();

        ctx.stop();
        ctx.close();
    }
}