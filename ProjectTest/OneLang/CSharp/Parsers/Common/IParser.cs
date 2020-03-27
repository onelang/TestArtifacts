using System.Collections.Generic;
using System.Threading.Tasks;
using System;
using Parsers.Common;
using One.Ast;

namespace Parsers.Common
{
    public interface IParser {
        NodeManager nodeManager { get; set; }
        
        SourceFile parse();
    }
}