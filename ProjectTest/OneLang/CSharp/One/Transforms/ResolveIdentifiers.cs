using System.Collections.Generic;
using System.Threading.Tasks;
using System;
using One;
using One.Ast;

namespace One.Transforms
{
    public class SymbolLookup {
        public ErrorManager errorMan;
        public string[][] levelSymbols;
        public string[] levelNames;
        public string[] currLevel;
        public Map<string, IReferencable> symbols;
        
        public void throw_(string msg) {
            this.errorMan.throw_($"{msg} (context: {this.levelNames.join(" > ")})");
        }
        
        public void pushContext(string name) {
            this.levelNames.push(name);
            this.currLevel = new string[0];
            this.levelSymbols.push(this.currLevel);
        }
        
        public void addSymbol(string name, IReferencable ref_) {
            if (this.symbols.has(name))
                this.throw_($"Symbol shadowing: {name}");
            this.symbols.set(name, ref_);
            this.currLevel.push(name);
        }
        
        public void popContext() {
            foreach (var name in this.currLevel)
                this.symbols.delete(name);
            this.levelSymbols.pop();
            this.levelNames.pop();
            this.currLevel = this.levelSymbols.get(this.levelSymbols.length() - 1);
        }
        
        public IReferencable getSymbol(string name) {
            return this.symbols.get(name);
        }
    }
    
    public class ResolveIdentifiers : AstTransformer {
        public SymbolLookup symbolLookup;
        
        public ResolveIdentifiers(): base("ResolveIdentifiers") {
            this.symbolLookup = new SymbolLookup();
        }
        
        protected override Expression visitIdentifier(Identifier id) {
            base.visitIdentifier(id);
            var symbol = this.symbolLookup.getSymbol(id.text);
            if (symbol == null) {
                this.errorMan.throw_($"Identifier '{id.text}' was not found in available symbols");
                return null;
            }
            
            Reference ref_ = null;
            if (symbol is Class && id.text == "this") {
                var withinStaticMethod = this.currentMethod is Method && ((Method)this.currentMethod).isStatic;
                ref_ = withinStaticMethod ? ((Reference)new StaticThisReference(((Class)symbol))) : new ThisReference(((Class)symbol));
            }
            else if (symbol is Class && id.text == "super")
                ref_ = new SuperReference(((Class)symbol));
            else
                ref_ = symbol.createReference();
            ref_.parentNode = id.parentNode;
            return ref_;
        }
        
        protected override Statement visitStatement(Statement stmt) {
            if (stmt is ForStatement) {
                this.symbolLookup.pushContext($"For");
                if (((ForStatement)stmt).itemVar != null)
                    this.symbolLookup.addSymbol(((ForStatement)stmt).itemVar.name, ((ForStatement)stmt).itemVar);
                base.visitStatement(((ForStatement)stmt));
                this.symbolLookup.popContext();
            }
            else if (stmt is ForeachStatement) {
                this.symbolLookup.pushContext($"Foreach");
                this.symbolLookup.addSymbol(((ForeachStatement)stmt).itemVar.name, ((ForeachStatement)stmt).itemVar);
                base.visitStatement(((ForeachStatement)stmt));
                this.symbolLookup.popContext();
            }
            else if (stmt is TryStatement) {
                this.symbolLookup.pushContext($"Try");
                this.visitBlock(((TryStatement)stmt).tryBody);
                if (((TryStatement)stmt).catchBody != null) {
                    this.symbolLookup.addSymbol(((TryStatement)stmt).catchVar.name, ((TryStatement)stmt).catchVar);
                    this.visitBlock(((TryStatement)stmt).catchBody);
                    this.symbolLookup.popContext();
                }
                if (((TryStatement)stmt).finallyBody != null)
                    this.visitBlock(((TryStatement)stmt).finallyBody);
            }
            else
                return base.visitStatement(stmt);
            return null;
        }
        
        protected override Lambda visitLambda(Lambda lambda) {
            this.symbolLookup.pushContext($"Lambda");
            foreach (var param in lambda.parameters)
                this.symbolLookup.addSymbol(param.name, param);
            base.visitBlock(lambda.body);
            // directly process method's body without opening a new scope again
            this.symbolLookup.popContext();
            return null;
        }
        
        protected override Block visitBlock(Block block) {
            this.symbolLookup.pushContext("block");
            base.visitBlock(block);
            this.symbolLookup.popContext();
            return null;
        }
        
        protected override VariableDeclaration visitVariableDeclaration(VariableDeclaration stmt) {
            this.symbolLookup.addSymbol(stmt.name, stmt);
            return base.visitVariableDeclaration(stmt);
        }
        
        protected override void visitMethodBase(IMethodBase method) {
            this.symbolLookup.pushContext(method is Method ? $"Method: {((Method)method).name}" : method is Constructor ? "constructor" : "???");
            
            foreach (var param in method.parameters) {
                this.symbolLookup.addSymbol(param.name, param);
                if (param.initializer != null)
                    this.visitExpression(param.initializer);
            }
            
            if (method.body != null)
                base.visitBlock(method.body);
            // directly process method's body without opening a new scope again
            
            this.symbolLookup.popContext();
        }
        
        protected override void visitClass(Class cls) {
            this.symbolLookup.pushContext($"Class: {cls.name}");
            this.symbolLookup.addSymbol("this", cls);
            if (cls.baseClass is ClassType)
                this.symbolLookup.addSymbol("super", ((ClassType)cls.baseClass).decl);
            base.visitClass(cls);
            this.symbolLookup.popContext();
        }
        
        public override void visitSourceFile(SourceFile sourceFile) {
            this.errorMan.resetContext(this);
            this.symbolLookup.pushContext($"File: {sourceFile.sourcePath}");
            
            foreach (var symbol in sourceFile.availableSymbols.values())
                if (symbol is Class)
                    this.symbolLookup.addSymbol(((Class)symbol).name, ((Class)symbol));
                else if (symbol is Interface) { }
                else if (symbol is Enum_)
                    this.symbolLookup.addSymbol(((Enum_)symbol).name, ((Enum_)symbol));
                else if (symbol is GlobalFunction)
                    this.symbolLookup.addSymbol(((GlobalFunction)symbol).name, ((GlobalFunction)symbol));
                else { }
            
            base.visitSourceFile(sourceFile);
            
            this.symbolLookup.popContext();
            this.errorMan.resetContext();
        }
    }
}