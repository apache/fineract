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
import org.mifosplatform.infrastructure.documentmanagement.exception.InvalidImageTypeException;
import org.mifosplatform.organisation.staff.domain.Staff;
import org.mifosplatform.organisation.staff.domain.StaffRepositoryWrapper;
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
    private final StaffRepositoryWrapper staffRepositoryWrapper;

    @Autowired
    public ImageWritePlatformServiceJpaRepositoryImpl(final ContentRepositoryFactory documentStoreFactory,
                                                      final ClientRepositoryWrapper clientRepositoryWrapper, final ImageRepository imageRepository, StaffRepositoryWrapper staffRepositoryWrapper) {
        this.contentRepositoryFactory = documentStoreFactory;
        this.clientRepositoryWrapper = clientRepositoryWrapper;
        this.imageRepository = imageRepository;
        this.staffRepositoryWrapper = staffRepositoryWrapper;
    }

    @Transactional
    @Override
    public CommandProcessingResult saveOrUpdateImage(String clientName, final Long clientId, final String imageName, final InputStream inputStream,
            final Long fileSize) {
        Object owner = null;
        if (IMAGE_TYPE.CLIENTS.toString().equals(clientName)){
            owner =this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
        } else if (IMAGE_TYPE.STAFF.toString().equals(clientName)) {
            owner = this.staffRepositoryWrapper.findOneWithNotFoundDetection(clientId);
        }
        deletePreviousImage(owner);

        final ContentRepository contentRepository = this.contentRepositoryFactory.getRepository();
        final String imageLocation = contentRepository.saveImage(inputStream, clientId, imageName, fileSize);
        return updateImage(owner, imageLocation, contentRepository.getStorageType());
    }

    @Transactional
    @Override
    public CommandProcessingResult saveOrUpdateImage(String clientName, final Long clientId, final Base64EncodedImage encodedImage) {
        Object owner = null;
        if (IMAGE_TYPE.CLIENTS.toString().equals(clientName)){
            owner =this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
        } else if (IMAGE_TYPE.STAFF.toString().equals(clientName)) {
            owner = this.staffRepositoryWrapper.findOneWithNotFoundDetection(clientId);
        }
        deletePreviousImage(owner);
        validateImageType(clientName);

        final ContentRepository contenRepository = this.contentRepositoryFactory.getRepository();
        final String imageLocation = contenRepository.saveImage(encodedImage, clientId, "image");

        return updateImage(owner, imageLocation, contenRepository.getStorageType());
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteImage(String clientName, final Long clientId) {

        Object owner = null;
        Image image = null;
        if (IMAGE_TYPE.CLIENTS.toString().equals(clientName)){
            owner =this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
            Client client = (Client) owner;
            image = client.getImage();
            client.setImage(null);
            this.clientRepositoryWrapper.save(client);

        } else if (IMAGE_TYPE.STAFF.toString().equals(clientName)) {
            owner = this.staffRepositoryWrapper.findOneWithNotFoundDetection(clientId);
            Staff staff = (Staff) owner;
            image = staff.getImage();
            staff.setImage(null);
            this.staffRepositoryWrapper.save(staff);

        }
        // delete image from the file system
        if (image != null) {
            final ContentRepository contentRepository = this.contentRepositoryFactory.getRepository(StorageType.fromInt(image
                    .getStorageType()));
            contentRepository.deleteImage(clientId, image.getLocation());
            this.imageRepository.delete(image);
        }

        return new CommandProcessingResult(clientId);
    }

    private void deletePreviousImage(final Object owner) {
        Image image = null;
        Long clientId=null;
        if (owner instanceof Client){
            Client client = (Client) owner;
            image = client.getImage();
            clientId = client.getId();
        } else if (owner instanceof Staff){
            Staff staff = (Staff) owner;
            image = staff.getImage();
            clientId = staff.getId();
        }
        if (image != null) {
            final ContentRepository contentRepository = this.contentRepositoryFactory.getRepository(StorageType.fromInt(image
                    .getStorageType()));
            contentRepository.deleteImage(clientId, image.getLocation());
        }
    }

    private CommandProcessingResult updateImage(final Object owner, final String imageLocation, final StorageType storageType) {
        Image image = null;
        Long clientId=null;
        if (owner instanceof Client){
            Client client = (Client) owner;
            image = client.getImage();
            clientId = client.getId();
            image = createImage(image, imageLocation, storageType);
            client.setImage(image);
            this.clientRepositoryWrapper.save(client);
        } else if (owner instanceof Staff){
            Staff staff = (Staff) owner;
            image = staff.getImage();
            clientId = staff.getId();
            image = createImage(image, imageLocation, storageType);
            staff.setImage(image);
            this.staffRepositoryWrapper.save(staff);
        }

        this.imageRepository.save(image);
        return new CommandProcessingResult(clientId);
    }

    private Image createImage(Image image, final String imageLocation, final StorageType storageType) {
        if (image == null) {
            image = new Image(imageLocation, storageType);
        } else {
            image.setLocation(imageLocation);
            image.setStorageType(storageType.getValue());
        }
        return image;
    }

    private void validateImageType(final String clientName) {
        if (!checkValidEntityType(clientName)) { throw new InvalidImageTypeException(
                clientName); }
    }

    private static boolean checkValidEntityType(final String entityType) {
        for (final IMAGE_TYPE entities : IMAGE_TYPE.values()) {
            if (entities.name().equalsIgnoreCase(entityType)) { return true; }
        }
        return false;
    }

    /*** Entities for document Management **/
    public static enum IMAGE_TYPE {
        STAFF,CLIENTS;

        @Override
        public String toString() {
            return name().toString().toLowerCase();
        }}

}