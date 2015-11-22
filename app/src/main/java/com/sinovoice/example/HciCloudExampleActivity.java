package com.sinovoice.example;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import com.sinovoice.hcicloudsdk.api.HciCloudSys;
import com.sinovoice.hcicloudsdk.common.AuthExpireTime;
import com.sinovoice.hcicloudsdk.common.HciErrorCode;
import com.sinovoice.hcicloudsdk.common.InitParam;

/**
 * @author sinovoice
 */
public class HciCloudExampleActivity extends Activity {
	private static final String TAG = "HciCloudExampleActivity";
	/**
	 * չʾ���淵����Ϣ��TextView
	 */
	private TextView mLogView;

	/**
	 * �����û���Ϣ������
	 */
	private AccountInfo mAccountInfo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		// ��ʼ������
		mLogView = (TextView) findViewById(R.id.logview);

		mAccountInfo = AccountInfo.getInstance();
		boolean loadResult = mAccountInfo.loadAccountInfo(this);
		if (loadResult) {
			// ������Ϣ�ɹ�����������
			mLogView.setText("loadaccountSunness");
		} else {
			// ������Ϣʧ�ܣ���ʾʧ�ܽ���
			mLogView.setText("���������˺�ʧ�ܣ�����assets/AccountInfo.txt�ļ�����д��ȷ�������˻���Ϣ���˻���Ҫ��www.hcicloud.com������������ע�����롣");
			return;
		}

		// ������Ϣ,����InitParam, ������ò������ַ���
		InitParam initParam = getInitParam();
		String strConfig = initParam.getStringConfig();
		Log.i(TAG, "\nhciInit config:" + strConfig);

		// ��ʼ��
		int errCode = HciCloudSys.hciInit(strConfig, this);
		if (errCode != HciErrorCode.HCI_ERR_NONE
				&& errCode != HciErrorCode.HCI_ERR_SYS_ALREADY_INIT) {
			mLogView.append("\nhciInit error: "
					+ HciCloudSys.hciGetErrorInfo(errCode));
			return;
		} else {
			mLogView.append("\nhciInit success");
		}

		// ��ȡ��Ȩ/������Ȩ�ļ� :
		errCode = checkAuthAndUpdateAuth();
		if (errCode != HciErrorCode.HCI_ERR_NONE) {
			// ����ϵͳ�Ѿ���ʼ���ɹ�,�ڽ���ǰ��Ҫ���÷���hciRelease()����ϵͳ�ķ���ʼ��
			mLogView.append("\nCheckAuthAndUpdateAuth error: "
					+ HciCloudSys.hciGetErrorInfo(errCode));
			HciCloudSys.hciRelease();
			return;
		}
		HciCloudFuncHelper.Func(this, mAccountInfo.getCapKey(), mLogView);
		return;
	}

	@Override
	protected void onDestroy() {
		// �ͷ�HciCloudSys������������ȫ���ͷ���Ϻ󣬲��ܵ���HciCloudSys���ͷŷ���
		HciCloudSys.hciRelease();
		mLogView.append("\nhciRelease");
		super.onDestroy();
	}

	/**
	 * ���س�ʼ����Ϣ
	 * 
	 * @param context �������ﾳ
	 * @return ϵͳ��ʼ������
	 */
	private InitParam getInitParam() {
		String authDirPath = this.getFilesDir().getAbsolutePath();

		// ǰ����������
		InitParam initparam = new InitParam();
		// ��Ȩ�ļ�����·�����������
		initparam
				.addParam(InitParam.AuthParam.PARAM_KEY_AUTH_PATH, authDirPath);
		// �����Ʒ���Ľӿڵ�ַ���������
		initparam.addParam(InitParam.AuthParam.PARAM_KEY_CLOUD_URL, AccountInfo
				.getInstance().getCloudUrl());
		// ������Key���������ɽ�ͨ�����ṩ
		initparam.addParam(InitParam.AuthParam.PARAM_KEY_DEVELOPER_KEY,
				AccountInfo.getInstance().getDeveloperKey());
		// Ӧ��Key���������ɽ�ͨ�����ṩ
		initparam.addParam(InitParam.AuthParam.PARAM_KEY_APP_KEY, AccountInfo
				.getInstance().getAppKey());

		// ������־����
		String sdcardState = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(sdcardState)) {
			String sdPath = Environment.getExternalStorageDirectory()
					.getAbsolutePath();
			String packageName = this.getPackageName();

			String logPath = sdPath + File.separator + "sinovoice"
					+ File.separator + packageName + File.separator + "log"
					+ File.separator;

			// ��־�ļ���ַ
			File fileDir = new File(logPath);
			if (!fileDir.exists()) {
				fileDir.mkdirs();
			}

			// ��־��·������ѡ�������������Ϊ����������־
			initparam.addParam(InitParam.LogParam.PARAM_KEY_LOG_FILE_PATH,
					logPath);
			// ��־��Ŀ��Ĭ�ϱ������ٸ���־�ļ��������򸲸���ɵ���־
			initparam.addParam(InitParam.LogParam.PARAM_KEY_LOG_FILE_COUNT, "5");
			// ��־��С��Ĭ��һ����־�ļ�д��󣬵�λΪK
			initparam.addParam(InitParam.LogParam.PARAM_KEY_LOG_FILE_SIZE,
					"1024");
			// ��־�ȼ���0=�ޣ�1=����2=���棬3=��Ϣ��4=ϸ�ڣ�5=���ԣ�SDK�����С�ڵ���logLevel����־��Ϣ
			initparam.addParam(InitParam.LogParam.PARAM_KEY_LOG_LEVEL, "5");
		}

		return initparam;
	}

	/**
	 * ��ȡ��Ȩ
	 * 
	 * @return true �ɹ�
	 */
	private int checkAuthAndUpdateAuth() {

		// ��ȡϵͳ��Ȩ����ʱ��
		int initResult;
		AuthExpireTime objExpireTime = new AuthExpireTime();
		initResult = HciCloudSys.hciGetAuthExpireTime(objExpireTime);
		if (initResult == HciErrorCode.HCI_ERR_NONE) {
			// ��ʾ��Ȩ����,���û�����Ҫ��ע��ֵ,�˴�����ɺ���
			Date date = new Date(objExpireTime.getExpireTime() * 1000);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",
					Locale.CHINA);
			Log.i(TAG, "expire time: " + sdf.format(date));

			if (objExpireTime.getExpireTime() * 1000 > System
					.currentTimeMillis()) {
				// �Ѿ��ɹ���ȡ����Ȩ,���Ҿ�����Ȩ�����г����ʱ��(>7��)
				Log.i(TAG, "checkAuth success");
				return initResult;
			}

		}

		// ��ȡ����ʱ��ʧ�ܻ����Ѿ�����
		initResult = HciCloudSys.hciCheckAuth();
		if (initResult == HciErrorCode.HCI_ERR_NONE) {
			Log.i(TAG, "checkAuth success");
			return initResult;
		} else {
			Log.e(TAG, "checkAuth failed: " + initResult);
			return initResult;
		}
	}

}
