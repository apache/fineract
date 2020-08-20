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

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ResourceList;
import io.github.classgraph.ScanResult;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Check classpath for duplicates.
 *
 * @author Michael Vorburger.ch
 */
public class ClasspathHellDuplicatesChecker {

    public static final ClasspathHellDuplicatesChecker INSTANCE = new ClasspathHellDuplicatesChecker();

    private final Map<String, List<String>> duplicates;

    public ClasspathHellDuplicatesChecker() {
        duplicates = recheck();
    }

    public Map<String, List<String>> getDuplicates() {
        return duplicates;
    }

    public String toString(Map<String, List<String>> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            sb.append(entry.getKey());
            sb.append('\n');
            for (String location : entry.getValue()) {
                sb.append("    ");
                sb.append(location);
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    private Map<String, List<String>> recheck() {
        Map<String, List<String>> dupes = new HashMap<>();
        // To debug this scanner, use ClassGraph().verbose()
        // We intentionally do not use .classFilesOnly(), or
        // .nonClassFilesOnly(), to check both
        try (ScanResult scanResult = new ClassGraph().scan()) {
            for (Map.Entry<String, ResourceList> dupe : scanResult.getAllResources().findDuplicatePaths()) {
                String resourceName = dupe.getKey();
                if (!isHarmlessDuplicate(resourceName)) {
                    boolean addIt = true;
                    List<String> jars = dupe.getValue().stream().map(resource -> resource.getURL().toExternalForm())
                            .collect(Collectors.toList());
                    for (String jar : jars) {
                        if (skipJAR(jar)) {
                            addIt = false;
                            break;
                        }
                    }
                    if (addIt) {
                        dupes.put(resourceName, jars);
                    }
                }
            }
            return dupes;
        }
    }

    private boolean skipJAR(String jarPath) {
        // ./gradlew test finds classes from the Gradle Wrapper (which don't
        // show up in-IDE), exclude those
        return jarPath.contains("/.gradle/wrapper/dists/");
    }

    protected boolean isHarmlessDuplicate(String resourcePath) {
        // list from org.jhades.reports.DuplicatesReport
        return resourcePath.equals("META-INF/MANIFEST.MF") || resourcePath.equals("META-INF/INDEX.LIST")
                || resourcePath.equals("META-INF/ORACLE_J.SF") || resourcePath.toUpperCase().startsWith("META-INF/ASL")
                || resourcePath.toUpperCase().startsWith("META-INF/NOTICE") || resourcePath.toUpperCase().startsWith("META-INF/LICENSE")
                || resourcePath.toUpperCase().startsWith("LICENSE") || resourcePath.toUpperCase().startsWith("LICENSE/NOTICE")
                // list formerly in ClasspathHellDuplicatesCheckRule (moved here
                // in INFRAUTILS-52)
                || resourcePath.endsWith(".txt") || resourcePath.endsWith("LICENSE") || resourcePath.endsWith("license.html")
                || resourcePath.endsWith("about.html") || resourcePath.endsWith("readme.html")
                || resourcePath.startsWith("META-INF/services") || resourcePath.equals("META-INF/DEPENDENCIES")
                || resourcePath.equals("META-INF/git.properties") || resourcePath.equals("META-INF/io.netty.versions.properties")
                || resourcePath.equals("META-INF/jersey-module-version") || resourcePath.startsWith("OSGI-INF/blueprint/")
                || resourcePath.startsWith("org/opendaylight/blueprint/") || resourcePath.endsWith("reference.conf") // in
                                                                                                                     // Akka's
                                                                                                                     // JARs
                // json-schema-core and json-schema-validator depend on each
                // other and include these files
                || resourcePath.equals("draftv4/schema") || resourcePath.equals("draftv3/schema") //
                || resourcePath.equals("WEB-INF/web.xml") || resourcePath.equals("META-INF/web-fragment.xml")
                || resourcePath.equals("META-INF/eclipse.inf") || resourcePath.equals("META-INF/ECLIPSE_.SF")
                || resourcePath.equals("META-INF/ECLIPSE_.RSA") || resourcePath.equals("META-INF/BC2048KE.DSA")
                || resourcePath.equals("META-INF/BC2048KE.SF") || resourcePath.equals("META-INF/BC1024KE.SF")
                || resourcePath.equals("OSGI-INF/bundle.info")
                // Spring Framework knows what they are do..
                || resourcePath.startsWith("META-INF/spring") || resourcePath.startsWith("META-INF/additional-spring")
                || resourcePath.startsWith("META-INF/terracotta")
                // Groovy is groovy
                || resourcePath.startsWith("META-INF/groovy")
                // Something doesn't to be a perfectly clean in Maven Surefire:
                || resourcePath.startsWith("META-INF/maven/") || resourcePath.contains("surefire")
                // org.slf4j.impl.StaticLoggerBinder.class in testutils for the
                // LogCaptureRule
                || resourcePath.equals("org/slf4j/impl/StaticLoggerBinder.class")
                // INFRAUTILS-35: JavaLaunchHelper is both in java and
                // libinstrument.dylib (?) on Mac OS X
                || resourcePath.contains("JavaLaunchHelper")
                // javax.annotation is a big mess... :( E.g.
                // javax.annotation.Resource (and some others)
                // are present both in rt.jar AND javax.annotation-api-1.3.2.jar
                // and similar - BUT those
                // JARs cannot just be excluded, because they contain some
                // additional annotations, in the
                // (reserved!) package javax.annotation, such as
                // javax.annotation.Priority et al. The
                // super proper way to address this cleanly would be to make our
                // own JAR for javax.annotation
                // and have it contain ONLY what is not already in package
                // javax.annotation in rt.jar.. but for now:
                || resourcePath.equals("javax/annotation/Resource$AuthenticationType.class")
                // NEUTRON-205: javax.inject is a mess :( because of
                // javax.inject:javax.inject (which we widely use in ODL)
                // VS. org.glassfish.hk2.external:javax.inject (which Glassfish
                // Jersey has dependencies on). Attempts to
                // cleanly exclude glassfish.hk2's javax.inject and align
                // everything on only depending on
                // javax.inject:javax.inject have failed, because the OSGi
                // bundle
                // org.glassfish.jersey.containers.jersey-container-servlet-core
                // (2.25.1) has a non-optional Package-Import
                // for javax.inject, but we made javax.inject:javax.inject
                // <optional>true in odlparent, and don't bundle it.
                || resourcePath.startsWith("javax/inject/")
                // Java 9 modules
                || resourcePath.endsWith("module-info.class") || resourcePath.contains("findbugs")
                // list newly introduced in INFRAUTILS-52, because classgraph
                // scans more than JHades did
                || resourcePath.equals("plugin.properties") || resourcePath.equals(".api_description")
                // errorprone with Java 11 integration leaks to classpath, which
                // causes a conflict between
                // checkerframework/checker-qual and checkerframework/dataflow
                || resourcePath.startsWith("org/checkerframework/dataflow/qual/")
                // Pentaho reports harmless duplicates
                || resourcePath.endsWith("overview.html") || resourcePath.endsWith("classic-engine.properties")
                || resourcePath.endsWith("loader.properties");
    }
}
