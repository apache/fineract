/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.documentmanagement.service;

import java.io.InputStream;

import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.domain.Base64EncodedImage;
import org.mifosplatform.infrastructure.documentmanagement.contentrepository.ContentRepository;
import org.mifosplatform.infrastructure.documentmanagement.contentrepository.ContentRepositoryFactory;
import org.mifosplatform.infrastructure.documentmanagement.domain.Image;
import org.mifosplatform.infrastructure.documentmanagement.domain.ImageRepository;
import org.mifosplatform.infrastructure.documentmanagement.domain.StorageType;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepositoryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ImageWritePlatformServiceJpaRepositoryImpl implements ImageWritePlatformService {

    private final ContentRepositoryFactory contentRepositoryFactory;
    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final ImageRepository imageRepository;

    @Autowired
    public ImageWritePlatformServiceJpaRepositoryImpl(final ContentRepositoryFactory documentStoreFactory,
            final ClientRepositoryWrapper clientRepositoryWrapper, final ImageRepository imageRepository) {
        this.contentRepositoryFactory = documentStoreFactory;
        this.clientRepositoryWrapper = clientRepositoryWrapper;
        this.imageRepository = imageRepository;
    }

    @Transactional
    @Override
    public CommandProcessingResult saveOrUpdateClientImage(final Long clientId, final String imageName, final InputStream inputStream,
            final Long fileSize) {
        final Client client = this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
        deletePreviousClientImage(client);

        final ContentRepository contentRepository = this.contentRepositoryFactory.getRepository();
        final String imageLocation = contentRepository.saveImage(inputStream, clientId, imageName, fileSize);
        return updateClientImage(client, imageLocation, contentRepository.getStorageType());
    }

    @Transactional
    @Override
    public CommandProcessingResult saveOrUpdateClientImage(final Long clientId, final Base64EncodedImage encodedImage) {
        final Client client = this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
        deletePreviousClientImage(client);

        final ContentRepository contenRepository = this.contentRepositoryFactory.getRepository();
        final String imageLocation = contenRepository.saveImage(encodedImage, clientId, "image");

        return updateClientImage(client, imageLocation, contenRepository.getStorageType());
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteClientImage(final Long clientId) {

        final Client client = this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);

        final Image image = client.getImage();
        // delete image from the file system
        if (image != null) {
            final ContentRepository contentRepository = this.contentRepositoryFactory.getRepository(StorageType.fromInt(image
                    .getStorageType()));
            contentRepository.deleteImage(clientId, image.getLocation());
            client.setImage(null);
            this.imageRepository.delete(image);
            this.clientRepositoryWrapper.save(client);
        }

        return new CommandProcessingResult(clientId);
    }

    private void deletePreviousClientImage(final Client client) {
        final Image image = client.getImage();
        if (image != null) {
            final ContentRepository contentRepository = this.contentRepositoryFactory.getRepository(StorageType.fromInt(image
                    .getStorageType()));
            contentRepository.deleteImage(client.getId(), image.getLocation());
        }
    }

    private CommandProcessingResult updateClientImage(final Client client, final String imageLocation, final StorageType storageType) {
        Image image = client.getImage();
        if (image == null) {
            image = new Image(imageLocation, storageType);
        } else {
            image.setLocation(imageLocation);
            image.setStorageType(storageType.getValue());
        }
        this.imageRepository.save(image);
        client.setImage(image);
        this.clientRepositoryWrapper.save(client);
        return new CommandProcessingResult(client.getId());
    }

}