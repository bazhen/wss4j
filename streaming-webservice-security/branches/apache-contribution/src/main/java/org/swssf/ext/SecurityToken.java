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
package org.swssf.ext;

import java.security.Key;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

/**
 * This class represents the different token types which can occur in WS-Security
 * <p/>
 * Sometimes it isn't known (@see EncryptedKeyInputProcessor) which kind of Token(Asymmetric, Symmetric)
 * we have at creation time. So we use a generic interface for both types.
 *
 * @author $Author$
 * @version $Revision$ $Date$
 */
public interface SecurityToken {

    /**
     * Returns the token id aka wsu:Id
     *
     * @return The id
     */
    public String getId();

    /**
     * Returns the responsible processor for this token
     *
     * @return
     */
    public Object getProcessor();

    /**
     * Returns the token type
     *
     * @return true if asymmetric token, false if symmetric token
     */
    public boolean isAsymmetric();

    /**
     * Returns the secret key
     *
     * @param algorithmURI for the requested key
     * @return The requested key for the specified algorithmURI, or null if no matching key is found
     * @throws WSSecurityException if the key can't be loaded
     */
    public Key getSecretKey(String algorithmURI, Constants.KeyUsage keyUsage) throws WSSecurityException;

    /**
     * Returns the public key if one exist for this token type
     *
     * @return The Public-Key for asymmetric algorithms
     * @throws WSSecurityException if the key can't be loaded
     */
    public PublicKey getPublicKey(Constants.KeyUsage keyUsage) throws WSSecurityException;

    /**
     * Returns the certificate chain if one exists for this token type
     *
     * @return The certificate chain
     * @throws WSSecurityException if the certificates can't be retrieved
     */
    public X509Certificate[] getX509Certificates() throws WSSecurityException;

    /**
     * Verifies the key if applicable
     *
     * @throws WSSecurityException if the key couldn't be verified or the key isn't valid
     */
    public void verify() throws WSSecurityException;

    /**
     * Returns the key wrapping token
     *
     * @return The wrapping SecurityToken
     */
    public SecurityToken getKeyWrappingToken();

    /**
     * Returns the Key wrapping token's algorithm
     *
     * @return the KeyWrappingToken algorithm
     */
    public String getKeyWrappingTokenAlgorithm();

    /**
     * Returns the KeyIdentifierType
     *
     * @return the KeyIdentifierType
     */
    public Constants.TokenType getTokenType();
}
