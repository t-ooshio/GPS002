package sanoapplication002.ne.jp.sanoapplication002;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class LogDB extends SQLiteOpenHelper {

	public String[] items;
	public final static String DB_NAME = "M_LOG.db";
	public final String TABLE_NAME = "SANOApplication002_LOG_Ver001";

	public LogDB(Context context) {
		super(context, DB_NAME, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table if not exists " + TABLE_NAME + "("
				+ "time text primar key, "			// 測位終了時刻
				+ "success INTEGER,"				// 成功フラグ (1:成功、0:失敗)
				+ "IDO REAL,"						// 緯度
				+ "KEIDO REAL,"						// 経度
				+ "diiference REAL,"				// 測位誤差
				+ "M_time1 text,"					// 測位時間1(測位のみの時間(準備時間は含まず))
				+ "M_time2 text,"					// 測位時間2(測位に要した全時間)
				+ "Sort text,"						// 測位種別 ("G":GPSプロバイダー、"N":NetWorkプロバイダー)
				+ "M_no INTEGER, "					// 測位No
				+ "M_interval text, "				// 測位間隔設定値
				+ "M_timeout text, "				// 測位タイムアウト設定値
				+ "M_mtimes text, "					// 測位回数設定値
				+ "C1_delay text, "					// アシストデータ削除(XTRA時刻情報取得)時間
				+ "C2_delay text, "					// アシストデータ削除時間
				+ "G_delay text, "					// XTRAデータ取得時間
				+ "M_delay text, "					// 測位開始遅延時間
				+ "M_Sort text, "					// 測位種別 ("G":GPSプロバイダー、"N":NetWorkプロバイダー、"P":Passiveプロバイダー)
				+ "Log_Flg text, "					// ログ保存	選択設定フラグ
				+ "GPS_Cold_Start_Qual_Flg text, "	// コールドスタート(Qualcomチップ用)選択設定フラグ
				+ "GPS_Cold_Start_Other_Flg text, "	// コールドスタート(その他チップ用)選択設定フラグ
				+ "GPS_OneXTRA_Timer_Flg text, "	// GPSoneXTRA時間情報強制取得選択設定フラグ
				+ "GPS_OneXTRA_Data_Flg text, "		// GPSoneXTRAデータ強制取得	選択設定フラグ
				+ "GPS_Mode_Flg text, "				// GPS測位モード設定フラグ
				+ "MAP_Flg text"					// GoogleMAP表示フラグ
				+ ")");
	
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		db.execSQL("drop table if exists " + TABLE_NAME);
		this.onCreate(db);
	}

}
