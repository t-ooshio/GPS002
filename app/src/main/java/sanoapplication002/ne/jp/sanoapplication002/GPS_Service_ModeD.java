package sanoapplication002.ne.jp.sanoapplication002;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlarmManager;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.widget.Toast;

public class GPS_Service_ModeD extends Service implements LocationListener
{

	private LocationManager lm	= null;					// LocationManager
	private Timer mTimer	= null;						// 停止タイマ
	private Handler mHandler;							// 停止タイマハンドラー
	private Timer rTimer	= null;						// 測位間隔タイマ
	private Handler rHandler;							// 再開始ハンドラー
	private IntervalTimerTask	intervaltimertask			= null;
	private int start_Flg	= 0;						// 測位開始フラグ
	private int timout_Flg		= 0;					// タイムアウトフラグ

	private final Calendar calendar = Calendar.getInstance();
	private final int year		= calendar.get(Calendar.YEAR);
	private final int month	= calendar.get(Calendar.MONTH);
	private final int day		= calendar.get(Calendar.DAY_OF_MONTH);
	private final int hour		= calendar.get(Calendar.HOUR_OF_DAY);
	private final int minute	= calendar.get(Calendar.MINUTE);
	private final int second	= calendar.get(Calendar.SECOND);
	private final int ms		= calendar.get(Calendar.MILLISECOND);
	private int m_interval	= 0;						// 測位間隔設定値
	private int m_timeout	= 0;						// 測位タイムアウト設定値
	private long m_mtimes	= 0;						// 測位回数設定値
	private int c1_delay	= 0;						// アシストデータ削除(XTRA時刻情報取得)時間
	private int c2_delay	= 0;						// アシストデータ削除時間
	private int g_delay	= 0;						// XTRAデータ取得時間
	private int m_delay	= 0;						// 測位開始遅延時間
	private String Filename;							// ファイル名(今は使ってない)
	private String sPath;								// ファイルパス(今は使ってない)
	private long measure_num;							// 測位回数
	private long success_times;							// 測位成功回数
	private int gps_Flg;								// GPSプロバイダー			選択設定フラグ
	private int net_Flg;								// NetWorkプロバイダー		選択設定フラグ
	private int passive_Flg;							// Passiveプロバイダー		選択設定フラグ
	private int Log_Flg		= 1;					// ログ保存					選択設定フラグ
	private int GPS_Cold_Start_Qual_Flg;				// コールドスタート(Qualcomチップ用)選択設定フラグ
	private int GPS_Cold_Start_Other_Flg;				// コールドスタート(その他チップ用)	選択設定フラグ
	private int GPS_OneXTRA_Timer_Flg;					// GPSoneXTRA時間情報強制取得選択設定フラグ
	private int GPS_OneXTRA_Data_Flg;					// GPSoneXTRAデータ強制取得	選択設定フラグ
	private int GPS_Mode_Flg	= 0;					// GPS測位モード設定フラグ
	private int MAP_Flg		= 0;					// GoogleMAP表示フラグ
	private Boolean First_m_Flg				= false;	// 測位を一度でも行ったかフラグ
	private Boolean EXTRA_Time_Flg			= false;	// GPSoneXTRA時間データ強制取得可否フラグ
	private Boolean EXTRA_Data_Flg			= false;	// GPSoneXTRAデータ強制取得可否フラグ

	private SharedPreferences _Pref;					// 設定データ本体用インスタンス
	private SharedPreferences.Editor _PrefEditor;		// 設定データ書き換え用インスタンス

	public static int _defm_interval		= 30;		// GPS測位間隔時間(sec）
	public static int _defm_timeout		= 25;		// GPSタイムアウト時間(sec)
	public static long _defm_mtimes		= 0;		// GPS測位回数
	public static int _defm_c1delay		= 0;		// アシストデータ削除(XTRA時刻情報取得)遅延時間(sec)
	public static int _defm_c2delay		= 0;		// アシストデータ削除遅延時間(sec)
	public static int _defm_gdelay			= 0;		// XTRAデータ取得遅延時間(sec)
	public static int _defm_mdelay			= 0;		// GPS測位開始遅延時間(sec)
	public static String _defm_filemane	= "GPSLOG"; // ファイル名
	public static long _defm_total_num		= 0;		// GPS測位累計回数
	public static long _defonbtn_push_time= 0;	// ボタン押下時間

	private AlarmManager alarmManager;					// 防止用アラームマネージャ
	private PendingIntent pi;
	private WakeLock wakelock;							// CPUロッククラス
	private boolean CPULockFlg				= false;	// CPU占有フラグ
	private boolean alarmManagerFLG		= false;	// 防止用アラームマネージャフラグ
	private boolean FirstMeasureFLG		= true;		// 一番最初の測位フラグ

	private Control_DB cnt_DB;							// DBクラス
	private Bundle bundle;								// ぶん取る
	
	private long ONbtnPushTime	= 0;					// ボタン押下時の時間 
	Calendar Startcalendar1		= null;					// 測位開始時刻1
	Calendar Startcalendar2		= null;					// 測位開始時刻2
	Calendar Endcalendar		= null;					// 測位終了時刻

	private Context PublicContext;						// Toastを色々な所で使うためのContext

	private boolean Delay_mStart_Flg;					// 測位開始遅延タイマー完了フラグ 


	class GPS_Service_ModeD_Binder extends Binder
	{
		GPS_Service_ModeD getService()
		{
			return GPS_Service_ModeD.this;
		}
	}

	public static final String Timer = "GPS timer D";

	@Override
	public void onCreate()
	{
		super.onCreate();
		
		bundle					= new Bundle();
    	PublicContext			= this.getBaseContext();

    	gps_Flg					= 1;
		net_Flg					= 0;
		passive_Flg				= 0;
		GPS_Cold_Start_Qual_Flg	= 0;
		GPS_Cold_Start_Other_Flg= 0;
		GPS_OneXTRA_Timer_Flg	= 0;
		GPS_OneXTRA_Data_Flg	= 0;
		GPS_Mode_Flg			= 0;
		MAP_Flg					= 0;
		measure_num				= 0;
		success_times			= 0;
		start_Flg				= 0;
		timout_Flg				= 0;
		c1_delay				= 0;
		c2_delay				= 0;
		g_delay					= 0;
		m_delay					= 0;
		
		mHandler				= new Handler();
		mTimer					= null;
		rHandler				= new Handler();
		rTimer					= null;

		// サービスがKillされるのを防止する処理 ⇒ 2011.10.11 ？？？なぜこれでKill防止になるのか不明？？？
		if (!alarmManagerFLG)
		{
			Intent i = new Intent();
			i.setClassName(this.getPackageName(), this.getPackageName() + ".ServiceGPS");
			pi = PendingIntent.getService(this, 0, i, 0);

			Calendar cal = Calendar.getInstance();				// Calendar取得
			cal.setTimeInMillis(System.currentTimeMillis());	// 現在時刻を取得
			cal.add(Calendar.SECOND, 15);						// 現時刻より15秒後を設定

			alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
			// 一定時間後に端末を起動させる
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 3600000, pi);
			alarmManagerFLG = true;
		}

		// パワーマネージャでスリープ時のコールドを防止
		if (!CPULockFlg)
		{
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
			wakelock.acquire();
			CPULockFlg = true;
		}

		// GPS測位累計回数。
		_Pref = getSharedPreferences(getString(R.string.PREF_KEY),ListActivity.MODE_PRIVATE);
		_PrefEditor = _Pref.edit();
		_defm_total_num = Long.parseLong(_Pref.getString(
				getString(R.string.GPS_m_no), String.valueOf(_defm_total_num)));

		cnt_DB = new Control_DB(this);
	}

	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart(intent, startId);

		measure_num				= 0;
		success_times			= 0;
		c1_delay				= 0;
		c2_delay				= 0;
		g_delay					= 0;
		m_delay					= 0;
		start_Flg				= 0;
		timout_Flg				= 0;

		// GPS測位設定値の取得(測位設定関連)
		m_interval				= _Pref.getInt(getString(R.string.GPS_m_interval),_defm_interval);
		m_timeout				= _Pref.getInt(getString(R.string.GPS_m_timeout),_defm_timeout);
		m_mtimes				= _Pref.getLong(getString(R.string.GPS_m_mtimes),_defm_mtimes);
		c1_delay				= _Pref.getInt(getString(R.string.GPS_c1_delay),_defm_c1delay);
		c2_delay				= _Pref.getInt(getString(R.string.GPS_c2_delay),_defm_c2delay);
		g_delay					= _Pref.getInt(getString(R.string.GPS_g_delay),_defm_gdelay);
		m_delay					= _Pref.getInt(getString(R.string.GPS_m_delay),_defm_mdelay);
		Filename				= _Pref.getString(getString(R.string.file_name),_defm_filemane);
		
		// 時間設定単位がsecなので、msecに変換する 
		m_interval				= m_interval	* 1000;
		m_timeout				= m_timeout		* 1000;
		c1_delay				= c1_delay		* 1000;
		c2_delay				= c2_delay		* 1000;
		g_delay					= g_delay		* 1000;
		m_delay					= m_delay		* 1000;
		
		// GPS測位設定値の取得(機能選択関連)
		Log_Flg					= _Pref.getInt(getString(R.string.GPS_Log_Flg), 1);
		gps_Flg					= _Pref.getInt(getString(R.string.gps_Flg), 1);
		net_Flg					= _Pref.getInt(getString(R.string.net_Flg), 0);
		passive_Flg				= _Pref.getInt(getString(R.string.passive_Flg), 0);
		GPS_Cold_Start_Qual_Flg	= _Pref.getInt(getString(R.string.GPS_Cold_Start_Qual_Flg), 0);
		GPS_Cold_Start_Other_Flg= _Pref.getInt(getString(R.string.GPS_Cold_Start_Other_Flg), 0);
		GPS_OneXTRA_Timer_Flg	= _Pref.getInt(getString(R.string.GPS_OneXTRA_Timer_Flg), 0);
		GPS_OneXTRA_Data_Flg	= _Pref.getInt(getString(R.string.GPS_OneXTRA_Data_Flg), 0);
		GPS_Mode_Flg			= _Pref.getInt(getString(R.string.GPS_Mode_Flg), 0);
		MAP_Flg					= _Pref.getInt(getString(R.string.GPS_MAP_Flg), 0);
		
		ONbtnPushTime			= _Pref.getLong(getString(R.string.ONbtn_Push_time),_defonbtn_push_time);
		
		_PrefEditor.putInt(getString(R.string.IDO), 35455281);
		_PrefEditor.putInt(getString(R.string.KEIDO), 139629711);
		_PrefEditor.putLong(getString(R.string.success_num), 0);
		_PrefEditor.putLong(getString(R.string.total_num), 0);
		_PrefEditor.putString(getString(R.string.timer_state), "None");

		// SheredPreferenceへコミット
		_PrefEditor.commit();

		//m_Start();

		// 測位間隔タイマーの初期化処理
		if (rTimer != null)
		{
			rTimer.cancel();
			rTimer = null;
		}
		
		// 測位間隔タイマーの設定
		if ((rTimer == null) && (m_interval != 0))
		{
			intervaltimertask = new IntervalTimerTask();
			rTimer = new Timer(true);
				
			int rTimes = m_interval + m_delay;
				
			if (gps_Flg == 1) 					// GPSプロバイダが選択された
			{
				if(GPS_Cold_Start_Qual_Flg == 1)// コールドスタート(Qualcomチップ用)ON？
				{	
					rTimes += c1_delay;
					rTimes += c2_delay;
				}
				else if(GPS_Cold_Start_Other_Flg == 1)// コールドスタート(その他チップ用)ON？
				{
					rTimes += c2_delay;
				}
				if ((GPS_OneXTRA_Timer_Flg == 1) || (GPS_OneXTRA_Data_Flg == 1))// XTRAデータ取得ON?
					rTimes += g_delay;
			}
			else if (net_Flg == 1) 				// ネットワークプロバイダが選択された
			{
				if(GPS_Cold_Start_Qual_Flg == 1)// コールドスタート(Qualcomチップ用)ON？
				{	
					rTimes += c1_delay;
					rTimes += c2_delay;
				}
				else if(GPS_Cold_Start_Other_Flg == 1)// コールドスタート(その他チップ用)ON？
				{
					rTimes += c2_delay;
				}
			}
	
			rTimer.schedule(intervaltimertask, 10, rTimes);
		}
		else
		{
			m_Start();
		}
	}

	public void m_Start()
	{

		// 測位開始時刻2を取得
		if (Startcalendar2 == null)
			Startcalendar2 = Calendar.getInstance();

		if (lm != null)
		{
			lm.removeUpdates(this);
			lm = null;
		}

		// ローケーション取得条件の設定
		if (gps_Flg == 1) // GPSプロバイダが選択された ※めんどいから遅延時間はSleepで。直しはいずれ。
		{
			lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			if(GPS_Cold_Start_Qual_Flg == 1)// コールドスタート(Qualcomチップ用)ON？
			{
				EXTRA_Time_Flg = lm.sendExtraCommand(LocationManager.GPS_PROVIDER,"force_time_injection",null);

				// アシストデータ削除用XTRA時刻情報取得時間
				try{
			    	Thread.sleep(c1_delay);
		    	}catch(InterruptedException e){
			    }
		    	
		    	lm.sendExtraCommand(LocationManager.GPS_PROVIDER,"delete_aiding_data",null);

		    	// アシストデータ削除時間
				try{
			    	Thread.sleep(c2_delay);
		    	}catch(InterruptedException e){
			    }
			}
			else if(GPS_Cold_Start_Other_Flg == 1)// コールドスタート(その他チップ用)ON？
			{
		    	lm.sendExtraCommand(LocationManager.GPS_PROVIDER,"delete_aiding_data",null);

		    	// アシストデータ削除時間
				try{
			    	Thread.sleep(c2_delay);
		    	}catch(InterruptedException e){
			    }
			}
		    
			if (GPS_OneXTRA_Timer_Flg == 1)	// GPSOneXTRA時間情報強制取得
			{
				EXTRA_Time_Flg = lm.sendExtraCommand(LocationManager.GPS_PROVIDER,"force_time_injection",bundle);
				if (EXTRA_Time_Flg)
					Toast.makeText(PublicContext, "GPSoneXTRA時間情報取得成功", Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(PublicContext, "GPSoneXTRA時間情報取得失敗", Toast.LENGTH_SHORT).show();
			}
			
			if (GPS_OneXTRA_Data_Flg == 1)	// GPSOneXTRA XTRAデータ強制取得
			{
				EXTRA_Data_Flg = lm.sendExtraCommand(LocationManager.GPS_PROVIDER,"force_xtra_injection",bundle);
				if (EXTRA_Data_Flg)
					Toast.makeText(PublicContext, "GPSoneXTRAデータ取得成功", Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(PublicContext, "GPSoneXTRAデータ取得失敗", Toast.LENGTH_SHORT).show();
			}
			
			// XTRAデータ取得がONなら取得遅延時間発生
			if ((GPS_OneXTRA_Timer_Flg == 1) || (GPS_OneXTRA_Data_Flg == 1))
			{
				// XTRAデータ取得時間
				try{
			    	Thread.sleep(g_delay);
		    	}catch(InterruptedException e){
			    }
			}

			// 測位開始遅延時間
		    try{
		    	Thread.sleep(m_delay);
	    	}catch(InterruptedException e){
		    }
	    	
			lm.requestLocationUpdates(lm.GPS_PROVIDER, 0, 0, this);
		}
		else if (net_Flg == 1) // ネットワークプロバイダが選択された
		{
			lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			if(GPS_Cold_Start_Qual_Flg == 1)// コールドスタート(Qualcomチップ用)ON？
			{
				lm.sendExtraCommand(LocationManager.GPS_PROVIDER,"force_time_injection",null);

				// アシストデータ削除用XTRA時刻情報取得時間
				try{
			    	Thread.sleep(c1_delay);
		    	}catch(InterruptedException e){
			    }
		    	
		    	lm.sendExtraCommand(LocationManager.GPS_PROVIDER,"delete_aiding_data",null);

		    	// アシストデータ削除時間
				try{
			    	Thread.sleep(c2_delay);
		    	}catch(InterruptedException e){
			    }
			}
			else if(GPS_Cold_Start_Other_Flg == 1)// コールドスタート(その他チップ用)ON？
			{
		    	lm.sendExtraCommand(LocationManager.GPS_PROVIDER,"delete_aiding_data",null);

		    	// アシストデータ削除時間
				try{
			    	Thread.sleep(c2_delay);
		    	}catch(InterruptedException e){
			    }
			}
			
		    // 測位開始遅延時間
		    try{
		    	Thread.sleep(m_delay);
	    	}catch(InterruptedException e){
		    }

			lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		}
		else if (passive_Flg == 1) // Passiveプロバイダが選択された
		{
			lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

			// 測位開始遅延時間
		    try{
		    	Thread.sleep(m_delay);
	    	}catch(InterruptedException e){
		    }
	    	lm.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
		}

		
		// Timer の設定をする
		// 停止タイマーの初期化処理
		//if (mTimer == null)
		//{
		//	stoptimerTask = new StopTimerTask();
		//	mTimer = new Timer(true);
		//	mTimer.schedule(stoptimerTask, m_timeout);
		//}

		// 測位開始時刻1を取得
		if (Startcalendar1 == null)
			Startcalendar1 = Calendar.getInstance();

	}

	public void m_Stop()
	{
		if (lm != null)
		{
			lm.removeUpdates(this);
			lm = null;
		}
		
		if (rTimer != null)
		{
			rTimer.cancel();
			rTimer = null;
		}
		
		// 防止アラームマネージャ停止
		if (alarmManagerFLG)
		{
			alarmManagerFLG = false;
			alarmManager.cancel(pi);
		}

		// CPUロック解放
		if (CPULockFlg)
		{
			CPULockFlg = false;
			wakelock.release();
		}
	}

	@Override
	public void onLocationChanged(Location location)
	{
		// 測位終了時刻を取得
		if (Endcalendar == null)
			Endcalendar = Calendar.getInstance();

		// 測位開始時刻2を取得
		if (Startcalendar2 == null)
			Startcalendar2 = Calendar.getInstance();

		// ローケーション取得条件の設定
		//if (gps_Flg == 1) // GPSプロバイダが選択された
		//{
		//	lm.requestLocationUpdates(lm.GPS_PROVIDER, 0, 0, this);
		//}
		//else if (net_Flg == 1) // ネットワークプロバイダが選択された
		//{
		//	lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		//}
		//else if (passive_Flg == 1) // Passiveプロバイダが選択された
		//{
	    //	lm.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
		//}

		// 測位開始時刻1を取得
		if (Startcalendar1 == null)
			Startcalendar1 = Calendar.getInstance();

		++measure_num;
		++success_times;

		/*
		 * String writeString = "Success" + "," +
		 * String.valueOf(Double.toString(location.getLatitude())) // 緯度 +
		 * "," + String.valueOf(Double.toString(location.getLongitude())) //
		 * 経度 + "," +
		 * String.valueOf(Double.toString(location.getAltitude())) // 高度 +
		 * "," + String.valueOf(Double.toString(location.getAccuracy())); //
		 * 誤差
		 */

		String[] writeString = new String[24];

		writeString[0] = "";		// 測位終了時刻
		writeString[1] = "1";		// 成功フラグ (1:成功、0:失敗)
		writeString[2] = String.valueOf(Double.toString(location
				.getLatitude()));	// 緯度
		writeString[3] = String.valueOf(Double.toString(location
				.getLongitude()));	// 経度
		writeString[4] = String.valueOf(Double.toString(location
				.getAccuracy()));	// 測位誤差
		writeString[5] = "";		// 測位時間1
		writeString[6] = "";		// 測位時間2
		writeString[7] = "";		// 測位種別 ("G":GPSプロバイダー、"N":NetWorkプロバイダー、"P":Passiveプロバイダー)
		writeString[8] = "";		// 測位No
		writeString[9] = "";		// 測位間隔設定値
		writeString[10] = "";		// 測位タイムアウト設定値
		writeString[11] = "";		// 測位回数設定値
		writeString[12] = "";		// アシストデータ削除(XTRA時刻情報取得)時間
		writeString[13] = "";		// アシストデータ削除時間
		writeString[14] = "";		// XTRAデータ取得時間
		writeString[15] = "";		// 測位開始遅延時間
		writeString[16] = "";		// 測位種別 ("G":GPSプロバイダー、"N":NetWorkプロバイダー、"P":Passiveプロバイダー)
		writeString[17] = "";		// ログ保存					選択設定フラグ
		writeString[18] = "";		// コールドスタート(Qualcomチップ用)選択設定フラグ
		writeString[19] = "";		// コールドスタート(その他チップ用)	選択設定フラグ
		writeString[20] = "";		// GPSoneXTRA時間情報強制取得選択設定フラグ
		writeString[21] = "";		// GPSoneXTRAデータ強制取得	選択設定フラグ
		writeString[22] = "";		// GPS測位モード設定フラグ
		writeString[23] = "";		// GoogleMAP表示フラグ

		String strProvider = location.getProvider();
		if (Log_Flg == 1)
		{
			try
			{
				if (strProvider.equals("gps")) // GPSプロバイダが選択された
					save("GPS_P", writeString);
				else if (strProvider.equals("network")) // ネットワークプロバイダが選択された
					save("NET_P", writeString);
				else if (strProvider.equals("passive")) // Passiveプロバイダが選択された
					save("PASS_P", writeString);
			}
			catch (IOException e)
			{
			
			}
		}

		_PrefEditor.putInt(getString(R.string.IDO), (int) (location
				.getLatitude() * 1E6));
		_PrefEditor.putInt(getString(R.string.KEIDO), (int) (location
				.getLongitude() * 1E6));
		_PrefEditor.putLong(getString(R.string.success_num), success_times);
		_PrefEditor.putLong(getString(R.string.total_num), measure_num);

		// 測位回数指定の場合の測位回数到達チェック
		if ((m_mtimes > 0) && (m_mtimes == measure_num))
		{
			m_Stop();
			_PrefEditor.putString(getString(R.string.timer_state),
					"m_finish&num_over");
		}
		else
			_PrefEditor.putString(getString(R.string.timer_state),
					"m_finish");

		_PrefEditor.commit();
		sendBroadcast(new Intent(Timer));
		Startcalendar1 = null;
		Startcalendar2 = null;
		Endcalendar = null;

		Startcalendar1 = Calendar.getInstance();
		Startcalendar2 = Calendar.getInstance();

		if (passive_Flg == 1) // Passiveプロバイダが選択された
		{
			Toast.makeText(PublicContext, "測位データを取得", Toast.LENGTH_SHORT).show();
		}
	}

	// 測位間隔タイマークラス
	class IntervalTimerTask extends TimerTask
	{
		@Override
		public void run()
		{
			rHandler.post(new Runnable()
			{
				public void run()
				{
					if(start_Flg == 0)
					{
						start_Flg = 1;
						m_Start();
					}
					else
					{
						m_Stop();

						_Pref = getSharedPreferences(getString(R.string.PREF_KEY),
								ListActivity.MODE_PRIVATE);
						_PrefEditor = _Pref.edit();
						_PrefEditor.putString(getString(R.string.timer_state),
								"m_finish&num_over");
						_PrefEditor.commit();
						sendBroadcast(new Intent(Timer));
					}
				}
			});
		}
	}

	
		// ファイル書き込み処理
	/*
	 * public void save(String FileSort,String WriteDate) throws IOException {
	 * 
	 * sPath = "/sdcard/" + this.getPackageName(); String FName = sPath + "/" +
	 * Filename + "_" + FileSort + ".txt";
	 * 
	 * File files = new File(FName); files.getParentFile().mkdir();
	 * 
	 * Calendar calendar = Calendar.getInstance(); String writeString =
	 * String.valueOf(calendar.get(Calendar.YEAR)) + "/" +
	 * String.valueOf(calendar.get(Calendar.MONTH)) + "/" +
	 * String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)) + "/" +
	 * String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)) + ":" +
	 * String.valueOf(calendar.get(Calendar.MINUTE)) + ":" +
	 * String.valueOf(calendar.get(Calendar.SECOND)) + "." +
	 * String.valueOf(calendar.get(Calendar.MILLISECOND)) + "," + WriteDate +
	 * "\n\r";
	 * 
	 * FileOutputStream fos = new FileOutputStream(files, true);
	 * OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
	 * BufferedWriter bw = new BufferedWriter(osw);
	 * 
	 * bw.write(writeString); bw.flush(); bw.close(); }
	 */
	public void save(String FileSort, String[] WriteDate) throws IOException
	{

		// 測位終了時刻
		Calendar calendar = Calendar.getInstance();
		String strBuff = String.valueOf(calendar.get(Calendar.YEAR))	+ "/"
				+ String.valueOf(calendar.get(Calendar.MONTH) + 1)		+ "/"
				+ String.valueOf(calendar.get(Calendar.DAY_OF_MONTH))	+ "/"
				+ String.valueOf(calendar.get(Calendar.HOUR_OF_DAY))	+ ":"
				+ String.valueOf(calendar.get(Calendar.MINUTE))			+ ":"
				+ String.valueOf(calendar.get(Calendar.SECOND))			+ "."
				+ String.valueOf(calendar.get(Calendar.MILLISECOND));

		WriteDate[0] = strBuff;

		// 測位時間差1
		long diffTime1 = Endcalendar.getTimeInMillis() - Startcalendar1.getTimeInMillis();
		
		// ミリ秒
		long msecond = diffTime1 % 1000;
		// 秒
		long second = (diffTime1 / 1000) % 60;
		// 分
		long minute = (diffTime1 / 1000) / 60;

		WriteDate[5] = String.valueOf(minute * 60 + second) + "." + String.valueOf(msecond);

		// 測位時間差2
		long diffTime2;
		if(FirstMeasureFLG)
		{
			diffTime2 = Endcalendar.getTimeInMillis() - ONbtnPushTime;
			FirstMeasureFLG = false;
		}
		else
			diffTime2 = Endcalendar.getTimeInMillis() - Startcalendar2.getTimeInMillis();

		// ミリ秒
		msecond = diffTime2 % 1000;
		// 秒
		second = (diffTime2 / 1000) % 60;
		// 分
		minute = (diffTime2 / 1000) / 60;

		WriteDate[6] = String.valueOf(minute * 60 + second) + "." + String.valueOf(msecond);

		// 測位種別("G":GPSプロバイダー、"N":NetWorkプロバイダー、"P":Passiveプロバイダー)
		if (FileSort.equals("GPS_P"))
		{
			WriteDate[7] = "G";
		}
		else if (FileSort.equals("NET_P"))
		{
			WriteDate[7] = "N";
		}
		else if (FileSort.equals("PASS_P"))
		{
			WriteDate[7] = "P";
		}

		// 最初の測位成功時であれば、累計回数を+1する
		if (!First_m_Flg)
		{
			First_m_Flg = true;
			++_defm_total_num;
			_PrefEditor.putString(getString(R.string.GPS_m_no), String
					.valueOf(_defm_total_num));
			_PrefEditor.commit();
		}
		WriteDate[8] = String.valueOf(_defm_total_num);

		
		// 測位間隔設定値
		WriteDate[9] = String.valueOf(m_interval/1000);	
		
		// 測位タイムアウト設定値
		WriteDate[10] = String.valueOf(m_timeout/1000);	
		
		// 測位回数設定値
		WriteDate[11] = String.valueOf(m_mtimes);	
		
		// アシストデータ削除(XTRA時刻情報取得)時間
		WriteDate[12] = String.valueOf(c1_delay/1000);	
		
		// アシストデータ削除時間
		WriteDate[13] = String.valueOf(c2_delay/1000);	
		
		// XTRAデータ取得時間
		WriteDate[14] = String.valueOf(g_delay/1000);	
		
		// 測位開始遅延時間
		WriteDate[15] = String.valueOf(m_delay/1000);	
		
		// 測位種別 ("G":GPSプロバイダー、"N":NetWorkプロバイダー)
		WriteDate[16] = WriteDate[7];	
		
		// ログ保存	選択設定フラグ
		if(Log_Flg == 1)
			WriteDate[17] = "ON";	
		else
			WriteDate[17] = "OFF";
		
		// コールドスタート(Qualcomチップ用)選択設定フラグ
		if(GPS_Cold_Start_Qual_Flg == 1)
			WriteDate[18] = "ON";	
		else
			WriteDate[18] = "OFF";
		
		// コールドスタート(その他チップ用)選択設定フラグ
		if(GPS_Cold_Start_Other_Flg == 1)
			WriteDate[19] = "ON";	
		else
			WriteDate[19] = "OFF";

		// GPSoneXTRA時間情報強制取得選択設定フラグ
		if(GPS_OneXTRA_Timer_Flg == 1)
			WriteDate[20] = "ON";	
		else
			WriteDate[20] = "OFF";
		
		// GPSoneXTRAデータ強制取得	選択設定フラグ
		if(GPS_OneXTRA_Data_Flg == 1)
			WriteDate[21] = "ON";	
		else
			WriteDate[21] = "OFF";
		
		// GPS測位モード設定フラグ
		if(GPS_Mode_Flg == 0)
			WriteDate[22] = "モードA";	
		else if(GPS_Mode_Flg == 1)
			WriteDate[22] = "モードB";
		else if(GPS_Mode_Flg == 2)
			WriteDate[22] = "モードC";
		else if(GPS_Mode_Flg == 3)
			WriteDate[22] = "モードD";
		
		// GoogleMAP表示フラグ
		if(MAP_Flg == 1)
			WriteDate[23] = "ON";	
		else
			WriteDate[23] = "OFF";

		
		ContentValues values = new ContentValues();

		values.put("time",						WriteDate[0]);		// 測位終了時刻
		values.put("success",		Integer.parseInt(WriteDate[1]));// 成功フラグ
																	// (1:成功、0:失敗)
		values.put("IDO",			Float.parseFloat(WriteDate[2]));// 緯度
		values.put("KEIDO",			Float.parseFloat(WriteDate[3]));// 経度
		values.put("diiference",	Float.parseFloat(WriteDate[4]));// 測位誤差
		values.put("M_time1", 					WriteDate[5]);		// 測位時間1
		values.put("M_time2", 					WriteDate[6]);		// 測位時間2
		values.put("Sort", 						WriteDate[7]);		// 測位種別
																	// ("G":GPSプロバイダー、"N":NetWorkプロバイダー、"P":Passiveプロバイダー)
		values.put("M_no", Integer.parseInt(WriteDate[8]));			// 測位No

		values.put("M_interval",				WriteDate[9]);		// 測位間隔設定値
		values.put("M_timeout",					WriteDate[10]);		// 測位タイムアウト設定値
		values.put("M_mtimes",					WriteDate[11]);		// 測位回数設定値
		values.put("C1_delay",					WriteDate[12]);		// アシストデータ削除(XTRA時刻情報取得)時間
		values.put("C2_delay",					WriteDate[13]);		// アシストデータ削除時間
		values.put("G_delay",					WriteDate[14]);		// XTRAデータ取得時間
		values.put("M_delay",					WriteDate[15]);		// 測位開始遅延時間
		values.put("M_Sort",					WriteDate[16]);		// 測位種別
		values.put("Log_Flg",					WriteDate[17]);		// ログ保存	選択設定フラグ
		values.put("GPS_Cold_Start_Qual_Flg",	WriteDate[18]);		// コールドスタート(Qualcomチップ用)選択設定フラグ
		values.put("GPS_Cold_Start_Other_Flg",	WriteDate[19]);		// コールドスタート(その他チップ用)選択設定フラグ
		values.put("GPS_OneXTRA_Timer_Flg",		WriteDate[20]);		// GPSoneXTRA時間情報強制取得選択設定フラグ
		values.put("GPS_OneXTRA_Data_Flg",		WriteDate[21]);		// GPSoneXTRAデータ強制取得	選択設定フラグ
		values.put("GPS_Mode_Flg",				WriteDate[22]);		// GPS測位モード設定フラグ
		values.put("MAP_Flg",					WriteDate[23]);		// GoogleMAP表示フラグ
		
		cnt_DB.Write_DB(values);

	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		
		if (lm != null)
		{
			lm.removeUpdates(this);
			lm = null;
		}
		
		if (mTimer != null)
		{
			mTimer.cancel();
			mTimer = null;
		}
		
		if (rTimer != null)
		{
			rTimer.cancel();
			rTimer = null;
		}

		// 防止アラームマネージャ停止
		if (alarmManagerFLG)
		{
			alarmManagerFLG = false;
			alarmManager.cancel(pi);
		}

		// CPUロック解放
		if (CPULockFlg)
		{
			CPULockFlg = false;
			wakelock.release();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new GPS_Service_ModeD_Binder();
	}

	@Override
	public void onRebind(Intent intent) {
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return true; // 再度クライアントから接続された際に onRebind を呼び出させる場合は true を返す
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

}