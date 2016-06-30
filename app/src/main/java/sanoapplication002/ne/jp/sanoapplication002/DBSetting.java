package sanoapplication002.ne.jp.sanoapplication002;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.content.*;
import android.database.Cursor;

public class DBSetting extends Activity {

	private String[] items;
	private Control_DB cnt_DB;
	private int Final_M_NO;
	private String PkgName;

	private SharedPreferences _Pref; // 設定データ本体用インスタンス
	private SharedPreferences.Editor _PrefEditor; // 設定データ書き換え用インスタンス

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		cnt_DB = new Control_DB(this);

		Cursor c;
		int i = 0;
		while (i < 1000) // 無限ループの防止のため、上限を1000とする。
		{
			c = cnt_DB.Read_DB(i + 1);
			if (c.getCount() != 0)
				++i;
			else
				break;
		}

		items = new String[i + 1];
		i = 0;

		while (i < 1000) // 無限ループの防止のため、上限を1000とする。
		{
			c = cnt_DB.Read_DB(i + 1);
			if (c.getCount() != 0) {
				c.moveToFirst();
				items[i] = c.getString(0);
				++i;
			} else
				break;
		}
		Final_M_NO = i;
		items[Final_M_NO] = "クリア";

		ListView lv = new ListView(this);
		setContentView(lv);
		lv.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_single_choice, items));
		lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		lv.setOnItemClickListener(new MyClickAdapter());

		PkgName = this.getPackageName();

		_Pref = getSharedPreferences(getString(R.string.PREF_KEY),
				ListActivity.MODE_PRIVATE);
		_PrefEditor = _Pref.edit();
	}

	class MyClickAdapter implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position,
				long id) {
			final int set_no = position + 1;

			// DBクリア以外
			if (position != Final_M_NO) {
				AlertDialog ald = new AlertDialog.Builder(view.getContext())
						.setTitle("データの書き出し").setMessage("データを出力します。")
						.setPositiveButton("はい",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										String File_Name = "";
										String sPath = "";
										String strBuff = "";
										String strTDBuff = "";
										String strDateBuff = "";
										String strTimeBuff = "";
										String strSettingInfoBuff = "";
										int splitpoint;
										int i;

										Cursor c = cnt_DB.Read_DB(set_no);
										i = c.getCount();

										if (c.getCount() != 0) {
											c.moveToFirst();

											// 設定情報の取得
											strSettingInfoBuff = strSettingInfoBuff
												+ "測位間隔,"
												+ "タイムアウト,"
												+ "測位回数,"
												+ "アシストデータ削除（XTRA時刻情報取得）時間,"
												+ "アシストデータ削除時間,"
												+ "XTRAデータ取得時間,"
												+ "測位開始遅延時間,"
												+ "測位種別,"
												+ "ログ保存,"
												+ "コールドスタート(Qualcomチップ用),"
												+ "コールドスタート(その他チップ用),"
												+ "XTRA時間情報強制取得,"
												+ "XTRAデータ強制取得,"
												+ "GPS測位モード,"
												+ "GoogleMAP表示"
												+ "\n\r";

											strSettingInfoBuff = strSettingInfoBuff
												+ c.getString(9) 			// 測位間隔設定値
												+ "," + c.getString(10) 	// 測位タイムアウト設定値
												+ "," + c.getString(11) 	// 測位回数設定値
												+ "," + c.getString(12)		// アシストデータ削除(XTRA時刻情報取得)時間
												+ "," + c.getString(13)		// アシストデータ削除時間
												+ "," + c.getString(14)		// XTRAデータ取得時間
												+ "," + c.getString(15)		// 測位開始遅延時間
												+ "," + c.getString(16)		// 測位種別 ("G":GPSプロバイダー、"N":NetWorkプロバイダー)
												+ "," + c.getString(17)		// ログ保存	選択設定フラグ
												+ "," + c.getString(18)		// コールドスタート(Qualcomチップ用)選択設定フラグ
												+ "," + c.getString(19)		// コールドスタート(その他チップ用)選択設定フラグ
												+ "," + c.getString(20)		// GPSoneXTRA時間情報強制取得選択設定フラグ
												+ "," + c.getString(21)		// GPSoneXTRAデータ強制取得	選択設定フラグ
												+ "," + c.getString(22)		// GPS測位モード設定フラグ
												+ "," + c.getString(23)		// GoogleMAP表示フラグ
												+ "\n\r";

											// レコードの項目名
											strBuff = strBuff
												+ "測位完了年月日,"
												+ "測位完了時刻,"
												+ "測位結果フラグ,"
												+ "緯度,"
												+ "経度,"
												+ "測位誤差,"
												+ "測位時間1,"
												+ "測位時間2,"
												+ "測位種別"
												+ "\n\r";
													
											while (i > 0) {
												strTDBuff   = "";
												strDateBuff = "";
												strTimeBuff = "";
												
												strTDBuff   = c.getString(0);
												splitpoint  = strTDBuff.lastIndexOf("/");
												strDateBuff = strTDBuff.substring(0, splitpoint); 
												strTimeBuff = strTDBuff.substring(splitpoint + 1);
												
												strBuff = strBuff
														+ strDateBuff 			// 測位終了年月日
														+ "," + strTimeBuff 	// 測位終了時刻
														+ "," + c.getInt(1) 	// 成功フラグ
														+ "," + c.getDouble(2)	// 緯度
														+ "," + c.getDouble(3)	// 経度
														+ "," + c.getDouble(4)	// 測位誤差
														+ "," + c.getString(5)	// 測位時間1
														+ "," + c.getString(6)	// 測位時間2
														+ "," + c.getString(7)	// 測位種別
														+ "\n\r";

												c.moveToNext();

												--i;
											}
											
											// レコード本体 + 設定項目
											strBuff = strBuff + strSettingInfoBuff;
											
											sPath = "/sdcard/" + PkgName;
											String stBuff = items[set_no - 1].replace(".", "-");
											stBuff = stBuff.replace("/", "-");
											stBuff = stBuff.replace(":", "-");
											File_Name = sPath + "/GPSLOG_"
													+ stBuff
													+ ".txt";
											File files = new File(File_Name);
											files.getParentFile().mkdir();

											try {
												FileOutputStream fos = new FileOutputStream(
														files, true);
												OutputStreamWriter osw = new OutputStreamWriter(
														fos, "Shift-JIS");
												BufferedWriter bw = new BufferedWriter(
														osw);

												bw.write(strBuff);
												bw.flush();
												bw.close();
											} catch (IOException e) {
												e.printStackTrace();
											}

										}
									}
								}).setNegativeButton("いいえ",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {

									}
								}).setCancelable(true).create();
				ald.show();
			}

			// DBクリア
			else {
				// GPSサービスが起動中は、DBクリアしない
				if (isServiceRunning(PkgName + ".GPS_Service") 
					|| isServiceRunning(PkgName + ".GPS_Service_ModeB")) {
					AlertDialog ald = new AlertDialog.Builder(view.getContext())
							.setTitle("データのクリア").setMessage(
									"GPS測位サービス起動中は、DBのクリアは出来ません")
							.setPositiveButton("戻る",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {

										}
									}).setCancelable(true).create();
					ald.show();
				} else {
					AlertDialog ald = new AlertDialog.Builder(view.getContext())
							.setTitle("データのクリア").setMessage(
									"データをクリアします(クリア後、設定画面は終了します)。")
							.setPositiveButton("はい",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											// GPS測位累計回数のクリア
											_PrefEditor
													.putString(
															getString(R.string.GPS_m_no),
															"0");
											_PrefEditor.commit();
											// DBのクリア
											cnt_DB.Clear_DB();
											// 強制終了
											finish();
										}
									}).setNegativeButton("いいえ",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {

										}
									}).setCancelable(true).create();
					ald.show();
				}

			}
		}

		// サービスが起動中かどうかの判断関数
		private boolean isServiceRunning(String className) {
			ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
			List<ActivityManager.RunningServiceInfo> serviceInfos = am
					.getRunningServices(Integer.MAX_VALUE);
			for (int i = 0; i < serviceInfos.size(); i++) {
				if (serviceInfos.get(i).service.getClassName()
						.equals(className)) {
					return true;
				}
			}
			return false;
		}

	}

}