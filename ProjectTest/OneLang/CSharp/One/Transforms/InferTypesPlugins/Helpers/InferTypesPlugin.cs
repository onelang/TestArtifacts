using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System;
using One;
using One.Ast;
using One.Transforms;

namespace One.Transforms.InferTypesPlugins.Helpers
{
    public class InferTypesPlugin {
        public InferTypes main;
        public ErrorManager errorMan;
        public string name;
        
        public InferTypesPlugin(string name)
        {
            this.name = name;
            this.errorMan = null;
        }
        
        public virtual bool canTransform(Expression expr) {
            return false;
        }
        
        public virtual bool canDetectType(Expression expr) {
            return false;
        }
        
        public virtual Expression transform(Expression expr) {
            return null;
        }
        
        public virtual bool detectType(Expression expr) {
            return false;
        }
        
        public virtual bool handleProperty(Property prop) {
            return false;
        }
        
        public virtual bool handleLambda(Lambda lambda) {
            return false;
        }
        
        public virtual bool handleMethod(IMethodBase method) {
            return false;
        }
        
        public virtual bool handleStatement(Statement stmt) {
            return false;
        }
    }
}