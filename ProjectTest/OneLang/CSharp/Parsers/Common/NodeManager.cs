using System.Collections.Generic;
using System.Threading.Tasks;
using System;
using Parsers.Common;

namespace Parsers.Common
{
    public class NodeManager {
        public object[] nodes;
        public Reader reader;
        
        public NodeManager(Reader reader) {
            this.nodes = new object[0];
            this.reader = reader;
        }
        
        public void addNode(object node, int start) {
            this.nodes.push(node);
        }
    }
}