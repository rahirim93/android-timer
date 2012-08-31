package de.codecentric.android.timer.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TimerDatabaseOpenHelper extends SQLiteOpenHelper {

	public TimerDatabaseOpenHelper(Context context) {
		super(context, Db.DATABASE_NAME, null, Db.VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		for (String sql : Db.TimerTable.Statements.ALL_CREATE_STATEMENTS) {
			db.execSQL(sql);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (newVersion <= 3) {
			for (String sql : Db.TimerTable.Statements.ALL_DROP_STATEMENTS) {
				db.execSQL(sql);
			}
			for (String sql : Db.TimerTable.Statements.ALL_CREATE_STATEMENTS) {
				db.execSQL(sql);
			}
		}
	}
}
