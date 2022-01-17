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
import org.eclipse.jgit.annotations.NonNull
import org.eclipse.jgit.annotations.Nullable
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.CanceledException
import org.eclipse.jgit.lib.CommitBuilder
import org.eclipse.jgit.lib.GpgSigner
import org.eclipse.jgit.lib.PersonIdent
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.StoredConfig
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.RefSpec
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class GitService {
    private static final Logger log = LoggerFactory.getLogger(GitService.class)

    private Git git
    private boolean dryRun

    GitService(FineractPluginExtension.FineractPluginConfigGit config, FineractPluginExtension.FineractPluginConfigGpg gpgConfig) {
        GpgSigner.setDefault(new GpgSigner() {
            @Override
            void sign(@NonNull CommitBuilder commit, @Nullable String gpgSigningKey, @NonNull PersonIdent committer, CredentialsProvider credentialsProvider) throws CanceledException {
                log.warn("------------------------ KEY: ${gpgSigningKey} IDENT: ${committer}")
            }

            @Override
            boolean canLocateSigningKey(@Nullable String gpgSigningKey, @NonNull PersonIdent committer, CredentialsProvider credentialsProvider) throws CanceledException {
                log.warn("------------------------ KEY: ${gpgSigningKey} IDENT: ${committer}")
                return false
            }
        });

        Repository repository = new FileRepositoryBuilder()
                .readEnvironment()
                .setGitDir(new File(config.dir))
                .findGitDir()
                .build();

        this.git = new Git(repository)
        this.dryRun = config.dryRun
    }

    void clone(String url, String branch, String directory) {
        def ref = "refs/heads/" + branch

        Git.cloneRepository()
                .setURI(url)
                .setDirectory(new File(directory))
                .setBranchesToClone(Arrays.asList(ref))
                .setBranch(ref)
                .call();
    }

    void pull(String directory) {
        Git git = Git.open(new File(directory));

        git.pull().call()
    }

    void commit(String directory) {
        Git git = Git.open(new File(directory));

        def status = git.status().call()

        if(!status.isClean()) {
            git.add().addFilepattern(".").call()

            git.commit().setSign(true).setMessage("chore: Publish current docs").call()
        }

        def result = git.push().call()
    }

    void config(String directory, List<FineractPluginExtension.FineractPluginConfigGitSection> sections) {
        Git git = Git.open(new File(directory));

        StoredConfig gitConfig = git.getRepository().getConfig();

        sections.forEach(section -> {
            gitConfig.setString(section.section, section.subsection, section.name, section.value);
        })
        // gitConfig.setString("branch", "master", "merge", "refs/heads/master");
        gitConfig.save();
    }

    void createTag(String name, String message) {
        def ref = git.tag()
                .setName(name)
                .setMessage(message)
                .setAnnotated(true)
                .setForceUpdate(true)
                .call()

        try {
            git.push().setPushTags().setForce(true).setDryRun(dryRun).call()

            log.warn("Tag created: ${name} (${ref.name})")
        } catch(Exception e) {
            log.error(e.toString(), e)
        }
    }

    // TODO: explicitly provide the directory
    void removeTag(String name) {
        git.tagDelete().setTags(name).call()

        RefSpec refSpec = new RefSpec(":refs/tags/${name}");
        git.push().setRefSpecs(refSpec).setRemote("origin").call()

        log.warn("Tag deleted: ${name}")
    }
}
