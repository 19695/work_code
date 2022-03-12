package org.colm.code.json;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.Map;

public class JsonUtil {

    /**
     * 该方法可以对 json 串中的 key 进行排序以保证每次生成的 string 串是一样的
     * @param map
     * @return
     */
    public static String map2OrderedJson(Map map) {
        return JSONObject.toJSONString(map, SerializerFeature.MapSortField);
    }

}
