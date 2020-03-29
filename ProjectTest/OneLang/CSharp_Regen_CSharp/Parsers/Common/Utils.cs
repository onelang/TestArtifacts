

namespace Parsers.Common
{
    public class Utils {
        public static int getPadLen(string line) {
            for (int i = 0; i < line.length(); i++) {
                if (line.get(i) != " ")
                    return i;
            }
            return -1;
        }
        
        public static string deindent(string str) {
            var lines = str.split(new RegExp("\\n"));
            if (lines.length() == 1)
                return str;
            
            if (Utils.getPadLen(lines.get(0)) == -1)
                lines.shift();
            
            var minPadLen = 9999;
            foreach (var padLen in lines.map((string x) => { return Utils.getPadLen(x); }).filter((int x) => { return x != -1; })) {
                if (padLen < minPadLen)
                    minPadLen = padLen;
            }
            var newStr = lines.map((string x) => { return x.length() != 0 ? x.substr(minPadLen) : x; }).join("\n");
            return newStr;
        }
    }
}