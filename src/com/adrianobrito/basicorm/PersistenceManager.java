package com.adrianobrito.basicorm;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.adrianobrito.basicorm.annotations.Id;
import com.adrianobrito.basicorm.helper.ReflectionHelper;


public class PersistenceManager {
	
	public PersistenceManager(SQLiteDatabase database) {
		this.database = database;
	}

	private SQLiteDatabase database;
	public SQLiteDatabase getDatabase(){ return database; }
	
	public void insert(Object row){
		ContentValues values = new ContentValues();
		Map<String, Object> objectProperties = ReflectionHelper.keyValue(row);
		for(String key:objectProperties.keySet()){
			Object value = objectProperties.get(key);
			if(value == null){
				values.putNull(key);
				continue;
			}
			
			Class<?> clazz = value.getClass();
			setValues(key, value, clazz , values);
		}
		
		String className = ReflectionHelper.className(row);
		Log.v("Insert", objectProperties.toString());
		database.insert(className, null, values);	
	}
	
	public void delete(Object row, String condicional, String[] valores){
		String className = ReflectionHelper.className(row);
		Log.v("Delete", ReflectionHelper.keyValue(row).toString() );
		database.delete(className, condicional, valores);
	}
	
	public void delete(Class<?> clazz, String condicional, String[] valores){
		database.delete(clazz.getSimpleName(), condicional, valores);
	}
	
	public <T> List<T> query(Class<T> clazz, String condicional, String[] args, CursorProcessor<T> cursorProcessor){
		Cursor cursor = null;
		try{
			String className = clazz.getSimpleName();
			Map<String, Object> objectProperties = ReflectionHelper.keyValue(clazz.newInstance());
			String[] columns = objectProperties.keySet().toArray(new String[objectProperties.keySet().size()]);
			List<T> list = new ArrayList<T>();
			
			Log.v("Query", className + " - " + condicional );
			cursor = database.query(className, columns, condicional, args, 
								 null, 
								 null, 
								 null, 
								 null);
		
			int numRows = cursor.getCount();
		    cursor.moveToFirst();
		    
		    for(int i=0; i < numRows; i++){
		    	T t = cursorProcessor.process(cursor);
		    	list.add(t);
		    	cursor.moveToNext();
		    }
		    
		    return list;
		} catch(Exception e){
			Log.v("Error - Database", PersistenceManager.class.getSimpleName(), e);
			return null;
		}finally{
			cursor.close();
		}
		
	}
	
	public <T> List<T> executeSQL(String sql, String[] args, CursorProcessor<T> cursorProcessor){
		Cursor cursor = null;
		try{
			List<T> list = new ArrayList<T>();
			
			Log.v("Query", sql );
			cursor = database.rawQuery(sql, args);	
			int numRows = cursor.getCount();
		    cursor.moveToFirst();
		    
		    for(int i=0; i < numRows; i++){
		    	T t = cursorProcessor.process(cursor);
		    	list.add(t);
		    	cursor.moveToNext();
		    }
		    
		    return list;
		} catch(Exception e){
			Log.v("Error - Database", PersistenceManager.class.getSimpleName(), e);
			return null;
		}finally{
			cursor.close();
		}
	}
	
	public void update(Object row, String condicional, String[] valores){
		ContentValues values = new ContentValues();
		Map<String, Object> objectProperties = ReflectionHelper.keyValue(row);
		for(String key:objectProperties.keySet()){
			
			Object value = objectProperties.get(key);
			if(value == null)
				continue;
			
			@SuppressWarnings("rawtypes")
			Class clazz = value.getClass();
			setValues(key, value, clazz , values);
		}
		
		String className = ReflectionHelper.className(row);
		Log.v("Update", objectProperties.toString());
		database.update(className, values, condicional, valores);
	}
	
	public Integer lastId(){
		Cursor cursor = null;
		String sql = "select last_insert_rowid();";
		
		try{
			cursor = database.rawQuery(sql, null);	
			cursor.moveToFirst();
		   
		    int lastId = cursor.getInt(0);
		    Log.v("Last Id", "" + lastId );
		    return lastId;
		} catch(Exception e){
			return null;
		}
	}
	
	private void setValues(String key, Object value, @SuppressWarnings("rawtypes") Class clazz, ContentValues values){
		if(clazz.equals(Integer.class))
			values.put(key, (Integer)value);
		else if(clazz.equals(Long.class))
			values.put(key, (Long)value);
		else if(clazz.equals(Boolean.class))
			values.put(key, (Boolean)value);
		else if(clazz.equals(Short.class))
			values.put(key, (Short)value );
		else if(clazz.equals(Byte.class))
			values.put(key, (Byte)value);
		else if(clazz.equals(Float.class))
			values.put(key, (Float)value);
		else if(clazz.equals(Double.class))
			values.put(key, (Double)value);
		else if(clazz.equals(String.class))
			values.put(key, (String)value);
		else if(clazz.equals(BigDecimal.class))
			values.put(key, ((BigDecimal) value).doubleValue());
		else
			values.put(key, (Integer)ReflectionHelper.getAnnotatedFieldValue(value, Id.class));
		
	}
	
	
	public interface CursorProcessor<T> {
		T process(Cursor cursor);
		
	}
	
	
}
