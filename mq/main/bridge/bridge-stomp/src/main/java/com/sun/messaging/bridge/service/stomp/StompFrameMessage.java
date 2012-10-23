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

package com.sun.messaging.bridge.service.stomp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.memory.MemoryManager;
import org.glassfish.grizzly.utils.BufferOutputStream;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import com.sun.messaging.bridge.service.stomp.resources.StompBridgeResources;

/**
 * @author amyk 
 */
public class StompFrameMessage {

    private Logger _logger = null;

    private static final String STOMP_VERSION = "1.0";

    protected static final String HEADER_SEPERATOR = ":";
	private static final String NEWLINESTR = "\n";

    private static final byte NEWLINE_BYTE = '\n';
    private static final byte NULL_BYTE = '\0';
    private static final byte[] END_OF_FRAME = new byte[]{0, '\n'};

    protected static final int MIN_COMMAND_LEN = 3;
    protected static final int MAX_COMMAND_LEN = 1024;
    protected static final int MAX_HEADER_LEN = 1024 * 10;
    private static final int MAX_HEADERS = 1000;

    public enum Command {
        CONNECT, SEND, DISCONNECT, SUBSCRIBE, UNSUBSCRIBE, BEGIN, COMMIT, ABORT, ACK, 

        UNKNOWN,

        //responses
        CONNECTED, MESSAGE, RECEIPT, ERROR
    };	

    public enum CommonHeader { 
        ;
        final static String RECEIPT = "receipt";
        final static String TRANSACTION = "transaction";
        final static String CONTENTLENGTH = "content-length";
    }   

    public enum ResponseCommonHeader {
        ;
        final static String RECEIPTID = "receipt-id";
    }

    public enum SendHeader {
        ;
        final static String DESTINATION = "destination";
        final static String EXPIRES = "expires";
		final static String PRIORITY = "priority";
        final static String TYPE = "type";
        final static String PERSISTENT = "persistent";
        final static String REPLYTO = "reply-to";
        final static String CORRELATIONID = "correlation-id";
    }

    public enum MessageHeader {
        ;
        final static String DESTINATION = "destination";
        final static String MESSAGEID = "message-id";
        final static String TIMESTAMP = "timestamp";
        final static String EXPIRES = "expires";
        final static String PRORITY = "priority";
        final static String REDELIVERED = "redelivered";
        final static String TYPE = "type";
        final static String REPLYTO = "reply-to";
        final static String CORRELATIONID = "correlation-id";
        final static String SUBSCRIPTION = "subscription";
    }

    public enum SubscribeHeader {
        ;
        final static String DESTINATION = "destination";
        final static String SELECTOR = "selector";
        final static String ACK = "ack";
        final static String ID = "id";
        final static String DURASUBNAME = "durable-subscriber-name";
        final static String NOLOCAL = "no-local";
    }

    public enum AckMode {
        ;
        final static String AUTO = "auto";
        final static String CLIENT = "client";
    }

    public enum UnsubscribeHeader {
        ;
        final static String DESTINATION = "destination";
        final static String ID = "id";
    }

    public enum ConnectHeader {
        ;
        final static String LOGIN = "login";
        final static String PASSCODE = "passcode";
        final static String CLIENTID = "client-id";
        final static String VERSION = "version";
    }

    public enum ErrorHeader {
        ;
        final static String MESSAGE = "message";
    }

    public enum ConnectedHeader {
        ;
        final static String SESSION = "session";
    }

    public enum AckHeader {
        ;
        final static String MESSAGEID = "message-id";
    }

    protected static enum ParseStage { COMMAND, HEADER, BODY, NULL, DONE };

    private Command _command = Command.UNKNOWN;

    private ArrayList<String> _requiredHeaders = new ArrayList<String>();
    private LinkedHashMap<String, String> _headers = new LinkedHashMap<String, String>();

    private Integer _contentLength = null; 

    protected ParseStage _parseStage = ParseStage.COMMAND;

    private int _byteBufferPosition = 0;

    private ByteArrayOutputStream _bao = null;
    private byte[] _body = null;
    private Exception _parseException = null;

    private boolean _fatalERROR = false;
    private StompBridgeResources _sbr = null;


    protected StompFrameMessage(Command cmd) { 
        _logger = StompServer.getLogger();

        _command = cmd;
        _sbr = StompServer.getStompBridgeResources();

        switch (cmd) {
            case CONNECT:
                _requiredHeaders.add((ConnectHeader.LOGIN).toString());
                _requiredHeaders.add((ConnectHeader.PASSCODE).toString());
                break;
            case SEND:
                _requiredHeaders.add((SendHeader.DESTINATION).toString());
                break;
        }
    }

    /**
     * to be used only for ERROR frame
     */
    protected void setFatalERROR() {
        _fatalERROR = true;
    }

    public boolean isFatalERROR() {
        return _fatalERROR;
    }

    protected Exception getParseException() {
        return _parseException;
    }

    public Command getCommand() {
        return _command;
    }
    
    protected void addHeader(String key, String val) {
        _headers.put(key, val);
    }

    protected LinkedHashMap<String, String> getHeaders() {
         return _headers;
    }

    protected String getHeader(String key) {
         return _headers.get(key);
    }

    protected byte[] getBody() {
        if (_body != null) return _body;
        if (_bao == null) return (new byte[]{});
        _body = _bao.toByteArray();
        return _body;
    }

    protected String getBodyText() throws FrameParseException {
        String text = "";
         
        if (_body != null)  {
            try {
                if (_body != null) return new String(_body, "UTF-8");
            } catch (Exception e) {
                throw new FrameParseException(e.getMessage(), e);
            }
        }

        if (_bao == null) return text;

        _body = _bao.toByteArray();

        try {
            text =  new String(_body, "UTF-8");
            return text;
        } catch (Exception e) {
            throw new FrameParseException(_sbr.getKString(
            _sbr.X_CANNOT_PARSE_BODY_TO_TEXT, getCommand(), e.getMessage()));
        }
    }
    
    private void writeByteToBody(byte b) throws Exception {
        if (_bao == null) {
            if (getContentLength() != -1) {
                _bao = new ByteArrayOutputStream(getContentLength());
            } else {
                _bao = new ByteArrayOutputStream();
            }
        }
        _bao.write(b);
    }

    protected void setBody(byte[] data) {
        _body = data;
    }

    protected void writeExceptionToBody(Throwable t) throws Exception {
        if (_bao == null) {
            _bao = new ByteArrayOutputStream();
        }
        t.printStackTrace(new PrintStream(_bao, true, "UTF-8"));
        addHeader(CommonHeader.CONTENTLENGTH, String.valueOf(getBodySize()));
    }

    private int getBodySize() {
        if (_bao == null) return 0;
        return _bao.size();
    }

    protected void setNextParseStage(ParseStage s) {
        _parseStage = s;
        if (s == ParseStage.BODY) {
            for (String key: _requiredHeaders) {
                if (_headers.get(key) == null) {
                    if (_parseException == null) {
                     _parseException = new FrameParseException(
                         _sbr.getKString(_sbr.X_HEADER_MISSING, key, getCommand()));
                     _logger.log(Level.SEVERE,  _parseException.getMessage());
                    }
                }
            }
        }
        if (s == ParseStage.DONE) {
            try {
                if (_bao != null) _bao.close();
            } catch (Exception e) {
                _logger.log(Level.WARNING, 
                "Exception in closing ByteArrayOutputStream:"+e.getMessage());
            }
        }
    }

    protected ParseStage getNextParseStage() {
        return _parseStage;
    }

    private void setByteBufferPosition(int pos) {
        _byteBufferPosition = pos;
    }

    protected int getByteBufferPosition() {
        return _byteBufferPosition;
    }

    protected int getContentLength() {
        if (_contentLength != null) return _contentLength.intValue(); 

        String val = _headers.get(CommonHeader.CONTENTLENGTH);
        if (val == null) return -1;
        int len = -1;
        try {
            len =Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            if (_parseException == null) {
                _parseException = new FrameParseException(_sbr.getKString(
                      _sbr.X_INVALID_HEADER_VALUE, val, CommonHeader.CONTENTLENGTH));
                len = -1;
                _logger.log(Level.SEVERE, _parseException.getMessage());
            }
        }
        _contentLength = new Integer(len); 
        return len;
    }

    public Buffer marshall(MemoryManager mm) throws IOException {
        BufferOutputStream bos = new BufferOutputStream(mm);
        DataOutputStream dos = new DataOutputStream(bos);

        StringBuffer sbuf = new StringBuffer();
        sbuf.append(getCommand());
        sbuf.append(NEWLINESTR);
        for (String key: _headers.keySet()) { 
            sbuf.append(key);
            sbuf.append(HEADER_SEPERATOR);
            sbuf.append(_headers.get(key));
            sbuf.append(NEWLINESTR);
        }
        sbuf.append(NEWLINESTR);

        dos.write(sbuf.toString().getBytes("UTF-8"));
        dos.write(getBody());
        dos.write(END_OF_FRAME);
        dos.flush();
        dos.close();
        bos.close();
        Buffer bb = bos.getBuffer();
        bb.flip();
        return bb;
    }

    /**
     *
     */
    public void parseHeader(Buffer buf) throws Exception {
        String header = null;

        int pos = buf.position();

        if (_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST, 
            "in parseHeader: position="+buf.position()+", remaining="+buf.remaining());
        }

        try {

        while (buf.hasRemaining()) {
            byte[] line = parseLine(buf, MAX_HEADER_LEN);
            if (line == null) {
                return;
            }
            header = new String(line, "UTF-8");

            if (_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST, 
               "parseHeader: got line byte-length="+line.length+
               ", header=:"+header+", header-length="+header.length()+", position="+buf.position());
            }

            if (header.trim().length() == 0) {
                setNextParseStage(ParseStage.BODY);
                if (_logger.isLoggable(Level.FINEST)) {
                    _logger.log(Level.FINEST, "parseHeader: DONE - position="+buf.position());
                }
                return;
            }
            int index = header.indexOf(HEADER_SEPERATOR);
            if (index == -1) {
                if (_parseException == null) {
                    _parseException = new FrameParseException(
                        _sbr.getKString(_sbr.X_INVALID_HEADER, header));
                    _logger.log(Level.SEVERE, _parseException.getMessage());
                }
                index = header.length()-1;
            }
            String key = header.substring(0, index).trim();
            String val = header.substring(index+1, header.length()).trim();
            addHeader(key, val);
            if (_headers.size() > MAX_HEADERS) { //XXX
                throw new FrameParseException(
                _sbr.getKString(_sbr.X_MAX_HEADERS_EXCEEDED, MAX_HEADERS));
            }
        }

        } catch (Exception e) {
            if (e instanceof FrameParseException) {
                throw e;
            }
            throw new FrameParseException(_sbr.getKString(
                _sbr.X_EXCEPTION_PARSE_HEADER, header, e.getMessage()), e);
        }
    }

    /**
     *
     */
    public void readBody(Buffer buf) throws Exception {

        int clen = getContentLength();

        if (_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST, 
            "in readBody:contentLen="+_contentLength+", position="+buf.position()+
            ", remaining="+buf.remaining() + ", bodySize=" + getBodySize());
        }

        byte b;
        while (buf.hasRemaining()) {
            if (clen != -1 && clen == getBodySize()) { 
                _logger.log(Level.FINEST, "Body has beed read!");
                setNextParseStage(ParseStage.NULL);
                return;
            }
            b = buf.get();
            if (b == NULL_BYTE && clen == -1) {

                if (buf.hasRemaining()) {
                    int pos = buf.position();
                    byte bb = buf.get();
                    if (bb != '\n' && bb != '\r') {
                        buf.position(pos);
                    }
                }
                if (buf.hasRemaining()) {
                    int pos = buf.position();
                    byte bb = buf.get();
                    if (bb != '\n') {
                        buf.position(pos);
                    }
                }
		        if (_logger.isLoggable(Level.FINEST)) {
                    _logger.log(Level.FINEST, 
                    "readBody: DONE - position="+buf.position()+", remaining="+buf.remaining());
                }

                setNextParseStage(ParseStage.DONE);
                return;
            }
            writeByteToBody(b);
        }
        _logger.log(Level.FINEST, "leaving readBody(): BODY_SIZE=" + getBodySize());
        return;
    }

    /**
     *
     */
    public void readNULL(Buffer buf) throws Exception {

        if (_logger.isLoggable(Level.FINEST)) {
        _logger.log(Level.FINEST, "in readNULL:"+buf.position()+":"+buf.remaining());
        }
        if (buf.remaining() <= 0) {
            return; 
        }
        byte b = buf.get();
        if (b != 0) {
            throw new FrameParseException(_sbr.getKString(_sbr.X_NO_NULL_TERMINATOR,
                      CommonHeader.CONTENTLENGTH+" "+getContentLength()));
        }

		if (_logger.isLoggable(Level.FINEST)) {
        _logger.log(Level.FINEST, "got NULL readNULL:"+buf.position()+":"+buf.remaining());
        }

        setNextParseStage(ParseStage.DONE);
        return;
    }

    public String toString() {
        return _command+"["+_headers+"]";
    }


    /**
     *
     */
    public static StompFrameMessage parseCommand(Buffer buf) throws Exception {

        Logger logger = StompServer.logger();

        StompFrameMessage message = null;
        String cmd = "";

        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, 
            "parseCommand: pos:remaining["+buf.position()+":"+buf.remaining()+"]");
        }

        try {

        while (cmd.trim().length() == 0) {
            byte[] line = parseLine(buf, MAX_COMMAND_LEN);
            if (line == null) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.log(Level.FINEST, 
                    "parseCommand: position["+buf.position()+"] command line not found");
                }
			    return null;
            }
            cmd = new String(line, "UTF-8");

            if (logger.isLoggable(Level.FINEST)) {
                logger.log(Level.FINEST, 
                "parseCommand: got line:"+cmd+", position="+buf.position());
            }
        }
        

        if (cmd.startsWith((Command.CONNECT).toString())) {
            message = new StompFrameMessage(Command.CONNECT);
        } else if (cmd.startsWith((Command.SEND).toString())) {
            message = new StompFrameMessage(Command.SEND);
        } else if (cmd.startsWith((Command.SUBSCRIBE).toString())) {
            message = new StompFrameMessage(Command.SUBSCRIBE);
        } else if (cmd.startsWith((Command.ACK).toString())) {
            message = new StompFrameMessage(Command.ACK);
        } else if (cmd.startsWith((Command.UNSUBSCRIBE).toString())) {
            message = new StompFrameMessage(Command.UNSUBSCRIBE);
        } else if (cmd.startsWith((Command.BEGIN).toString())) {
            message = new StompFrameMessage(Command.BEGIN);
        } else if (cmd.startsWith((Command.COMMIT).toString())) {
            message = new StompFrameMessage(Command.COMMIT);
        } else if (cmd.startsWith((Command.ABORT).toString())) {
            message = new StompFrameMessage(Command.ABORT);
        } else if (cmd.startsWith((Command.DISCONNECT).toString())) {
            message = new StompFrameMessage(Command.DISCONNECT);
        } else {
            message = new StompFrameMessage(Command.ERROR);
            String emsg = StompServer.getStompBridgeResources().getKString(
                              StompBridgeResources.X_UNKNOWN_STOMP_CMD, cmd);
            message._parseException = new FrameParseException(emsg);
            logger.log(Level.SEVERE, emsg);
        }

        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST,
            "parseCommand: DONE - cmd="+cmd+", position="+buf.position());
        }

        message.setNextParseStage(ParseStage.HEADER);

        } catch (Exception e) {
            if (e instanceof FrameParseException) {
                throw e;
            }
            throw new FrameParseException(e.getMessage(), e);
        }

        return message;
    }

    /**
     * 
     */
    private static byte[] parseLine(Buffer buf, int maxbytes) 
    throws Exception {

        Logger logger = StompServer.logger();

        byte[] line = new byte[maxbytes];
        int pos = buf.position();
        boolean foundline = false;
        int i = 0;
        byte b;
        while (buf.hasRemaining()) {
            b = buf.get();
/*
            if (logger.isLoggable(Level.FINEST)) {
                logger.log(Level.FINEST, "parseLine: byte="+Byte.valueOf(b));
            }
*/
            if (b == NEWLINE_BYTE) {
                foundline = true; 
                break;
            }
            line[i++] = b;
            if (i >= (maxbytes-1)) {
                throw new FrameParseException(
                    StompServer.getStompBridgeResources().getKString(
                    StompBridgeResources.X_MAX_LINELEN_EXCEEDED, maxbytes));
            }
        }
        if (!foundline) {
            buf.position(pos);
            return null;
        }
        byte[] tmp = new byte[i];
	    System.arraycopy(line, 0, tmp, 0, i);
        return tmp;
    }
         
}
