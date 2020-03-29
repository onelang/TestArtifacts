using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using One.Ast;
using One;
using Utils;

namespace One
{
    public enum LogType { Info, Warning, Error }
    
    public class CompilationError {
        public string msg;
        public bool isWarning;
        public string transformerName;
        public IAstNode node;
        
        public CompilationError(string msg, bool isWarning, string transformerName, IAstNode node)
        {
            this.msg = msg;
            this.isWarning = isWarning;
            this.transformerName = transformerName;
            this.node = node;
        }
    }
    
    public class ErrorManager {
        public AstTransformer transformer;
        public IAstNode currentNode;
        public List<CompilationError> errors;
        public List<string> contextInfo;
        
        public string lastContextInfo {
            get {
            
                return this.contextInfo.length() > 0 ? this.contextInfo.get(this.contextInfo.length() - 1) : null;
            }
        }
        
        public ErrorManager()
        {
            this.transformer = null;
            this.currentNode = null;
            this.errors = new List<CompilationError>();
            this.contextInfo = new List<string>();
        }
        
        public void resetContext(AstTransformer transformer = null) {
            this.transformer = transformer;
        }
        
        public void log(LogType type, string msg) {
            var t = this.transformer;
            
            var text = (t != null ? $"[{t.name}] " : "") + msg;
            
            var par = this.currentNode;
            while (par is Expression)
                par = ((Expression)par).parentNode;
            
            string location = null;
            if (par is Field)
                location = $"{((Field)par).parentInterface.parentFile.sourcePath} -> {((Field)par).parentInterface.name}::{((Field)par).name} (field)";
            else if (par is Property)
                location = $"{((Property)par).parentClass.parentFile.sourcePath} -> {((Property)par).parentClass.name}::{((Property)par).name} (property)";
            else if (par is Method)
                location = $"{((Method)par).parentInterface.parentFile.sourcePath} -> {((Method)par).parentInterface.name}::{((Method)par).name} (method)";
            else if (par is Constructor)
                location = $"{((Constructor)par).parentClass.parentFile.sourcePath} -> {((Constructor)par).parentClass.name}::constructor";
            else if (par == null) { }
            else if (par is Statement) { }
            else { }
            
            if (location == null && t != null && t.currentFile != null) {
                location = $"{t.currentFile.sourcePath}";
                if (t.currentInterface != null) {
                    location += $" -> {t.currentInterface.name}";
                    if (t.currentMethod is Method)
                        location += $"::{((Method)t.currentMethod).name}";
                    else if (t.currentMethod is Constructor)
                        location += $"::constructor";
                    else if (t.currentMethod == null) { }
                    else { }
                }
            }
            
            if (this.currentNode != null)
                text += $"\n  Node: {TSOverviewGenerator.nodeRepr(this.currentNode)}";
            
            if (location != null)
                text += $"\n  Location: {location}";
            
            if (t != null && t.currentStatement != null)
                text += $"\n  Statement: {TSOverviewGenerator.stmt(t.currentStatement, true)}";
            
            if (this.lastContextInfo != null)
                text += $"\n  Context: {this.lastContextInfo}";
            
            if (type == LogType.Info)
                console.log(text);
            else if (type == LogType.Warning)
                console.error($"[WARNING] {text}\n");
            else if (type == LogType.Error)
                console.error($"{text}\n");
            else { }
            
            if (type == LogType.Error || type == LogType.Warning)
                this.errors.push(new CompilationError(msg, type == LogType.Warning, t != null ? t.name : null, this.currentNode));
        }
        
        public void info(string msg) {
            this.log(LogType.Info, msg);
        }
        
        public void warn(string msg) {
            this.log(LogType.Warning, msg);
        }
        
        public void throw_(string msg) {
            this.log(LogType.Error, msg);
        }
    }
}