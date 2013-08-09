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

1.  Protocol -

    Packet format -

     0                   1                   2                   3
    |0 1 2 3 4 5 6 7|8 9 0 1 2 3 4 5|6 7 8 9 0 1 2 3|4 5 6 7 8 9 0 1|
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |            version            |        packet type            |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |                             size                              |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |                        source connection id                   |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |                      destination connection id                |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |                       packet seq number                       |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |                           checksum                            |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

    Connection establishment -

    1.  Client initiates connection by sending a request.
        Client -> Server : packet type = CONNECT,
                           source connection id = unique ID (client).

    2.  When the server accepts the connection, it responds with -
        Server -> Client : packet type = ACCEPTED,
                           dest connection id = unique ID (server).

    Sending packets from CLIENT to BROKER -

    1.  Client sends an HTTP request, client data is sent as request
        payload/content.

    2.  Servlet consumes the HTTP request and writes the data to the
        broker ingress queue.
    3.  Servlet responds with HTTP 200 OK. If there are any packets
        in the broker egress queue, they are sent as response content.

    4.  If client receives an HTTP error - it resends the packet.
    5.  If client receives the 200 OK, it read the data, if any, and
        enqueues it.

    Sending packets from BROKER to CLIENT -

    1.  Broker writes the data to the socket - the packet gets added
        to the egress queue.
    2.  Appropriate servlet request instance is selected for carrying
        the data back to the client - Piggybacking the packet on a
        'push' response is more efficient.

    3.  The data is sent back as response content.


2.  CONNECTION CLOSE protocol

    On HttpTunnelSocket.close() -
        rxShutdown
            Junk recvQ
            Start discarding all the incoming data pkts
            Stop sending ACKs.
        Enqueue CONN_CLOSE_PACKET to the transmit window.
        Disable further read / write calls from the application.
        Retransmit timers continue to work normally
        Continue processing ACKs normally

    On CONN_CLOSE_ACK -
        txShutdown
            Junk sendQ
            Stop all the retransmit timers
            disable ACK processing.

    On CONN_CLOSE_PACKET -
        Disable writes from the App.
        txShutdown
            Junk sendQ
            Stop all the retransmit timers
            disable ACK processing.

        Continue processing incoming packets -
            if incoming packet is :
                nextRecvSeq <= seq <= closeConnSeq
            then process it normally, else ignore it.
            Send acks normally.

    When HttpTunnelConnection.readData() hits the CONN_CLOSE_PACKET -
        rxShutdown
            Junk recvQ
            Start discarding all the incoming data pkts
            Stop sending ACKs.


3.  TODO -

    Development

        *   Retransmit timer handling - reentrancy issues???
        *   Window update timer - i.e. probe packet
        *   Configurable/Negotiable pull request rate
        *   Multiple listeners (listener ports)
        *   Multiple ServerLinks (scalability)
        *   Stats

    Testing
        Data communication
            Fast retransmit - random packet loss at the servlet.
            RTO binary exponential backoff - Intermediary failure
            Stress test with random packet loss - Track the following metrics -
                Throughput
                Queue lengths
                Memory consumption / object population.
                RTO
                Retranmission stats
            Flow control
            Run stress test on a multi-processor system.

        Connection state machine
            Connection establishment
            Connection failure
                Web server failure
                Server failure
                Client failure
            Connection closure

        API tests

