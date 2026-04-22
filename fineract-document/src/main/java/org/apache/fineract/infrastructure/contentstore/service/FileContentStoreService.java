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
package org.apache.fineract.infrastructure.contentstore.service;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.contentstore.data.ContentStoreType;
import org.apache.fineract.infrastructure.contentstore.detector.ContentDetectorContext;
import org.apache.fineract.infrastructure.contentstore.detector.ContentDetectorManager;
import org.apache.fineract.infrastructure.contentstore.exception.ContentPolicyException;
import org.apache.fineract.infrastructure.contentstore.exception.ContentStoreException;
import org.apache.fineract.infrastructure.contentstore.policy.ContentPolicyContext;
import org.apache.fineract.infrastructure.contentstore.policy.DefaultDeleteContentPolicy;
import org.apache.fineract.infrastructure.contentstore.policy.DefaultDownloadContentPolicy;
import org.apache.fineract.infrastructure.contentstore.policy.DefaultPostUploadContentPolicy;
import org.apache.fineract.infrastructure.contentstore.policy.DefaultPreUploadContentPolicy;
import org.apache.fineract.infrastructure.contentstore.util.ContentPathRandomizer;
import org.apache.fineract.infrastructure.contentstore.util.ContentPathSanitizer;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
@ConditionalOnProperty(name = "fineract.content.filesystem.enabled", havingValue = "true")
public class FileContentStoreService implements ContentStoreService {

    private final ContentPathSanitizer pathSanitizer;
    private final ContentPathRandomizer pathRandomizer;
    private final DefaultDownloadContentPolicy downloadContentPolicy;
    private final DefaultPreUploadContentPolicy preUploadContentPolicy;
    private final DefaultPostUploadContentPolicy postUploadContentPolicy;
    private final DefaultDeleteContentPolicy deleteContentPolicy;
    private final ContentDetectorManager contentDetectorManager;
    private final FineractProperties properties;

    @Override
    public InputStream download(String path) {
        downloadContentPolicy.check(ContentPolicyContext.builder().path(path).build());

        final var safePath = pathSanitizer.sanitize(path);

        try {
            final var target = getPath(safePath, false);

            return Files.newInputStream(target);
        } catch (IOException e) {
            throw new ContentStoreException(e);
        }
    }

    @Override
    public String upload(String path, InputStream is, String mimeType) {
        requireNonNull(path, "Path missing");
        requireNonNull(is, "Data missing");

        if (StringUtils.isEmpty(mimeType)) {
            mimeType = Optional.ofNullable(contentDetectorManager.detect(ContentDetectorContext.builder().fileName(path).build()))
                    .map(ContentDetectorContext::getMimeType).orElse(APPLICATION_OCTET_STREAM_VALUE);
        }

        preUploadContentPolicy.check(ContentPolicyContext.builder().path(path).mimeType(mimeType).build());

        var safePath = pathSanitizer.sanitize(path);

        final var target = Files.exists(getPath(safePath, false)) ? getPath(safePath, false) : getPath(safePath, true);

        try (var in = is) {
            IOUtils.copy(in, new FileOutputStream(target.toString()));
        } catch (Exception e) {
            throw new ContentStoreException(e);
        }

        final var relativePath = getRelativePath(target);

        try {
            postUploadContentPolicy
                    .check(ContentPolicyContext.builder().path(path).inputStream(getInputStream(target)).mimeType(mimeType).build());
        } catch (ContentPolicyException e) {
            log.warn("Delete file because it didn't comply with the post-upload policy check: {} - {}", target, e.getMessage());

            delete(relativePath.toString());

            throw e;
        }

        return relativePath.toString();
    }

    @Override
    public void delete(String path) {
        deleteContentPolicy.check(ContentPolicyContext.builder().path(path).build());

        final var safePath = pathSanitizer.sanitize(path);

        try {
            final var target = getPath(safePath, false);

            final boolean deleted = Files.deleteIfExists(target);

            if (!hasFiles(target.getParent())) {
                Files.deleteIfExists(target.getParent());
            }

            if (!deleted) {
                // no need to throw an Error, what's a caller going to do about it, so simply log a warning
                log.warn("Unable to delete file {}", safePath);
            }
        } catch (Exception e) {
            throw new ContentStoreException(e);
        }
    }

    @Override
    public ContentStoreType getType() {
        return ContentStoreType.FILE_SYSTEM;
    }

    private Path getPath(String path, boolean randomize) {
        Path p;

        if (randomize) {
            var tail = Path.of(path);
            var file = tail.getFileName();

            p = getRootPath().resolve(tail.getParent()).resolve(pathRandomizer.randomize()).resolve(file);

            try {
                if (Files.notExists(p.getParent())) {
                    Files.createDirectories(p.getParent());
                }
            } catch (Exception e) {
                throw new ContentStoreException(e);
            }
        } else {
            p = getRootPath().resolve(path);
        }

        return p;
    }

    private Path getRelativePath(Path fullPath) {
        return getRootPath().relativize(fullPath);
    }

    private Path getRootPath() {
        return Path.of(properties.getContent().getFilesystem().getRootFolder(),
                ThreadLocalContextUtil.getTenant().getName().replaceAll(" ", "").trim());
    }

    private InputStream getInputStream(Path path) {
        try {
            return Files.newInputStream(path);
        } catch (IOException ioe) {
            throw new ContentStoreException(ioe);
        }
    }

    boolean hasFiles(Path folder) throws IOException {
        if (Files.exists(folder) && Files.isDirectory(folder)) {
            try (Stream<Path> entries = Files.list(folder)) {
                return entries.anyMatch(Files::isRegularFile);
            }
        }

        return false;
    }
}
