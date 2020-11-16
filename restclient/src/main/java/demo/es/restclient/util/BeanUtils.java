package demo.es.restclient.util;


import demo.es.restclient.entity.IdEntity;
import org.springframework.util.Assert;

import javax.persistence.Entity;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.*;

public class BeanUtils {

    /**
     * 将实体转换为es数据格式的map
     * @param object
     * @param map
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public static void transformBeanToEsMap(Object object, Map<String, Object> map)
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
        List<Field> declaredFields = getAllFieldsList(object.getClass());
        for(Field field:declaredFields){
            field.setAccessible(true);
            Object value = field.get(object);
            if (value != null) {
                /**
                 * es不支持的数据类型 Timestamp
                 * Timestamp可以转为long 或 格式化的字符串
                 */
                if (field.getType().isAssignableFrom(Timestamp.class)) {
                    Date date = (Date) value;
                    Long newValue = date.getTime();
                    map.put(field.getName(), newValue);
                } else if (field.getType().getAnnotation(Entity.class) != null) {
                    String id = ((IdEntity) (value)).getId();
                    map.put(field.getName(), id);
                } else if(field.getType().isAssignableFrom(ArrayList.class) || field.getType().isAssignableFrom(Set.class)) {

                } else {
                    map.put(field.getName(), value);
                }
            } else {
//                map.put(field.getName(), value);
            }
        }
    }

    public static List<Field> getAllFieldsList(final Class<?> cls) {
//        Validate.isTrue(cls != null, "The class must not be null");
        Assert.isTrue(cls != null, "The class must not be null");
        final List<Field> allFields = new ArrayList<Field>();
        Class<?> currentClass = cls;
        while (currentClass != null) {
            final Field[] declaredFields = currentClass.getDeclaredFields();
            for (final Field field : declaredFields) {
                allFields.add(field);
            }
            currentClass = currentClass.getSuperclass();
        }
        return allFields;
    }

}
