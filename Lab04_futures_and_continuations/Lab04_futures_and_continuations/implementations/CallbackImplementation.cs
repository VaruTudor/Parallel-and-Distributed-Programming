using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using Lab04_futures_and_continuations.domain;

namespace Lab04_futures_and_continuations.implementations
{
    public static class CallbackImplementation
    {
        public static void Run(List<string> hostnames)
        {
            for (var i = 0; i < hostnames.Count; i++)
            {
                StartClient(hostnames[i], i);
                Thread.Sleep(1000);
            }
        }

        private static void StartClient(string host, int id)
        {
            // Dns - Provides simple domain name resolution functionality
            var ipHostInfo = Dns.GetHostEntry(host.Split('/')[0]);
            var ipAddress = ipHostInfo.AddressList[0];
            var remoteEndpoint = new IPEndPoint(ipAddress, Parser.Port);

            var clientSocket =
                new Socket(ipAddress.AddressFamily, SocketType.Stream, ProtocolType.Tcp);

            var requestSocket = new CustomSocket
            {
                Socket = clientSocket,
                Hostname = host.Split('/')[0],
                Endpoint = host.Contains("/") ? host[host.IndexOf("/", StringComparison.Ordinal)..] : "/",
                RemoteIpEndPoint = remoteEndpoint,
                Id = id
            };

            // connect to the remote endpoint
            requestSocket.Socket.BeginConnect(requestSocket.RemoteIpEndPoint, Connected, requestSocket); 
        }

        private static void Connected(IAsyncResult ar)
        {
            var resultSocket = (CustomSocket) ar.AsyncState;
            
            if (resultSocket == null) return; // null check
            var clientSocket = resultSocket.Socket;
            var clientId = resultSocket.Id;

            clientSocket.EndConnect(ar); // end connection
            Console.WriteLine("Connection {0} - Socket Connected", clientId);

            var byteData =
                Encoding.ASCII.GetBytes(Parser.GetRequestString(resultSocket.Hostname, resultSocket.Endpoint));

            // connect to the remote endpoint
            resultSocket.Socket.BeginSend(byteData, 0, byteData.Length, 0, Sent, resultSocket);
        }

        private static void Sent(IAsyncResult ar)
        {
            var resultSocket = (CustomSocket) ar.AsyncState;
            
            if (resultSocket == null) return; // null check
            var clientSocket = resultSocket.Socket;
            var clientId = resultSocket.Id;

            // send data to server
            var bytesSent = clientSocket.EndSend(ar);
            Console.WriteLine("Connection {0} - Sent {1} bytes to server.", clientId, bytesSent);

            // server response (data)
            resultSocket.Socket.BeginReceive(resultSocket.Buffer, 0, CustomSocket.Size, 0, Receiving, resultSocket);
        }

        private static void Receiving(IAsyncResult ar)
        {
            var resultSocket = (CustomSocket) ar.AsyncState;
            
            if (resultSocket == null) return; // null check
            var clientSocket = resultSocket.Socket;

            try
            {
                var bytesRead = clientSocket.EndReceive(ar); // read response data

                resultSocket.ResponseContent.Append(Encoding.ASCII.GetString(resultSocket.Buffer, 0, bytesRead));

                // if the response header has not been fully obtained, get the next chunk of data
                if (!Parser.ResponseHeaderObtained(resultSocket.ResponseContent.ToString()))
                {
                    clientSocket.BeginReceive(resultSocket.Buffer, 0, CustomSocket.Size, 0, Receiving,
                        resultSocket);
                }
                else
                {
                    /*Console.WriteLine("Content length is - {0}",
                        Parser.GetContentLen(resultSocket.ResponseContent.ToString()));*/

                    Console.WriteLine("Content length is - {0}", resultSocket.ResponseContent);
                    
                    clientSocket.Shutdown(SocketShutdown.Both); // Disables sends and receives on clientSocket
                    clientSocket.Close(); // Close clientSocket connection and releases all resources.
                }
            }
            catch (Exception e)
            {
                Console.WriteLine(e.ToString());
            }
        }
    }
}