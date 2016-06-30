package sanoapplication002.ne.jp.sanoapplication002;

import java.util.List;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.content.*;

public class GPSSetting extends ListActivity implements OnClickListener {
	private Button set_GPS_button;
	private EditText edit_GPS_intervaltext;
	private EditText edit_GPS_timeouttext;
	private EditText edit_GPS_mtimestext;
	private EditText edit_GPS_C1delayText;
	private EditText edit_GPS_C2delayText;
	private EditText edit_GPS_GdelayText;
	private EditText edit_GPS_MdelayText;
	private RadioButton radio_GPS;
	private RadioButton radio_NET;
	private RadioButton radio_PASS;
	private RadioButton radio_Mode_A;
	private RadioButton radio_Mode_B;
	private RadioButton radio_Mode_C;
	private RadioButton radio_Mode_D;
	private CheckBox ch_GPS_Log;
	private CheckBox ch_GPS_Use;
	private CheckBox ch_GPS_Cold_Start_Qual;
	private CheckBox ch_GPS_Cold_Start_Other;
	private CheckBox ch_GPS_OneXTRA_Timer;
	private CheckBox ch_GPS_OneXTRA_Data;
	private CheckBox ch_GPS_MAP;

	private SharedPreferences _Pref;				// 設定データ本体用インスタンス
	private SharedPreferences.Editor _PrefEditor;	// 設定データ書き換え用インスタンス

	public static int _defm_interval	= 30;		// GPS測位間隔時間(sec）
	public static int _defm_timeout	= 25;		// GPSタイムアウト時間(sec)
	public static long _defm_mtimes		= 0;		// GPS測位回数
	public static int _defc1_delay		= 0;		// GPS測位アシストデータ削除用XTRA時刻情報取得時間(sec)
	public static int _defc2_delay		= 0;		// GPS測位アシストデータ削除時間(sec)
	public static int _defg_delay		= 0;		// GPS測位XTRAデータ取得時間(sec)
	public static int _defm_delay		= 0;		// GPS測位開始遅延時間(sec)

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gpssetting);

		// SharedPrefernces の取得
		_Pref = getSharedPreferences(getString(R.string.PREF_KEY),
				ListActivity.MODE_PRIVATE);

		set_GPS_button			= (Button) findViewById(R.id.set_GPS_button);
		ch_GPS_Log				= (CheckBox) findViewById(R.id.ch_GPS_Log);
		ch_GPS_Cold_Start_Qual	= (CheckBox) findViewById(R.id.ch_GPS_Cold_Start_Qual);
		ch_GPS_Cold_Start_Other	= (CheckBox) findViewById(R.id.ch_GPS_Cold_Start_Other);
		ch_GPS_OneXTRA_Timer	= (CheckBox) findViewById(R.id.ch_GPS_OneXTRA_Timer);
		ch_GPS_OneXTRA_Data		= (CheckBox) findViewById(R.id.ch_GPS_OneXTRA_Data);
		ch_GPS_MAP				= (CheckBox) findViewById(R.id.ch_GPS_MAP);
		ch_GPS_Use				= (CheckBox) findViewById(R.id.ch_GPS_Use);
		ch_GPS_Use.setOnClickListener(this);
		ch_GPS_Cold_Start_Qual.setOnClickListener(this);
		ch_GPS_Cold_Start_Other.setOnClickListener(this);

		edit_GPS_intervaltext	= (EditText) findViewById(R.id.edit_GPS_IntervalText);
		edit_GPS_timeouttext	= (EditText) findViewById(R.id.edit_GPS_TimeoutText);
		edit_GPS_mtimestext		= (EditText) findViewById(R.id.edit_GPS_MtimesText);
		edit_GPS_C1delayText	= (EditText) findViewById(R.id.edit_GPS_C1delayText);
		edit_GPS_C2delayText	= (EditText) findViewById(R.id.edit_GPS_C2delayText);
		edit_GPS_GdelayText		= (EditText) findViewById(R.id.edit_GPS_GdelayText);
		edit_GPS_MdelayText		= (EditText) findViewById(R.id.edit_GPS_MdelayText);

		radio_GPS	= (RadioButton) findViewById(R.id.radio_GPS);
		radio_NET	= (RadioButton) findViewById(R.id.radio_NET);
		radio_PASS	= (RadioButton) findViewById(R.id.radio_PASS);
		radio_Mode_A= (RadioButton) findViewById(R.id.radio_Mode_A);
		radio_Mode_B= (RadioButton) findViewById(R.id.radio_Mode_B);
		radio_Mode_C= (RadioButton) findViewById(R.id.radio_Mode_C);
		radio_Mode_D= (RadioButton) findViewById(R.id.radio_Mode_D);
		radio_Mode_A.setOnClickListener(this);
		radio_Mode_B.setOnClickListener(this);
		radio_Mode_C.setOnClickListener(this);
		radio_Mode_D.setOnClickListener(this);
		radio_GPS.setOnClickListener(this);
		radio_NET.setOnClickListener(this);
		
		edit_GPS_intervaltext.setText(String.valueOf(_Pref.getInt(
				getString(R.string.GPS_m_interval), _defm_interval)));
		edit_GPS_timeouttext.setText(String.valueOf(_Pref.getInt(
				getString(R.string.GPS_m_timeout), _defm_timeout)));
		edit_GPS_mtimestext.setText(String.valueOf(_Pref.getLong(
				getString(R.string.GPS_m_mtimes), _defm_mtimes)));
		edit_GPS_C1delayText.setText(String.valueOf(_Pref.getInt(
				getString(R.string.GPS_c1_delay), _defc1_delay)));
		edit_GPS_C2delayText.setText(String.valueOf(_Pref.getInt(
				getString(R.string.GPS_c2_delay), _defc2_delay)));
		edit_GPS_GdelayText.setText(String.valueOf(_Pref.getInt(
				getString(R.string.GPS_g_delay), _defg_delay)));
		edit_GPS_MdelayText.setText(String.valueOf(_Pref.getInt(
				getString(R.string.GPS_m_delay), _defm_delay)));

		set_GPS_button.setOnClickListener(this);

		if (_Pref.getInt(getString(R.string.GPS_Log_Flg), 1) == 1)
			ch_GPS_Log.setChecked(true);
		else
			ch_GPS_Log.setChecked(false);

		if (_Pref.getInt(getString(R.string.GPS_Cold_Start_Qual_Flg), 0) == 1)
			ch_GPS_Cold_Start_Qual.setChecked(true);
		else
			ch_GPS_Cold_Start_Qual.setChecked(false);

		if (_Pref.getInt(getString(R.string.GPS_Cold_Start_Other_Flg), 0) == 1)
			ch_GPS_Cold_Start_Other.setChecked(true);
		else
			ch_GPS_Cold_Start_Other.setChecked(false);

		if (_Pref.getInt(getString(R.string.GPS_OneXTRA_Timer_Flg), 0) == 1)
			ch_GPS_OneXTRA_Timer.setChecked(true);
		else
			ch_GPS_OneXTRA_Timer.setChecked(false);

		if (_Pref.getInt(getString(R.string.GPS_OneXTRA_Data_Flg), 0) == 1)
			ch_GPS_OneXTRA_Data.setChecked(true);
		else
			ch_GPS_OneXTRA_Data.setChecked(false);

		if (_Pref.getInt(getString(R.string.GPS_MAP_Flg), 0) == 1)
			ch_GPS_MAP.setChecked(true);
		else
			ch_GPS_MAP.setChecked(false);

		if (_Pref.getInt(getString(R.string.gps_Flg), 1) == 1) {
			radio_GPS.setChecked(true);
			radio_NET.setChecked(false);
			radio_PASS.setChecked(false);
		}else if (_Pref.getInt(getString(R.string.net_Flg), 1) == 1) {
			radio_GPS.setChecked(false);
			radio_NET.setChecked(true);
			radio_PASS.setChecked(false);
		}else if (_Pref.getInt(getString(R.string.passive_Flg), 1) == 1) {
			radio_GPS.setChecked(false);
			radio_NET.setChecked(false);
			radio_PASS.setChecked(true);
		}

		if (_Pref.getInt(getString(R.string.GPS_Mode_Flg), 0) == 0) {
			radio_Mode_A.setChecked(true);
			radio_Mode_B.setChecked(false);
			radio_Mode_C.setChecked(false);
			radio_Mode_D.setChecked(false);
		} else if (_Pref.getInt(getString(R.string.GPS_Mode_Flg), 0) == 1) {
			radio_Mode_A.setChecked(false);
			radio_Mode_B.setChecked(true);
			radio_Mode_C.setChecked(false);
			radio_Mode_D.setChecked(false);
		} else if (_Pref.getInt(getString(R.string.GPS_Mode_Flg), 0) == 2) {
			radio_Mode_A.setChecked(false);
			radio_Mode_B.setChecked(false);
			radio_Mode_C.setChecked(true);
			radio_Mode_D.setChecked(false);
		} else if (_Pref.getInt(getString(R.string.GPS_Mode_Flg), 0) == 3) {
			radio_Mode_A.setChecked(false);
			radio_Mode_B.setChecked(false);
			radio_Mode_C.setChecked(false);
			radio_Mode_D.setChecked(true);
		}

		if (_Pref.getInt(getString(R.string.GPS_Use_Flg), 1) == 1) {
			ch_GPS_Use.setChecked(true);
			edit_GPS_intervaltext.setEnabled(true);
			edit_GPS_timeouttext.setEnabled(true);
			edit_GPS_mtimestext.setEnabled(true);
			edit_GPS_C1delayText.setEnabled(true);
			edit_GPS_C2delayText.setEnabled(true);
			edit_GPS_MdelayText.setEnabled(true);
			radio_GPS.setEnabled(true);
			radio_NET.setEnabled(true);
			radio_PASS.setEnabled(true);
			radio_Mode_A.setEnabled(true);
			radio_Mode_B.setEnabled(true);
			radio_Mode_C.setEnabled(true);
			radio_Mode_D.setEnabled(true);
			ch_GPS_Log.setEnabled(true);
			ch_GPS_Cold_Start_Qual.setEnabled(true);
			ch_GPS_Cold_Start_Other.setEnabled(true);
			ch_GPS_MAP.setEnabled(true);
		} else {
			ch_GPS_Use.setChecked(false);
			edit_GPS_intervaltext.setEnabled(false);
			edit_GPS_timeouttext.setEnabled(false);
			edit_GPS_mtimestext.setEnabled(false);
			edit_GPS_C1delayText.setEnabled(false);
			edit_GPS_C2delayText.setEnabled(false);
			edit_GPS_MdelayText.setEnabled(false);
			radio_GPS.setEnabled(false);
			radio_NET.setEnabled(false);
			radio_PASS.setEnabled(false);
			radio_Mode_A.setEnabled(false);
			radio_Mode_B.setEnabled(false);
			radio_Mode_C.setEnabled(false);
			radio_Mode_D.setEnabled(false);
			ch_GPS_Log.setEnabled(false);
			ch_GPS_Cold_Start_Qual.setEnabled(false);
			ch_GPS_Cold_Start_Other.setEnabled(false);
			ch_GPS_MAP.setEnabled(false);
		}

		if (_Pref.getInt(getString(R.string.GPS_Use_Flg), 1) == 1)
		{	
			if (_Pref.getInt(getString(R.string.gps_Flg), 1) == 1)
			{
				edit_GPS_GdelayText.setEnabled(true);
				ch_GPS_OneXTRA_Timer.setEnabled(true);
				ch_GPS_OneXTRA_Data.setEnabled(true);
			}
			else
			{
				edit_GPS_GdelayText.setEnabled(false);
				ch_GPS_OneXTRA_Timer.setEnabled(false);
				ch_GPS_OneXTRA_Data.setEnabled(false);
			}
		
			if (_Pref.getInt(getString(R.string.GPS_Mode_Flg), 0) == 2)
			{
				edit_GPS_intervaltext.setEnabled(false);
				edit_GPS_mtimestext.setEnabled(false);
			}
			else
			{
				edit_GPS_intervaltext.setEnabled(true);
				edit_GPS_mtimestext.setEnabled(true);
			}
		}
		else
		{
			edit_GPS_GdelayText.setEnabled(false);
			ch_GPS_OneXTRA_Timer.setEnabled(false);
			ch_GPS_OneXTRA_Data.setEnabled(false);
			edit_GPS_intervaltext.setEnabled(false);
			edit_GPS_mtimestext.setEnabled(false);
		}

		if (radio_Mode_C.isChecked())
		{
			edit_GPS_intervaltext.setEnabled(false);
			edit_GPS_mtimestext.setEnabled(false);
			edit_GPS_timeouttext.setEnabled(true);
		}
		else if(radio_Mode_D.isChecked())
		{
			edit_GPS_intervaltext.setEnabled(true);
			edit_GPS_mtimestext.setEnabled(true);
			edit_GPS_timeouttext.setEnabled(false);
		}
		else
		{
			edit_GPS_intervaltext.setEnabled(true);
			edit_GPS_mtimestext.setEnabled(true);
			edit_GPS_timeouttext.setEnabled(true);
		}

		ch_GPS_Log.setFocusable(true);
		ch_GPS_Log.setFocusableInTouchMode(true);
		ch_GPS_Log.requestFocus();

	}

	class ViewHolder {
		TextView mainText;
		TextView subText;
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub

		int Running_Mode_Flg = 0;
		int Error_Flg = 0;

		if (v.equals(set_GPS_button))
		{
			String PkgName = this.getPackageName();

			// 起動中モード確認
			// モードA起動中？
			if(isServiceRunning(PkgName + ".GPS_Service_ModeA"))
				Running_Mode_Flg = 1;
			// モードB起動中？
			else if (isServiceRunning(PkgName + ".GPS_Service_ModeB"))
				Running_Mode_Flg = 2;
			// モードC起動中？
			else if (isServiceRunning(PkgName + ".GPS_Service_ModeC"))
				Running_Mode_Flg = 3;
			// モードD起動中？
			else if (isServiceRunning(PkgName + ".GPS_Service_ModeD"))
				Running_Mode_Flg = 4;

			// エラー判定
			if (radio_Mode_A.isChecked()) // モードAが選択された
			{
				if((Running_Mode_Flg != 0)	// サービスが起動してなくない⇒何らかのモードが起動中 and
				&& (Running_Mode_Flg != 1)) // 起動モードはA以外
					Error_Flg = 1;
			}
			else if (radio_Mode_B.isChecked()) // モードBが選択された
			{
				if((Running_Mode_Flg != 0)	// サービスが起動してなくない⇒何らかのモードが起動中 and
				&& (Running_Mode_Flg != 2)) // 起動モードはB以外
					Error_Flg = 1;
			}
			else if (radio_Mode_C.isChecked()) // モードCが選択された
			{
				if((Running_Mode_Flg != 0)	// サービスが起動してなくない⇒何らかのモードが起動中 and
				&& (Running_Mode_Flg != 3)) // 起動モードはC以外
					Error_Flg = 1;
			}
			else if (radio_Mode_D.isChecked()) // モードDが選択された
			{
				if((Running_Mode_Flg != 0)	// サービスが起動してなくない⇒何らかのモードが起動中 and
				&& (Running_Mode_Flg != 4)) // 起動モードはD以外
					Error_Flg = 1;
			}

			// エラー処理
			if (Error_Flg == 1) 			// エラーになった
			{
				AlertDialog ald = new AlertDialog.Builder(v.getContext())
				.setTitle("モードの変更").setMessage(
						"GPS測位サービス起動中は、別モードへの変更は出来ません")
				.setPositiveButton("戻る",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(
									DialogInterface dialog,
									int which) {
								return;
							}
						}).setCancelable(true).create();
				ald.show();
			}

			InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

			_Pref = getSharedPreferences(getString(R.string.PREF_KEY),
					ListActivity.MODE_PRIVATE);
			_PrefEditor = _Pref.edit();

			_PrefEditor.putInt(getString(R.string.GPS_m_interval), Integer
					.parseInt(edit_GPS_intervaltext.getText().toString()));
			_PrefEditor.putInt(getString(R.string.GPS_m_timeout), Integer
					.parseInt(edit_GPS_timeouttext.getText().toString()));
			_PrefEditor.putLong(getString(R.string.GPS_m_mtimes), Long
					.parseLong(edit_GPS_mtimestext.getText().toString()));
			_PrefEditor.putInt(getString(R.string.GPS_c1_delay), Integer
					.parseInt(edit_GPS_C1delayText.getText().toString()));
			_PrefEditor.putInt(getString(R.string.GPS_c2_delay), Integer
					.parseInt(edit_GPS_C2delayText.getText().toString()));
			_PrefEditor.putInt(getString(R.string.GPS_g_delay), Integer
					.parseInt(edit_GPS_GdelayText.getText().toString()));
			_PrefEditor.putInt(getString(R.string.GPS_m_delay), Integer
					.parseInt(edit_GPS_MdelayText.getText().toString()));

			if (radio_GPS.isChecked()) // GPSプロバイダが選択された
			{
				_PrefEditor.putInt(getString(R.string.gps_Flg), 1);
				_PrefEditor.putInt(getString(R.string.net_Flg), 0);
				_PrefEditor.putInt(getString(R.string.passive_Flg), 0);
			}
			else if (radio_NET.isChecked()) // ネットワークプロバイダが選択された
			{
				_PrefEditor.putInt(getString(R.string.gps_Flg), 0);
				_PrefEditor.putInt(getString(R.string.net_Flg), 1);
				_PrefEditor.putInt(getString(R.string.passive_Flg), 0);
			}
			else if (radio_PASS.isChecked()) // Passiveプロバイダが選択された
			{
				_PrefEditor.putInt(getString(R.string.gps_Flg), 0);
				_PrefEditor.putInt(getString(R.string.net_Flg), 0);
				_PrefEditor.putInt(getString(R.string.passive_Flg), 1);
			}

			if(Error_Flg == 1) //モード設定エラーになった⇒元のモードに戻す 
			{
				_PrefEditor.putInt(getString(R.string.GPS_Mode_Flg), Running_Mode_Flg - 1);
			}
			else
			{
				if (radio_Mode_A.isChecked()) // モードAが選択された
				{
					_PrefEditor.putInt(getString(R.string.GPS_Mode_Flg), 0);
				}
				else if (radio_Mode_B.isChecked()) // モードBが選択された
				{
					_PrefEditor.putInt(getString(R.string.GPS_Mode_Flg), 1);
				}
				else if (radio_Mode_C.isChecked()) // モードCが選択された
				{
					_PrefEditor.putInt(getString(R.string.GPS_Mode_Flg), 2);
				}
				else if (radio_Mode_D.isChecked()) // モードDが選択された
				{
					_PrefEditor.putInt(getString(R.string.GPS_Mode_Flg), 3);
				}
			}
				
			if (ch_GPS_Log.isChecked())
				_PrefEditor.putInt(getString(R.string.GPS_Log_Flg), 1);
			else
				_PrefEditor.putInt(getString(R.string.GPS_Log_Flg), 0);

			if (ch_GPS_Cold_Start_Qual.isChecked())
				_PrefEditor.putInt(getString(R.string.GPS_Cold_Start_Qual_Flg), 1);
			else
				_PrefEditor.putInt(getString(R.string.GPS_Cold_Start_Qual_Flg), 0);

			if (ch_GPS_Cold_Start_Other.isChecked())
				_PrefEditor.putInt(getString(R.string.GPS_Cold_Start_Other_Flg), 1);
			else
				_PrefEditor.putInt(getString(R.string.GPS_Cold_Start_Other_Flg), 0);

			if (ch_GPS_OneXTRA_Timer.isChecked())
				_PrefEditor.putInt(getString(R.string.GPS_OneXTRA_Timer_Flg), 1);
			else
				_PrefEditor.putInt(getString(R.string.GPS_OneXTRA_Timer_Flg), 0);

			if (ch_GPS_OneXTRA_Data.isChecked())
				_PrefEditor.putInt(getString(R.string.GPS_OneXTRA_Data_Flg), 1);
			else
				_PrefEditor.putInt(getString(R.string.GPS_OneXTRA_Data_Flg), 0);

			if (ch_GPS_MAP.isChecked())
				_PrefEditor.putInt(getString(R.string.GPS_MAP_Flg), 1);
			else
				_PrefEditor.putInt(getString(R.string.GPS_MAP_Flg), 0);

			if (ch_GPS_Use.isChecked())
				_PrefEditor.putInt(getString(R.string.GPS_Use_Flg), 1);
			else
				_PrefEditor.putInt(getString(R.string.GPS_Use_Flg), 0);

			_PrefEditor.commit();
		}
		else if (v.equals(ch_GPS_Use))
		{
			if (ch_GPS_Use.isChecked())
			{
				edit_GPS_intervaltext.setEnabled(true);
				edit_GPS_timeouttext.setEnabled(true);
				edit_GPS_mtimestext.setEnabled(true);
				edit_GPS_C1delayText.setEnabled(true);
				edit_GPS_C2delayText.setEnabled(true);
				edit_GPS_MdelayText.setEnabled(true);
				radio_GPS.setEnabled(true);
				radio_NET.setEnabled(true);
				radio_PASS.setEnabled(true);
				radio_Mode_A.setEnabled(true);
				radio_Mode_B.setEnabled(true);
				radio_Mode_C.setEnabled(true);
				radio_Mode_D.setEnabled(true);
				ch_GPS_Log.setEnabled(true);
				ch_GPS_Cold_Start_Qual.setEnabled(true);
				ch_GPS_Cold_Start_Other.setEnabled(true);
				ch_GPS_MAP.setEnabled(true);
				if (radio_GPS.isChecked())
				{
					edit_GPS_GdelayText.setEnabled(true);
					ch_GPS_OneXTRA_Timer.setEnabled(true);
					ch_GPS_OneXTRA_Data.setEnabled(true);
				}
				else if (radio_NET.isChecked())
				{
					edit_GPS_GdelayText.setEnabled(false);
					ch_GPS_OneXTRA_Timer.setEnabled(false);
					ch_GPS_OneXTRA_Data.setEnabled(false);
				}

				if (radio_Mode_C.isChecked())
				{
					edit_GPS_intervaltext.setEnabled(false);
					edit_GPS_mtimestext.setEnabled(false);
					edit_GPS_timeouttext.setEnabled(true);
				}
				else if(radio_Mode_D.isChecked())
				{
					edit_GPS_intervaltext.setEnabled(true);
					edit_GPS_mtimestext.setEnabled(true);
					edit_GPS_timeouttext.setEnabled(false);
				}
				else
				{
					edit_GPS_intervaltext.setEnabled(true);
					edit_GPS_mtimestext.setEnabled(true);
					edit_GPS_timeouttext.setEnabled(true);
				}
			}
			else
			{
				edit_GPS_intervaltext.setEnabled(false);
				edit_GPS_timeouttext.setEnabled(false);
				edit_GPS_mtimestext.setEnabled(false);
				edit_GPS_C1delayText.setEnabled(false);
				edit_GPS_C2delayText.setEnabled(false);
				edit_GPS_GdelayText.setEnabled(false);
				edit_GPS_MdelayText.setEnabled(false);
				radio_GPS.setEnabled(false);
				radio_NET.setEnabled(false);
				radio_PASS.setEnabled(false);
				radio_Mode_A.setEnabled(false);
				radio_Mode_B.setEnabled(false);
				radio_Mode_C.setEnabled(false);
				radio_Mode_D.setEnabled(false);
				ch_GPS_Log.setEnabled(false);
				ch_GPS_Cold_Start_Qual.setEnabled(false);
				ch_GPS_Cold_Start_Other.setEnabled(false);
				ch_GPS_OneXTRA_Timer.setEnabled(false);
				ch_GPS_OneXTRA_Data.setEnabled(false);
				ch_GPS_MAP.setEnabled(false);
			}
		}
		else if ((v.equals(radio_GPS)) || (v.equals(radio_NET)))
		{
			if (ch_GPS_Use.isChecked())
			{	
				if (radio_GPS.isChecked())
				{
					edit_GPS_GdelayText.setEnabled(true);
					ch_GPS_OneXTRA_Timer.setEnabled(true);
					ch_GPS_OneXTRA_Data.setEnabled(true);
				}
				else  if (radio_NET.isChecked())
				{
					edit_GPS_GdelayText.setEnabled(false);
					ch_GPS_OneXTRA_Timer.setEnabled(false);
					ch_GPS_OneXTRA_Data.setEnabled(false);
				}
			}
			else
			{
				edit_GPS_GdelayText.setEnabled(false);
				ch_GPS_OneXTRA_Timer.setEnabled(false);
				ch_GPS_OneXTRA_Data.setEnabled(false);
			}
		}	
		else if ((v.equals(radio_Mode_A)) 
			   || (v.equals(radio_Mode_B))  
			   || (v.equals(radio_Mode_C))  
			   || (v.equals(radio_Mode_D)))
		{
			if (ch_GPS_Use.isChecked())
			{	
				if (radio_Mode_C.isChecked())
				{
					edit_GPS_intervaltext.setEnabled(false);
					edit_GPS_mtimestext.setEnabled(false);
					edit_GPS_timeouttext.setEnabled(true);
				}
				else if(radio_Mode_D.isChecked())
				{
					edit_GPS_intervaltext.setEnabled(true);
					edit_GPS_mtimestext.setEnabled(true);
					edit_GPS_timeouttext.setEnabled(false);
				}
				else
				{
					edit_GPS_intervaltext.setEnabled(true);
					edit_GPS_mtimestext.setEnabled(true);
					edit_GPS_timeouttext.setEnabled(true);
				}
			}
			else
			{
				edit_GPS_intervaltext.setEnabled(false);
				edit_GPS_mtimestext.setEnabled(false);
				edit_GPS_timeouttext.setEnabled(false);
			}
		}	
		else if (v.equals(ch_GPS_Cold_Start_Qual))
		{
			ch_GPS_Cold_Start_Other.setChecked(false);
		}
		else if (v.equals(ch_GPS_Cold_Start_Other))
		{
			ch_GPS_Cold_Start_Qual.setChecked(false);
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


}
