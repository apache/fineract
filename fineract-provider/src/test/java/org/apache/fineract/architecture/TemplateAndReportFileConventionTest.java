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

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaConstructorCall;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Coding conventions for template rendering and report output files.
 */
class TemplateAndReportFileConventionTest {

    private static final String BASE = "org.apache.fineract";

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter().withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS).importPackages(BASE);
    }

    /**
     * Templates must be rendered through NoPartialsMustacheFactory (see TemplateMergeServiceImpl).
     */
    @Test
    void mustacheFactoriesMustBeCreatedOnlyViaNoPartialsMustacheFactory() {
        ArchRule rule = ArchRuleDefinition.noClasses() //
                .that(new DescribedPredicate<JavaClass>("are not subclasses of the default factory") {

                    @Override
                    public boolean test(JavaClass input) {
                        return !input.isAssignableTo("com.github.mustachejava.DefaultMustacheFactory");
                    }
                }) //
                .should().callConstructorWhere(constructorOf("com.github.mustachejava.DefaultMustacheFactory"));
        rule.check(classes);
    }

    /**
     * Report output files are located via ReportOutputFiles only.
     */
    @Test
    void campaignJobsMustLocateReportFilesOnlyViaReportOutputFiles() {
        ArchRule rule = ArchRuleDefinition.noClasses() //
                .that().resideInAPackage("org.apache.fineract.infrastructure.campaigns.jobs..") //
                .and().doNotHaveFullyQualifiedName("org.apache.fineract.infrastructure.campaigns.jobs.ReportOutputFiles") //
                .should().callMethodWhere(new DescribedPredicate<JavaMethodCall>("target is Path.resolve or Path.resolveSibling") {

                    @Override
                    public boolean test(JavaMethodCall call) {
                        return "java.nio.file.Path".equals(call.getTargetOwner().getName())
                                && ("resolve".equals(call.getName()) || "resolveSibling".equals(call.getName()));
                    }
                });
        rule.check(classes);
    }

    private static DescribedPredicate<JavaConstructorCall> constructorOf(String ownerName) {
        return new DescribedPredicate<JavaConstructorCall>("constructor of " + ownerName) {

            @Override
            public boolean test(JavaConstructorCall call) {
                return ownerName.equals(call.getTargetOwner().getName());
            }
        };
    }
}
