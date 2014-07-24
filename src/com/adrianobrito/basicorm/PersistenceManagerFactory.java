package com.adrianobrito.basicorm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.adrianobrito.basicorm.annotations.Id;
import com.adrianobrito.basicorm.helper.ReflectionHelper;

public abstract class PersistenceManagerFactory {
	
	@SuppressWarnings("rawtypes")
	private List<Class> entidadesRegistradas = new ArrayList<Class>();
	private static PersistenceManager persistenceManager;
	private DBOpenHelper dbOpenHelper;
	private static Context context;
	
	public PersistenceManager create(){		
		if(persistenceManager == null){
			dbOpenHelper = new DBOpenHelper(context, database(), 1);
			persistenceManager = new PersistenceManager(dbOpenHelper.getWritableDatabase());
		}
		return persistenceManager;
	}
	
	private void setUp(SQLiteDatabase sqLiteDatabase){
		entidadesRegistradas = registerEntities();
		for(@SuppressWarnings("rawtypes") Class clazz:entidadesRegistradas){
			
			int index = 0;
			String SQL = "CREATE TABLE IF NOT EXISTS " + clazz.getSimpleName() + " (";
			Map<String, String> fields = ReflectionHelper.fields(clazz);
			for(String key:fields.keySet()){
				index++;
				SQL += key + " " + fields.get(key) 
						   + (ReflectionHelper.containsAnnotation(key, clazz, Id.class) ? " PRIMARY KEY " 
						   + (((Id)ReflectionHelper.getAnnotation(key, clazz, Id.class)).autoIncrement() ?  " AUTOINCREMENT NOT NULL" : "")      
						   : "")
						   + (index != fields.size() ?  ", " : "");
 			}
			SQL += ");";			
			
			Log.v("SQL", SQL );
			sqLiteDatabase.execSQL(SQL);
		}
		
	}
	
	public class DBOpenHelper extends SQLiteOpenHelper {
		
		public DBOpenHelper(Context context, String dbName, int version) {
		    super(context, dbName, null, version);
		}
		
		@Override
		public void onCreate(SQLiteDatabase sqLiteDatabase) {
			setUp(sqLiteDatabase);
		}
		
		@Override
		public void onOpen(SQLiteDatabase db) {
			super.onOpen(db);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			for(@SuppressWarnings("rawtypes") Class clazz:entidadesRegistradas){
				 db.execSQL("DROP TABLE IF EXISTS " + clazz.getSimpleName() + " ;");
			}
			
		}
		
	}
	
	@SuppressWarnings("rawtypes")
	public abstract List<Class> registerEntities();
	public abstract String database();

	public static void setContext(Context context) {
	    PersistenceManagerFactory.context = context;
	}
	
	
	
	
	
}
