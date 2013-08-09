/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

/*
 * @(#)BASE64Decoder.java	1.3 06/29/07
 */ 

package com.sun.messaging.jmq.util;

import java.io.OutputStream;
import java.io.InputStream;
import java.io.PrintStream;

/**
 * This class implements a BASE64 Character decoder as specified in RFC1521.
 *
 * This RFC is part of the MIME specification which is published by the 
 * Internet Engineering Task Force (IETF). Unlike some other encoding 
 * schemes there is nothing in this encoding that tells the decoder 
 * where a buffer starts or stops, so to use it you will need to isolate 
 * your encoded data into a single chunk and then feed them this decoder. 
 * The simplest way to do that is to read all of the encoded data into a 
 * string and then use:
 * <pre>
 *	byte	mydata[];
 *	BASE64Decoder base64 = new BASE64Decoder();
 *
 *	mydata = base64.decodeBuffer(bufferString);
 * </pre>
 * This will decode the String in <i>bufferString</i> and give you an array 
 * of bytes in the array <i>myData</i>. 
 *
 * @see		CharacterEncoder
 * @see		BASE64Decoder
 */

public class BASE64Decoder extends CharacterDecoder {
	
    /** This class has 4 bytes per atom */
    protected int bytesPerAtom() {
	return (4);
    }

    /** Any multiple of 4 will do, 72 might be common */
    protected int bytesPerLine() {
	return (72);
    }

    /**
     * This character array provides the character to value map
     * based on RFC1521.
     */
    private final static char pem_array[] = {
	//       0   1   2   3   4   5   6   7
		'A','B','C','D','E','F','G','H', // 0
		'I','J','K','L','M','N','O','P', // 1
		'Q','R','S','T','U','V','W','X', // 2
		'Y','Z','a','b','c','d','e','f', // 3
		'g','h','i','j','k','l','m','n', // 4
		'o','p','q','r','s','t','u','v', // 5
		'w','x','y','z','0','1','2','3', // 6
		'4','5','6','7','8','9','+','/'  // 7
	};

    private final static byte pem_convert_array[] = new byte[256];

    static {
	for (int i = 0; i < 255; i++) {
	    pem_convert_array[i] = -1;
	}
	for (int i = 0; i < pem_array.length; i++) {
	    pem_convert_array[pem_array[i]] = (byte) i;
	}
    }

    byte decode_buffer[] = new byte[4];

    /**
     * Decode one BASE64 atom into 1, 2, or 3 bytes of data.
     */
    protected void decodeAtom(InputStream inStream, OutputStream outStream, int rem) 
	throws java.io.IOException
    {
	int	i;
	byte	a = -1, b = -1, c = -1, d = -1;

	if (rem < 2) 
	    throw new java.io.IOException();

	do {
	    i = inStream.read();
	    if (i == -1) {
		throw new CEStreamExhausted();
	    }
	} while (i == '\n' || i == '\r');
	decode_buffer[0] = (byte) i;

	i = readFully(inStream, decode_buffer, 1, rem-1);
	if (i == -1) {
	    throw new CEStreamExhausted();
	}

	if (rem > 3 && decode_buffer[3] == '=') {
	    rem = 3;
	}
	if (rem > 2 && decode_buffer[2] == '=') {
	    rem = 2;
	}
	switch (rem) {
	case 4:
	    d = pem_convert_array[decode_buffer[3] & 0xff];
	    // NOBREAK
	case 3:
	    c = pem_convert_array[decode_buffer[2] & 0xff];
	    // NOBREAK
	case 2:
	    b = pem_convert_array[decode_buffer[1] & 0xff];
	    a = pem_convert_array[decode_buffer[0] & 0xff];
	    break;
	}

	switch (rem) {
	case 2:
	    outStream.write( (byte)(((a << 2) & 0xfc) | ((b >>> 4) & 3)) );
	    break;
	case 3:
	    outStream.write( (byte) (((a << 2) & 0xfc) | ((b >>> 4) & 3)) );
	    outStream.write( (byte) (((b << 4) & 0xf0) | ((c >>> 2) & 0xf)) );
	    break;
	case 4:
	    outStream.write( (byte) (((a << 2) & 0xfc) | ((b >>> 4) & 3)) );
	    outStream.write( (byte) (((b << 4) & 0xf0) | ((c >>> 2) & 0xf)) );
	    outStream.write( (byte) (((c << 6) & 0xc0) | (d  & 0x3f)) );
	    break;
	}
	return;
    }
}
