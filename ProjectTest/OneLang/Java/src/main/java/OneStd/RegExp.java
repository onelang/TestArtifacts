public class RegExp {
    String pattern;
    String modifiers;
    Integer lastIndex;

    public RegExp(String pattern) { this(pattern, null); }
    public RegExp(String pattern, String modifiers) {
        this.pattern = pattern;
        this.modifiers = modifiers;
    }

    public String[] exec(String data) {
        // TODO
        return null;
    }
}