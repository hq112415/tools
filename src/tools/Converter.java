package tools;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Converter {

	public static  void converte(Class clazz,String path){
		String cname = clazz.getSimpleName();
		Set<String> fields = getFields(clazz);
		Map<String, String> getMethods = getMethods(fields);
		Map<String, String> setMethods = setMethods(fields);
		Map<String, String> isSetMethods = isSetMethods(fields);
		StringBuilder builder = new StringBuilder();
		builder.append("public class " + cname + "Converter{" + "\n" );
		String fromThrift = fromThrift(fields, cname, getMethods, setMethods, isSetMethods);
		builder.append(fromThrift);
		String toThrift = toThrift(fields, cname, getMethods, setMethods, isSetMethods);
		builder.append(toThrift);
		builder.append("}"+ "\n");
		String finalstr = builder.toString();
		FileOutputStream fos = null;
		try{
		fos = new FileOutputStream(path + ""+cname + "Converter.java");
		fos.write(finalstr.getBytes());
		}catch(IOException e){
			throw new RuntimeException(e);
		}finally{
			if(fos != null){
				try {
					fos.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	/**
	 * 获取所有属性
	 * @param clazz
	 * @return
	 */
	public static  Set<String> getFields(Class clazz) {
		Field[] fields = clazz.getDeclaredFields();
		Set<String> atts = new HashSet<String>();
		for (Field field : fields) {
			atts.add(field.getName());
		}
		return atts;
	}
	/**
	 * get方法
	 * @param clazz
	 * @return
	 */
	public static  Map<String, String> getMethods(Set<String> fields) {
		Map<String, String> getMethods = new HashMap<String, String>();
		for (String field : fields) {
			String mname = "get" + field.substring(0, 1).toUpperCase() + field.substring(1, field.length()) + "()";
			getMethods.put(field, mname);
		}
		return getMethods;
	}
	/**
	 * set方法
	 * @param clazz
	 * @return
	 */
	public static  Map<String, String> setMethods(Set<String> fields) {
		Map<String, String> getMethods = new HashMap<String, String>();
		for (String field : fields) {
			String mname = "set" + field.substring(0, 1).toUpperCase() + field.substring(1, field.length());
			getMethods.put(field, mname);
		}
		return getMethods;
	}
	/**
	 * isSetMethods方法
	 * @param clazz
	 * @return
	 */
	public static  Map<String, String> isSetMethods(Set<String> fields) {
		Map<String, String> getMethods = new HashMap<String, String>();
		for (String field : fields) {
			String mname = "isSet" + field.substring(0, 1).toUpperCase() + field.substring(1, field.length()) + "()";
			getMethods.put(field, mname);
		}
		return getMethods;
	}
	/**
	 * if语句拼装
	 * @param fields
	 * @param cname
	 * @param getMethods
	 * @param setMethods
	 * @param isSetMethods
	 * @return
	 */
	public static  String ifCluse1(	Set<String> fields,
							String cname,
							Map<String, String> getMethods,
							Map<String, String> setMethods,
							Map<String, String> isSetMethods){
		StringBuilder builder = new StringBuilder();
		for (String field : fields){
			 builder.append(
					 "if(t" + cname +"." + isSetMethods.get(field) + "){"+ "\n"
							 + cname.toLowerCase()+"."+ setMethods.get(field) + "(t" + cname + "." + getMethods.get(field)+");"+ "\n"
						+"}"	+ "\n" 
					 );
		}
		return builder.toString();
	}
	
	/**
	 * if语句拼装
	 * @param fields
	 * @param cname
	 * @param getMethods
	 * @param setMethods
	 * @param isSetMethods
	 * @return
	 */
	public static  String ifCluse2(	Set<String> fields,
							String cname,
							Map<String, String> getMethods,
							Map<String, String> setMethods,
							Map<String, String> isSetMethods){
		StringBuilder builder = new StringBuilder();
		for (String field : fields){
			builder.append(
					"if(" + cname.toLowerCase() +"." + getMethods.get(field) + "!=null){"+ "\n"
							+ "builder." + setMethods.get(field) + "(" + cname.toLowerCase() + "." + getMethods.get(field)+");"+ "\n"
							+"}"	+ "\n" 
			);
		}
		return builder.toString();
}
	/**
	 * fromThrift语句拼装
	 * @param fields
	 * @param cname
	 * @param getMethods
	 * @param setMethods
	 * @param isSetMethods
	 * @return
	 */
	public static  String fromThrift(Set<String> fields,
							 String cname,
							 Map<String, String> getMethods,
							 Map<String, String> setMethods,
							 Map<String, String> isSetMethods){
		StringBuilder builder = new StringBuilder();
		builder.append(
			"public static " + cname + " fromThrift(T" + cname + " t" + cname + "){" + "\n"
			+ "if (t" + cname + " == null) {" + "\n"
					+"return new " + cname + "();"+ "\n"
			+ "}");
		builder.append(cname + " "+cname.toLowerCase() + " = new " + cname + "();" + "\n");
		for(String field : fields){
			builder.append(ifCluse1(fields,cname,getMethods,setMethods,isSetMethods));
		}
		builder.append("return " + cname.toLowerCase()+";" + "\n");
		builder.append("}" + "\n");
		return builder.toString();
	}
	
	/**
	 * toThrift语句拼装
	 * @param fields
	 * @param cname
	 * @param getMethods
	 * @param setMethods
	 * @param isSetMethods
	 * @return
	 */
	public static  String toThrift(Set<String> fields,
			 String cname,
			 Map<String, String> getMethods,
			 Map<String, String> setMethods,
			 Map<String, String> isSetMethods){
		StringBuilder builder = new StringBuilder();
		builder.append(
				"public static T" + cname + " toThrift(" + cname + " " + cname.toLowerCase() + "){" + "\n"
						+ "if (" + cname.toLowerCase() + " == null) {"+ "\n"
						+ "return new T" + cname + ".Builder().build();"+ "\n"
				+"}"+ "\n");
		builder.append(
				"T" + cname + ".Builder builder = new T" + cname + ".Builder();"+ "\n"
				);
		
		for(String field : fields){
			builder.append(ifCluse2(fields,cname,getMethods,setMethods,isSetMethods));
			}
		builder.append("return builder.build();");
		builder.append("}");
		return builder.toString();
	}

}
