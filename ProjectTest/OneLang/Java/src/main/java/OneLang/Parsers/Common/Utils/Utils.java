import java.util.Arrays;
import java.util.stream.Collectors;

public class Utils {
    public static Integer getPadLen(String line)
    {
        for (Integer i = 0; i < line.length(); i++) {
            if (line.substring(i, i + 1) != " ")
                return i;
        }
        return -1;
    }
    
    public static String deindent(String str)
    {
        var lines = Arrays.asList(str.split("\\n"));
        if (lines.size() == 1)
            return str;
        
        if (Utils.getPadLen(lines.get(0)) == -1)
            lines.remove(0);
        
        var minPadLen = 9999;
        for (var padLen : Arrays.stream(lines.stream().map(x -> Utils.getPadLen(x)).toArray(Integer[]::new)).filter(x -> x != -1).toArray(Integer[]::new)) {
            if (padLen < minPadLen)
                minPadLen = padLen;
        }
        // @java final var minPadLen2 = minPadLen;
        final var minPadLen2 = minPadLen;
        var newStr = Arrays.stream(lines.stream().map(x -> x.length() != 0 ? x.substring(minPadLen2) : x).toArray(String[]::new)).collect(Collectors.joining("\n"));
        return newStr;
    }
}