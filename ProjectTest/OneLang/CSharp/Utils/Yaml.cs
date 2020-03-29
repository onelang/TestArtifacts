using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System;

namespace Utils
{
    public class Yaml {
        public static T load<T>(string content) {
            // @csharp return YAML.safeLoad<T>(content);
            return YAML.safeLoad<T>(content);
        }
    }
}