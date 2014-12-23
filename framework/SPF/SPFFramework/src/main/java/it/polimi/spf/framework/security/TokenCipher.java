/* 
 * Copyright 2014 Jacopo Aliprandi, Dario Archetti
 * 
 * This file is part of SPF.
 * 
 * SPF is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free 
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * SPF is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with SPF.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package it.polimi.spf.framework.security;

import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

public class TokenCipher {

	public static class WrongPassphraseException extends Exception {

		private static final long serialVersionUID = 1L;

	}

	private static final String PASSWORD_HASH_ALGORITHM = "SHA-256";
	private static final String KEY_ALGORITHM = "AES";
	private static final String KEY_CYPHER_ALGORITHM = "AES";
	private static final String TOKEN_PREFIX = "token:";
	private static final Charset CHARSET = Charset.forName("ASCII");
	private static final int KEY_SIZE = 16;

	public static String encryptToken(String token, String passphrase) throws GeneralSecurityException {
		byte[] tokenBytes = (TOKEN_PREFIX + token).getBytes(CHARSET);
		Key key = buildKey(passphrase);
		Cipher cipher = buildCipher(Cipher.ENCRYPT_MODE, key);
		byte[] encrypted = cipher.doFinal(tokenBytes);
		return Base64.encodeToString(encrypted, Base64.DEFAULT);
	}

	public static String decryptToken(String cryptedToken, String passphrase) throws GeneralSecurityException, WrongPassphraseException {
		byte[] cryptedBytes = Base64.decode(cryptedToken, Base64.DEFAULT);
		Key key = buildKey(passphrase);
		Cipher cipher = buildCipher(Cipher.DECRYPT_MODE, key);
		byte[] decrypted;
		
		try {
			decrypted = cipher.doFinal(cryptedBytes);
		} catch (BadPaddingException e) {
			throw new WrongPassphraseException();
		}
		
		String prefixedToken = new String(decrypted, CHARSET);

		String[] split = prefixedToken.split(":");
		if (split.length < 2 || !split[0].equals("token")) {
			throw new WrongPassphraseException();
		}

		return split[1];
	}

	private static Key buildKey(String password) throws NoSuchAlgorithmException {
		MessageDigest digester = MessageDigest.getInstance(PASSWORD_HASH_ALGORITHM);
		digester.update(String.valueOf(password).getBytes(CHARSET));
		byte[] key = new byte[KEY_SIZE];
		System.arraycopy(digester.digest(), 0, key, 0, KEY_SIZE);
		System.out.println("Crypt key: " + Arrays.toString(key));
		SecretKeySpec spec = new SecretKeySpec(key, KEY_ALGORITHM);
		return spec;
	}

	private static Cipher buildCipher(int mode, Key key) throws GeneralSecurityException {
		Cipher cipher = Cipher.getInstance(KEY_CYPHER_ALGORITHM);
		cipher.init(mode, key);
		return cipher;
	}
}