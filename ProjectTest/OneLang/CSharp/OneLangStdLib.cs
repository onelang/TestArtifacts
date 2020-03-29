using System;
using System.Collections;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using Newtonsoft.Json;
using StdLib;
using YamlDotNet.Serialization;
using YamlDotNet.Serialization.NamingConventions;

public class Error : Exception {
    public Error() { }
    public Error(string msg): base(msg) { }
    public static int stackTraceLimit = 0;
    public string stack;
}

public static class console {
    public static void log(string msg) {
        Console.WriteLine(msg);
    }

    public static void error(string msg) {
        var oldColor = Console.ForegroundColor;
        Console.ForegroundColor = ConsoleColor.Red;
        Console.WriteLine(msg);
        Console.ForegroundColor = oldColor;
    }
}

public static class Global {
    public static int parseInt(string str) {
        return int.Parse(str);
    }

    public static async Task<object> import(string moduleName) {
        if (moduleName == "fs") return await Task.FromResult(new Fs());
        else if (moduleName == "glob") return await Task.FromResult(new Glob());
        else if (moduleName == "path") return await Task.FromResult(new Path());
        throw new NotImplementedException();
    }
}

public static class Math {
    public static int floor(int num) {
        return num;
    }
}

public class Promise {
    public static Task<T> resolve<T>(T value) {
        return Task.FromResult(value);
    }
}

public class YAML {
    public static T safeLoad<T>(string str) {
        return new DeserializerBuilder()
            .WithNamingConvention(HyphenatedNamingConvention.Instance)
            .IgnoreUnmatchedProperties()
            .Build()
            .Deserialize<T>(new StringReader(str));
    }
}

public class Glob {
    public string[] sync(string dir, object config) {
        if (dir.EndsWith(dir)) {
            var baseDir = dir.Replace("/**/*", "");
            return Directory.GetFiles(baseDir, "*", SearchOption.AllDirectories).Where(x => x.Replace(baseDir, "").Contains("/")).ToArray();
        } else {
            throw new NotImplementedException();
        }
    }
}

public class Path {
    public string relative(string dir, string fn) {
        if (fn.StartsWith(dir))
            return fn.Substring(dir.Length);
        throw new NotImplementedException();
    }
}

public class Fs {
    public string readFileSync(string fn, string encoding) {
        if (encoding != "utf-8")
            throw new NotImplementedException();
        return File.ReadAllText(fn);
    }
}

public class Set<T>: IEnumerable<T>
{
    HashSet<T> items;

    public Set() {
        items = new HashSet<T>();
    }

    public Set(IEnumerable<T> items) {
        this.items = new HashSet<T>(items);
    }

    public void add(T item) {
        items.Add(item);
    }

    public T[] values() {
        return items.ToArray();
    }

    IEnumerator IEnumerable.GetEnumerator()
    {
        return items.GetEnumerator();
    }

    public IEnumerator<T> GetEnumerator()
    {
        return items.GetEnumerator();
    }
}

public class Map<TKey, TValue> {
    Dictionary<TKey, TValue> items = new Dictionary<TKey, TValue>();

    public TValue get(TKey key)
    {
        return items.GetValueOrDefault(key);
    }

    public void set(TKey key, TValue value)
    {
        items[key] = value;
    }

    public void delete(TKey key)
    {
        items.Remove(key);
    }

    public bool has(TKey key)
    {
        return items.ContainsKey(key);
    }

    public TValue[] values()
    {
        return items.Values.ToArray();
    }
}

public static class Object {
    public static string[] keys<TValue>(Dictionary<string, TValue> dict) {
        return dict.Select(x => x.Key).ToArray();
    }

    public static TValue[] values<TValue>(Dictionary<string, TValue> dict) {
        return dict.Select(x => x.Value).ToArray();
    }
}

public static class JSON {
    public static string stringify(object obj) {
        return JsonConvert.SerializeObject(obj);
    }
}

public static class Array {
    public static T[] from<T>(IEnumerable<T> obj) {
        return obj.ToArray();
    }
}

public class RegExp {
    public string pattern;
    public string modifiers;
    public int lastIndex;
    public Match lastMatch;

    public RegExp(string pattern): this(pattern, null) { }

    public RegExp(string pattern, string modifiers) {
        this.pattern = pattern;
        this.modifiers = modifiers;
    }

    public string[] exec(string data) {
        this.lastMatch = this.lastMatch == null ? new Regex($"\\G(?:{this.pattern})").Match(data, this.lastIndex) : lastMatch.NextMatch();
        return this.lastMatch.Success ? this.lastMatch.Groups.Cast<Group>().Select(x => x.Value).ToArray() : null;
    }
}

public static class ExtensionMethods {
    public static bool startsWith(this string str, string v) {
        return str.StartsWith(v);
    }
    
    public static bool endsWith(this string str, string v) {
        return str.EndsWith(v);
    }

    public static string substr(this string str, int offs, int len) {
        return str.Substring(offs, len);
    }

    public static string substr(this string str, int offs) {
        if (offs >= str.Length) return "";
        return str.Substring(offs);
    }

    public static string substring(this string str, int offs, int end) {
        return str.Substring(offs, end - offs);
    }

    public static int length(this string str) {
        return str.Length;
    }

    public static bool includes(this string str, string substr) {
        return str.Contains(substr);
    }

    public static string replace(this string str, string from, string to) {
        return str.Replace(from, to);
    }

    public static string replace(this string str, RegExp from, string to) {
        return Regex.Replace(str, from.pattern, to);
    }

    public static T2[] map<T, T2>(this List<T> items, Func<T, T2> converter) {
        return items.Select(converter).ToArray();
    }

    public static T2[] map<T, T2>(this T[] items, Func<T, T2> converter) {
        return items.Select(converter).ToArray();
    }

    public static T find<T>(this IEnumerable<T> items, Func<T, bool> filter) {
        return items.FirstOrDefault(filter);
    }

    public static T[] filter<T>(this IEnumerable<T> items, Func<T, bool> filter) {
        return items.Where(filter).ToArray();
    }

    public static bool some<T>(this IEnumerable<T> items, Func<T, bool> filter) {
        return items.Any(filter);
    }

    public static bool every<T>(this IEnumerable<T> items, Func<T, int, bool> filter) {
        return !items.Where((x, i) => !filter(x, i)).Any();
    }

    public static string join(this string[] items, string separator) {
        return String.Join(separator, items);
    }

    public static string join(this List<string> items, string separator) {
        return String.Join(separator, items);
    }

    public static T get<T>(this List<T> items, int idx){
        return items[idx];
    }

    public static T get<T>(this T[] items, int idx){
        return items[idx];
    }

    public static void set<T>(this List<T> items, int idx, T value){
        items[idx] = value;
    }

    public static void set<T>(this T[] items, int idx, T value){
        items[idx] = value;
    }

    public static void push<T>(this List<T> items, T newItem){
        items.Add(newItem);
    }

    public static string get(this string str, int idx){
        return "" + str[idx];
    }

    public static bool startsWith(this string str, string substr, int idx) {
        return String.Compare(str, idx, substr, 0, substr.Length) == 0;
    }

    public static string toUpperCase(this string str) {
        return str.ToUpper();
    }

    public static int length<T>(this List<T> array) {
        return array.Count;
    }

    public static int length<T>(this T[] array) {
        return array.Length;
    }

    public static string repeat(this string str, int count) {
        var sb = new StringBuilder(str.Length * count);
        for (int i = 0; i < count; i++)
            sb.Append(str);
        return sb.ToString();
    }

    public static T shift<T>(this List<T> items) {
        var result = items[0];
        items.RemoveAt(0);
        return result;
    }

    public static TValue get<TKey, TValue>(this Dictionary<TKey, TValue> dict, TKey key) {
        return dict.GetValueOrDefault(key);
    }

    public static void set<TKey, TValue>(this Dictionary<TKey, TValue> dict, TKey key, TValue value) {
        dict[key] = value;
    }

    public static T pop<T>(this List<T> items) {
        var idx = items.Count - 1;
        var result = items[idx];
        items.RemoveAt(idx);
        return result;
    }

    public static bool includes<T>(this List<T> items, T item) {
        return items.IndexOf(item) !=  -1;
    }

    public static bool includes<T>(this T[] items, T item) where T: class {
        return items.Any(x => Object.Equals(x, item));
    }

    public static List<string> split(this string str, RegExp separator) {
        return new Regex(separator.pattern).Split(str).ToList();
    }

    public static T[] concat<T>(this IEnumerable<T> items, IEnumerable<T> otherItems) {
        return items.Concat(otherItems).ToArray();
    }

    public static void splice<T>(this List<T> items, int offset, int count) {
        items.RemoveRange(offset, count);
    }

    class LambdaComparer<T> : IComparer<T>
    {
        public readonly Func<T, T, int> Comparer;

        public LambdaComparer(Func<T, T, int> comparer) {
            Comparer = comparer;
        }

        public int Compare(T x, T y)
        {
            return this.Comparer(x, y);
        }
    }

    public static T[] sort<T>(this IEnumerable<T> items, Func<T, T, int> comparer) {
        return items.OrderBy(x => x, new LambdaComparer<T>(comparer)).ToArray();
    }

    public static bool hasKey<TKey, TValue>(this Dictionary<TKey, TValue> dict, TKey key) {
        return dict.ContainsKey(key);
    }

    public static int indexOf(this string str, string substr, int offset) {
        return str.IndexOf(substr, offset);
    }

    public static int lastIndexOf(this string str, string substr, int offset) {
        return str.LastIndexOf(substr, offset);
    }

    public static int charCodeAt(this string str, int offset) {
        return (int)str[offset];
    }
}