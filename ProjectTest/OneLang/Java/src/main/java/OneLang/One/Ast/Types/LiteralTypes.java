public class LiteralTypes {
    public ClassType boolean_;
    public ClassType numeric;
    public ClassType string;
    public ClassType regex;
    public ClassType array;
    public ClassType map;
    public ClassType error;
    public ClassType promise;
    
    public LiteralTypes(ClassType boolean_, ClassType numeric, ClassType string, ClassType regex, ClassType array, ClassType map, ClassType error, ClassType promise)
    {
        this.boolean_ = boolean_;
        this.numeric = numeric;
        this.string = string;
        this.regex = regex;
        this.array = array;
        this.map = map;
        this.error = error;
        this.promise = promise;
    }
}