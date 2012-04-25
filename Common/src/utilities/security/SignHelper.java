/*
Copyright (c) 2012, Hammurabi Mendes
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package utilities.security;

import java.io.FileInputStream;

import java.security.KeyStore;
import java.security.Signature;

import java.security.PrivateKey;
import java.security.cert.Certificate;

public class SignHelper {
	public byte[] generateSignature(byte[] data, String trustStore, String trustPassword) {
		try {
			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());

			keystore.load(new FileInputStream(trustStore), trustPassword.toCharArray());

			Signature signatureHelper = Signature.getInstance("SHA1WithRSA", "SUN");

			PrivateKey privateKey = (PrivateKey) keystore.getKey("manager_signature_key", trustPassword.toCharArray());

			signatureHelper.initSign(privateKey);

			signatureHelper.update(data);

			return signatureHelper.sign();
		} catch(Exception exception) {
			return null;
		}
	}

	public boolean verifySignature(byte[] data, byte[] signature, String trustStore, String trustPassword) {
		try {
			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());

			keystore.load(new FileInputStream(trustStore), trustPassword.toCharArray());

			Signature signatureHelper = Signature.getInstance("SHA1WithRSA", "SUN");

			Certificate certificate = keystore.getCertificate("manager_signature_key");

			signatureHelper.initVerify(certificate);

			signatureHelper.update(data);

			return signatureHelper.verify(signature);
		} catch(Exception exception) {
			return false;
		}
	}
}
