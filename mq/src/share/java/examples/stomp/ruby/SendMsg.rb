#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2000-2010 Oracle and/or its affiliates. All rights reserved.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common Development
# and Distribution License("CDDL") (collectively, the "License").  You
# may not use this file except in compliance with the License.  You can
# obtain a copy of the License at
# https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
# or packager/legal/LICENSE.txt.  See the License for the specific
# language governing permissions and limitations under the License.
#
# When distributing the software, include this License Header Notice in each
# file and include the License file at packager/legal/LICENSE.txt.
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
# A simple script to send JMS text message(s) using STOMP Protocol.
#
require 'getoptlong' 
require 'socket'
require 'uri'

$host = nil
$port = nil
$dst = "simpleQ"
$msg = "Hello, World! from Ruby STOMP client"
$domain = "queue"
$user = "guest"
$passcode = "guest"
$count = 1

# Function to print usage
def usage
  puts
  puts "usage: jruby SendMsg.rb [options]"
  puts
  puts "where options include:"
  puts "  -h               Usage"
  puts "  -s <host:port>   Specify the STOMP server host and port."
  puts "  -d <name>        Specify the destination name. Default is simpleQ."
  puts "  -m \"<message>\"   Specify the msg to sent."
  puts "  -n <count>       Specify the number of message to send."
  puts "  -q               Specify the domain is a queue. Default is queue."
  puts "  -t               Specify the domain is a topic."
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
      [ '--m', '-m', GetoptLong::REQUIRED_ARGUMENT ],
      [ '--n', '-n', GetoptLong::REQUIRED_ARGUMENT ],
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
      when '--m'
        $msg = arg
      when '--u'
        $user = arg
      when '--p'
        $passcode = arg
      when '--s'
        $host = arg
      when '--n'
        $count = arg.to_i
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

  puts "STOMP server: #{$host}, Destination: #{$dst}, Domain: #{$domain}\n"

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

  # Send message(s)
  headers = {
    "destination" => "/#{$domain}/#{$dst}",
  }
  i = 0
  while i < $count
     if $count > 1
        textMsg = "(msg##{i}) #{$msg}"
     else
        textMsg = $msg
     end

     # Send request
     begin
       headers["receipt"] = "message-#{i}"
       doTransmit(socket, "SEND", headers, textMsg)
       replyFrame = checkStatus(socket, "SEND")

       # Print out the reply
       replyFrame.each do | value |
         puts value
       end
     rescue
        puts "Failed to send message.", $!
        break
     end

     puts "Sent msg: #{textMsg}"
     i += 1
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
