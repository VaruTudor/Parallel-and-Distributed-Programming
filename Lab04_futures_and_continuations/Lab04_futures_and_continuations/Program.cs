using System;
using System.Linq;
using Lab04_futures_and_continuations.implementations;

namespace Lab04_futures_and_continuations
{
    internal static class Program
    {
        private static void Main(string[] args)
        {
            var hosts = new [] {"www.cs.ubbcluj.ro/~rlupsa/edu/pdp/", "www.cs.ubbcluj.ro/~forest"}.ToList();
            // CallbackImplementation.Run(hosts);
            TaskImplementation.Run(hosts, true);
        }
    }
}