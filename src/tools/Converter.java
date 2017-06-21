import com.zhishibao.feedback.api.model.Feedback;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class Converter {
    public static void main(String args[]) {

    }

    @Test
    public void test() {
        getFields(Feedback.class);
    }

    /**
     * 不指定路径会在本项目下converter文件夹下生成转换类
     *
     * @param clazz
     */
    public static void converte(Class clazz) {
        String pname = System.getProperty("user.dir");
        String path = pname + "/converter/";
        new File(path).mkdirs();
        Converter.converte(clazz, path);
    }

    /**
     * 特定路径下生成类
     *
     * @param clazz
     * @param path
     */
    public static void converte(Class clazz, String path) {
        String cname = clazz.getSimpleName();
        Map<String, String> fieldsMap = getFields(clazz);
        Set<String> fields = fieldsMap.keySet();
        methods(fieldsMap, clazz);
        StringBuilder builder = new StringBuilder();
        builder.append("public class " + cname + "Converter{" + "\n");
        String fromThrift = fromThrift(fieldsMap, cname);
        builder.append(fromThrift);
        String toThrift = toThrift(fieldsMap, cname);
        builder.append(toThrift);
        builder.append("}" + "\n");
        String finalstr = builder.toString();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path + "" + cname + "Converter.java");
            fos.write(finalstr.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    /**
     * 获取属性名与类型
     */
    public static Map<String, String> getFields(Class clazz) {
        Field[] fields = clazz.getDeclaredFields();
        Map<String, String> atts = new HashMap<String, String>();
        for (Field field : fields) {
            Class<?> type = field.getType();
            String simpleName = type.getSimpleName();
            atts.put(field.getName(), simpleName);
        }
        return atts;
    }


    /**
     * get set 方法
     */
    private static Map<String, String> getMethods = new HashMap<>();
    private static Map<String, String> setMethods = new HashMap<>();
    private static Map<String, String> isSetMethods = new HashMap<>();

    public static void methods(Map<String, String> fields, Class clazz) {
        //获取所有方法
        Method[] methods = clazz.getMethods();
        Set<String> methodanmes = new HashSet<>();
        for (Method m : methods) {
            methodanmes.add(m.getName());
        }
        //set、get  method
        Set<String> fieldname = fields.keySet();
        List<String> list = new ArrayList<>();
        list.addAll(fieldname);
        for (int i = 0; i < list.size(); i++) {
            String f = list.get(i);
            for (String m : methodanmes) {
                String d = m.substring(3);
                if (m.contains("set") && m.substring(3).toLowerCase().equals(f.toLowerCase()))
                    setMethods.put(f, m);
                if (m.contains("get") && m.substring(3).toLowerCase().equals(f.toLowerCase()))
                    getMethods.put(f, m + "()");

            }
        }
        //isSetMethod
        for (String field : fields.keySet()) {
            String mname = "isSet" + field.substring(0, 1).toUpperCase() + field.substring(1, field.length()) + "()";
            isSetMethods.put(field, mname);
        }
    }


    /**
     * if语句拼装
     */
    public static String ifCluse1(String field, String type, String cname) {

        StringBuilder builder = new StringBuilder();
        if (type.equals("Date")) {
            builder.append("" +
                    "if (thrift" + cname + "." + isSetMethods.get(field) + ") {\n" +
                    cname.toLowerCase() + "." + setMethods.get(field) + "(new Date(thrift" + cname + "." + getMethods.get(field) + "));" + "\n" +
                    " }" + "\n"
            );
        } else {

            builder.append(
                    "if(thrift" + cname + "." + isSetMethods.get(field) + "){  \n"
                            + cname.toLowerCase() + "." + setMethods.get(field) + "(thrift" + cname + "." + getMethods.get(field) + ");" + "\n"
                            + "}" + "\n"
            );
        }
        return builder.toString();
    }

    /**
     * if语句拼装
     */
    public static String ifCluse2(String field, String type, String cname) {
        StringBuilder builder = new StringBuilder();
        if (type.equals("Date")) {
            builder.append("" +
                    "if (" + cname.toLowerCase() + "." + getMethods.get(field) + "!= null) { \n" +
                    "builder." + setMethods.get(field) + "(" + cname.toLowerCase() + "." + getMethods.get(field) + ".getTime());\n" +
                    "}\n"
            );
        } else {

            builder.append(
                    "if(" + cname.toLowerCase() + "." + getMethods.get(field) + "!=null){" + "\n"
                            + "builder." + setMethods.get(field) + "(" + cname.toLowerCase() + "." + getMethods.get(field) + ");" + "\n"
                            + "}" + "\n"
            );
        }
        return builder.toString();
    }

    /**
     * fromThrift语句拼装
     */
    public static String fromThrift(Map<String, String> fieldmap, String cname) {

        StringBuilder builder = new StringBuilder();
        builder.append(
                "public static " + cname + " fromThrift(T" + cname + " thrift" + cname + "){" + "\n"
                        + "if (thrift" + cname + " == null) {" + "\n"
                        + "return new " + cname + "();" + "\n"
                        + "}");
        builder.append(cname + " " + cname.toLowerCase() + " = new " + cname + "();" + "\n");
        Set<String> fields = fieldmap.keySet();
        for (String field : fields) {
            builder.append(ifCluse1(field, fieldmap.get(field), cname));
        }
        builder.append("return " + cname.toLowerCase() + ";" + "\n");
        builder.append("}" + "\n");
        return builder.toString();
    }

    /**
     * toThrift语句拼装
     */
    public static String toThrift(Map<String, String> fieldmap, String cname) {
        StringBuilder builder = new StringBuilder();
        builder.append(
                "public static T" + cname + " toThrift(" + cname + " " + cname.toLowerCase() + "){" + "\n"
                        + "if (" + cname.toLowerCase() + " == null) {" + "\n"
                        + "return new T" + cname + ".Builder().build();" + "\n"
                        + "}" + "\n");
        builder.append(
                "T" + cname + ".Builder builder = new T" + cname + ".Builder();" + "\n"
        );
        Set<String> fields = fieldmap.keySet();
        for (String field : fields) {
            builder.append(ifCluse2(field, fieldmap.get(field), cname));
        }
        builder.append("return builder.build();\n");
        builder.append("}\n");
        return builder.toString();
    }

}