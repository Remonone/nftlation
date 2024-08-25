package remonone.nftilation.utils;

import java.util.Map;

@SuppressWarnings("unchecked")
public class NestedObjectFetcher {

    public static Object getNestedObject(String path, Map<String, Object> values, int level) {
        int index = path.indexOf('.');
        if(index == -1) {
            return getLevelBasedObject(level, values.get(path));
        }
        String firstPart = path.substring(0, index);
        for(Map.Entry<String, Object> entry : values.entrySet()) {
            if(entry.getKey().equals(firstPart)) {
                Object step = getLevelBasedObject(level, entry.getValue());
                Map<String, Object> map = (Map<String, Object>) step;
                return getNestedObject(path.substring(index + 1), map, level);
            }
        }
        return null;
    }

    public static Object getLevelBasedObject(int i, Object value) {
        if(value == null) return null;
        if(!(value instanceof Map)) return value;
        Map<String, Object> values = (Map<String, Object>) value;
        if(!values.containsKey("level_base")) return values;
        if(!values.containsKey(String.valueOf(i))) {
            if (i <= 1) return null;
            return getLevelBasedObject(i - 1, value);
        }
        return values.get(String.valueOf(i));
    }
    public static boolean containsExactLevelForPath(String path, int i, Object scope) {
        if(!(scope instanceof Map)) return false;
        int index = path.indexOf('.');
        Map<String, Object> values = (Map<String, Object>) scope;
        if(index == -1) {
            Map<String, Object> value = (Map<String, Object>) values.get(path);
            if(!value.containsKey("level_base")) return false;
            return value.containsKey(String.valueOf(i));
        }
        String firstPart = path.substring(0, index);
        for(Map.Entry<String, Object> entry : values.entrySet()) {
            if(entry.getKey().equals(firstPart)) {
                return containsExactLevelForPath(path.substring(index + 1), i, entry.getValue());
            }
        }
        return false;

    }
}
