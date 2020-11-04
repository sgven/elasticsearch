package demo.es.restclient.util;


import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class BeanUtils {

    /**
     * 将实体转换为es数据格式的map
     * @param object
     * @param map
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public static void transformBeanToEsMap(Object object, Map<String, Object> map)
            throws IllegalArgumentException, IllegalAccessException {
        List<Field> declaredFields = getAllFieldsList(object.getClass());
        for(Field field:declaredFields){
            field.setAccessible(true);
            Object value = field.get(object);
            if (value != null) {
                /**
                 * es不支持的数据类型 Timestamp
                 * Timestamp可以转为long 或 格式化的字符串
                 *
                 * es6.4.2版本不支持BigDecimal、BigInteger，之后的版本已经支持
                 * BigDecimal转为double
                 * BigInteger转为long
                 */
                if (value.getClass().isAssignableFrom(Timestamp.class)) {
                    Date date = (Date) value;
                    Long newValue= date.getTime();
                    map.put(field.getName(), newValue);
                } else {
                    map.put(field.getName(), value);
                }
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
