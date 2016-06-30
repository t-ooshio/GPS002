package sanoapplication002.ne.jp.sanoapplication002;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class Control_DB {
	private LogDB logdb;
	private SQLiteDatabase sdb;

	public Control_DB(Context context) {
		logdb = new LogDB(context);
		try {
			sdb = logdb.getWritableDatabase();
		} catch (SQLiteException e) {

		}
	}

	// DBよりIDを引数としたデータの読み取り(引数0の時は全データの読み取り)
	public Cursor Read_DB(int m_no) {
		Cursor c;
		if (m_no != 0)
			c = sdb.query(logdb.TABLE_NAME, new String[] { "time", "success",
					"IDO", "KEIDO", "diiference", "M_time1", "M_time2", "Sort", "M_no", 
					"M_interval ", "M_timeout ", "M_mtimes ", "C1_delay ", "C2_delay ", "G_delay ", 
					"M_delay ", "M_Sort ", "Log_Flg ", "GPS_Cold_Start_Qual_Flg ", "GPS_Cold_Start_Other_Flg ",
					"GPS_OneXTRA_Timer_Flg ", "GPS_OneXTRA_Data_Flg ", "GPS_Mode_Flg ", "MAP_Flg text" },
					"M_no='" + String.valueOf(m_no) + "'", null, null, null,
					null);
		else
			c = sdb.query(logdb.TABLE_NAME, new String[] { "time", "success",
					"IDO", "KEIDO", "diiference", "M_time1", "M_time2", "Sort", "M_no",
					"M_interval ", "M_timeout ", "M_mtimes ", "C1_delay ", "C2_delay ", "G_delay ", 
					"M_delay ", "M_Sort ", "Log_Flg ", "GPS_Cold_Start_Qual_Flg ", "GPS_Cold_Start_Other_Flg ", 
					"GPS_OneXTRA_Timer_Flg ", "GPS_OneXTRA_Data_Flg ", "GPS_Mode_Flg ", "MAP_Flg text" },
					null, null, null, null, null);
		return c;
	}

	// レコード数のカウント(引数0の時は全データ数のカウント)
	public int Count_Read_Num(int m_no) {
		Cursor c = this.Read_DB(m_no);
		return c.getCount();
	}

	// データの追加
	public void Write_DB(ContentValues value) {
		sdb.insert(logdb.TABLE_NAME, null, value);
	}

	// テーブルのクリア
	public void Clear_DB() {

		sdb.execSQL("delete from " + logdb.TABLE_NAME);
		/*
		 * sdb.execSQL("drop table if exists " + logdb.TABLE_NAME);
		 * sdb.execSQL("create table if not exists " + logdb.TABLE_NAME + "(" +
		 * "time text primar key, " + // 測位終了時刻 "success INTEGER," + // 成功フラグ
		 * (1:成功、0:失敗) "IDO REAL," + // 緯度 "KEIDO REAL," + // 経度
		 * "diiference REAL," + // 測位誤差 "M_time text," + // 測位時間 "Sort text," +
		 * // 測位種別 ("G":GPSプロバイダー、"N":NetWorkプロバイダー) "M_no INTEGER)"); // 測位No
		 */
	}
}
