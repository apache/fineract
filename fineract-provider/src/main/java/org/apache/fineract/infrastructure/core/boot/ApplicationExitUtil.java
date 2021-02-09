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
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

public abstract class ApplicationExitUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationExitUtil.class);

    private ApplicationExitUtil() {}

    public static void waitForKeyPressToCleanlyExit(ConfigurableApplicationContext ctx) throws IOException {
        // In some environments, System.console() is not available (e.g. "./gradlew bootRun", or in a container, or in
        // Eclipse, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=122429.
        //
        // Also (but as a separate problem), e.g. in Eclipse, the Shutdown Hooks are not invoked on Exit with the Red
        // Button of the Console view (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=38016); that's a problem e.g.
        // in the case of MariaDB4j, because then mysqld won't be stopped; same problem similarly applies on any other
        // library else that relies on an orderly Spring Context / JVM shutdown.
        //
        if (System.console() == null) {
            LOG.info("\nNo Console available, running until stopped by signal/Ctrl-C...");
            boolean interrupted = false;
            do {
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            } while (!interrupted);
        } else {
            LOG.info("\nHit Enter to quit...");
            BufferedReader d = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            d.readLine();

            ctx.stop();
            ctx.close();
        }
    }
}
