package emperatriz.hatomico2;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DB {
	
	
	private SQLiteDatabase db;
	private final String MY_DATABASE_NAME = "hatomicoDB";
	private final String WHITE_LIST = "WhiteList";
	private final String CREATE_WHILETLIST_TABLE = "CREATE TABLE IF NOT EXISTS " + WHITE_LIST +
	    											"(id INTEGER PRIMARY KEY)";
	
	private DB(Context context){
		db = context.openOrCreateDatabase(MY_DATABASE_NAME, Context.MODE_PRIVATE, null);
		db.execSQL(CREATE_WHILETLIST_TABLE);
	}
	
	private static DB instance; 
	
	public static DB init(Context context){
		if (instance==null)
			instance = new DB(context);
		return instance;
	}
	
	public static DB init(){
		return instance;
	}
	
	public void createNewWhiteList(long[] ids){
		db.execSQL("DELETE FROM "+WHITE_LIST);
		for (int i=0;i<ids.length;i++){
			db.execSQL("INSERT INTO "+WHITE_LIST+ " (id) VALUES ('"+ids[i]+"')");
		}
	}
	
	public long[] getWhiteList(){		
		Cursor c = db.query(WHITE_LIST, null, null, null, null, null, null);
		if (c!=null){
			long[] ret = new long[c.getCount()];
			if (c.moveToFirst()){
				do {

					ret[c.getPosition()]=c.getLong(c.getColumnIndex("id"));

				}while (c.moveToNext());
				c.close();
				return ret;
			}
		}
		c.close();
		return new long[0];
	}
	
	
	
}
