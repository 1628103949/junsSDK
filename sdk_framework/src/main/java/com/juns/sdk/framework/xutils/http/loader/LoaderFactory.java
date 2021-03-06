package com.juns.sdk.framework.xutils.http.loader;


import com.juns.sdk.framework.xutils.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;

public final class LoaderFactory {

    /**
     * key: loadType
     */
    private static final HashMap<Type, Loader> converterHashMap = new HashMap<Type, Loader>();

    static {
        converterHashMap.put(JSONObject.class, new JSONObjectLoader());
        converterHashMap.put(JSONArray.class, new JSONArrayLoader());
        converterHashMap.put(String.class, new StringLoader());
        converterHashMap.put(File.class, new FileLoader());
        converterHashMap.put(byte[].class, new ByteArrayLoader());
        BooleanLoader booleanLoader = new BooleanLoader();
        converterHashMap.put(boolean.class, booleanLoader);
        converterHashMap.put(Boolean.class, booleanLoader);
        IntegerLoader integerLoader = new IntegerLoader();
        converterHashMap.put(int.class, integerLoader);
        converterHashMap.put(Integer.class, integerLoader);
    }

    private LoaderFactory() {
    }

    @SuppressWarnings("unchecked")
    public static Loader<?> getLoader(Type type, RequestParams params) {
        Loader<?> result = converterHashMap.get(type);
        if (result == null) {
            result = new ObjectLoader(type);
        } else {
            result = result.newInstance();
        }
        result.setParams(params);
        return result;
    }

    public static <T> void registerLoader(Type type, Loader<T> loader) {
        converterHashMap.put(type, loader);
    }
}
