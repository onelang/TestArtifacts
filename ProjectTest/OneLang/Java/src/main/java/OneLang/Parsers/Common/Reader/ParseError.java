public class ParseError {
    public String message;
    public Cursor cursor;
    public Reader reader;
    
    public ParseError(String message, Cursor cursor, Reader reader)
    {
        this.message = message;
        this.cursor = cursor;
        this.reader = reader;
    }
    
    public ParseError(String message, Cursor cursor) {
        this(message, cursor, null);
    }
    
    public ParseError(String message) {
        this(message, null, null);
    }
}