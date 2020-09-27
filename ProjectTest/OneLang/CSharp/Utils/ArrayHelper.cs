using System;
using System.Collections.Generic;

namespace Utils
{
    public class ArrayHelper {
        public static T[] sortBy<T>(T[] items, Func<T, int> keySelector)
        {
            return items.sort((a, b) => keySelector(a) - keySelector(b));
        }
        
        public static void removeLastN<T>(List<T> items, int count)
        {
            items.splice(items.length() - count, count);
        }
    }
}