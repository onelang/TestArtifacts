﻿using System;
using System.IO;
using System.Threading.Tasks;
using Generator;
using One;
using Test;

namespace CSharp
{
    class Program
    {
        static async Task Main(string[] args)
        {
            Environment.ExitCode = await new SelfTestRunner("../../../../../").runTest(new CsharpGenerator()) ? 0 : 1;
        }
    }
}
