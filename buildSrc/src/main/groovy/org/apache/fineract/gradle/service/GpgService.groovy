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
import org.apache.fineract.gradle.FineractPluginExtension.FineractPluginConfigGpg
import org.bouncycastle.bcpg.ArmoredOutputStream
import org.bouncycastle.bcpg.BCPGOutputStream
import org.bouncycastle.gpg.keybox.BlobType
import org.bouncycastle.gpg.keybox.PublicKeyRingBlob
import org.bouncycastle.gpg.keybox.bc.BcKeyBox
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openpgp.*
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator
import org.bouncycastle.openpgp.operator.bc.BcPGPContentSignerBuilder
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.security.GeneralSecurityException
import java.security.MessageDigest
import java.security.Security

class GpgService {
    private static final Logger log = LoggerFactory.getLogger(GpgService.class)

    private PGPPrivateKey privateKey;
    private PGPPublicKey publicKey;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    GpgService(FineractPluginConfigGpg config) {
        // TODO: provide implementation for *.gpg key rings
        def keyBox = config.publicKeyring.endsWith(".kbx") ? new BcKeyBox(new FileInputStream(config.publicKeyring)) : null

        if(keyBox) {
            for(def keyBlob : keyBox.getKeyBlobs()) {
                switch (keyBlob.type) {
                    case BlobType.X509_BLOB:
                        break
                    case BlobType.OPEN_PGP_BLOB:
                        def publicKeyRing = (keyBlob as PublicKeyRingBlob).getPGPPublicKeyRing();
                        def iterator = publicKeyRing.getPublicKeys();

                        while (publicKey == null && iterator.hasNext()) {
                            def k = iterator.next();

                            if (k.isEncryptionKey()) {
                                def keyName = Long.toHexString(k.keyID).toUpperCase()

                                if(config.keyName.substring(config.keyName.length()-keyName.length()) == keyName) {
                                    publicKey = k;
                                    log.warn("Found: ${publicKey.getUserIDs().next()}")
                                    break
                                } else {
                                    log.warn("Key mismatch: ${config.keyName} <-> ${keyName}")
                                }
                            }
                        }
                        break
                }
            }

            PGPSecretKeyRingCollection secretRingCollection = new PGPSecretKeyRingCollection(new FileInputStream(config.secretKeyring), new BcKeyFingerprintCalculator())

            def secretKey = secretRingCollection.getSecretKey(publicKey.keyID)

            if(secretKey!=null) {
                this.privateKey = secretKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider(BouncyCastleProvider.PROVIDER_NAME).build(config.password.toCharArray()))
            }
        } else {
            log.warn("Could not open the public key ring.")
        }
    }

    void sign(FineractPluginExtension.FineractPluginGpgParams params) {
        params.files.findAll {
            InputStream is = new FileInputStream(it)

            BCPGOutputStream os = new BCPGOutputStream(new ArmoredOutputStream(new FileOutputStream(it + ".asc")))
            sign(is).encode(os)
            os.close();
        }
    }

    PGPSignature sign(InputStream is) throws IOException, PGPException, GeneralSecurityException {

        PGPSignatureGenerator generator = new PGPSignatureGenerator(new BcPGPContentSignerBuilder(publicKey.getAlgorithm(), PGPUtil.SHA1));
        generator.init(PGPSignature.BINARY_DOCUMENT, privateKey);

        byte[] buf = new byte[4096];
        int len;

        while ((len = is.read(buf)) >= 0) {
            generator.update(buf, 0, len);
        }

        return generator.generate();
    }


    void md5(FineractPluginExtension.FineractPluginGpgParams params) {
        params.files.findAll {
            def result = calc(new FileInputStream(it), MessageDigest.getInstance("MD5", BouncyCastleProvider.PROVIDER_NAME))
            def file = new File("${it}.md5")
            file.write result
        }
    }

    void sha512(FineractPluginExtension.FineractPluginGpgParams params) {
        params.files.findAll {
            def result = calc(new FileInputStream(it), MessageDigest.getInstance("SHA-512", BouncyCastleProvider.PROVIDER_NAME))
            def file = new File("${it}.sha512")
            file.write result
        }
    }

    private static String calc(InputStream is, MessageDigest digest) {
        String output
        int read
        byte[] buffer = new byte[8192]

        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read)
            }
            byte[] hash = digest.digest()
            BigInteger bigInt = new BigInteger(1, hash)
            output = bigInt.toString(16)
            while ( output.length() < 32 ) {
                output = "0"+output
            }
        } catch (Exception e) {
            log.error(e.toString(), e)
            return null
        }

        return output
    }
}
