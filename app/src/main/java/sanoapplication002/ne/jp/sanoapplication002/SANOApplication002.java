package sanoapplication002.ne.jp.sanoapplication002;

import android.app.ActivityManager;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;

public class SANOApplication002 extends AppCompatActivity implements OnClickListener{

    private ToggleButton tbtn_on;
    private ToggleButton tbtn_off;
    private TextView tx_state;
    private TextView tx_setting;
    private TextView tx_Accele_state;
    private TextView tx_IDO;
    private TextView tx_KEIDO;
    private TextView tx_NUM;
    private Timer mTimer = null;	// 停止タイマ
    private Handler mHandler;		// 停止タイマハンドラー
    private Timer rTimer = null;	// 再開始タイマ
    private Handler rHandler;		// 再開始ハンドラー
    private int success_Flg = 0;
    private int timout_Flg = 0;

    private final Calendar calendar = Calendar.getInstance();
    private final int year		= calendar.get(Calendar.YEAR);
    private final int month	= calendar.get(Calendar.MONTH);
    private final int day		= calendar.get(Calendar.DAY_OF_MONTH);
    private final int hour		= calendar.get(Calendar.HOUR_OF_DAY);
    private final int minute	= calendar.get(Calendar.MINUTE);
    private final int second	= calendar.get(Calendar.SECOND);
    private final int ms		= calendar.get(Calendar.MILLISECOND);
    private int m_interval	= 0;
    private int m_timeout	= 0;
    private long m_mtimes	= 0;
    private int c1_delay	= 0;
    private int c2_delay	= 0;
    private int g_delay	= 0;
    private int m_delay	= 0;
    private String Filename;
    private String sPath;
    private long measure_num;
    private long success_times;
    private int gps_Flg;
    private int net_Flg;
    private int passive_Flg;
    private int Log_Flg = 1;
    private int GPS_OneXTRA_Timer_Flg	= 0;
    private int GPS_OneXTRA_Data_Flg	= 0;
    private int Cold_Start_Qual_Flg	= 0;
    private int Cold_Start_Other_Flg	= 0;
    private int MAP_Flg		= 0;
    private int Accele_Log_Flg	= 1;
    private int GPS_Use_Flg	= 1;
    private int Accele_Use_Flg	= 1;
    private int GPS_Mode_Flg	= 0;
    private double IDO;
    private double KEIDO;
    private long ONbtnPushTime= 0;							// ボタン押下時の時間

    private SharedPreferences _Pref; 						// 設定データ本体用インスタンス
    private SharedPreferences.Editor _PrefEditor; 			// 設定データ書き換え用インスタンス

    private static final int MENU1_ID = (Menu.FIRST + 1); // メニューID
    private static final int MENU2_ID = (Menu.FIRST + 2); // メニューID

    private Intent intent_A;
    private Intent intent_B;
    private Intent intent_C;
    private Intent intent_D;

    private boolean visible = true;

    private Calendar ONbtnPushcalendar			= null;		// ボタン押下時の時間


    public static int _GPS_defm_interval		= 30;		// GPS測位間隔時間(sec）
    public static int _GPS_defm_timeout			= 25;		// GPSタイムアウト時間(sec)
    public static long _GPS_defm_mtimes			= 0;		// GPS測位回数
    public static int _GPS_defc1_delay			= 0;		// GPS測位アシストデータ削除用XTRA時刻情報取得時間(sec)
    public static int _GPS_defc2_delay			= 0;		// GPS測位アシストデータ削除時間(sec)
    public static int _GPS_defg_delay			= 0;		// GPS測位XTRAデータ取得時間(sec)
    public static int _GPS_defm_delay			= 0;		// GPS測位開始遅延時間(sec)
    public static int _Accele_defm_interval		= 30;		// 加速度センサ測位間隔時間(sec）
    public static int _Accele_defm_timer		= 5;		// 加速度センサ測位時間(sec)
    public static int _Accele_defm_mtimes		= 0;		// 加速度センサ測位回数
    public static String _defm_filemane			= "GPSLOG"; // ファイル名
    public static String _DB_defm_no			= "0";		// 測位累計
    public static int _GPS_defm_MAP				= 0;		// GoogleMAP表示フラグ
    public static long _defonbtn_push_time		= 0;		// ONボタン押下時の時間

    public static final int SETTING_MENU = 0;
    /************************************************************************/
	/* サービスからのイベント受信処理 */
    /************************************************************************/

    class GPS_ServiceReceiver_ModeA extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            _Pref = getSharedPreferences(getString(R.string.PREF_KEY),
                    ListActivity.MODE_PRIVATE);
            String str_buff = _Pref.getString(getString(R.string.timer_state),
                    "None");

            _GPS_defm_MAP = _Pref.getInt(getString(R.string.GPS_MAP_Flg), _GPS_defm_MAP);

            // 測位終了 or 測位終了+測位回数オーバー
            if (str_buff.equals("m_finish")
                    || str_buff.equals("m_finish&num_over")) {

                measure_num = _Pref.getLong(getString(R.string.total_num), 0);
                success_times = _Pref
                        .getLong(getString(R.string.success_num), 0);
                IDO = (_Pref.getInt(getString(R.string.IDO), 0)) / 1E6;
                KEIDO = (_Pref.getInt(getString(R.string.KEIDO), 0)) / 1E6;

                tx_IDO.setText("●緯度：  " + Double.toString(IDO));
                tx_KEIDO.setText("●経度：  " + Double.toString(KEIDO));

                if (m_mtimes > 0) // 回数指定あり
                {
                    tx_NUM.setText("●測位回数：" + String.valueOf(measure_num)
                            + " / " + String.valueOf(m_mtimes) + "(成功回数： "
                            + String.valueOf(success_times) + ")");
                }

                else // 回数指定なし
                {
                    tx_NUM.setText("●測位回数：" + String.valueOf(measure_num)
                            + " / ∞" + "(成功回数： "
                            + String.valueOf(success_times) + ")");
                }

                // 測位回数指定の場合の測位回数到達チェック
                if (str_buff.equals("m_finish&num_over"))
                    tx_state.setText("　停止中");
                else
                    tx_state.setText("　一時停止中");

            }
            // タイムアウト or タイムアウト+測位回数オーバー
            else if (str_buff.equals("timeout")
                    || str_buff.equals("timeout&num_over")) {

                tx_IDO.setText("●緯度：  タイムアウト");
                tx_KEIDO.setText("●経度：  タイムアウト");
                measure_num = _Pref.getLong(getString(R.string.total_num), 0);
                success_times = _Pref
                        .getLong(getString(R.string.success_num), 0);

                if (m_mtimes > 0) // 回数指定あり
                {
                    tx_NUM.setText("●測位回数：" + String.valueOf(measure_num)
                            + " / " + String.valueOf(m_mtimes) + "(成功回数： "
                            + String.valueOf(success_times) + ")");
                }

                else // 回数指定なし
                {
                    tx_NUM.setText("●測位回数：" + String.valueOf(measure_num)
                            + " / ∞" + "(成功回数： "
                            + String.valueOf(success_times) + ")");
                }

                // 測位回数指定の場合の測位回数到達チェック
                if (str_buff.equals("timeout&num_over"))
                    tx_state.setText("　停止中");
                else
                    tx_state.setText("　一時停止中");

            }
            // 測位再開
            else if (str_buff.equals("start") || str_buff.equals("Re_start")) {
                tx_state.setText("　測位中");
            }

        }
    }

    private GPS_Service_ModeA GPS_Service_ModeA;
    private final GPS_ServiceReceiver_ModeA receiver = new GPS_ServiceReceiver_ModeA();

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            GPS_Service_ModeA = ((GPS_Service_ModeA.GPS_Service_ModeA_Binder) service)
                    .getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            GPS_Service_ModeA = null;
        }

    };

    class GPS_ServiceReceiver_ModeB extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            _Pref = getSharedPreferences(getString(R.string.PREF_KEY),
                    ListActivity.MODE_PRIVATE);
            String str_buff = _Pref.getString(getString(R.string.timer_state),
                    "None");

            _GPS_defm_MAP = _Pref.getInt(getString(R.string.GPS_MAP_Flg), _GPS_defm_MAP);

            // 測位終了 or 測位終了+測位回数オーバー
            if (str_buff.equals("m_finish")
                    || str_buff.equals("m_finish&num_over")) {

                measure_num = _Pref.getLong(getString(R.string.total_num), 0);
                success_times = _Pref
                        .getLong(getString(R.string.success_num), 0);
                IDO = (_Pref.getInt(getString(R.string.IDO), 0)) / 1E6;
                KEIDO = (_Pref.getInt(getString(R.string.KEIDO), 0)) / 1E6;

                tx_IDO.setText("●緯度：  " + Double.toString(IDO));
                tx_KEIDO.setText("●経度：  " + Double.toString(KEIDO));

                if (m_mtimes > 0) // 回数指定あり
                {
                    tx_NUM.setText("●測位回数：" + String.valueOf(measure_num)
                            + " / " + String.valueOf(m_mtimes) + "(成功回数： "
                            + String.valueOf(success_times) + ")");
                }

                else // 回数指定なし
                {
                    tx_NUM.setText("●測位回数：" + String.valueOf(measure_num)
                            + " / ∞" + "(成功回数： "
                            + String.valueOf(success_times) + ")");
                }

                // 測位回数指定の場合の測位回数到達チェック
                if (str_buff.equals("m_finish&num_over"))
                    tx_state.setText("　停止中");
                else
                    tx_state.setText("　一時停止中");

            }
            // タイムアウト or タイムアウト+測位回数オーバー
            else if (str_buff.equals("timeout")
                    || str_buff.equals("timeout&num_over")) {

                tx_IDO.setText("●緯度：  タイムアウト");
                tx_KEIDO.setText("●経度：  タイムアウト");
                measure_num = _Pref.getLong(getString(R.string.total_num), 0);
                success_times = _Pref
                        .getLong(getString(R.string.success_num), 0);

                if (m_mtimes > 0) // 回数指定あり
                {
                    tx_NUM.setText("●測位回数：" + String.valueOf(measure_num)
                            + " / " + String.valueOf(m_mtimes) + "(成功回数： "
                            + String.valueOf(success_times) + ")");
                }

                else // 回数指定なし
                {
                    tx_NUM.setText("●測位回数：" + String.valueOf(measure_num)
                            + " / ∞" + "(成功回数： "
                            + String.valueOf(success_times) + ")");
                }

                // 測位回数指定の場合の測位回数到達チェック
                if (str_buff.equals("timeout&num_over"))
                    tx_state.setText("　停止中");
                else
                    tx_state.setText("　一時停止中");

            }
            // 測位再開
            else if (str_buff.equals("start") || str_buff.equals("Re_start")) {
                tx_state.setText("　測位中");
            }

        }
    }

    private GPS_Service_ModeB GPS_Service_ModeB;
    private final GPS_ServiceReceiver_ModeB receiver_ModeB = new GPS_ServiceReceiver_ModeB();

    private ServiceConnection serviceConnection_ModeB = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            GPS_Service_ModeB = ((GPS_Service_ModeB.GPS_Service_ModeB_Binder) service)
                    .getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            GPS_Service_ModeB = null;
        }

    };

    class GPS_ServiceReceiver_ModeC extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            _Pref = getSharedPreferences(getString(R.string.PREF_KEY),
                    ListActivity.MODE_PRIVATE);
            String str_buff = _Pref.getString(getString(R.string.timer_state),
                    "None");

            _GPS_defm_MAP = _Pref.getInt(getString(R.string.GPS_MAP_Flg), _GPS_defm_MAP);

            // 測位終了 or 測位終了+測位回数オーバー
            if (str_buff.equals("m_finish")
                    || str_buff.equals("m_finish&num_over")) {

                measure_num = _Pref.getLong(getString(R.string.total_num), 0);
                success_times = _Pref
                        .getLong(getString(R.string.success_num), 0);
                IDO = (_Pref.getInt(getString(R.string.IDO), 0)) / 1E6;
                KEIDO = (_Pref.getInt(getString(R.string.KEIDO), 0)) / 1E6;

                tx_IDO.setText("●緯度：  " + Double.toString(IDO));
                tx_KEIDO.setText("●経度：  " + Double.toString(KEIDO));

                tx_NUM.setText("●測位回数：Single Shot");

                // 測位回数指定の場合の測位回数到達チェック
                if (str_buff.equals("m_finish&num_over"))
                    tx_state.setText("　停止中");
                else
                    tx_state.setText("　一時停止中");

            }
            // タイムアウト or タイムアウト+測位回数オーバー
            else if (str_buff.equals("timeout")
                    || str_buff.equals("timeout&num_over")) {

                tx_IDO.setText("●緯度：  タイムアウト");
                tx_KEIDO.setText("●経度：  タイムアウト");
                measure_num = _Pref.getLong(getString(R.string.total_num), 0);
                success_times = _Pref
                        .getLong(getString(R.string.success_num), 0);

                tx_NUM.setText("●測位回数：Single Shot");

                // 測位回数指定の場合の測位回数到達チェック
                if (str_buff.equals("timeout&num_over"))
                    tx_state.setText("　停止中");
                else
                    tx_state.setText("　一時停止中");

            }
            // 測位再開
            else if (str_buff.equals("start") || str_buff.equals("Re_start")) {
                tx_state.setText("　測位中");
            }

        }
    }

    private GPS_Service_ModeC GPS_Service_ModeC;
    private final GPS_ServiceReceiver_ModeC receiver_ModeC = new GPS_ServiceReceiver_ModeC();

    private ServiceConnection serviceConnection_ModeC = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            GPS_Service_ModeC = ((GPS_Service_ModeC.GPS_Service_ModeC_Binder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            GPS_Service_ModeC = null;
        }

    };

    class GPS_ServiceReceiver_ModeD extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            _Pref = getSharedPreferences(getString(R.string.PREF_KEY),
                    ListActivity.MODE_PRIVATE);
            String str_buff = _Pref.getString(getString(R.string.timer_state),
                    "None");

            _GPS_defm_MAP = _Pref.getInt(getString(R.string.GPS_MAP_Flg), _GPS_defm_MAP);

            // 測位終了 or 測位終了+測位回数オーバー
            if (str_buff.equals("m_finish")
                    || str_buff.equals("m_finish&num_over")) {

                measure_num = _Pref.getLong(getString(R.string.total_num), 0);
                success_times = _Pref
                        .getLong(getString(R.string.success_num), 0);

                if (m_mtimes > 0) // 回数指定あり
                {
                    tx_NUM.setText("●測位回数：" + String.valueOf(measure_num)
                            + " / " + String.valueOf(m_mtimes) + "(成功回数： "
                            + String.valueOf(success_times) + ")");
                }
                else // 回数指定なし
                {
                    tx_NUM.setText("●測位回数：" + String.valueOf(measure_num)
                            + " / ∞" + "(成功回数： "
                            + String.valueOf(success_times) + ")");
                }

                // 測位回数指定の場合の測位回数到達チェック
                if (str_buff.equals("m_finish&num_over"))
                    tx_state.setText("　停止中");
                //else
                //	tx_state.setText("　一時停止中");

                if(str_buff.equals("m_finish"))
                {
                    IDO = (_Pref.getInt(getString(R.string.IDO), 0)) / 1E6;
                    KEIDO = (_Pref.getInt(getString(R.string.KEIDO), 0)) / 1E6;

                    tx_IDO.setText("●緯度：  " + Double.toString(IDO));
                    tx_KEIDO.setText("●経度：  " + Double.toString(KEIDO));

                }

            }
            // タイムアウト or タイムアウト+測位回数オーバー
            else if (str_buff.equals("timeout")
                    || str_buff.equals("timeout&num_over")) {

                tx_IDO.setText("●緯度：  タイムアウト");
                tx_KEIDO.setText("●経度：  タイムアウト");
                measure_num = _Pref.getLong(getString(R.string.total_num), 0);
                success_times = _Pref
                        .getLong(getString(R.string.success_num), 0);

                if (m_mtimes > 0) // 回数指定あり
                {
                    tx_NUM.setText("●測位回数：" + String.valueOf(measure_num)
                            + " / " + String.valueOf(m_mtimes) + "(成功回数： "
                            + String.valueOf(success_times) + ")");
                }

                else // 回数指定なし
                {
                    tx_NUM.setText("●測位回数：" + String.valueOf(measure_num)
                            + " / ∞" + "(成功回数： "
                            + String.valueOf(success_times) + ")");
                }

                // 測位回数指定の場合の測位回数到達チェック
                if (str_buff.equals("timeout&num_over"))
                    tx_state.setText("　停止中");
                else
                    tx_state.setText("　一時停止中");

            }
            // 測位再開
            else if (str_buff.equals("start") || str_buff.equals("Re_start")) {
                tx_state.setText("　測位中");
            }

        }
    }

    private GPS_Service_ModeD GPS_Service_ModeD;
    private final GPS_ServiceReceiver_ModeD receiver_ModeD = new GPS_ServiceReceiver_ModeD();

    private ServiceConnection serviceConnection_ModeD = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            GPS_Service_ModeD = ((GPS_Service_ModeD.GPS_Service_ModeD_Binder) service)
                    .getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            GPS_Service_ModeD = null;
        }

    };

    /************************************************************************/
	/* 起動時処理 */
    /************************************************************************/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // UI関連処理
        tx_state		= (TextView) findViewById(R.id.tx_state);
        tx_setting		= (TextView) findViewById(R.id.tx_setting);
        tx_IDO			= (TextView) findViewById(R.id.tx_IDO);
        tx_KEIDO		= (TextView) findViewById(R.id.tx_KEIDO);
        tx_NUM			= (TextView) findViewById(R.id.tx_NUM);

        tbtn_on		= (ToggleButton) findViewById(R.id.tbtn_on);
        tbtn_off	= (ToggleButton) findViewById(R.id.tbtn_off);
        tbtn_on.setOnClickListener(this);
        tbtn_off.setOnClickListener(this);

        mHandler	= new Handler();
        mTimer		= null;
        rHandler	= new Handler();
        rTimer		= null;

        // パッケージ名を取得してディレクトリパス生成
        sPath = "/sdcard/" + this.getPackageName();

        File file = new File(sPath);
        try {
            if (!file.exists()) {
                // パッケージ名のディレクトリが存在しない場合新規作成
                file.mkdir();
            }
        } catch (SecurityException ex) {
        }

        _Pref = getSharedPreferences(getString(R.string.PREF_KEY),
                ListActivity.MODE_PRIVATE);
        _PrefEditor = _Pref.edit();

        // サービスインテントの生成
        intent_A		= new Intent(this, GPS_Service_ModeA.class);
        intent_B		= new Intent(this, GPS_Service_ModeB.class);
        intent_C		= new Intent(this, GPS_Service_ModeC.class);
        intent_D		= new Intent(this, GPS_Service_ModeD.class);

        String strBuff_A = this.getPackageName() + ".GPS_Service_ModeA";
        String strBuff_B = this.getPackageName() + ".GPS_Service_ModeB";
        String strBuff_C = this.getPackageName() + ".GPS_Service_ModeC";
        String strBuff_D = this.getPackageName() + ".GPS_Service_ModeD";

        _GPS_defm_MAP	= _Pref.getInt(getString(R.string.GPS_MAP_Flg), _GPS_defm_MAP);

        // 設定の表示
        Cold_Start_Qual_Flg	= _Pref.getInt(getString(R.string.GPS_Cold_Start_Qual_Flg), 0);
        Cold_Start_Other_Flg= _Pref.getInt(getString(R.string.GPS_Cold_Start_Other_Flg), 0);
        GPS_Mode_Flg		= _Pref.getInt(getString(R.string.GPS_Mode_Flg), 0);
        String strBuff4		= "";

        strBuff4 = strBuff4 + "●";

        if(GPS_Mode_Flg == 0)
            strBuff4 = strBuff4 + "測位モードA";
        else if(GPS_Mode_Flg == 1)
            strBuff4 = strBuff4 + "測位モードB";
        else if(GPS_Mode_Flg == 2)
            strBuff4 = strBuff4 + "測位モードC";
        else if(GPS_Mode_Flg == 3)
            strBuff4 = strBuff4 + "測位モードD";

        if((Cold_Start_Qual_Flg == 1) || (Cold_Start_Other_Flg == 1))
            strBuff4 = strBuff4 + "　Cold設定";

        tx_setting.setText(strBuff4);


        // サービスの起動中確認
        // サービスが起動していなかった
        if ((isServiceRunning(strBuff_A) == false)
                && (isServiceRunning(strBuff_B) == false)
                && (isServiceRunning(strBuff_C) == false)
                && (isServiceRunning(strBuff_D) == false)) {

            // ONボタン OFF
            tbtn_on.setChecked(false);
            tbtn_on.setClickable(true);

            // OFFボタン ON
            tbtn_off.setChecked(true);
            tbtn_off.setClickable(false);

            gps_Flg			= 1;
            net_Flg			= 0;
            measure_num 	= 0;
            success_times	= 0;
            success_Flg		= 0;
            timout_Flg		= 0;
            GPS_Mode_Flg	= 0;

            Filename = String.valueOf(year)		+ String.valueOf(month)
                    + String.valueOf(day)		+ String.valueOf(hour)
                    + String.valueOf(minute)	+ String.valueOf(second)
                    + String.valueOf(ms);

            // SharedPrefernces の初期化

            _GPS_defm_interval		= _Pref.getInt(getString(R.string.GPS_m_interval), _GPS_defm_interval);
            _GPS_defm_timeout		= _Pref.getInt(getString(R.string.GPS_m_timeout), _GPS_defm_timeout);
            _GPS_defm_mtimes		= _Pref.getLong(getString(R.string.GPS_m_mtimes), _GPS_defm_mtimes);
            _GPS_defc1_delay		= _Pref.getInt(getString(R.string.GPS_c1_delay), _GPS_defc1_delay);
            _GPS_defc2_delay		= _Pref.getInt(getString(R.string.GPS_c2_delay), _GPS_defc2_delay);
            _GPS_defg_delay			= _Pref.getInt(getString(R.string.GPS_g_delay), _GPS_defg_delay);
            _GPS_defm_delay			= _Pref.getInt(getString(R.string.GPS_m_delay), _GPS_defm_delay);

            Log_Flg					= _Pref.getInt(getString(R.string.GPS_Log_Flg), 1);
            gps_Flg					= _Pref.getInt(getString(R.string.gps_Flg), 1);
            net_Flg					= _Pref.getInt(getString(R.string.net_Flg), 0);
            passive_Flg				= _Pref.getInt(getString(R.string.passive_Flg), 0);
            GPS_Use_Flg				= _Pref.getInt(getString(R.string.GPS_Use_Flg), 1);
            Cold_Start_Qual_Flg		= _Pref.getInt(getString(R.string.GPS_Cold_Start_Qual_Flg), 0);
            Cold_Start_Other_Flg	= _Pref.getInt(getString(R.string.GPS_Cold_Start_Other_Flg), 0);
            GPS_OneXTRA_Timer_Flg	= _Pref.getInt(getString(R.string.GPS_OneXTRA_Timer_Flg), 0);
            GPS_OneXTRA_Data_Flg	= _Pref.getInt(getString(R.string.GPS_OneXTRA_Data_Flg), 0);
            MAP_Flg					= _Pref.getInt(getString(R.string.GPS_MAP_Flg), 0);
            GPS_Mode_Flg			= _Pref.getInt(getString(R.string.GPS_Mode_Flg), 0);

            _defm_filemane			= _Pref.getString(getString(R.string.file_name), _defm_filemane);
            _DB_defm_no				= _Pref.getString(getString(R.string.GPS_m_no), _DB_defm_no);
            _defonbtn_push_time		= _Pref.getLong(getString(R.string.ONbtn_Push_time), _defonbtn_push_time);


            _PrefEditor.clear();
            _PrefEditor.putInt(getString(R.string.GPS_m_interval),
                    _GPS_defm_interval);
            _PrefEditor.putInt(getString(R.string.GPS_m_timeout),
                    _GPS_defm_timeout);
            _PrefEditor.putLong(getString(R.string.GPS_m_mtimes),
                    _GPS_defm_mtimes);
            _PrefEditor.putInt(getString(R.string.GPS_c1_delay),
                    _GPS_defc1_delay);
            _PrefEditor.putInt(getString(R.string.GPS_c2_delay),
                    _GPS_defc2_delay);
            _PrefEditor.putInt(getString(R.string.GPS_g_delay),
                    _GPS_defg_delay);
            _PrefEditor.putInt(getString(R.string.GPS_m_delay),
                    _GPS_defm_delay);
            _PrefEditor.putInt(getString(R.string.gps_Flg), gps_Flg);
            _PrefEditor.putInt(getString(R.string.net_Flg), net_Flg);
            _PrefEditor.putInt(getString(R.string.passive_Flg), passive_Flg);
            _PrefEditor.putInt(getString(R.string.GPS_Log_Flg), Log_Flg);
            _PrefEditor.putInt(getString(R.string.GPS_Use_Flg), GPS_Use_Flg);
            _PrefEditor.putInt(getString(R.string.GPS_Cold_Start_Qual_Flg), Cold_Start_Qual_Flg);
            _PrefEditor.putInt(getString(R.string.GPS_Cold_Start_Other_Flg), Cold_Start_Other_Flg);
            _PrefEditor.putInt(getString(R.string.GPS_OneXTRA_Timer_Flg), GPS_OneXTRA_Timer_Flg);
            _PrefEditor.putInt(getString(R.string.GPS_OneXTRA_Data_Flg), GPS_OneXTRA_Data_Flg);
            _PrefEditor.putInt(getString(R.string.GPS_MAP_Flg), MAP_Flg);
            _PrefEditor.putInt(getString(R.string.GPS_Mode_Flg), GPS_Mode_Flg);

            _PrefEditor.putString(getString(R.string.file_name), Filename);
            _PrefEditor.putString(getString(R.string.GPS_m_no), _DB_defm_no);

            _PrefEditor.putLong(getString(R.string.ONbtn_Push_time), _defonbtn_push_time);

            // SheredPreferenceへコミット
            _PrefEditor.commit();
        }
        else
        {
            // ONボタン ON
            tbtn_on.setChecked(true);
            tbtn_on.setClickable(false);

            // OFFボタン OFF
            tbtn_off.setChecked(false);
            tbtn_off.setClickable(true);

            // 機能選択の取得
            GPS_Use_Flg = _Pref.getInt(getString(R.string.GPS_Use_Flg), 1);


            // GPS測位サービス起動中_ModeA起動中
            if (isServiceRunning(strBuff_A))
            {
                success_Flg = 0;
                timout_Flg = 0;

                Filename = String.valueOf(year)		+ String.valueOf(month)
                        + String.valueOf(day)		+ String.valueOf(hour)
                        + String.valueOf(minute)	+ String.valueOf(second)
                        + String.valueOf(ms);

                // 変数の初期化
                m_interval	= _Pref.getInt(getString(R.string.GPS_m_interval),
                        _GPS_defm_interval);
                m_timeout	= _Pref.getInt(getString(R.string.GPS_m_timeout),
                        _GPS_defm_timeout);
                m_mtimes	= _Pref.getLong(getString(R.string.GPS_m_mtimes),
                        _GPS_defm_mtimes);
                c1_delay	= _Pref.getInt(getString(R.string.GPS_c1_delay),
                        _GPS_defc1_delay);
                c2_delay	= _Pref.getInt(getString(R.string.GPS_c2_delay),
                        _GPS_defc2_delay);
                g_delay		= _Pref.getInt(getString(R.string.GPS_g_delay),
                        _GPS_defg_delay);
                m_delay		= _Pref.getInt(getString(R.string.GPS_m_delay),
                        _GPS_defm_delay);
                Log_Flg		= _Pref.getInt(getString(R.string.GPS_Log_Flg), 1);
                gps_Flg		= _Pref.getInt(getString(R.string.gps_Flg), 1);
                net_Flg		= _Pref.getInt(getString(R.string.net_Flg), 0);
                passive_Flg	= _Pref.getInt(getString(R.string.passive_Flg), 0);
                MAP_Flg		= _Pref.getInt(getString(R.string.GPS_MAP_Flg), 1);
                GPS_Mode_Flg= _Pref.getInt(getString(R.string.GPS_Mode_Flg), 0);

                Cold_Start_Qual_Flg		= _Pref.getInt(getString(R.string.GPS_Cold_Start_Qual_Flg), 0);
                Cold_Start_Other_Flg	= _Pref.getInt(getString(R.string.GPS_Cold_Start_Other_Flg), 0);
                GPS_OneXTRA_Timer_Flg	= _Pref.getInt(getString(R.string.GPS_OneXTRA_Timer_Flg), 0);
                GPS_OneXTRA_Data_Flg	= _Pref.getInt(getString(R.string.GPS_OneXTRA_Data_Flg), 0);

                Filename= _Pref.getString(getString(R.string.file_name),
                        _defm_filemane);

                success_times = _Pref
                        .getLong(getString(R.string.success_num), 0);
                measure_num = _Pref.getLong(getString(R.string.total_num), 0);
                IDO			= (_Pref.getInt(getString(R.string.IDO), 0)) / 1E6;
                KEIDO		= (_Pref.getInt(getString(R.string.KEIDO), 0)) / 1E6;

                ONbtnPushTime = _Pref.getLong(getString(R.string.ONbtn_Push_time), _defonbtn_push_time);

                String str_buff = _Pref.getString(
                        getString(R.string.timer_state), "None");

                IntentFilter filter = new IntentFilter(GPS_Service_ModeA.Timer);
                registerReceiver(receiver, filter);

                // サービスにバインド
                bindService(intent_A, serviceConnection, Context.BIND_AUTO_CREATE);

                // 測位終了 or 測位終了+測位回数オーバー
                if (str_buff.equals("m_finish")
                        || str_buff.equals("m_finish&num_over")) {

                    tx_IDO.setText("●緯度：  " + Double.toString(IDO));
                    tx_KEIDO.setText("●経度：  " + Double.toString(KEIDO));

                    if (m_mtimes > 0) // 回数指定あり
                    {
                        tx_NUM.setText("●測位回数：" + String.valueOf(measure_num)
                                + " / " + String.valueOf(m_mtimes) + "(成功回数： "
                                + String.valueOf(success_times) + ")");
                    }

                    else // 回数指定なし
                    {
                        tx_NUM.setText("●測位回数：" + String.valueOf(measure_num)
                                + " / ∞" + "(成功回数： "
                                + String.valueOf(success_times) + ")");
                    }

                    // 測位回数指定の場合の測位回数到達チェック
                    if (str_buff.equals("m_finish&num_over"))
                        tx_state.setText("　停止中");
                    else
                        tx_state.setText("　一時停止中");

                }
                // タイムアウト or タイムアウト+測位回数オーバー
                else if (str_buff.equals("timeout")
                        || str_buff.equals("timeout&num_over")) {

                    tx_IDO.setText("●緯度：  タイムアウト");
                    tx_KEIDO.setText("●経度：  タイムアウト");

                    if (m_mtimes > 0) // 回数指定あり
                    {
                        tx_NUM.setText("●測位回数：" + String.valueOf(measure_num)
                                + " / " + String.valueOf(m_mtimes) + "(成功回数： "
                                + String.valueOf(success_times) + ")");
                    }

                    else // 回数指定なし
                    {
                        tx_NUM.setText("●測位回数：" + String.valueOf(measure_num)
                                + " / ∞" + "(成功回数： "
                                + String.valueOf(success_times) + ")");
                    }

                    // 測位回数指定の場合の測位回数到達チェック
                    if (str_buff.equals("timeout&num_over"))
                        tx_state.setText("　停止中");
                    else
                        tx_state.setText("　一時停止中");

                }
                // 測位再開
                else if (str_buff.equals("Re_start")) {
                    tx_state.setText("　測位中");
                }

            }
            // GPS測位サービス_ModeB起動中
            else if (isServiceRunning(strBuff_B))
            {
                success_Flg = 0;
                timout_Flg = 0;

                Filename = String.valueOf(year)		+ String.valueOf(month)
                        + String.valueOf(day)		+ String.valueOf(hour)
                        + String.valueOf(minute)	+ String.valueOf(second)
                        + String.valueOf(ms);

                // 変数の初期化
                m_interval	= _Pref.getInt(getString(R.string.GPS_m_interval),
                        _GPS_defm_interval);
                m_timeout	= _Pref.getInt(getString(R.string.GPS_m_timeout),
                        _GPS_defm_timeout);
                m_mtimes	= _Pref.getLong(getString(R.string.GPS_m_mtimes),
                        _GPS_defm_mtimes);
                c1_delay	= _Pref.getInt(getString(R.string.GPS_c1_delay),
                        _GPS_defc1_delay);
                c2_delay	= _Pref.getInt(getString(R.string.GPS_c2_delay),
                        _GPS_defc2_delay);
                g_delay		= _Pref.getInt(getString(R.string.GPS_g_delay),
                        _GPS_defg_delay);
                m_delay		= _Pref.getInt(getString(R.string.GPS_m_delay),
                        _GPS_defm_delay);
                Log_Flg		= _Pref.getInt(getString(R.string.GPS_Log_Flg), 1);
                gps_Flg		= _Pref.getInt(getString(R.string.gps_Flg), 1);
                net_Flg		= _Pref.getInt(getString(R.string.net_Flg), 0);
                passive_Flg	= _Pref.getInt(getString(R.string.passive_Flg), 0);
                MAP_Flg		= _Pref.getInt(getString(R.string.GPS_MAP_Flg), 1);
                GPS_Mode_Flg= _Pref.getInt(getString(R.string.GPS_Mode_Flg), 0);

                Cold_Start_Qual_Flg		= _Pref.getInt(getString(R.string.GPS_Cold_Start_Qual_Flg), 0);
                Cold_Start_Other_Flg	= _Pref.getInt(getString(R.string.GPS_Cold_Start_Other_Flg), 0);
                GPS_OneXTRA_Timer_Flg	= _Pref.getInt(getString(R.string.GPS_OneXTRA_Timer_Flg), 0);
                GPS_OneXTRA_Data_Flg	= _Pref.getInt(getString(R.string.GPS_OneXTRA_Data_Flg), 0);

                Filename= _Pref.getString(getString(R.string.file_name),
                        _defm_filemane);

                success_times = _Pref
                        .getLong(getString(R.string.success_num), 0);
                measure_num = _Pref.getLong(getString(R.string.total_num), 0);
                IDO			= (_Pref.getInt(getString(R.string.IDO), 0)) / 1E6;
                KEIDO		= (_Pref.getInt(getString(R.string.KEIDO), 0)) / 1E6;

                ONbtnPushTime = _Pref.getLong(getString(R.string.ONbtn_Push_time), _defonbtn_push_time);

                String str_buff = _Pref.getString(
                        getString(R.string.timer_state), "None");

                IntentFilter filter = new IntentFilter(GPS_Service_ModeB.Timer);
                registerReceiver(receiver_ModeB, filter);

                // サービスにバインド
                bindService(intent_B, serviceConnection_ModeB, Context.BIND_AUTO_CREATE);

                // 測位終了 or 測位終了+測位回数オーバー
                if (str_buff.equals("m_finish")
                        || str_buff.equals("m_finish&num_over")) {

                    tx_IDO.setText("●緯度：  " + Double.toString(IDO));
                    tx_KEIDO.setText("●経度：  " + Double.toString(KEIDO));

                    if (m_mtimes > 0) // 回数指定あり
                    {
                        tx_NUM.setText("●測位回数：" + String.valueOf(measure_num)
                                + " / " + String.valueOf(m_mtimes) + "(成功回数： "
                                + String.valueOf(success_times) + ")");
                    }

                    else // 回数指定なし
                    {
                        tx_NUM.setText("●測位回数：" + String.valueOf(measure_num)
                                + " / ∞" + "(成功回数： "
                                + String.valueOf(success_times) + ")");
                    }

                    // 測位回数指定の場合の測位回数到達チェック
                    if (str_buff.equals("m_finish&num_over"))
                        tx_state.setText("　停止中");
                    else
                        tx_state.setText("　一時停止中");

                }
                // タイムアウト or タイムアウト+測位回数オーバー
                else if (str_buff.equals("timeout")
                        || str_buff.equals("timeout&num_over")) {

                    tx_IDO.setText("●緯度：  タイムアウト");
                    tx_KEIDO.setText("●経度：  タイムアウト");

                    if (m_mtimes > 0) // 回数指定あり
                    {
                        tx_NUM.setText("●測位回数：" + String.valueOf(measure_num)
                                + " / " + String.valueOf(m_mtimes) + "(成功回数： "
                                + String.valueOf(success_times) + ")");
                    }

                    else // 回数指定なし
                    {
                        tx_NUM.setText("●測位回数：" + String.valueOf(measure_num)
                                + " / ∞" + "(成功回数： "
                                + String.valueOf(success_times) + ")");
                    }

                    // 測位回数指定の場合の測位回数到達チェック
                    if (str_buff.equals("timeout&num_over"))
                        tx_state.setText("　停止中");
                    else
                        tx_state.setText("　一時停止中");

                }
                // 測位再開
                else if (str_buff.equals("Re_start")) {
                    tx_state.setText("　測位中");
                }

            }
            // GPS測位サービス_ModeC起動中
            else if (isServiceRunning(strBuff_C))
            {
                success_Flg = 0;
                timout_Flg = 0;

                Filename = String.valueOf(year)		+ String.valueOf(month)
                        + String.valueOf(day)		+ String.valueOf(hour)
                        + String.valueOf(minute)	+ String.valueOf(second)
                        + String.valueOf(ms);

                // 変数の初期化
                m_interval	= _Pref.getInt(getString(R.string.GPS_m_interval),
                        _GPS_defm_interval);
                m_timeout	= _Pref.getInt(getString(R.string.GPS_m_timeout),
                        _GPS_defm_timeout);
                m_mtimes	= _Pref.getLong(getString(R.string.GPS_m_mtimes),
                        _GPS_defm_mtimes);
                c1_delay	= _Pref.getInt(getString(R.string.GPS_c1_delay),
                        _GPS_defc1_delay);
                c2_delay	= _Pref.getInt(getString(R.string.GPS_c2_delay),
                        _GPS_defc2_delay);
                g_delay		= _Pref.getInt(getString(R.string.GPS_g_delay),
                        _GPS_defg_delay);
                m_delay		= _Pref.getInt(getString(R.string.GPS_m_delay),
                        _GPS_defm_delay);
                Log_Flg		= _Pref.getInt(getString(R.string.GPS_Log_Flg), 1);
                gps_Flg		= _Pref.getInt(getString(R.string.gps_Flg), 1);
                net_Flg		= _Pref.getInt(getString(R.string.net_Flg), 0);
                passive_Flg	= _Pref.getInt(getString(R.string.passive_Flg), 0);
                MAP_Flg		= _Pref.getInt(getString(R.string.GPS_MAP_Flg), 1);
                GPS_Mode_Flg= _Pref.getInt(getString(R.string.GPS_Mode_Flg), 0);

                Cold_Start_Qual_Flg		= _Pref.getInt(getString(R.string.GPS_Cold_Start_Qual_Flg), 0);
                Cold_Start_Other_Flg	= _Pref.getInt(getString(R.string.GPS_Cold_Start_Other_Flg), 0);
                GPS_OneXTRA_Timer_Flg	= _Pref.getInt(getString(R.string.GPS_OneXTRA_Timer_Flg), 0);
                GPS_OneXTRA_Data_Flg	= _Pref.getInt(getString(R.string.GPS_OneXTRA_Data_Flg), 0);

                Filename= _Pref.getString(getString(R.string.file_name),
                        _defm_filemane);

                success_times = _Pref
                        .getLong(getString(R.string.success_num), 0);
                measure_num = _Pref.getLong(getString(R.string.total_num), 0);
                IDO			= (_Pref.getInt(getString(R.string.IDO), 0)) / 1E6;
                KEIDO		= (_Pref.getInt(getString(R.string.KEIDO), 0)) / 1E6;

                ONbtnPushTime = _Pref.getLong(getString(R.string.ONbtn_Push_time), _defonbtn_push_time);

                String str_buff = _Pref.getString(
                        getString(R.string.timer_state), "None");

                IntentFilter filter = new IntentFilter(GPS_Service_ModeC.Timer);
                registerReceiver(receiver_ModeC, filter);

                // サービスにバインド
                bindService(intent_C, serviceConnection_ModeC, Context.BIND_AUTO_CREATE);

                // 測位終了 or 測位終了+測位回数オーバー
                if (str_buff.equals("m_finish")
                        || str_buff.equals("m_finish&num_over")) {

                    tx_IDO.setText("●緯度：  " + Double.toString(IDO));
                    tx_KEIDO.setText("●経度：  " + Double.toString(KEIDO));

                    tx_NUM.setText("●測位回数：Single Shot");

                    // 測位回数指定の場合の測位回数到達チェック
                    if (str_buff.equals("m_finish&num_over"))
                        tx_state.setText("　停止中");
                    else
                        tx_state.setText("　一時停止中");

                }
                // タイムアウト or タイムアウト+測位回数オーバー
                else if (str_buff.equals("timeout")
                        || str_buff.equals("timeout&num_over")) {

                    tx_IDO.setText("●緯度：  タイムアウト");
                    tx_KEIDO.setText("●経度：  タイムアウト");

                    tx_NUM.setText("●測位回数：Single Shot");

                    // 測位回数指定の場合の測位回数到達チェック
                    if (str_buff.equals("timeout&num_over"))
                        tx_state.setText("　停止中");
                    else
                        tx_state.setText("　一時停止中");

                }
                // 測位再開
                else if (str_buff.equals("Re_start")) {
                    tx_state.setText("　測位中");
                }

            }

            // GPS測位サービス_ModeD起動中
            else if (isServiceRunning(strBuff_D))
            {
                success_Flg = 0;
                timout_Flg = 0;

                Filename = String.valueOf(year)		+ String.valueOf(month)
                        + String.valueOf(day)		+ String.valueOf(hour)
                        + String.valueOf(minute)	+ String.valueOf(second)
                        + String.valueOf(ms);

                // 変数の初期化
                m_interval	= _Pref.getInt(getString(R.string.GPS_m_interval),
                        _GPS_defm_interval);
                m_timeout	= _Pref.getInt(getString(R.string.GPS_m_timeout),
                        _GPS_defm_timeout);
                m_mtimes	= _Pref.getLong(getString(R.string.GPS_m_mtimes),
                        _GPS_defm_mtimes);
                c1_delay	= _Pref.getInt(getString(R.string.GPS_c1_delay),
                        _GPS_defc1_delay);
                c2_delay	= _Pref.getInt(getString(R.string.GPS_c2_delay),
                        _GPS_defc2_delay);
                g_delay		= _Pref.getInt(getString(R.string.GPS_g_delay),
                        _GPS_defg_delay);
                m_delay		= _Pref.getInt(getString(R.string.GPS_m_delay),
                        _GPS_defm_delay);
                Log_Flg		= _Pref.getInt(getString(R.string.GPS_Log_Flg), 1);
                gps_Flg		= _Pref.getInt(getString(R.string.gps_Flg), 1);
                net_Flg		= _Pref.getInt(getString(R.string.net_Flg), 0);
                passive_Flg	= _Pref.getInt(getString(R.string.passive_Flg), 0);
                MAP_Flg		= _Pref.getInt(getString(R.string.GPS_MAP_Flg), 1);
                GPS_Mode_Flg= _Pref.getInt(getString(R.string.GPS_Mode_Flg), 0);

                Cold_Start_Qual_Flg		= _Pref.getInt(getString(R.string.GPS_Cold_Start_Qual_Flg), 0);
                Cold_Start_Other_Flg	= _Pref.getInt(getString(R.string.GPS_Cold_Start_Other_Flg), 0);
                GPS_OneXTRA_Timer_Flg	= _Pref.getInt(getString(R.string.GPS_OneXTRA_Timer_Flg), 0);
                GPS_OneXTRA_Data_Flg	= _Pref.getInt(getString(R.string.GPS_OneXTRA_Data_Flg), 0);

                Filename= _Pref.getString(getString(R.string.file_name),
                        _defm_filemane);

                success_times = _Pref
                        .getLong(getString(R.string.success_num), 0);
                measure_num = _Pref.getLong(getString(R.string.total_num), 0);
                IDO			= (_Pref.getInt(getString(R.string.IDO), 0)) / 1E6;
                KEIDO		= (_Pref.getInt(getString(R.string.KEIDO), 0)) / 1E6;

                ONbtnPushTime = _Pref.getLong(getString(R.string.ONbtn_Push_time), _defonbtn_push_time);

                String str_buff = _Pref.getString(
                        getString(R.string.timer_state), "None");

                IntentFilter filter = new IntentFilter(GPS_Service_ModeD.Timer);
                registerReceiver(receiver_ModeD, filter);

                // サービスにバインド
                bindService(intent_D, serviceConnection_ModeD, Context.BIND_AUTO_CREATE);

                // 測位終了 or 測位終了+測位回数オーバー
                if (str_buff.equals("m_finish")
                        || str_buff.equals("m_finish&num_over")) {

                    tx_IDO.setText("●緯度：  " + Double.toString(IDO));
                    tx_KEIDO.setText("●経度：  " + Double.toString(KEIDO));

                    if (m_mtimes > 0) // 回数指定あり
                    {
                        tx_NUM.setText("●測位回数：" + String.valueOf(measure_num)
                                + " / " + String.valueOf(m_mtimes) + "(成功回数： "
                                + String.valueOf(success_times) + ")");
                    }

                    else // 回数指定なし
                    {
                        tx_NUM.setText("●測位回数：" + String.valueOf(measure_num)
                                + " / ∞" + "(成功回数： "
                                + String.valueOf(success_times) + ")");
                    }

                    // 測位回数指定の場合の測位回数到達チェック
                    if (str_buff.equals("m_finish&num_over"))
                        tx_state.setText("　停止中");
                    else
                        tx_state.setText("　測位中");

                }
                // タイムアウト or タイムアウト+測位回数オーバー
                else if (str_buff.equals("timeout")
                        || str_buff.equals("timeout&num_over")) {

                    tx_IDO.setText("●緯度：  タイムアウト");
                    tx_KEIDO.setText("●経度：  タイムアウト");

                    if (m_mtimes > 0) // 回数指定あり
                    {
                        tx_NUM.setText("●測位回数：" + String.valueOf(measure_num)
                                + " / " + String.valueOf(m_mtimes) + "(成功回数： "
                                + String.valueOf(success_times) + ")");
                    }

                    else // 回数指定なし
                    {
                        tx_NUM.setText("●測位回数：" + String.valueOf(measure_num)
                                + " / ∞" + "(成功回数： "
                                + String.valueOf(success_times) + ")");
                    }

                    // 測位回数指定の場合の測位回数到達チェック
                    if (str_buff.equals("timeout&num_over"))
                        tx_state.setText("　停止中");
                    else
                        tx_state.setText("　一時停止中");

                }
                // 測位再開
                else if (str_buff.equals("Re_start")) {
                    tx_state.setText("　測位中");
                }
            }
        }
    }

    // サービスが起動中かどうかの判断関数
    private boolean isServiceRunning(String className) {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceInfos = am
                .getRunningServices(Integer.MAX_VALUE);
        for (int i = 0; i < serviceInfos.size(); i++) {
            if (serviceInfos.get(i).service.getClassName().equals(className)) {
                return true;
            }
        }
        return false;
    }

    /************************************************************************/
	/* ボタン押下処理 */
    /************************************************************************/

    @Override
    public void onClick(View v)
    {
        // ONが押された
        if (v.equals(tbtn_on))
        {
            tbtn_on.setChecked(true);
            tbtn_on.setClickable(false);
            tbtn_off.setChecked(false);
            tbtn_off.setClickable(true);

            _Pref = getSharedPreferences(getString(R.string.PREF_KEY),
                    ListActivity.MODE_PRIVATE);
            _PrefEditor = _Pref.edit();

            // ボタン押下時刻
            ONbtnPushcalendar = Calendar.getInstance();
            ONbtnPushTime = ONbtnPushcalendar.getTimeInMillis();
            _PrefEditor.putLong(getString(R.string.ONbtn_Push_time), ONbtnPushTime);
            _PrefEditor.commit();

            // 機能選択の取得
            GPS_Use_Flg = _Pref.getInt(getString(R.string.GPS_Use_Flg), 1);

            // 測位モード選択の取得
            GPS_Mode_Flg = _Pref.getInt(getString(R.string.GPS_Mode_Flg), 0);

            if (GPS_Use_Flg == 1)
            {
                measure_num = 0;
                success_times = 0;
                success_Flg = 0;
                timout_Flg = 0;
                tx_state.setText("　測位中");

                tx_IDO.setText("●緯度：  ―");
                tx_KEIDO.setText("●経度：  ―");

                _Pref = getSharedPreferences(getString(R.string.PREF_KEY),
                        ListActivity.MODE_PRIVATE);

                m_mtimes = _Pref.getLong(getString(R.string.GPS_m_mtimes), 0);

                // 測位回数表示処理
                if(GPS_Mode_Flg == 2)					// ModeC?
                {
                    tx_NUM.setText("●測位回数：Single Shot");
                }
                else
                {
                    if (m_mtimes > 0) // 回数指定あり
                    {
                        tx_NUM.setText(String.valueOf("●測位回数：" + measure_num
                                + " / " + m_mtimes));
                    }
                    else // 回数指定なし
                    {
                        tx_NUM.setText(String.valueOf("●測位回数：" + measure_num
                                + " / ∞"));
                    }
                }
                // モードA
                if(GPS_Mode_Flg == 0)
                {
                    // サービスを開始
                    startService(intent_A);
                    IntentFilter filter = new IntentFilter(GPS_Service_ModeA.Timer);
                    registerReceiver(receiver, filter);

                    // サービスにバインド
                    bindService(intent_A, serviceConnection, Context.BIND_AUTO_CREATE);
                }
                // モードB
                else if(GPS_Mode_Flg == 1)
                {
                    // サービスを開始
                    startService(intent_B);
                    IntentFilter filter = new IntentFilter(GPS_Service_ModeB.Timer);
                    registerReceiver(receiver_ModeB, filter);

                    // サービスにバインド
                    bindService(intent_B, serviceConnection_ModeB, Context.BIND_AUTO_CREATE);
                }
                // モードC
                else if(GPS_Mode_Flg == 2)
                {
                    // サービスを開始
                    startService(intent_C);
                    IntentFilter filter = new IntentFilter(GPS_Service_ModeC.Timer);
                    registerReceiver(receiver_ModeC, filter);

                    // サービスにバインド
                    bindService(intent_C, serviceConnection_ModeC, Context.BIND_AUTO_CREATE);
                }
                // モードD
                else if(GPS_Mode_Flg == 3)
                {
                    // サービスを開始
                    startService(intent_D);
                    IntentFilter filter = new IntentFilter(GPS_Service_ModeD.Timer);
                    registerReceiver(receiver_ModeD, filter);

                    // サービスにバインド
                    bindService(intent_D, serviceConnection_ModeD, Context.BIND_AUTO_CREATE);
                }
            }

        }

        // OFFが押された
        if (v.equals(tbtn_off))
        {
            tbtn_off.setChecked(true);
            tbtn_off.setClickable(false);
            tbtn_on.setChecked(false);
            tbtn_on.setClickable(true);
            tx_state.setText("　停止中");

            if (GPS_Use_Flg == 1)
            {
                // モードA
                if(GPS_Mode_Flg == 0)
                {
                    unbindService(serviceConnection);	// バインド解除
                    unregisterReceiver(receiver);		// 登録解除
                    GPS_Service_ModeA.stopSelf();				// サービスは必要ないので終了させる。
                }
                // モードB
                else if(GPS_Mode_Flg == 1)
                {
                    unbindService(serviceConnection_ModeB);	// バインド解除
                    unregisterReceiver(receiver_ModeB);		// 登録解除
                    GPS_Service_ModeB.stopSelf();			// サービスは必要ないので終了させる。
                }
                // モードC
                else if(GPS_Mode_Flg == 2)
                {
                    unbindService(serviceConnection_ModeC);	// バインド解除
                    unregisterReceiver(receiver_ModeC);		// 登録解除
                    GPS_Service_ModeC.stopSelf();			// サービスは必要ないので終了させる。
                }
                // モードD
                else if(GPS_Mode_Flg == 3)
                {
                    unbindService(serviceConnection_ModeD);	// バインド解除
                    unregisterReceiver(receiver_ModeD);		// 登録解除
                    GPS_Service_ModeD.stopSelf();			// サービスは必要ないので終了させる。
                }
            }
        }
    }

    @Override
    public void onStart()
    {
        _Pref = getSharedPreferences(getString(R.string.PREF_KEY),
                ListActivity.MODE_PRIVATE);
        // 設定の表示
        Cold_Start_Qual_Flg		= _Pref.getInt(getString(R.string.GPS_Cold_Start_Qual_Flg), 0);
        Cold_Start_Other_Flg	= _Pref.getInt(getString(R.string.GPS_Cold_Start_Other_Flg), 0);
        GPS_Mode_Flg			= _Pref.getInt(getString(R.string.GPS_Mode_Flg), 0);
        String strBuff			= "";

        strBuff = strBuff + "●";

        if(GPS_Mode_Flg == 0)
            strBuff = strBuff + "測位モードA";
        else if(GPS_Mode_Flg == 1)
            strBuff = strBuff + "測位モードB";
        else if(GPS_Mode_Flg == 2)
            strBuff = strBuff + "測位モードC";
        else if(GPS_Mode_Flg == 3)
            strBuff = strBuff + "測位モードD";

        if((Cold_Start_Qual_Flg == 1) || (Cold_Start_Other_Flg == 1))
            strBuff = strBuff + "　Cold設定";


        if (isServiceRunning(this.getPackageName() + ".GPS_Service_ModeC"))	// ModeC起動中
        {
            tx_NUM.setText("●測位回数：Single Shot");
        }
        else if((!isServiceRunning(this.getPackageName() + ".GPS_Service_ModeA"))
                && (!isServiceRunning(this.getPackageName() + ".GPS_Service_ModeB"))
                && (!isServiceRunning(this.getPackageName() + ".GPS_Service_ModeD")))
        {
            if(GPS_Mode_Flg == 2)					// ModeC?
            {
                tx_NUM.setText("●測位回数：Single Shot");
            }
            else
            {
                tx_NUM.setText("●測位回数：  ―");
            }
        }

        tx_setting.setText(strBuff);


        super.onStart();
    }

    /************************************************************************/
	/* メニュー関連 */
    /************************************************************************/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item0 = menu.add(Menu.NONE, MENU1_ID, Menu.NONE,
                R.string.menu1);
        MenuItem item1 = menu.add(Menu.NONE, MENU2_ID, Menu.NONE,
                R.string.menu2);
        item0.setIcon(android.R.drawable.ic_menu_preferences);
        item1.setIcon(android.R.drawable.ic_menu_save);
        return super.onCreateOptionsMenu(menu);
    }

    // オプションメニューが表示される度に呼び出されます

    /**
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(MENU1_ID).setVisible(visible);
        menu.findItem(MENU2_ID).setVisible(visible);
        visible = !visible;
        return super.onPrepareOptionsMenu(menu);
    }
*/
    /*
     * アイテムセレクト時イベント onOptionsItemSelected
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case MENU1_ID:
                // Intentにより設定用画面の呼び出し(GPS)
                Intent GPS_Pre_intent = new Intent();
                GPS_Pre_intent.setClassName(this.getPackageName(), this
                        .getPackageName()
                        + ".GPSSetting");
                GPS_Pre_intent.setAction(Intent.ACTION_VIEW);
                startActivity(GPS_Pre_intent);
                return true;

            case MENU2_ID:
                // Intentにより設定用画面の呼び出し(GPSファイル書き出し)
                Intent DB_intent = new Intent();
                DB_intent.setClassName(this.getPackageName(), this.getPackageName()
                        + ".DBSetting");
                DB_intent.setAction(Intent.ACTION_VIEW);
                startActivity(DB_intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}