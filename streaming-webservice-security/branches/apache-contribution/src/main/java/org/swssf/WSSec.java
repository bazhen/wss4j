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
package org.swssf;

import org.swssf.config.Init;
import org.swssf.ext.*;

import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is the central class of the streaming webservice-security framework.<br/>
 * Instances of the inbound and outbound security streams can be retrieved
 * with this class.
 *
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class WSSec {

    //todo crl check
    //todo outgoing client setup per policy

    static {
        try {
            Class c = WSSec.class.getClassLoader().loadClass("org.bouncycastle.jce.provider.BouncyCastleProvider");
            if (null == Security.getProvider("BC")) {
                int i = Security.addProvider((Provider) c.newInstance());
            }
        } catch (Throwable e) {
            throw new RuntimeException("Adding BouncyCastle provider failed", e);
        }
    }

    /**
     * Creates and configures an outbound streaming security engine
     *
     * @param securityProperties The user-defined security configuration
     * @return A new OutboundWSSec
     * @throws org.swssf.ext.WSSecurityException
     *          if the initialisation failed
     * @throws org.swssf.ext.WSSConfigurationException
     *          if the configuration is invalid
     */
    public static OutboundWSSec getOutboundWSSec(SecurityProperties securityProperties) throws WSSecurityException {
        if (securityProperties == null) {
            throw new WSSConfigurationException(WSSecurityException.ErrorCode.FAILURE, "missingSecurityProperties");
        }

        Init.init(null);

        securityProperties = validateAndApplyDefaultsToOutboundSecurityProperties(securityProperties);
        return new OutboundWSSec(securityProperties);
    }

    /**
     * Creates and configures an inbound streaming security engine
     *
     * @param securityProperties The user-defined security configuration
     * @return A new InboundWSSec
     * @throws org.swssf.ext.WSSecurityException
     *          if the initialisation failed
     * @throws org.swssf.ext.WSSConfigurationException
     *          if the configuration is invalid
     */
    public static InboundWSSec getInboundWSSec(SecurityProperties securityProperties) throws WSSecurityException {
        if (securityProperties == null) {
            throw new WSSConfigurationException(WSSecurityException.ErrorCode.FAILURE, "missingSecurityProperties");
        }

        Init.init(null);

        securityProperties = validateAndApplyDefaultsToInboundSecurityProperties(securityProperties);
        return new InboundWSSec(securityProperties);
    }

    /**
     * Validates the user supplied configuration and applies default values as apropriate for the outbound security engine
     *
     * @param securityProperties The configuration to validate
     * @return The validated configuration
     * @throws org.swssf.ext.WSSConfigurationException
     *          if the configuration is invalid
     */
    public static SecurityProperties validateAndApplyDefaultsToOutboundSecurityProperties(SecurityProperties securityProperties) throws WSSConfigurationException {
        if (securityProperties.getOutAction() == null) {
            throw new WSSConfigurationException(WSSecurityException.ErrorCode.FAILURE, "noOutputAction");
        }

        //todo encrypt sigconf when original signature was encrypted
        int pos = Arrays.binarySearch(securityProperties.getOutAction(), Constants.Action.SIGNATURE_CONFIRMATION);
        if (pos >= 0) {
            if (Arrays.binarySearch(securityProperties.getOutAction(), Constants.Action.SIGNATURE) < 0) {
                List<Constants.Action> actionList = new ArrayList<Constants.Action>(securityProperties.getOutAction().length);
                actionList.addAll(Arrays.asList(securityProperties.getOutAction()));
                actionList.add(pos, Constants.Action.SIGNATURE);
                securityProperties.setOutAction(actionList.toArray(new Constants.Action[securityProperties.getOutAction().length + 1]));
            }
        }

        for (int i = 0; i < securityProperties.getOutAction().length; i++) {
            Constants.Action action = securityProperties.getOutAction()[i];
            switch (action) {
                case TIMESTAMP:
                    if (securityProperties.getTimestampTTL() == null) {
                        securityProperties.setTimestampTTL(300);
                    }
                    break;
                case SIGNATURE:
                    if (securityProperties.getSignatureKeyStore() == null) {
                        throw new WSSConfigurationException(WSSecurityException.ErrorCode.FAILURE, "signatureKeyStoreNotSet");
                    }
                    if (securityProperties.getSignatureUser() == null) {
                        throw new WSSConfigurationException(WSSecurityException.ErrorCode.FAILURE, "noSignatureUser");
                    }
                    if (securityProperties.getCallbackHandler() == null) {
                        throw new WSSConfigurationException(WSSecurityException.ErrorCode.FAILURE, "noCallback");
                    }
                    //signature namespace part will be set in SecurityHeaderOutputProcessor
                    if (securityProperties.getSignatureSecureParts().isEmpty()) {
                        securityProperties.addSignaturePart(new SecurePart("Body", "*", SecurePart.Modifier.Element));
                    }
                    if (securityProperties.getSignatureAlgorithm() == null) {
                        securityProperties.setSignatureAlgorithm("http://www.w3.org/2000/09/xmldsig#rsa-sha1");
                    }
                    if (securityProperties.getSignatureDigestAlgorithm() == null) {
                        securityProperties.setSignatureDigestAlgorithm("http://www.w3.org/2000/09/xmldsig#sha1");
                    }
                    if (securityProperties.getSignatureCanonicalizationAlgorithm() == null) {
                        securityProperties.setSignatureCanonicalizationAlgorithm("http://www.w3.org/2001/10/xml-exc-c14n#");
                    }
                    if (securityProperties.getSignatureKeyIdentifierType() == null) {
                        securityProperties.setSignatureKeyIdentifierType(Constants.KeyIdentifierType.ISSUER_SERIAL);
                    }
                    break;

                case ENCRYPT:
                    if (securityProperties.getEncryptionUseThisCertificate() == null
                            && securityProperties.getEncryptionKeyStore() == null
                            && !securityProperties.isUseReqSigCertForEncryption()) {
                        throw new WSSConfigurationException(WSSecurityException.ErrorCode.FAILURE, "encryptionKeyStoreNotSet");
                    }
                    if (securityProperties.getEncryptionUser() == null
                            && securityProperties.getEncryptionUseThisCertificate() == null
                            && !securityProperties.isUseReqSigCertForEncryption()) {
                        throw new WSSConfigurationException(WSSecurityException.ErrorCode.FAILURE, "noEncryptionUser");
                    }
                    //encryption namespace part will be set in SecurityHeaderOutputProcessor
                    if (securityProperties.getEncryptionSecureParts().isEmpty()) {
                        securityProperties.addEncryptionPart(new SecurePart("Body", "*", SecurePart.Modifier.Content));
                    }
                    if (securityProperties.getEncryptionSymAlgorithm() == null) {
                        securityProperties.setEncryptionSymAlgorithm("http://www.w3.org/2001/04/xmlenc#aes256-cbc");
                    }
                    if (securityProperties.getEncryptionKeyTransportAlgorithm() == null) {
                        //@see http://www.w3.org/TR/2002/REC-xmlenc-core-20021210/Overview.html#rsa-1_5 :
                        //"RSA-OAEP is RECOMMENDED for the transport of AES keys"
                        //@see http://www.w3.org/TR/2002/REC-xmlenc-core-20021210/Overview.html#rsa-oaep-mgf1p
                        securityProperties.setEncryptionKeyTransportAlgorithm("http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p");
                    }
                    if (securityProperties.getEncryptionKeyIdentifierType() == null) {
                        securityProperties.setEncryptionKeyIdentifierType(Constants.KeyIdentifierType.ISSUER_SERIAL);
                    }
                    break;
                case USERNAMETOKEN:
                    if (securityProperties.getTokenUser() == null) {
                        throw new WSSConfigurationException(WSSecurityException.ErrorCode.FAILURE, "noTokenUser");
                    }
                    if (securityProperties.getCallbackHandler() == null) {
                        throw new WSSConfigurationException(WSSecurityException.ErrorCode.FAILURE, "noCallback");
                    }
                    if (securityProperties.getUsernameTokenPasswordType() == null) {
                        securityProperties.setUsernameTokenPasswordType(Constants.UsernameTokenPasswordType.PASSWORD_DIGEST);
                    }
                    break;
                case USERNAMETOKEN_SIGNED:
                    if (securityProperties.getTokenUser() == null) {
                        throw new WSSConfigurationException(WSSecurityException.ErrorCode.FAILURE, "noTokenUser");
                    }
                    if (securityProperties.getCallbackHandler() == null) {
                        throw new WSSConfigurationException(WSSecurityException.ErrorCode.FAILURE, "noCallback");
                    }
                    //signature namespace part will be set in SecurityHeaderOutputProcessor
                    if (securityProperties.getSignatureSecureParts().isEmpty()) {
                        securityProperties.addSignaturePart(new SecurePart("Body", "*", SecurePart.Modifier.Element));
                    }
                    if (securityProperties.getSignatureAlgorithm() == null) {
                        securityProperties.setSignatureAlgorithm("http://www.w3.org/2000/09/xmldsig#hmac-sha1");
                    }
                    if (securityProperties.getSignatureDigestAlgorithm() == null) {
                        securityProperties.setSignatureDigestAlgorithm("http://www.w3.org/2000/09/xmldsig#sha1");
                    }
                    if (securityProperties.getSignatureCanonicalizationAlgorithm() == null) {
                        securityProperties.setSignatureCanonicalizationAlgorithm("http://www.w3.org/2001/10/xml-exc-c14n#");
                    }
                    securityProperties.setSignatureKeyIdentifierType(Constants.KeyIdentifierType.USERNAMETOKEN_REFERENCE);
                    if (securityProperties.getUsernameTokenPasswordType() == null) {
                        securityProperties.setUsernameTokenPasswordType(Constants.UsernameTokenPasswordType.PASSWORD_DIGEST);
                    }
                    break;
                case SIGNATURE_CONFIRMATION:
                    securityProperties.addSignaturePart(new SecurePart(Constants.TAG_wsse11_SignatureConfirmation.getLocalPart(), Constants.TAG_wsse11_SignatureConfirmation.getNamespaceURI(), SecurePart.Modifier.Element));
                    break;
                case SIGNATURE_WITH_DERIVED_KEY:
                    if (securityProperties.getCallbackHandler() == null) {
                        throw new WSSConfigurationException(WSSecurityException.ErrorCode.FAILURE, "noCallback");
                    }
                    //signature namespace part will be set in SecurityHeaderOutputProcessor
                    if (securityProperties.getSignatureSecureParts().isEmpty()) {
                        securityProperties.addSignaturePart(new SecurePart("Body", "*", SecurePart.Modifier.Element));
                    }
                    if (securityProperties.getSignatureAlgorithm() == null) {
                        securityProperties.setSignatureAlgorithm("http://www.w3.org/2000/09/xmldsig#hmac-sha1");
                    }
                    if (securityProperties.getSignatureDigestAlgorithm() == null) {
                        securityProperties.setSignatureDigestAlgorithm("http://www.w3.org/2000/09/xmldsig#sha1");
                    }
                    if (securityProperties.getSignatureCanonicalizationAlgorithm() == null) {
                        securityProperties.setSignatureCanonicalizationAlgorithm("http://www.w3.org/2001/10/xml-exc-c14n#");
                    }
                    securityProperties.setSignatureKeyIdentifierType(Constants.KeyIdentifierType.EMBEDDED_SECURITY_TOKEN_REF);
                    if (securityProperties.getEncryptionSymAlgorithm() == null) {
                        securityProperties.setEncryptionSymAlgorithm("http://www.w3.org/2001/04/xmlenc#aes256-cbc");
                    }
                    if (securityProperties.getEncryptionKeyTransportAlgorithm() == null) {
                        //@see http://www.w3.org/TR/2002/REC-xmlenc-core-20021210/Overview.html#rsa-1_5 :
                        //"RSA-OAEP is RECOMMENDED for the transport of AES keys"
                        //@see http://www.w3.org/TR/2002/REC-xmlenc-core-20021210/Overview.html#rsa-oaep-mgf1p
                        securityProperties.setEncryptionKeyTransportAlgorithm("http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p");
                    }
                    if (securityProperties.getEncryptionKeyIdentifierType() == null) {
                        securityProperties.setEncryptionKeyIdentifierType(Constants.KeyIdentifierType.X509_KEY_IDENTIFIER);
                    }
                    if (securityProperties.getDerivedKeyKeyIdentifierType() == null) {
                        securityProperties.setDerivedKeyKeyIdentifierType(Constants.KeyIdentifierType.X509_KEY_IDENTIFIER);
                    }
                    if (securityProperties.getDerivedKeyTokenReference() == null) {
                        securityProperties.setDerivedKeyTokenReference(Constants.DerivedKeyTokenReference.DirectReference);
                    }
                    if (securityProperties.getDerivedKeyTokenReference() != Constants.DerivedKeyTokenReference.DirectReference) {
                        securityProperties.setDerivedKeyKeyIdentifierType(Constants.KeyIdentifierType.EMBEDDED_SECURITY_TOKEN_REF);
                    }
                    break;
                case ENCRYPT_WITH_DERIVED_KEY:
                    if (securityProperties.getCallbackHandler() == null) {
                        throw new WSSConfigurationException(WSSecurityException.ErrorCode.FAILURE, "noCallback");
                    }
                    if (securityProperties.getEncryptionUseThisCertificate() == null
                            && securityProperties.getEncryptionKeyStore() == null
                            && !securityProperties.isUseReqSigCertForEncryption()) {
                        throw new WSSConfigurationException(WSSecurityException.ErrorCode.FAILURE, "encryptionKeyStoreNotSet");
                    }
                    if (securityProperties.getEncryptionUser() == null
                            && securityProperties.getEncryptionUseThisCertificate() == null
                            && !securityProperties.isUseReqSigCertForEncryption()) {
                        throw new WSSConfigurationException(WSSecurityException.ErrorCode.FAILURE, "noEncryptionUser");
                    }
                    //encryption namespace part will be set in SecurityHeaderOutputProcessor
                    if (securityProperties.getEncryptionSecureParts().isEmpty()) {
                        securityProperties.addEncryptionPart(new SecurePart("Body", "*", SecurePart.Modifier.Content));
                    }
                    if (securityProperties.getEncryptionSymAlgorithm() == null) {
                        securityProperties.setEncryptionSymAlgorithm("http://www.w3.org/2001/04/xmlenc#aes256-cbc");
                    }
                    if (securityProperties.getEncryptionKeyTransportAlgorithm() == null) {
                        //@see http://www.w3.org/TR/2002/REC-xmlenc-core-20021210/Overview.html#rsa-1_5 :
                        //"RSA-OAEP is RECOMMENDED for the transport of AES keys"
                        //@see http://www.w3.org/TR/2002/REC-xmlenc-core-20021210/Overview.html#rsa-oaep-mgf1p
                        securityProperties.setEncryptionKeyTransportAlgorithm("http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p");
                    }
                    if (securityProperties.getEncryptionKeyIdentifierType() == null) {
                        securityProperties.setEncryptionKeyIdentifierType(Constants.KeyIdentifierType.X509_KEY_IDENTIFIER);
                    }
                    if (securityProperties.getDerivedKeyKeyIdentifierType() == null) {
                        securityProperties.setDerivedKeyKeyIdentifierType(Constants.KeyIdentifierType.X509_KEY_IDENTIFIER);
                    }
                    if (securityProperties.getDerivedKeyTokenReference() == null) {
                        securityProperties.setDerivedKeyTokenReference(Constants.DerivedKeyTokenReference.EncryptedKey);
                    }
                    if (securityProperties.getDerivedKeyTokenReference() != Constants.DerivedKeyTokenReference.DirectReference) {
                        securityProperties.setDerivedKeyKeyIdentifierType(Constants.KeyIdentifierType.EMBEDDED_SECURITY_TOKEN_REF);
                    }
                    break;
                case SAML_TOKEN_SIGNED:
                    if (securityProperties.getCallbackHandler() == null) {
                        throw new WSSConfigurationException(WSSecurityException.ErrorCode.FAILURE, "noCallback");
                    }
                    //signature namespace part will be set in SecurityHeaderOutputProcessor
                    if (securityProperties.getSignatureSecureParts().isEmpty()) {
                        securityProperties.addSignaturePart(new SecurePart("Body", "*", SecurePart.Modifier.Element));
                    }
                    if (securityProperties.getSignatureAlgorithm() == null) {
                        securityProperties.setSignatureAlgorithm("http://www.w3.org/2000/09/xmldsig#rsa-sha1");
                    }
                    if (securityProperties.getSignatureDigestAlgorithm() == null) {
                        securityProperties.setSignatureDigestAlgorithm("http://www.w3.org/2000/09/xmldsig#sha1");
                    }
                    if (securityProperties.getSignatureCanonicalizationAlgorithm() == null) {
                        securityProperties.setSignatureCanonicalizationAlgorithm("http://www.w3.org/2001/10/xml-exc-c14n#");
                    }
                    if (securityProperties.getSignatureKeyIdentifierType() == null) {
                        securityProperties.setSignatureKeyIdentifierType(Constants.KeyIdentifierType.EMBEDDED_SECURITY_TOKEN_REF);
                    }
                    break;
                case SAML_TOKEN_UNSIGNED:
                    if (securityProperties.getCallbackHandler() == null) {
                        throw new WSSConfigurationException(WSSecurityException.ErrorCode.FAILURE, "noCallback");
                    }
                    break;
            }
        }
        //todo clone securityProperties
        return securityProperties;
    }

    /**
     * Validates the user supplied configuration and applies default values as apropriate for the inbound security engine
     *
     * @param securityProperties The configuration to validate
     * @return The validated configuration
     * @throws org.swssf.ext.WSSConfigurationException
     *          if the configuration is invalid
     */
    public static SecurityProperties validateAndApplyDefaultsToInboundSecurityProperties(SecurityProperties securityProperties) throws WSSConfigurationException {
        //todo clone securityProperties
        return securityProperties;
    }
}
