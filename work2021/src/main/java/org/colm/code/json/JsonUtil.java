package org.colm.code.json;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.Map;

public class JsonUtil {

    public static String map2OrderedJson(Map map) {
        return JSONObject.toJSONString(map, SerializerFeature.MapSortField);
    }

}
