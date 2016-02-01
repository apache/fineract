/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.jobs.annotation;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.mifosplatform.infrastructure.jobs.service.JobName;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

/**
 * Parser to find method which is marked with CronTargetMethod annotation
 */
public class CronMethodParser {

    public static class ClassMethodNamesPair {

        public String className;
        public String methodName;
    }

    private static final String SEARCH_PACKAGE = "org.mifosplatform.";

    private static final String CRON_ANNOTATION_ATTRIBUTE_NAME = "jobName";

    private static final String RESOURCE_PATTERN = "**/*.class";

    private static final Map<String, ClassMethodNamesPair> targetMethosMap = new HashMap<>();

    private static final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    private static final MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

    public static ClassMethodNamesPair findTargetMethodDetails(final String attributeValue) throws IOException {
        if (!targetMethosMap.containsKey(attributeValue)) {
            findAnnotationMethods(CronTarget.class, CRON_ANNOTATION_ATTRIBUTE_NAME);
        }
        return targetMethosMap.get(attributeValue);
    }

    /**
     * method adds all the method names to map with annotation attribute value
     * as key
     */
    private static void findAnnotationMethods(final Class<? extends Annotation> annotationClass, final String attributeName)
            throws IOException {
        final String basePackagePath = ClassUtils.convertClassNameToResourcePath(new StandardEnvironment()
                .resolveRequiredPlaceholders(SEARCH_PACKAGE));
        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + basePackagePath + "/" + RESOURCE_PATTERN;
        packageSearchPath = packageSearchPath.replace("//", "/"); // else it doesn't work if *.class are in WAR!!
        final Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
        for (final Resource resource : resources) {
            if (resource.isReadable()) {
                final MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                final Set<MethodMetadata> metadataSet = metadataReader.getAnnotationMetadata().getAnnotatedMethods(
                        annotationClass.getName());
                if (metadataSet != null && metadataSet.size() > 0) {
                    for (final MethodMetadata metadata : metadataSet) {
                        final Map<String, Object> attributes = metadata.getAnnotationAttributes(annotationClass.getName());
                        final JobName attributeValue = (JobName) attributes.get(attributeName);
                        final String className = metadata.getDeclaringClassName();
                        final ClassMethodNamesPair pair = new ClassMethodNamesPair();
                        pair.className = className;
                        pair.methodName = metadata.getMethodName();
                        targetMethosMap.put(attributeValue.toString(), pair);
                    }
                }
            }
        }
    }
}
