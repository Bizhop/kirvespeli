package fi.bizhop.jassu.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

public class JsonUtil {
    private final static ObjectMapper MAPPER = new ObjectMapper();

    public static Optional<String> getJson(Object object){
        try {
            return Optional.ofNullable(MAPPER.writeValueAsString(object));
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static <T> Optional<T> getJavaObject(String json, Class<T> type){
        try {
            return Optional.ofNullable(MAPPER.readValue(json, type));
        }
        catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
