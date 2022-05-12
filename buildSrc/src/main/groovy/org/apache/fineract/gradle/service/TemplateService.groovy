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
package org.apache.fineract.gradle.service

import freemarker.core.Environment
import freemarker.template.Configuration
import freemarker.template.Template
import freemarker.template.TemplateException
import freemarker.template.TemplateExceptionHandler
import org.apache.fineract.gradle.FineractPluginExtension
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TemplateService {
    private static final Logger log = LoggerFactory.getLogger(TemplateService.class)

    private Configuration config

    TemplateService(FineractPluginExtension.FineractPluginConfigTemplate config) {
        def dir = new File(config.templateDir);

        this.config = new Configuration(Configuration.VERSION_2_3_31);
        this.config.setDirectoryForTemplateLoading(dir)
        this.config.setDefaultEncoding("UTF-8");
        this.config.setLogTemplateExceptions(false);
        this.config.setWrapUncheckedExceptions(true);
        this.config.setFallbackOnNullLoopVariable(false);
        this.config.setTemplateExceptionHandler(new TemplateExceptionHandler() {
            @Override
            void handleTemplateException(TemplateException te, Environment env, Writer out) throws TemplateException {
                if (!env.isInAttemptBlock()) {
                    PrintWriter pw = (out instanceof PrintWriter) ? (PrintWriter) out : new PrintWriter(out);
                    pw.print("FreeMarker template error\n");
                    te.printStackTrace(pw, false, true, false);
                    pw.flush();  // To commit the HTTP response
                }
            }}
        )
    }

    FineractPluginExtension.FineractPluginTemplateParams render(FineractPluginExtension.FineractPluginTemplateParams params, Object data) {
        Template template = null;

        if(params.templateFile) {
            template = new Template("template", new FileReader(new File(params.templateFile)), this.config)
        }
        if(params.template) {
            template = new Template("template", new StringReader(params.template), this.config)
        }

        if(template) {
            if(params.outputFile) {
                def output = new File(params.outputFile)
                output.createNewFile()

                template.process(data, new FileWriter(output, false))
            } else {
                def output = new StringWriter()

                template.process(data, output)

                params.output = output.toString()
            }
        }

        return params
    }
}
