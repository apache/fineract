/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.boot.tests;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.mifosplatform.infrastructure.core.boot.EmbeddedTomcatWithSSLConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class EmbeddedTomcatWithSSLConfigurationTest {

    @Test
    public void testGetFileWithFileResource() throws IOException {
        // Test class probably isn't in a JAR
        checkClassResource(getClass());
    }

    @Test
    public void testGetFileWithClasspathResource() throws IOException {
        // Spring Resource class probably is in a JAR
        File f1 = checkClassResource(Resource.class);
        f1.delete();
        checkClassResource(Resource.class);
    }

    protected File checkClassResource(Class<?> clazz) throws IOException {
        String testClasspathResourcePath = clazz.getCanonicalName().replace('.', '/') + ".class";
        Resource r = new ClassPathResource(testClasspathResourcePath);
        File f = new EmbeddedTomcatWithSSLConfiguration().getFile(r);
        assertTrue(f.exists());
        f = new EmbeddedTomcatWithSSLConfiguration().getFile(r);
        assertTrue(f.exists());
        return f;
    }
}
