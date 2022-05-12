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
package org.apache.fineract.gradle

import org.apache.fineract.gradle.service.JiraService
import org.gradle.api.Project

class FineractPluginExtension {
    Project project
    Map<String, FineractPluginStep> steps = [:]

    FineractPluginExtension(Project project) {
        this.project = project
    }

    static class FineractPluginConfig {
        String username
        String password
    }

    static class FineractPluginConfigDoc {
        String url
        String directory
        String branch
    }

    static class FineractPluginConfigJira {
        String url
        String username
        String password
        String project
    }

    static class FineractPluginConfigConfluence {
        String url
        String username
        String password
        String project
    }

    static class FineractPluginConfigGpg {
        String keyName
        String publicKeyring
        String secretKeyring
        String password
    }

    static class FineractPluginConfigTemplate {
        String templateDir
    }

    static class FineractPluginConfigSmtp {
        String host
        int port
        String username
        String password
        boolean tls
        boolean ssl
    }

    static class FineractPluginConfigGit {
        String username
        String password
        String dir
        boolean dryRun
        List<FineractPluginConfigGitSection> sections
    }

    static class FineractPluginConfigGitSection {
        String section
        String subsection
        String name
        String value
    }

    static class FineractPluginConfigSubversion {
        String username
        String password
        String revision
    }

    static class FineractPluginStep {
        int order
        String description
        FineractPluginEmailParams email
        FineractPluginJiraParams jira
        FineractPluginTemplateParams template
        FineractPluginConfluenceParams confluence
        FineractPluginGitParams git
        FineractPluginSubversionParams subversion
        FineractPluginGpgParams gpg
    }

    static class FineractPluginEmailParams {
        String from
        String name
        String to
        String cc
        String bcc
        String mime
        String subject
        FineractPluginTemplateParams subjectTemplate
        String message
        FineractPluginTemplateParams messageTemplate
    }

    static class FineractPluginJiraParams {
        String command
        String projectId
        String fields = "*all"
        String query
        List<JiraService.JiraIssue> result = new ArrayList<>()
        int pageOffset = 0
        int pageSize= 50
        int total = 1000
        List<String> includes = ["summary", "status", "assignee", "fixVersions"]
    }

    static class FineractPluginConfluenceParams {
        String title
        String content
        Integer ancestor
    }

    static class FineractPluginGitParams {
        String tag
        String message
        FineractPluginTemplateParams messageTemplate
    }

    static class FineractPluginSubversionParams {
        String url
        String command
        String revision = "HEAD"
        String directory
    }

    static class FineractPluginTemplateParams {
        String template
        String templateFile
        String output
        String outputFile
    }

    static class FineractPluginGpgParams {
        List<String> files
    }
}
