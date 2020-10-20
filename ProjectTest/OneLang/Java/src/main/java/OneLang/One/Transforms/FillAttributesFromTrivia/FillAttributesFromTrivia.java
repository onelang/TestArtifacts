import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;

public class FillAttributesFromTrivia {
    public static Map<String, String> processTrivia(String trivia) {
        var result = new LinkedHashMap<String, String>();
        if (trivia != null && !trivia.equals("")) {
            var regex = new RegExp("(?:\\n|^)\\s*(?://|#|/\\*\\*?)\\s*@([a-z0-9_.-]+) ?((?!\\n|\\*/|$).+)?");
            while (true) {
                var match = regex.exec(trivia);
                if (match == null)
                    break;
                if (result.containsKey(match[1]))
                    // @php $result[$match[1]] .= "\n" . $match[2];
                    // @python result[match[1]] += "\n" + match[2]
                    // @csharp result[match[1]] += "\n" + match[2];
                    result.get(match[1]) += "\n" + match[2];
                else
                    result.put(match[1], match[2] != null ? match[2] : "true");
            }
        }
        return result;
    }
    
    private static void process(IHasAttributesAndTrivia[] items) {
        for (var item : items)
            item.setAttributes(FillAttributesFromTrivia.processTrivia(item.getLeadingTrivia()));
    }
    
    private static void processBlock(Block block) {
        if (block == null)
            return;
        FillAttributesFromTrivia.process(block.statements.toArray(Statement[]::new));
        for (var stmt : block.statements) {
            if (stmt instanceof ForeachStatement)
                FillAttributesFromTrivia.processBlock(((ForeachStatement)stmt).body);
            else if (stmt instanceof ForStatement)
                FillAttributesFromTrivia.processBlock(((ForStatement)stmt).body);
            else if (stmt instanceof WhileStatement)
                FillAttributesFromTrivia.processBlock(((WhileStatement)stmt).body);
            else if (stmt instanceof DoStatement)
                FillAttributesFromTrivia.processBlock(((DoStatement)stmt).body);
            else if (stmt instanceof IfStatement) {
                FillAttributesFromTrivia.processBlock(((IfStatement)stmt).then);
                FillAttributesFromTrivia.processBlock(((IfStatement)stmt).else_);
            }
        }
    }
    
    private static void processMethod(IMethodBaseWithTrivia method) {
        if (method == null)
            return;
        FillAttributesFromTrivia.process(new IMethodBaseWithTrivia[] { method });
        FillAttributesFromTrivia.process(method.getParameters());
        FillAttributesFromTrivia.processBlock(method.getBody());
    }
    
    public static void processFile(SourceFile file) {
        FillAttributesFromTrivia.process(file.imports);
        FillAttributesFromTrivia.process(file.enums);
        FillAttributesFromTrivia.process(file.interfaces);
        FillAttributesFromTrivia.process(file.classes);
        FillAttributesFromTrivia.processBlock(file.mainBlock);
        
        for (var intf : file.interfaces)
            for (var method : intf.getMethods())
                FillAttributesFromTrivia.processMethod(method);
        
        for (var cls : file.classes) {
            FillAttributesFromTrivia.processMethod(cls.constructor_);
            FillAttributesFromTrivia.process(cls.getFields());
            FillAttributesFromTrivia.process(cls.properties);
            for (var prop : cls.properties) {
                FillAttributesFromTrivia.processBlock(prop.getter);
                FillAttributesFromTrivia.processBlock(prop.setter);
            }
            for (var method : cls.getMethods())
                FillAttributesFromTrivia.processMethod(method);
        }
    }
    
    public static void processPackage(Package pkg) {
        for (var file : pkg.files.values().toArray(SourceFile[]::new))
            FillAttributesFromTrivia.processFile(file);
    }
}