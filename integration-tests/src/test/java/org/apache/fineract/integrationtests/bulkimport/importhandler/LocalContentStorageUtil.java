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
package org.apache.fineract.integrationtests.bulkimport.importhandler;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class LocalContentStorageUtil {

    private LocalContentStorageUtil() {}

    public static String path(String path) {
        var currentPath = Path.of("").toAbsolutePath();

        if (Files.exists(Path.of(path))) {
            return path;
        }

        if (Files.exists(Path.of("/", path))) {
            return "/" + path;
        }

        final var ghaPath = Path.of("/home/runner/.fineract/DefaultDemoTenant").resolve(path);

        // github actions
        if (Files.exists(ghaPath)) {
            return ghaPath.toString();
        }

        // local dev run
        final var devRunPath = Path.of(System.getProperty("user.home")).toAbsolutePath().resolve(".fineract/DefaultDemoTenant")
                .resolve(path);

        if (Files.exists(devRunPath)) {
            return devRunPath.toString();
        }

        // local docker volumes
        final var dockerPath = Path.of("").toAbsolutePath().getParent().resolve("build/fineract/tmp/DefaultDemoTenant").resolve(path);

        if (Files.exists(dockerPath)) {
            return dockerPath.toString();
        }

        throw new RuntimeException("Cannot find local fineract path: " + path + " (" + currentPath + ")");
    }

    @SuppressWarnings("UnusedMethod")
    public static void waitFor(String path) throws InterruptedException {
        Path file = Paths.get(path);
        long maxWaitMillis = 10000;
        long start = System.currentTimeMillis();
        boolean exists = false;

        while ((System.currentTimeMillis() - start) < maxWaitMillis) {
            if (Files.exists(file)) {
                exists = true;
                break;
            }

            Thread.sleep(500);
        }

        if (exists) {
            log.info("File found!");
        } else {
            log.warn("Timed out waiting for file.");
        }
    }
}
