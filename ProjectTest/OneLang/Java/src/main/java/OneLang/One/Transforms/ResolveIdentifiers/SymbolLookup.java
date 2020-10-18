import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class SymbolLookup {
    public ErrorManager errorMan;
    public List<List<String>> levelStack;
    public List<String> levelNames;
    public List<String> currLevel;
    public Map<String, IReferencable> symbols;
    
    public SymbolLookup()
    {
        this.errorMan = new ErrorManager();
        this.levelStack = new ArrayList<List<String>>();
        this.levelNames = new ArrayList<String>();
        this.symbols = new HashMap<String, IReferencable>();
    }
    
    public void throw_(String msg)
    {
        this.errorMan.throw_(msg + " (context: " + this.levelNames.stream().collect(Collectors.joining(" > ")) + ")");
    }
    
    public void pushContext(String name)
    {
        this.levelStack.add(this.currLevel);
        this.levelNames.add(name);
        this.currLevel = new ArrayList<String>();
    }
    
    public void addSymbol(String name, IReferencable ref)
    {
        if (this.symbols.containsKey(name))
            this.throw_("Symbol shadowing: " + name);
        this.symbols.put(name, ref);
        this.currLevel.add(name);
    }
    
    public void popContext()
    {
        for (var name : this.currLevel)
            this.symbols.remove(name);
        this.levelNames.remove(this.levelNames.size() - 1);
        this.currLevel = this.levelStack.size() > 0 ? this.levelStack.remove(this.levelStack.size() - 1) : null;
    }
    
    public IReferencable getSymbol(String name)
    {
        return this.symbols.get(name);
    }
}