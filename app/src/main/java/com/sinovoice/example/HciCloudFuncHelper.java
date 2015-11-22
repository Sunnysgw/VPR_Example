package com.sinovoice.example;

import java.util.ArrayList;

import android.content.Context;
import android.widget.TextView;

import com.sinovoice.hcicloudsdk.api.HciCloudSys;
import com.sinovoice.hcicloudsdk.api.HciCloudUser;
import com.sinovoice.hcicloudsdk.api.vpr.HciCloudVpr;
import com.sinovoice.hcicloudsdk.common.HciErrorCode;
import com.sinovoice.hcicloudsdk.common.Session;
import com.sinovoice.hcicloudsdk.common.vpr.VprConfig;
import com.sinovoice.hcicloudsdk.common.vpr.VprEnrollResult;
import com.sinovoice.hcicloudsdk.common.vpr.VprEnrollVoiceData;
import com.sinovoice.hcicloudsdk.common.vpr.VprEnrollVoiceDataItem;
import com.sinovoice.hcicloudsdk.common.vpr.VprIdentifyResult;
import com.sinovoice.hcicloudsdk.common.vpr.VprInitParam;
import com.sinovoice.hcicloudsdk.common.vpr.VprVerifyResult;

public class HciCloudFuncHelper extends HciCloudHelper {
	private static final String TAG = HciCloudFuncHelper.class.getSimpleName();

	/*
	 * VPRע���ѵ��
	 */
	public static boolean Enroll(String capkey, VprConfig enrollConfig) {

		ShowMessage("vprEnroll enter...");
		// ��װ��Ƶ������һ�δ�������Ƶ
		int nEnrollDataCount = 1;
		int nIndex = 0;
		ArrayList<VprEnrollVoiceDataItem> enrollVoiceDataList = new ArrayList<VprEnrollVoiceDataItem>();

		for (; nIndex < nEnrollDataCount; nIndex++) {
			String voiceDataName = "enroll_" + nIndex + ".pcm";
			byte[] voiceData = getAssetFileData(voiceDataName);
			if (null == voiceData) {
				ShowMessage("Open input voice file" + voiceDataName + "error!");
				break;
			}
			VprEnrollVoiceDataItem voiceDataItem = new VprEnrollVoiceDataItem();
			voiceDataItem.setVoiceData(voiceData);
			enrollVoiceDataList.add(voiceDataItem);
		}
		if (nIndex <= 0) {
			ShowMessage("no enroll data found in assets folder!");
			return false;
		}

		// ���� VPR Session
		VprConfig sessionConfig = new VprConfig();
		sessionConfig.addParam(VprConfig.SessionConfig.PARAM_KEY_CAP_KEY,
				capkey);
		// ����ʵ�����ָ����Դǰ׺
		if (capkey.contains("local")) {
			sessionConfig.addParam(
					VprConfig.SessionConfig.PARAM_KEY_RES_PREFIX, "16k_");
		}
		ShowMessage("hciVprSessionStart config: "
				+ sessionConfig.getStringConfig());

		Session session = new Session();
		int errCode = HciCloudVpr.hciVprSessionStart(
				sessionConfig.getStringConfig(), session);
		if (HciErrorCode.HCI_ERR_NONE != errCode) {
			ShowMessage("hciVprSessionStart return " + errCode);
			return false;
		}
		ShowMessage("hciVprSessionStart Success");

		// VPR ע��
		VprEnrollVoiceData enrollVoiceData = new VprEnrollVoiceData();
		enrollVoiceData.setEnrollVoiceDataCount(nEnrollDataCount);
		enrollVoiceData.setEnrollVoiceDataList(enrollVoiceDataList);
		VprEnrollResult enrollResult = new VprEnrollResult();
		ShowMessage("hciVprEnroll config:" + enrollConfig.getStringConfig());
		errCode = HciCloudVpr.hciVprEnroll(session, enrollVoiceData,
				enrollConfig.getStringConfig(), enrollResult);
		if (HciErrorCode.HCI_ERR_NONE != errCode) {
			// ����ʧ��
			HciCloudVpr.hciVprSessionStop(session);
			ShowMessage("hciVprEnroll return " + errCode);
			return false;
		}
		ShowMessage("hciVprEnroll Success");
		ShowMessage("enroll result is:" + enrollResult.getUserId());

		// �ر�session
		HciCloudVpr.hciVprSessionStop(session);
		ShowMessage("hciVprSessionStop Success");

		ShowMessage("vprEnroll leave...");
		return true;
	}

	/*
	 * VPR ȷ�ϣ�Verify��
	 */
	public static boolean Verify(String capkey, VprConfig verifyConfig) {
		ShowMessage("vprVerify enter...");

		String voiceDataName = "verify.pcm";
		byte[] voiceDataVerify = getAssetFileData(voiceDataName);
		if (null == voiceDataVerify) {
			ShowMessage("Open input voice file " + voiceDataName + " error!");
			return false;
		}

		// ���� VPR Session
		VprConfig sessionConfig = new VprConfig();
		sessionConfig.addParam(VprConfig.SessionConfig.PARAM_KEY_CAP_KEY,
				capkey);
		// ����ʵ�����ָ����Դǰ׺
		if (capkey.contains("local")) {
			sessionConfig.addParam(
					VprConfig.SessionConfig.PARAM_KEY_RES_PREFIX, "16k_");
		}
		ShowMessage("hciVprSessionStart config: "
				+ sessionConfig.getStringConfig());

		Session session = new Session();
		int errCode = HciCloudVpr.hciVprSessionStart(
				sessionConfig.getStringConfig(), session);
		if (HciErrorCode.HCI_ERR_NONE != errCode) {
			ShowMessage("hciVprSessionStart return " + errCode);
			return false;
		}
		ShowMessage("hciVprSessionStart Success");

		// ��ʼУ��
		VprVerifyResult verifyResult = new VprVerifyResult();
		ShowMessage("hciVprVerify config:" + verifyConfig.getStringConfig());
		errCode = HciCloudVpr.hciVprVerify(session, voiceDataVerify,
				verifyConfig.getStringConfig(), verifyResult);
		if (HciErrorCode.HCI_ERR_NONE != errCode) {
			ShowMessage("Hcivpr hciVprVerify return " + errCode);
			HciCloudVpr.hciVprSessionStop(session);
			return false;
		}
		ShowMessage("hciVprVerify Success");

		if (verifyResult.getStatus() == VprVerifyResult.VPR_VERIFY_STATUS_MATCH) {
			ShowMessage("voice data matches with user_id !");
		} else {
			ShowMessage("voice data doesn't match with user_id !");
		}

		HciCloudVpr.hciVprSessionStop(session);
		ShowMessage("hciVprSessionStop Success");

		ShowMessage("vprVerify leave...");
		return true;
	}

	/*
	 * VPR ��ʶ��Identify��
	 */
	public static boolean Identify(String capkey, VprConfig identifyConfig) {

		ShowMessage("vprIdentify enter...");

		String voiceDataName = "verify.pcm";
		byte[] voiceDataVerify = getAssetFileData(voiceDataName);
		if (null == voiceDataVerify) {
			ShowMessage("Open input voice file " + voiceDataName + " error!");
			return false;
		}

		// ���� VPR Session
		VprConfig sessionConfig = new VprConfig();
		sessionConfig.addParam(VprConfig.SessionConfig.PARAM_KEY_CAP_KEY,
				capkey);
		ShowMessage("Hcivpr hciVprSessionStart config: "
				+ sessionConfig.getStringConfig());
		// ����ʵ�����ָ����Դǰ׺
		if (capkey.contains("local")) {
			sessionConfig.addParam(
					VprConfig.SessionConfig.PARAM_KEY_RES_PREFIX, "16k_");
		}

		Session session = new Session();
		int errCode = HciCloudVpr.hciVprSessionStart(
				sessionConfig.getStringConfig(), session);
		if (HciErrorCode.HCI_ERR_NONE != errCode) {
			ShowMessage("Hcivpr hciVprSessionStart return " + errCode);
			return false;
		}
		ShowMessage("Hcivpr hciVprSessionStart Success");

		// ��ʶ
		VprIdentifyResult identifyResult = new VprIdentifyResult();
		ShowMessage("hciVprIdentify config:" + identifyConfig.getStringConfig());
		errCode = HciCloudVpr.hciVprIdentify(session, voiceDataVerify,
				identifyConfig.getStringConfig(), identifyResult);
		if (HciErrorCode.HCI_ERR_NONE != errCode) {
			ShowMessage("Hcivpr hciVprIdentify return " + errCode);
			HciCloudVpr.hciVprSessionStop(session);
			return false;
		}
		ShowMessage("hciVprIdentify Success");
		ShowMessage("size: " + identifyResult.getIdentifyResultItemList().size());
		for (int index = 0; index < identifyResult.getIdentifyResultItemList()
				.size(); index++) {
			ShowMessage("index" + index);
			ShowMessage("userid:"
					+ identifyResult.getIdentifyResultItemList().get(index)
							.getUserId());
			ShowMessage("score:"
					+ identifyResult.getIdentifyResultItemList().get(index)
							.getScore());
		}

		HciCloudVpr.hciVprSessionStop(session);
		ShowMessage("hciVprSessionStop Success");

		ShowMessage("vprIdentify leave...");
		return true;
	}

	public static void Func(Context context, String capkey, TextView view) {

		setTextView(view);
		setContext(context);

		// ��ʼ��VPR
		// ����VPR��ʼ���İ������ʵ��
		VprInitParam initParam = new VprInitParam();
		// ��ȡAppӦ���е�lib��·��,��������������Դ�ļ������ʹ��/data/data/packagename/libĿ¼,��Ҫ���android_so�ı��
		String dataPath = context.getFilesDir().getAbsolutePath().replace("files", "lib");
		initParam.addParam(VprInitParam.PARAM_KEY_DATA_PATH, dataPath);
		initParam.addParam(VprInitParam.PARAM_KEY_FILE_FLAG, VprInitParam.VALUE_OF_PARAM_FILE_FLAG_ANDROID_SO);
		initParam.addParam(VprInitParam.PARAM_KEY_INIT_CAP_KEYS, capkey);
		ShowMessage("HciVprInit config :" + initParam.getStringConfig());
		int errCode = HciCloudVpr.hciVprInit(initParam.getStringConfig());
		if (errCode != HciErrorCode.HCI_ERR_NONE) {
			ShowMessage("HciVprInit error:"	+ HciCloudSys.hciGetErrorInfo(errCode));
			return;
		} else {
			ShowMessage("HciVprInit Success");
		}

		// ע��ӿ�
		VprConfig enrollConfig = new VprConfig();
		enrollConfig.addParam(VprConfig.UserConfig.PARAM_KEY_USER_ID, "123456");
		enrollConfig.addParam(VprConfig.AudioConfig.PARAM_KEY_AUDIO_FORMAT,
				VprConfig.AudioConfig.VALUE_OF_PARAM_AUDIO_FORMAT_PCM_8K16BIT);
		Enroll(capkey, enrollConfig);

		// Verify
		VprConfig verifyConfig = new VprConfig();
		verifyConfig.addParam(VprConfig.UserConfig.PARAM_KEY_USER_ID, "123456");
		Verify(capkey, verifyConfig);

		// ��userid 123456 ��ӵ���test_example,����Ѿ����ڣ���᷵��
		HciCloudUser.hciCreateGroup("test_example",HciCloudUser.kHciGroupTypeShare);
		HciCloudUser.hciAddUser("test_example", "123456");

		// Identify
		VprConfig identifyConfig = new VprConfig();
		identifyConfig.addParam("groupid", "test_example");
		Identify(capkey, identifyConfig);

		// ����ʼ��VPR
		HciCloudVpr.hciVprRelease();
		ShowMessage("hciVprRelease");
		return;
	}

}
