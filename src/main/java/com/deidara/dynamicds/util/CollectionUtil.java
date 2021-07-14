package com.deidara.dynamicds.util;

import java.util.*;
import java.util.function.Function;

public class CollectionUtil {

    public static <T,M> Map<M, List<T>> groupBy(Function<T, M> function, Collection<T> collection) {
        Map<M, List<T>> result = new HashMap<>();
        for(T t : collection){
            M key = function.apply(t);
            if(result.containsKey(key)){
                result.get(key).add(t);
            }else{
                List<T> list = new ArrayList<>();
                list.add(t);
                result.put(key, list);
            }
        }
        return result;
    }

}
