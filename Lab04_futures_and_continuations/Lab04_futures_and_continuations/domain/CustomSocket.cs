using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;

namespace Lab04_futures_and_continuations.domain
{
    public class CustomSocket
    {
        // thread synchronization event that, when signaled, must be reset manually
        public readonly ManualResetEvent ConnectDone = new(false);
        public readonly ManualResetEvent SendDone = new(false);
        public readonly ManualResetEvent ReceiveDone = new(false);

        public Socket Socket = null;
        public const int Size = 1024;

        public readonly byte[] Buffer = new byte[Size];
        public readonly StringBuilder ResponseContent = new();

        public int Id;
        public string Hostname; 
        public string Endpoint;
        public IPEndPoint RemoteIpEndPoint;
    }
}