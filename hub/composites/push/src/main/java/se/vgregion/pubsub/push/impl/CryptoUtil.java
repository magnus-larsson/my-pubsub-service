/**
 * Copyright 2010 Västra Götalandsregionen
 *
 *   This library is free software; you can redistribute it and/or modify
 *   it under the terms of version 2.1 of the GNU Lesser General Public
 *   License as published by the Free Software Foundation.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the
 *   Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *   Boston, MA 02111-1307  USA
 *
 */

package se.vgregion.pubsub.push.impl;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;

public class CryptoUtil {

	private static final String ENCODING = "UTF-8";
	private static final String ALGORITHM_SHA1 = "HmacSHA1";
	
	public static String calculateHmacSha1(String secret, String content) {
		try {
			Mac mac = Mac.getInstance(ALGORITHM_SHA1);
			SecretKeySpec key = new SecretKeySpec(secret.getBytes(ENCODING), ALGORITHM_SHA1);
			mac.init(key);
			byte[] digest = mac.doFinal(content.getBytes(ENCODING));

			return Hex.encodeHexString(digest);
		} catch (NoSuchAlgorithmException e) {
			// should never happen
			throw new RuntimeException(e);
		} catch (InvalidKeyException e) {
			// should never happen
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			// should never happen
			throw new RuntimeException(e);
		}
	}

}
