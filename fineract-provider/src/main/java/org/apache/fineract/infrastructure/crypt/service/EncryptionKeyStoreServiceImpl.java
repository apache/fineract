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
package org.apache.fineract.infrastructure.crypt.service;

import com.sun.jersey.spi.resource.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.crypt.domain.EncryptionKeyPair;
import org.apache.fineract.infrastructure.crypt.utils.RSAEncryptionUtils;
import org.joda.time.Seconds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

/**
 * @author manoj
 */
@Service
@Singleton
public class EncryptionKeyStoreServiceImpl  implements EncryptionKeyStoreService{
    private Map<String, EncryptionKeyPair> keyMap =  new ConcurrentHashMap<>();


    private final ConfigurationDomainService configurationDomainService;
    private final RSAEncryptionUtils rsaEncryptionUtils;

    @Autowired
    public EncryptionKeyStoreServiceImpl(ConfigurationDomainService configurationDomainService, RSAEncryptionUtils rsaEncryptionUtils) {
        this.configurationDomainService = configurationDomainService;
        this.rsaEncryptionUtils = rsaEncryptionUtils;
    }

    @Caching(evict = {
            @CacheEvict(value = "encryptionKeys", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#type)")})
    public void storeKey(String type, EncryptionKeyPair key){
        this.keyMap.put(getKeyType(type), key);
    }

    private EncryptionKeyPair retrieveValidKey(String type){

        EncryptionKeyPair keys = getKeys(type);
        if(keys == null){
            return null;
        }
        //check validityOftheKey
        //get validity time from config
        Integer validUpto = configurationDomainService.retrieveEncKeyExpirySeconds(type);
        if(validUpto.equals(-1)){
            return keys;
        } else {
            Seconds seconds = Seconds.secondsBetween(keys.getCreatedDateTime(), DateUtils.getLocalDateTimeOfTenant());
            if(seconds.getSeconds() < validUpto) {
                return keys;
            }

        }
        return null;
    }

        @Cacheable(value = "encryptionKeys", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#type)")
    public EncryptionKeyPair getKeys(String type){
        return this.keyMap.get(getKeyType(type));
    }

    private String getKeyType(String type){
        return (type + ThreadLocalContextUtil.getTenant().getTenantIdentifier());
    }


    @Override
    public EncryptionKeyPair retrieveKey(String type){
        EncryptionKeyPair keys = retrieveValidKey(type);
        if(null == keys) {
            //create new key pair and store
            keys = rsaEncryptionUtils.generateKeys();
            //store keys
            this.storeKey(type, keys);
            //return public key
        }
        return keys;
    }

}