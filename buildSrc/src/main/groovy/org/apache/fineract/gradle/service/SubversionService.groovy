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

import org.apache.fineract.gradle.FineractPluginExtension
import org.gradle.api.GradleException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.tmatesoft.svn.core.SVNCancelException
import org.tmatesoft.svn.core.SVNDepth
import org.tmatesoft.svn.core.SVNException
import org.tmatesoft.svn.core.SVNURL
import org.tmatesoft.svn.core.wc.*

class SubversionService {
    private static final Logger log = LoggerFactory.getLogger(SubversionService.class)

    private SVNUpdateClient client;
    private String revision;

    SubversionService(FineractPluginExtension.FineractPluginConfigSubversion config) {
        ISVNOptions options = SVNWCUtil.createDefaultOptions(true)
        SVNClientManager clientManager = SVNClientManager.newInstance(options, SVNWCUtil.createDefaultAuthenticationManager(config.username, config.password))

        this.client = clientManager.updateClient;
        this.client.ignoreExternals = false
        this.client.eventHandler = new ISVNEventHandler() {
            @Override
            void handleEvent(SVNEvent event, double progress) throws SVNException {
                println ">>> PROGRESS: ${progress>0?:'-'}"
            }

            @Override
            void checkCancelled() throws SVNCancelException {

            }
        }

        this.revision = config.revision
    }

    void checkout(FineractPluginExtension.FineractPluginSubversionParams params) {
        SVNURL svnUrl = SVNURL.parseURIEncoded(params.url)

        SVNRevision svnRevision = this.revision == null ? SVNRevision.HEAD : SVNRevision.parse(this.revision)

        if (svnRevision == SVNRevision.UNDEFINED) {
            throw new GradleException("Invalid SVN revision: " + this.revision)
        }

        this.client.doCheckout(svnUrl, new File(params.directory), SVNRevision.HEAD, svnRevision, SVNDepth.INFINITY, false)
    }

    void commit(FineractPluginExtension.FineractPluginSubversionParams params) {
        // TODO: implement this
        SVNURL svnUrl = SVNURL.parseURIEncoded(params.url)
    }
}
