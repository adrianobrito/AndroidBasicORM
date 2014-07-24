package com.adrianobrito.basicorm.helper;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.adrianobrito.basicorm.annotations.Id;
import com.adrianobrito.basicorm.annotations.Transient;


public class ReflectionHelper {

	
	public static Map<String, Object> keyValue(Object source){
		
		Map<String, Object> map = new HashMap<String, Object>();
		try{
			@SuppressWarnings("rawtypes")
			Class clazz = source.getClass();
			for(Field field:clazz.getDeclaredFields()){
				if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
					field.setAccessible(true);
					map.put(field.getName(), field.get(source));		        
			    }
			}
			return map;
		} catch(Exception e){
			return null;
		}
	}
	
	public static Map<String,String> fields(@SuppressWarnings("rawtypes") Class clazz){
		Map<String, String> fieldMap = new HashMap<String, String>();
		for(Field field:clazz.getDeclaredFields()){
			field.setAccessible(true);
			if (!java.lang.reflect.Modifier.isStatic(field.getModifiers()) || 
					containsAnnotation(field.getName(), clazz, Transient.class))
				fieldMap.put(field.getName(),dataType(field.getType()));
		}
		
		return fieldMap;
	}
	
	public static String dataType(@SuppressWarnings("rawtypes") Class clazz){
		if(clazz.equals(Float.class) || clazz.equals(Double.class) || clazz.equals(BigDecimal.class))
			return "REAL";
		else if(clazz.equals(Integer.class) || clazz.equals(Long.class) ||
				clazz.equals(Byte.class) || clazz.equals(Short.class) || 
				clazz.equals(Boolean.class))
			return "INTEGER";
		else if(clazz.equals(String.class) || clazz.equals(String.class))
			return "TEXT";
		else
			return "INTEGER";
	}
	
	public static boolean containsAnnotation(String field, Class<?> clazz ,Class<? extends Annotation> annotation){
		try {
			clazz.getDeclaredField(field).setAccessible(true);
			return clazz.getDeclaredField(field).getAnnotation(annotation) != null;
		} catch (NoSuchFieldException e) {
			return false;
		}
		
	}
	
	public static Annotation getAnnotation(String field, Class<?> clazz ,Class<? extends Annotation> annotation){
		try {
			clazz.getDeclaredField(field).setAccessible(true);
			return clazz.getDeclaredField(field).getAnnotation(annotation);
		} catch (NoSuchFieldException e) {
			return null;
		}
		
	}
	
	
	public static String className(Object source){
		return source.getClass().getSimpleName();
	}
	
	public static String formatFieldNames(Collection<String> fields){
		String formatted = ""; int i = 0;
		for(String s:fields){
			if((i + 1) == fields.size())
				formatted += s;
			else
				formatted += s + ",";
		}
		
		return formatted;
	}
	
	public static Object getAnnotatedFieldValue(Object source, Class<? extends Annotation> annotation){
		Class<?> clazz = source.getClass();
		try {
			for(Field field:clazz.getDeclaredFields()){
				field.setAccessible(true);
				if(field.getAnnotation(Id.class) != null)
					return field.get(source);
			}
			
			return null;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
}
