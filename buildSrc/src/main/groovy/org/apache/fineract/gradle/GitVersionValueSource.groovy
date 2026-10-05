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

import groovy.transform.CompileStatic
import java.nio.file.Files
import java.util.regex.Matcher
import java.util.regex.Pattern
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import org.eclipse.jgit.errors.NoWorkTreeException
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevObject
import org.eclipse.jgit.revwalk.RevTag
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

/**
 * Derives the project version from git, reproducing the rules of me.qoomon's gradle-git-versioning-plugin 6.4.4 as
 * configured for Fineract. That plugin reads git (and spawns git processes) while configuring the build, which the
 * configuration cache forbids; a ValueSource is the sanctioned way to do that work, and Gradle re-obtains it on every
 * build to decide whether a cached configuration is still valid.
 *
 * Version rules: on a branch matching one of {@code releaseBranchPatterns}, or when HEAD carries a tag matching
 * {@code releaseTagPattern}, the version is the major.minor.patch of the describe tag; otherwise it is
 * major.(minor+1).0-SNAPSHOT. The describe tag is the first tag matching {@code describeTagPattern} found while
 * walking HEAD's ancestors (all parents, newest commit first) -- note this differs from `git describe`.
 * Returns no value when versioning is disabled or the project is not in a git repository.
 */
@CompileStatic
abstract class GitVersionValueSource implements ValueSource<String, Parameters> {

    interface Parameters extends ValueSourceParameters {

        DirectoryProperty getProjectDirectory()

        Property<String> getDescribeTagPattern()

        ListProperty<String> getReleaseBranchPatterns()

        Property<String> getReleaseTagPattern()
    }

    private static final Logger LOG = Logging.getLogger(GitVersionValueSource)

    private static final Pattern VERSION_PATTERN = Pattern.compile(
            '.*?(?<version>(?<core>(?<major>\\d+)(?:\\.(?<minor>\\d+)(?:\\.(?<patch>\\d+))?)?)(?:-(?<label>.*))?)|')

    private static final String NO_COMMIT = '0000000000000000000000000000000000000000'

    @Override
    String obtain() {
        String disable = commandOption('versioning.disable')
        if (disable != null && Boolean.parseBoolean(disable)) {
            LOG.warn('skip - versioning is disabled by command option')
            return null
        }

        FileRepositoryBuilder builder = new FileRepositoryBuilder().findGitDir(parameters.projectDirectory.get().asFile)
        if (builder.gitDir == null) {
            LOG.warn('skip - project is not part of a git repository')
            return null
        }

        Repository repository = builder.build()
        try {
            return version(repository)
        } finally {
            repository.close()
        }
    }

    private String version(Repository repository) {
        Repository common = commonRepository(repository)
        try {
            return version(repository, common)
        } finally {
            if (!common.is(repository)) {
                common.close()
            }
        }
    }

    private String version(Repository repository, Repository common) {
        ObjectId head = resolveHead(repository)
        String rev = head != null ? head.name : NO_COMMIT
        Map<ObjectId, List<String>> tagsByCommit = reverseTagRefMap(common)

        String branch = branch(repository)
        List<String> headTags = head != null ? tagsByCommit.getOrDefault(head, []) : []

        // Same precedence as the plugin: explicit overrides, then CI environment (only when it describes HEAD).
        String overrideBranch = commandOption('git.branch')
        String overrideTag = commandOption('git.tag')
        String providedRef = commandOption('git.ref')
        if (overrideBranch != null || overrideTag != null) {
            branch = normalizeBranch(blankToNull(overrideBranch))
            headTags = blankToNull(overrideTag) == null ? [] : [stripTagRef(overrideTag.trim())]
        } else if (providedRef != null) {
            if (!providedRef.startsWith('refs/')) {
                throw new IllegalArgumentException("invalid provided ref ${providedRef} -  needs to start with refs/")
            }
            if (providedRef.startsWith('refs/tags/')) {
                branch = null
                headTags = [stripTagRef(providedRef)]
            } else {
                branch = normalizeBranch(providedRef)
                headTags = []
            }
        } else if (repository.branch != null) {
            CiRef ciRef = ciRef(rev)
            if (ciRef != null) {
                if (ciRef.branch != null) {
                    branch = normalizeBranch(ciRef.branch)
                }
                if (ciRef.tag != null) {
                    headTags = headTags + [stripTagRef(ciRef.tag)]
                }
            }
        }

        Pattern releaseTag = Pattern.compile(parameters.releaseTagPattern.get())
        boolean release = headTags.any { String tag -> releaseTag.matcher(tag).matches() } ||
                (branch != null && parameters.releaseBranchPatterns.get().any { String p -> Pattern.compile(p).matcher(branch).matches() })

        String describeTag = describeTag(head, Pattern.compile(parameters.describeTagPattern.get()), tagsByCommit, common, repository)
        Matcher version = VERSION_PATTERN.matcher(describeTag)
        version.find()
        String major = version.group('major') ?: '0'
        String minor = version.group('minor') ?: '0'
        String patch = version.group('patch') ?: '0'

        String result = release ? "${major}.${minor}.${patch}" : "${major}.${increase(minor)}.0-SNAPSHOT"
        LOG.info("git version: ${result} (branch: ${branch}, head tags: ${headTags}, describe tag: ${describeTag})")
        return result.replace('/', '-')
    }

    private static String describeTag(ObjectId head, Pattern tagPattern, Map<ObjectId, List<String>> tagsByCommit,
            Repository common, Repository repository) {
        if (head == null) {
            return 'root'
        }
        RevWalk walk = new RevWalk(common)
        try {
            walk.retainBody = false
            walk.firstParent = false
            walk.markStart(walk.parseCommit(head))
            for (RevCommit commit : walk) {
                String match = tagsByCommit.getOrDefault(commit, []).find { String tag -> tagPattern.matcher(tag).matches() }
                if (match != null) {
                    return match
                }
            }
        } finally {
            walk.close()
        }
        if (new File(repository.directory, 'shallow').isFile()) {
            throw new GradleException("couldn't find matching tag in shallow git repository")
        }
        return 'root'
    }

    // Tags per peeled commit, ordered like the plugin: annotated tags newest first, then lightweight by version descending.
    private static Map<ObjectId, List<String>> reverseTagRefMap(Repository common) {
        RevWalk walk = new RevWalk(common)
        try {
            Map<ObjectId, List<Ref>> grouped = [:]
            for (Ref ref : common.refDatabase.getRefsByPrefix(Constants.R_TAGS)) {
                Ref peeled = common.refDatabase.peel(ref)
                ObjectId target = peeled.peeledObjectId != null ? peeled.peeledObjectId : peeled.objectId
                grouped.computeIfAbsent(target) { ObjectId key -> new ArrayList<Ref>() }.add(ref)
            }
            Map<ObjectId, List<String>> result = [:]
            grouped.each { ObjectId commit, List<Ref> refs ->
                result[commit] = refs.sort(false) { Ref a, Ref b -> compareTags(walk, a, b) }
                        .collect { Ref r -> Repository.shortenRefName(r.name) }
            }
            return result
        } finally {
            walk.close()
        }
    }

    private static int compareTags(RevWalk walk, Ref a, Ref b) {
        RevObject revA = walk.parseAny(a.objectId)
        RevObject revB = walk.parseAny(b.objectId)
        if (revA instanceof RevTag && revB instanceof RevTag) {
            return -((RevTag) revA).taggerIdent.whenAsInstant.compareTo(((RevTag) revB).taggerIdent.whenAsInstant)
        }
        if (revA instanceof RevTag) {
            return -1
        }
        if (revB instanceof RevTag) {
            return 1
        }
        return -new DefaultArtifactVersion(a.name).compareTo(new DefaultArtifactVersion(b.name))
    }

    private static CiRef ciRef(String rev) {
        Map<String, String> env = System.getenv()
        if ('true'.equalsIgnoreCase(env['GITHUB_ACTIONS'])) {
            if (rev != env['GITHUB_SHA']) {
                return null
            }
            String ref = env['GITHUB_REF']
            return ref.startsWith('refs/tags/') ? new CiRef(null, ref) : new CiRef(ref, null)
        }
        if ('true'.equalsIgnoreCase(env['GITLAB_CI'])) {
            if (rev != env['CI_COMMIT_SHA']) {
                return null
            }
            if (!isBlank(env['CI_COMMIT_BRANCH'])) {
                return new CiRef(env['CI_COMMIT_BRANCH'], null)
            }
            if (!isBlank(env['CI_MERGE_REQUEST_SOURCE_BRANCH_NAME'])) {
                return new CiRef(env['CI_MERGE_REQUEST_SOURCE_BRANCH_NAME'], null)
            }
            return !isBlank(env['CI_COMMIT_TAG']) ? new CiRef(null, env['CI_COMMIT_TAG']) : new CiRef(null, null)
        }
        if ('true'.equalsIgnoreCase(env['CIRCLECI'])) {
            if (rev != env['CIRCLE_SHA1']) {
                return null
            }
            if (!isBlank(env['CIRCLE_BRANCH'])) {
                return new CiRef(env['CIRCLE_BRANCH'].trim(), null)
            }
            return !isBlank(env['CIRCLE_TAG']) ? new CiRef(null, env['CIRCLE_TAG'].trim()) : new CiRef(null, null)
        }
        if (!isBlank(env['JENKINS_HOME'])) {
            if (rev != env['GIT_COMMIT']) {
                return null
            }
            String branch = env['BRANCH_NAME']
            String tag = env['TAG_NAME']
            if (!isBlank(branch)) {
                return branch == tag ? new CiRef(null, branch) : new CiRef(branch, null)
            }
            return !isBlank(tag) ? new CiRef(null, tag) : new CiRef(null, null)
        }
        return null
    }

    private static final class CiRef {
        final String branch
        final String tag

        CiRef(String branch, String tag) {
            this.branch = branch
            this.tag = tag
        }
    }

    private static String branch(Repository repository) {
        String branch = repository.branch
        return ObjectId.isId(branch) ? null : branch
    }

    private static String normalizeBranch(String branch) {
        if (branch == null) {
            return null
        }
        if (branch.startsWith('refs/tags/')) {
            throw new IllegalArgumentException("invalid branch ref${branch}")
        }
        return branch.replaceFirst('^refs/heads/', '').replaceFirst('^refs/', '')
    }

    private static String stripTagRef(String tag) {
        if (tag.startsWith('refs/') && !tag.startsWith('refs/tags/')) {
            throw new IllegalArgumentException("invalid tag ref${tag}")
        }
        return tag.replaceFirst('^refs/tags/', '')
    }

    // -Dname, else env VERSIONING_<NAME>; e.g. git.branch -> VERSIONING_GIT_BRANCH, versioning.disable -> VERSIONING_DISABLE.
    private static String commandOption(String name) {
        String value = System.getProperty(name)
        if (value == null) {
            String plainName = name.replaceFirst('^versioning\\.', '')
            String envName = 'VERSIONING_' + String.join('_', plainName.split('(?=\\p{Lu})')).replaceAll('\\.', '_').toUpperCase()
            value = System.getenv(envName)
        }
        return value
    }

    // Keeps zero padding, as the plugin does: '09' -> '10', '9' -> '10'.
    private static String increase(String number) {
        String sanitized = number.isEmpty() ? '0' : number
        return String.format('%0' + sanitized.length() + 'd', Long.parseLong(sanitized) + 1)
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty()
    }

    private static String blankToNull(String value) {
        return isBlank(value) ? null : value.trim()
    }

    // Linked worktrees: jgit may not expose a work tree, so follow commondir/HEAD by hand like the plugin.
    private static Repository commonRepository(Repository repository) {
        try {
            repository.workTree
            return repository
        } catch (NoWorkTreeException e) {
            File commonDirFile = new File(repository.directory, 'commondir')
            if (!commonDirFile.exists()) {
                throw e
            }
            String commonDirPath = Files.readAllLines(commonDirFile.toPath()).get(0)
            return new FileRepositoryBuilder().setGitDir(new File(repository.directory, commonDirPath)).build()
        }
    }

    private static ObjectId resolveHead(Repository repository) {
        try {
            repository.workTree
            return repository.resolve(Constants.HEAD)
        } catch (NoWorkTreeException e) {
            File headFile = new File(repository.directory, 'HEAD')
            if (!headFile.exists()) {
                throw e
            }
            String head = Files.readAllLines(headFile.toPath()).get(0)
            if (head.startsWith('ref:')) {
                String refPath = head.replaceFirst('^ref: *', '')
                String commonDirPath = Files.readAllLines(new File(repository.directory, 'commondir').toPath()).get(0)
                File refFile = new File(new File(repository.directory, commonDirPath), refPath)
                head = Files.readAllLines(refFile.toPath()).get(0)
            }
            return repository.resolve(head)
        }
    }
}
