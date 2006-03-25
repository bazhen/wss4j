/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ws.security.message;

import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.message.token.SecurityContextToken;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;

/**
 * Builder class to add a <code>wsc:SecurityContextToken</code> into the
 * <code>wsse:Security</code>
 * 
 * @author Ruchith Fernando (ruchith.fernando@gmail.com)
 */
public class WSSecSecurityContextToken {

    /**
     * The <code>wsc:SecurityContextToken</code> to be added to the
     * <code>wsse:SecurityHeader</code>
     */
    private SecurityContextToken sct;

    /**
     * The <code>wsu:Id</code> of the <code>wsc:SecurityContextToken</code> 
     */
    private String sctId;

    /**
     * The <code>wsc:Identifier</code> of the
     * <code>wsc:SecurityContextToken</code>
     */
    private String identifier;

    /**
     * The symmetric secret associated with the SecurityContextToken
     */
    protected byte[] secret;

    public Document build(Document doc, Crypto crypto, WSSecHeader secHeader)
            throws WSSecurityException {
        this.prepare(doc, crypto);
        return doc;
    }

    protected void prepare(Document doc, Crypto crypto)
            throws WSSecurityException {

        if (sct == null) {
            if (this.identifier != null) {
                this.sct = new SecurityContextToken(doc, this.identifier);
            } else {
                this.sct = new SecurityContextToken(doc);
                this.identifier = this.sct.getIdentifier();
            }
        }

        // The wsu:Id of the wsc:SecurityContextToken
        if (this.sctId == null) {
            this.sctId = "sctid-" + this.sct.getElement().hashCode();
        }
        this.sct.setID(this.sctId);

    }

    public void commit(Document doc, Crypto crypto, WSSecHeader secHeader)
            throws WSSecurityException {
        WSSecurityUtil.prependChildElement(doc, secHeader.getSecurityHeader(),
                sct.getElement(), false);
    }

    /**
     * @return Returns the sct.
     */
    public SecurityContextToken getSct() {
        return sct;
    }

    /**
     * @param sct
     *            The sct to set.
     */
    public void setSct(SecurityContextToken sct) {
        this.sct = sct;
    }

    /**
     * @return Returns the ephemeralKey.
     */
    public byte[] getSecret() {
        return secret;
    }

    /**
     * @param ephemeralKey
     *            The ephemeralKey to set.
     */
    protected void setSecret(byte[] ephemeralKey) {
        this.secret = ephemeralKey;
    }

    /**
     * @return Returns the identifier.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier The identifier to set.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * @return Returns the sctId.
     */
    public String getSctId() {
        return sctId;
    }

    /**
     * @param sctId The sctId to set.
     */
    public void setSctId(String sctId) {
        this.sctId = sctId;
    }
    
}
