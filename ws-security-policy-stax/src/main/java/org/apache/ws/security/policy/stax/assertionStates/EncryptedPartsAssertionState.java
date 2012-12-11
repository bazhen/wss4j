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
package org.apache.ws.security.policy.stax.assertionStates;

import org.apache.ws.security.policy.AssertionState;
import org.apache.ws.security.policy.WSSPolicyException;
import org.apache.ws.security.policy.model.AbstractSecurityAssertion;
import org.apache.ws.security.policy.model.EncryptedParts;
import org.apache.ws.security.policy.model.Header;
import org.apache.xml.security.stax.securityEvent.SecurityEvent;
import org.apache.xml.security.stax.securityEvent.SecurityEventConstants;
import org.apache.ws.security.policy.stax.Assertable;
import org.apache.ws.security.stax.ext.WSSConstants;
import org.apache.ws.security.stax.ext.WSSUtils;
import org.apache.ws.security.stax.securityEvent.EncryptedPartSecurityEvent;
import org.apache.ws.security.stax.securityEvent.WSSecurityEventConstants;

import javax.xml.namespace.QName;
import java.util.LinkedList;
import java.util.List;

/**
 * WSP1.3, 4.2.1 EncryptedParts Assertion
 *
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class EncryptedPartsAssertionState extends AssertionState implements Assertable {

    public EncryptedPartsAssertionState(AbstractSecurityAssertion assertion, boolean asserted) {
        super(assertion, asserted);
    }

    @Override
    public SecurityEventConstants.Event[] getSecurityEventType() {
        return new SecurityEventConstants.Event[]{
                WSSecurityEventConstants.EncryptedPart
        };
    }

    @Override
    public boolean assertEvent(SecurityEvent securityEvent) throws WSSPolicyException {

        //todo attachments

        EncryptedPartSecurityEvent encryptedPartSecurityEvent = (EncryptedPartSecurityEvent) securityEvent;
        EncryptedParts encryptedParts = (EncryptedParts) getAssertion();

        if (encryptedParts.isBody()
                && (WSSUtils.pathMatches(WSSConstants.SOAP_11_BODY_PATH, encryptedPartSecurityEvent.getElementPath(), true, false))) {
            if (encryptedPartSecurityEvent.isEncrypted()) {
                setAsserted(true);
                return true;
            } else {
                setAsserted(false);
                setErrorMessage("Element " + WSSUtils.pathAsString(encryptedPartSecurityEvent.getElementPath()) + " must be encrypted");
                return false;
            }
        }
        //body processed above. so this must be a header element
        for (int i = 0; i < encryptedParts.getHeaders().size(); i++) {
            Header header = encryptedParts.getHeaders().get(i);
            QName headerQName = new QName(header.getNamespace(), header.getName() == null ? "" : header.getName());

            List<QName> header11Path = new LinkedList<QName>();
            header11Path.addAll(WSSConstants.SOAP_11_HEADER_PATH);
            header11Path.add(headerQName);

            if (WSSUtils.pathMatches(header11Path, encryptedPartSecurityEvent.getElementPath(), true, header.getName() == null)) {
                if (encryptedPartSecurityEvent.isEncrypted()) {
                    setAsserted(true);
                    return true;
                } else {
                    setAsserted(false);
                    setErrorMessage("Element " + WSSUtils.pathAsString(encryptedPartSecurityEvent.getElementPath()) + " must be encrypted");
                    return false;
                }
            }
        }

        //if we return false here other encrypted elements will trigger a PolicyViolationException
        return true;
    }
}