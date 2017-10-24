#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2000-2017 Oracle and/or its affiliates. All rights reserved.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common Development
# and Distribution License("CDDL") (collectively, the "License").  You
# may not use this file except in compliance with the License.  You can
# obtain a copy of the License at
# https://oss.oracle.com/licenses/CDDL+GPL-1.1
# or LICENSE.txt.  See the License for the specific
# language governing permissions and limitations under the License.
#
# When distributing the software, include this License Header Notice in each
# file and include the License file at LICENSE.txt.
#
# GPL Classpath Exception:
# Oracle designates this particular file as subject to the "Classpath"
# exception as provided by Oracle in the GPL Version 2 section of the License
# file that accompanied this code.
#
# Modifications:
# If applicable, add the following below the License Header, with the fields
# enclosed by brackets [] replaced by your own identifying information:
# "Portions Copyright [year] [name of copyright owner]"
#
# Contributor(s):
# If you wish your version of this file to be governed by only the CDDL or
# only the GPL Version 2, indicate your decision by adding "[Contributor]
# elects to include this software in this distribution under the [CDDL or GPL
# Version 2] license."  If you don't indicate a single choice of license, a
# recipient has the option to distribute your version of this file under
# either the CDDL, the GPL Version 2 or to extend the choice of license to
# its licensees as provided above.  However, if you add GPL Version 2 code
# and therefore, elected the GPL Version 2 license, then the option applies
# only if the new code is made subject to such option by the copyright
# holder.
#

#############################################################################
#
# A simple script to receive JMS text message(s) using STOMP Protocol.
#
require 'getoptlong'
require 'socket'
require 'uri'

$host = nil
$dst = "simpleQ"
$domain = "queue"
$user = "guest"
$passcode = "guest"

# Function to print usage
def usage
  puts
  puts "usage: jruby ReceiveMsg.rb [options]"
  puts
  puts "where options include:"
  puts "  -h               Usage"
  puts "  -s <host:port>   Specify the STOMP server host and port."
  puts "  -d <name>        Specify the destination name. Default is simpleQ."
  puts "  -q               Specify domain is a queue. Default is queue."
  puts "  -t               Specify domain is a topic."
  puts "  -u <user>        Specify the user name. Default is guest."
  puts "  -p <passcode>    Specify the passcode. Default is guest."
  puts
  exit 1
end

# Function to parse command line arguments
def parseArgs
  begin
    opts = GetoptLong.new(
      [ '--h', '-h', GetoptLong::NO_ARGUMENT ],
      [ '--q', '-q', GetoptLong::NO_ARGUMENT ],
      [ '--t', '-t', GetoptLong::NO_ARGUMENT ],
      [ '--d', '-d', GetoptLong::REQUIRED_ARGUMENT ],
      [ '--s', '-s', GetoptLong::REQUIRED_ARGUMENT ],
      [ '--u', '-u', GetoptLong::REQUIRED_ARGUMENT ],
      [ '--p', '-p', GetoptLong::REQUIRED_ARGUMENT ]
  )
  rescue
    puts "Error: parsing command line arguments"
    usage()
  end

  opts.each do |opt, arg|
    case opt
      when '--h'
        usage()
      when '--q'
        $domain = "queue"
      when '--t'
        $domain = "topic"
      when '--d'
        $dst = arg
      when '--u'
        $user = arg
      when '--p'
        $passcode = arg
      when '--s'
        $host = arg
    end
  end

  if $host == nil
    puts "Please specify the STOMP server host and port!"
    usage()
  end
end

# Function to get protocol reply frame
#    Returns server's responsed frame as an array of string
def getReply(socket, command)

  puts "\nGet #{command} reply ..."

  replyFrame = Array.new

  # Read data from socket until we find the end of frame char, i.e. null
  until (line = socket.gets) == nil
    line = line.chomp

    if line == "\0"
      break
    end

    replyFrame.push(line)

    # Check if null char is at the end of the string
    if line.rindex("\0") != nil
      break
    end
  end

  return replyFrame
end

# Function to check status
#    Returns server's responsed frame as an array of string
#    Raise exception if not successfull
def checkStatus(socket, command)

  replyFrame = getReply(socket, command)

  if replyFrame.length == 0
    raise "No reply"
  end

  if replyFrame.first == "ERROR"
    raise replyFrame.join("\n")
  end

  return replyFrame
end

# Function to transmit data
#    Raise exception if not successfull
def doTransmit(socket, command, headers={}, body="")

  puts "\nTransmit #{command} ..."

  begin
    # Write STOMP command
    socket.write "#{command}\n"

    # Write headers
    headers.each do | k,v |
      socket.write "#{k}:#{v}\n"
    end

    # Write blank line; indicates the end of the headers
    socket.write "\n"

    # Write body
    socket.write body

    socket.write "\0"
    socket.flush
  rescue
    raise "Error: #{$!}"
  end
end

# Main program
begin

  # Process command line args
  parseArgs()

  puts "STOMP Server: #{$host}, Destination: #{$dst}, Domain: #{$domain}\n"

  # Open a stream socket
  url = URI.parse("tcp://#{$host}")
  socket = TCPSocket::new(url.host, url.port)

  # Connect to the STOMP server
  begin
    headers = { "login" => $user, "passcode" => $passcode }
    doTransmit(socket, "CONNECT", headers)
    replyFrame = checkStatus(socket, "CONNECT")

    # Print out the reply
    replyFrame.each do | value |
      puts value
    end
  rescue
    puts "Failed to connect to STOMP server.", $!
    socket.close
    exit 1
  end

  # Send request to retrieve a JMS message
  begin
    headers = { "destination" => "/#{$domain}/#{$dst}" }
    doTransmit(socket, "SUBSCRIBE", headers)
  rescue
    puts "Failed to receive message.", $!
    break
  end

  # Get a message(s) from the server
  while true
    replyFrame = checkStatus(socket, "SUBSCRIBE")
    if replyFrame.length > 0
      time = Time.now
      puts time.strftime("[%d/%m/%Y:%H:%M:%S] ") + "Received msg:"
      replyFrame.each do | value |
        puts value
      end
    end
  end

  # Disconnect from STOMP server
  begin
    doTransmit(socket, "DISCONNECT")
  rescue
    puts "Failed to disconnect from STOMP server.", $!
  end

  # Closing the socket
  socket.close
end
