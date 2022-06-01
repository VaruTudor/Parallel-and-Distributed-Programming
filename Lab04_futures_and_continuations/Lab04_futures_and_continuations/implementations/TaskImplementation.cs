using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;
using Lab04_futures_and_continuations.domain;

namespace Lab04_futures_and_continuations.implementations
{
    public static class TaskImplementation
    {
        private static List<string> _hosts;

        public static void Run(List<string> hostnames, bool async)
        {
            _hosts = hostnames;
            var tasks = new List<Task>();

            for (var i = 0; i < hostnames.Count; i++)
            {
                tasks.Add(async ? Task.Factory.StartNew(StartAsync, i) : Task.Factory.StartNew(Start, i));
            }

            Task.WaitAll(tasks.ToArray());
        }

        private static void StartAsync(object idObject)
        {
            var id = (int)idObject;

            StartAsyncClient(_hosts[id], id);
        }

        private static void Start(object idObject)
        {
            var id = (int)idObject;

            StartClient(_hosts[id], id);
        }

        private static void StartClient(string host, int id)
        {
            var ipHostInfo = Dns.GetHostEntry(host.Split('/')[0]); 
            var ipAddr = ipHostInfo.AddressList[0];
            var remEndPoint = new IPEndPoint(ipAddr, Parser.Port);
            var client = new Socket(ipAddr.AddressFamily, SocketType.Stream, ProtocolType.Tcp);

            var requestSocket = new CustomSocket
            {
                Socket = client,
                Hostname = host.Split('/')[0],
                Endpoint = host.Contains("/") ? host[host.IndexOf("/", StringComparison.Ordinal)..] : "/",
                RemoteIpEndPoint = remEndPoint,
                Id = id
            }; 

            // connect
            Connect(requestSocket).Wait();
            
            // request data
            Send(requestSocket, Parser.GetRequestString(requestSocket.Hostname, requestSocket.Endpoint)).Wait();
            
            // receive response
            Receive(requestSocket).Wait(); 

            Console.WriteLine("Connection {0} > Content length is:{1}", requestSocket.Id, Parser.GetContentLen(requestSocket.ResponseContent.ToString()));
            client.Shutdown(SocketShutdown.Both);
            client.Close();
        }

        private static async void StartAsyncClient(string host, int id)
        {
            var ipHostInfo = Dns.GetHostEntry(host.Split('/')[0]);
            var ipAddress = ipHostInfo.AddressList[0];
            var remoteIpEndPoint = new IPEndPoint(ipAddress, Parser.Port);
            var client = new Socket(ipAddress.AddressFamily, SocketType.Stream, ProtocolType.Tcp);

            var requestSocket = new CustomSocket
            {
                Socket = client,
                Hostname = host.Split('/')[0],
                Endpoint = host.Contains("/") ? host[host.IndexOf("/", StringComparison.Ordinal)..] : "/",
                RemoteIpEndPoint = remoteIpEndPoint,
                Id = id
            };

            // connect
            await ConnectAsync(requestSocket);

            // request data
            await SendAsync(requestSocket, Parser.GetRequestString(requestSocket.Hostname, requestSocket.Endpoint)); 

            // receive response
            await ReceiveAsync(requestSocket); 

            Console.WriteLine("Connection {0} > Content is:{1}", requestSocket.Id, requestSocket.ResponseContent);
            client.Shutdown(SocketShutdown.Both);
            client.Close();
        }

        private static async Task ConnectAsync(CustomSocket state)
        {
            state.Socket.BeginConnect(state.RemoteIpEndPoint, ConnectCallback, state);

            // Blocks the current thread until the current WaitHandle receives a signal.
            await Task.FromResult<object>(state.ConnectDone.WaitOne()); 
        }

        private static Task Connect(CustomSocket state)
        {
            state.Socket.BeginConnect(state.RemoteIpEndPoint, ConnectCallback, state);

            // Blocks the current thread until the current WaitHandle receives a signal.
            return Task.FromResult(state.ConnectDone.WaitOne()); 
        }

        private static void ConnectCallback(IAsyncResult ar)
        {
            var resultSocket = (CustomSocket)ar.AsyncState;
            Debug.Assert(resultSocket != null, nameof(resultSocket) + " != null");
            var clientSocket = resultSocket.Socket;
            var clientId = resultSocket.Id;
            var hostname = resultSocket.Hostname;

            clientSocket.EndConnect(ar);
            Console.WriteLine("Connection {0} > Socket connected to {1} ({2})", clientId, hostname, clientSocket.RemoteEndPoint);

            // Set the state of the event to signaled, allowing one or more waiting threads to proceed.
            resultSocket.ConnectDone.Set();
        }

        private static async Task SendAsync(CustomSocket state, string data)
        {
            var byteData = Encoding.ASCII.GetBytes(data);

            // send data
            state.Socket.BeginSend(byteData, 0, byteData.Length, 0, SendCallback, state);
            await Task.FromResult<object>(state.SendDone.WaitOne());
        }


        private static Task Send(CustomSocket state, string data)
        {
            var byteData = Encoding.ASCII.GetBytes(data);

            // send data  
            state.Socket.BeginSend(byteData, 0, byteData.Length, 0, SendCallback, state);
            return Task.FromResult(state.SendDone.WaitOne());
        }

        private static void SendCallback(IAsyncResult ar)
        {
            var resultSocket = (CustomSocket)ar.AsyncState;
            Debug.Assert(resultSocket != null, nameof(resultSocket) + " != null");
            var clientSocket = resultSocket.Socket;
            var clientId = resultSocket.Id;

            // ends a pending asynchronous send
            var bytesSent = clientSocket.EndSend(ar);
            Console.WriteLine("Connection {0} > Sent {1} bytes to server.", clientId, bytesSent);

            // Set the state of the event to signaled, allowing one or more waiting threads to proceed.
            resultSocket.SendDone.Set();
        }

        private static async Task ReceiveAsync(CustomSocket state)
        {
            // receive data
            state.Socket.BeginReceive(state.Buffer, 0, CustomSocket.Size, 0, ReceiveCallback, state);

            await Task.FromResult<object>(state.ReceiveDone.WaitOne());
        }

        private static Task Receive(CustomSocket state)
        {
            // receive data
            state.Socket.BeginReceive(state.Buffer, 0, CustomSocket.Size, 0, ReceiveCallback, state);

            return Task.FromResult(state.ReceiveDone.WaitOne());
        }

        private static void ReceiveCallback(IAsyncResult ar)
        {
            var resultSocket = (CustomSocket)ar.AsyncState;
            Debug.Assert(resultSocket != null, nameof(resultSocket) + " != null");
            var clientSocket = resultSocket.Socket;

            try
            {
                // read data 
                var bytesRead = clientSocket.EndReceive(ar);

                // get from the buffer, a number of characters <= to the buffer size, and store it in the responseContent
                resultSocket.ResponseContent.Append(Encoding.ASCII.GetString(resultSocket.Buffer, 0, bytesRead));

                // if the response header has not been fully obtained, get the next chunk of data
                if (!Parser.ResponseHeaderObtained(resultSocket.ResponseContent.ToString()))
                {
                    clientSocket.BeginReceive(resultSocket.Buffer, 0, CustomSocket.Size, 0, ReceiveCallback, resultSocket);
                }
                else
                {
                    resultSocket.ReceiveDone.Set(); // signal that all bytes have been received       
                }
            }
            catch (Exception e)
            {
                Console.WriteLine(e.ToString());
            }

        }
        
    }
}