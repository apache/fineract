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
package org.apache.fineract.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Source scan that keeps ORDER BY clauses limited to validated values.
 */
class OrderByConventionTest {

    private static final Pattern ORDER_BY_APPEND = Pattern.compile("(?i)order\\s+by\\s*\"\\s*\\)\\s*\\.append\\(\\s*([^;]*)");

    private static final Pattern ORDER_BY_PLUS = Pattern.compile("(?i)order\\s+by\\s*\"\\s*\\+\\s*([^;]*)");

    private static final Pattern ORDER_BY_OK_OPERAND = Pattern
            .compile("^(\"|'|(this\\.)?(\\w+\\.)?getOrderBy\\(\\)|(this\\.)?(\\w+\\.)?orderBySql\\(\\))");

    // These append a local variable that is taken from SearchParameters.getOrderBy() or validated right before use.
    private static final Set<String> ORDER_BY_VARIABLE_EXCEPTIONS = Set.of("organisation/office/service/OfficeReadPlatformServiceImpl.java",
            "portfolio/client/service/ClientReadPlatformServiceImpl.java", "portfolio/group/service/CenterReadPlatformServiceImpl.java",
            "infrastructure/dataqueries/service/DatatableUtil.java");

    /**
     * Values appended after "order by" must come from getOrderBy()/orderBySql() or be literals.
     */
    @Test
    void orderByClausesMustUseValidatedValues() throws IOException {
        List<String> violations = new ArrayList<>();
        for (Path file : mainFiles("src/main/java", ".java")) {
            String rel = file.toString().replace('\\', '/');
            if (ORDER_BY_VARIABLE_EXCEPTIONS.stream().anyMatch(rel::endsWith)) {
                continue;
            }
            String content = Files.readString(file);
            for (Pattern pattern : List.of(ORDER_BY_APPEND, ORDER_BY_PLUS)) {
                Matcher matcher = pattern.matcher(content);
                while (matcher.find()) {
                    if (!isValidatedOperand(matcher.group(1).trim())) {
                        violations.add(rel + ": order by followed by '" + abbreviate(matcher.group(1).trim()) + "'");
                    }
                }
            }
        }
        assertThat(violations).as("order by must be followed by getOrderBy(), orderBySql() or a literal").isEmpty();
    }

    private static boolean isValidatedOperand(String operand) {
        return ORDER_BY_OK_OPERAND.matcher(operand).find();
    }

    private static String abbreviate(String text) {
        return text.length() > 60 ? text.substring(0, 60) : text;
    }

    // ---------------------------------------------------------------- helpers

    private static Path repositoryRoot() {
        Path dir = Path.of("").toAbsolutePath();
        while (dir != null && !Files.exists(dir.resolve("settings.gradle"))) {
            dir = dir.getParent();
        }
        assertThat(dir).as("repository root containing settings.gradle").isNotNull();
        return dir;
    }

    private static List<Path> mainFiles(String sourceFolder, String... suffixes) throws IOException {
        List<Path> result = new ArrayList<>();
        try (Stream<Path> modules = Files.list(repositoryRoot())) {
            for (Path module : (Iterable<Path>) modules.filter(Files::isDirectory)
                    .filter(p -> p.getFileName().toString().startsWith("fineract-")
                            && !p.getFileName().toString().startsWith("fineract-e2e-tests"))::iterator) {
                Path source = module.resolve(sourceFolder);
                if (!Files.isDirectory(source)) {
                    continue;
                }
                try (Stream<Path> files = Files.walk(source)) {
                    files.filter(Files::isRegularFile).filter(p -> {
                        String name = p.getFileName().toString();
                        for (String suffix : suffixes) {
                            if (name.endsWith(suffix)) {
                                return true;
                            }
                        }
                        return false;
                    }).forEach(result::add);
                }
            }
        }
        return result;
    }
}
