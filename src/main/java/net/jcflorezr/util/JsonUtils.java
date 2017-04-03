package net.jcflorezr.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static <T> T convertMapToPojo (Map map, Class<T> pojoClass) {
        return MAPPER.convertValue(map, pojoClass);
    }


}
