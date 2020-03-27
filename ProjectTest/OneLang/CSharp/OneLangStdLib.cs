using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

public class Error : Exception {
    public Error() { }
    public Error(string msg) { }
    public static int stackTraceLimit = 0;
    public string stack;
}

public static class console {
    public static void log(string msg) {
        throw new NotImplementedException();
    }

    public static void error(string msg) {
        throw new NotImplementedException();
    }
}

public static class Global {
    public static int parseInt(string str) {
        throw new NotImplementedException();
    }

    public static async Task<object> import(string moduleName) {
        if (moduleName == "fs") return new Fs();
        else if (moduleName == "glob") return new Glob();
        else if (moduleName == "path") return new Path();
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
    public static object safeLoad(string str) {
        throw new NotImplementedException();
    }
}

public class Glob {
    public string[] sync(string dir, object config) {
        throw new NotImplementedException();
    }
}

public class Path {
    public string relative(string dir, string fn) {
        throw new NotImplementedException();
    }
}

public class Fs {
    public string readFileSync(string fn, string encoding) {
        throw new NotImplementedException();
    }
}

public class Set<T>: IEnumerable<T>
{
    public Set() {}
    public Set(T[] items) {
        throw new NotImplementedException();
    }

    public void add(T item) {
        throw new NotImplementedException();
    }

    public T[] values() {
        throw new NotImplementedException();
    }

    IEnumerator IEnumerable.GetEnumerator()
    {
        throw new NotImplementedException();
    }

    public IEnumerator<T> GetEnumerator()
    {
        throw new NotImplementedException();
    }
}

public class Map<TKey, TValue> {
    public TValue get(TKey key) {
        throw new NotImplementedException();
    }

    public void set(TKey key, TValue value) {
        throw new NotImplementedException();
    }

    public void delete(TKey key) {
        throw new NotImplementedException();
    }

    public bool has(TKey key) {
        throw new NotImplementedException();
    }

    public TValue[] values() {
        throw new NotImplementedException();
    }
}

public static class Object {
    public static string[] keys<TValue>(Dictionary<string, TValue> dict) {
        throw new NotImplementedException();
    }

    public static TValue[] values<TValue>(Dictionary<string, TValue> dict) {
        throw new NotImplementedException();
    }
}

public static class JSON {
    public static string stringify(object obj) {
        throw new NotImplementedException();
    }
}

public static class Array {
    public static T[] from<T>(T[] obj) {
        return obj;
    }
}

public class RegExp {
    public int lastIndex;

    public RegExp(string pattern) {
        throw new NotImplementedException();
    }

    public RegExp(string pattern, string modifiers) {
        throw new NotImplementedException();
    }

    public string[] exec(string data) {
        throw new NotImplementedException();
    }
}

public static class ExtensionMethods {
    public static bool startsWith(this string str, string v) {
        throw new NotImplementedException();
    }
    
    public static bool endsWith(this string str, string v) {
        throw new NotImplementedException();
    }

    public static string substr(this string str, int offs, int len) {
        throw new NotImplementedException();
    }

    public static string substr(this string str, int offs) {
        throw new NotImplementedException();
    }

    public static string substring(this string str, int offs, int len) {
        throw new NotImplementedException();
    }

    public static int length(this string str) {
        return str.Length;
    }

    public static bool includes(this string str, string substr) {
        throw new NotImplementedException();
    }

    public static string replace(this string str, string from, string to) {
        return str.Replace(from, to);
    }

    public static string replace(this string str, RegExp from, string to) {
        throw new NotImplementedException();
    }

    public static T2[] map<T, T2>(this T[] items, Func<T, T2> converter) {
        return items.Select(converter).ToArray();
    }

    public static T find<T>(this T[] items, Func<T, bool> filter) {
        return items.FirstOrDefault(filter);
    }

    public static T[] filter<T>(this T[] items, Func<T, bool> filter) {
        return items.Where(filter).ToArray();
    }

    public static bool some<T>(this T[] items, Func<T, bool> filter) {
        return items.Any(filter);
    }

    public static bool every<T>(this T[] items, Func<T, int, bool> filter) {
        return items.Where((x, i) => !filter(x, i)).Any();
    }

    public static string join(this string[] items, string separator) {
        return String.Join(separator, items);
    }

    public static T get<T>(this T[] items, int idx){
        return items[idx];
    }

    public static void set<T>(this T[] items, int idx, T value){
        items[idx] = value;
    }

    public static void push<T>(this T[] items, T newItem){
        throw new NotImplementedException();
    }

    public static string get(this string str, int idx){
        return "" + str[idx];
    }

    public static bool startsWith(this string str, string substr, int idx) {
        throw new NotImplementedException();
    }

    public static int length<T>(this T[] array) {
        return array.Length;
    }

    public static string repeat(this string str, int count) {
        throw new NotImplementedException();
    }

    public static T shift<T>(this T[] items) {
        throw new NotImplementedException();
    }

    public static TValue get<TKey, TValue>(this Dictionary<TKey, TValue> dict, TKey key) {
        return dict[key];
    }

    public static void set<TKey, TValue>(this Dictionary<TKey, TValue> dict, TKey key, TValue value) {
        dict[key] = value;
    }

    public static T pop<T>(this T[] items) {
        throw new NotImplementedException();
    }

    public static bool includes<T>(this T[] items, T item) {
        throw new NotImplementedException();
    }

    public static string[] split(this string str, string separator) {
        throw new NotImplementedException();
    }

    public static string[] split(this string str, RegExp separator) {
        throw new NotImplementedException();
    }

    public static T[] concat<T>(this T[] items, T[] otherItems) {
        throw new NotImplementedException();
    }

    public static void splice<T>(this T[] items, int offset, int count) {
        throw new NotImplementedException();
    }

    public static T[] sort<T>(this T[] items, Func<T, T, int> comparer) {
        throw new NotImplementedException();
    }

    public static bool hasKey<TKey, TValue>(this Dictionary<TKey, TValue> dict, string key) {
        throw new NotImplementedException();
    }

    public static int indexOf(this string str, string substr, int offset) {
        throw new NotImplementedException();
    }

    public static int lastIndexOf(this string str, string substr, int offset) {
        throw new NotImplementedException();
    }

    public static int charCodeAt(this string str, int offset) {
        return (int)str[offset];
    }
}