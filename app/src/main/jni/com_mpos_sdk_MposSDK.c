#include "platform.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <jni.h>
#include "android/log.h"
#include "com_mpos_sdk_MposSDK.h"
#include "callback.h"
#include "mpos.h"
#include "protims.h"
#include "common.h"

///////////////////////////////////JNI//////////////////////////////////////
// TMS
#define METHOD_CONNECT_TMS				"connectTms"
#define METHOD_SIG_CONNECT_TMS			"()Z"
#define METHOD_CLOSE_TMS				"closeTms"
#define METHOD_SIG_CLOSE_TMS			"()V"
#define METHOD_RESET_TMS				"resetTms"
#define METHOD_SIG_RESET_TMS			"()V"
#define METHOD_RECV_TMS					"recvTms"
#define METHOD_SIG_RECV_TMS				"([BII)I"
#define METHOD_SEND_TMS					"sendTms"
#define METHOD_SIG_SEND_TMS				"([BII)I"
#define METHOD_ISCONNECTED_TMS			"isConnectedTms"
#define METHOD_SIG_ISCONNECTED_TMS		"()Z"
// MPOS
#define METHOD_CONNECT_MPOS				"connectMpos"
#define METHOD_SIG_CONNECT_MPOS			"()Z"
#define METHOD_CLOSE_MPOS				"closeMpos"
#define METHOD_SIG_CLOSE_MPOS			"()V"
#define METHOD_RESET_MPOS				"resetMpos"
#define METHOD_SIG_RESET_MPOS			"()V"
#define METHOD_RECV_MPOS				"recvMpos"
#define METHOD_SIG_RECV_MPOS			"([BII)I"
#define METHOD_SEND_MPOS				"sendMpos"
#define METHOD_SIG_SEND_MPOS			"([BII)I"
#define METHOD_ISCONNECTED_MPOS			"isConnectedMpos"
#define METHOD_SIG_ISCONNECTED_MPOS		"()Z"
#define METHOD_GET_TERM_ID				"getTermId"
#define METHOD_SIG_GET_TERM_ID			"()Ljava/lang/String;"
#define METHOD_GET_PATH					"getPath"
#define METHOD_SIG_GET_PATH				"()Ljava/lang/String;"
#define METHOD_GET_CALL_MODE			"getCallMode"
#define METHOD_SIG_GET_CALL_MODE		"()I"

#define METHOD_SET_PROGRESS				"setProgress"
#define METHOD_SIG_SET_PROGRESS			"(IIIIIIIII)V"
#define FIELD_LISTENER					"listener"
#define FIELD_SIG_LISTENER				"Lcom/mpos/sdk/IProgressListener;"

static int ExecStringGetMethod(jmethodID mdId, unsigned char *pszExcCls, unsigned char *pszExcMsg,
		                       unsigned char *psOutDataBuff, int iOffset, unsigned int iMaxLen, int *iOutLen);		                       
		                       
static jmethodID sgMdConnectTms;
static jmethodID sgMdCloseTms;
static jmethodID sgMdResetTms;
static jmethodID sgMdRecvTms;
static jmethodID sgMdSendTms;
static jmethodID sgMdIsConnectedTms;
static jmethodID sgMdConnectMpos;
static jmethodID sgMdCloseMpos;
static jmethodID sgMdResetMpos;
static jmethodID sgMdRecvMpos;
static jmethodID sgMdSendMpos;
static jmethodID sgMdIsConnectedMpos;
static jmethodID sgMdGetTermId;
static jmethodID sgMdGetPath;
static jmethodID sgMdGetCallMode;
static jmethodID sgMdSetProgress;
static jclass 	 sgClzDownloader;
//cld test
//static JNIEnv   *sgEnv;
static JavaVM   *g_jvm;

//static TLS jobject   sgObj;
static  jobject   sgObj;

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG,__VA_ARGS__)

JNIEXPORT jint JNICALL Java_com_mpos_sdk_MposSDK_initMethodID
  (JNIEnv *env, jclass clz){
	LOGI("starting initMethodID...");
	sgClzDownloader = clz;
	// TMS
	//2 寻找class里面的方法
	sgMdConnectTms = (*env)->GetMethodID(env, clz, METHOD_CONNECT_TMS, METHOD_SIG_CONNECT_TMS);
	if(NULL == sgMdConnectTms){
		return -1;
	}
	sgMdCloseTms = (*env)->GetMethodID(env, clz, METHOD_CLOSE_TMS, METHOD_SIG_CLOSE_TMS);
	if(NULL == sgMdCloseTms){
		return -1;
	}
	sgMdResetTms = (*env)->GetMethodID(env, clz, METHOD_RESET_TMS, METHOD_SIG_RESET_TMS);
	if(NULL == sgMdResetTms){
		return -1;
	}
	sgMdRecvTms = (*env)->GetMethodID(env, clz, METHOD_RECV_TMS, METHOD_SIG_RECV_TMS);
	if(NULL == sgMdRecvTms){
		return -1;
	}
	sgMdSendTms = (*env)->GetMethodID(env, clz, METHOD_SEND_TMS, METHOD_SIG_SEND_TMS);
	if(NULL == sgMdSendTms){
		return -1;
	}
	sgMdIsConnectedTms = (*env)->GetMethodID(env, clz, METHOD_ISCONNECTED_TMS, METHOD_SIG_ISCONNECTED_TMS);
	if(NULL == sgMdIsConnectedTms){
		return -1;
	}
	// MPOS
	sgMdConnectMpos = (*env)->GetMethodID(env, clz, METHOD_CONNECT_MPOS, METHOD_SIG_CONNECT_MPOS);
	if(NULL == sgMdConnectMpos){
		return -1;
	}
	sgMdCloseMpos = (*env)->GetMethodID(env, clz, METHOD_CLOSE_MPOS, METHOD_SIG_CLOSE_MPOS);
	if(NULL == sgMdCloseMpos){
		return -1;
	}
	sgMdResetMpos = (*env)->GetMethodID(env, clz, METHOD_RESET_MPOS, METHOD_SIG_RESET_MPOS);
	if(NULL == sgMdResetMpos){
		return -1;
	}
	sgMdRecvMpos = (*env)->GetMethodID(env, clz, METHOD_RECV_MPOS, METHOD_SIG_RECV_MPOS);
	if(NULL == sgMdRecvMpos){
		return -1;
	}
	sgMdSendMpos = (*env)->GetMethodID(env, clz, METHOD_SEND_MPOS, METHOD_SIG_SEND_MPOS);
	if(NULL == sgMdSendMpos){
		return -1;
	}
	sgMdIsConnectedMpos = (*env)->GetMethodID(env, clz, METHOD_ISCONNECTED_MPOS, METHOD_SIG_ISCONNECTED_MPOS);
	if(NULL == sgMdIsConnectedMpos){
		return -1;
	}
	sgMdGetTermId = (*env)->GetMethodID(env, clz, METHOD_GET_TERM_ID, METHOD_SIG_GET_TERM_ID);
	if(NULL == sgMdGetTermId){
		return -1;
	}
	sgMdGetPath = (*env)->GetMethodID(env, clz, METHOD_GET_PATH, METHOD_SIG_GET_PATH);
	if(NULL == sgMdGetPath){
		return -1;
	}
	sgMdGetCallMode = (*env)->GetMethodID(env, clz, METHOD_GET_CALL_MODE, METHOD_SIG_GET_CALL_MODE);
	if(NULL == sgMdGetCallMode){
		return -1;
	}
	sgMdSetProgress = (*env)->GetMethodID(env, clz, METHOD_SET_PROGRESS, METHOD_SIG_SET_PROGRESS);
	if(NULL == sgMdSetProgress){
		return -1;
	}
	LOGI("finish  initMethodID...");
	return 0;
}

JNIEXPORT jint JNICALL Java_com_mpos_sdk_MposSDK_initEnv
  (JNIEnv *env, jobject obj){
  int result, state;
   JavaVM *jvm;
	LOGI("start   initEnv...");


    (*env)->GetJavaVM(env, &g_jvm); //保存到全局变量中JVM
	//sgEnv = env;
	//sgObj = (*env)->NewGlobalRef(env, obj);
    //state = g_jvm->GetEnv(g_jvm, (void**) &sgEnv, NULL);



    //(*g_jvm)->AttachCurrentThread(g_jvm, &sgEnv, NULL);
    sgObj = (*env)->NewGlobalRef(env, obj);
	LOGI("finish  initEnv...");
	return 0;
}

JNIEXPORT jint JNICALL Java_com_mpos_sdk_MposSDK_download
  (JNIEnv *env, jobject obj){
	int iRet = 0;
	int iCnt = 0;
	int iStatus = 0;
	ST_TERMINAL stTerminal;
	ST_CONTROL  stControl;
	unsigned char szTmpName[128];
	unsigned char szPathName[128];
	unsigned char szFileName[128];
	unsigned char sExTermInfo[8192];
	unsigned char sTaskBuff[RECV_BUFFER_SIZE];
	unsigned long ulFileSize;
	TASK_TABLE stRemoteTaskTables[PROTIMS_MAX_TASK];
	TASK_TABLE stPukTaskTables[PROTIMS_MAX_TASK];
	TASK_TABLE stLastTaskTables[PROTIMS_MAX_TASK];
	int iRemoteNum = 0, iPukNum = 0;

	LOGI("starting download...");

	// connect TMS
	DroidSetProgress(TYPE_TMS, STEP_TMS_CONNECT, DONE_START, 0, 0, 0, 0, 0, 0);
	iRet = TmsConnect();
	if(0 == iRet) // 0-false
	{
		LOGE("fail to connect TMS, iRet=%d", 1);
		DroidSetProgress(TYPE_TMS, STEP_TMS_CONNECT, DONE_FINISH, 0, 0, 0, 0, 0, STEP_TMS_CONNECT);
		return 0;
	}
	DroidSetProgress(TYPE_TMS, STEP_TMS_CONNECT, DONE_FINISH, 0, 0, 0, 0, 0, 0);
	TmsReset();


	// connect MPOS
	DroidSetProgress(TYPE_MPOS, STEP_MPOS_CONNECT, DONE_START, 0, 0, 0, 0, 0, 0);
	LOGI("MposConnect befor");
	iRet = MposConnect();
	LOGI("MposConnect return:%d", iRet);
	if(0 == iRet)// 0-false
	{
		LOGE("fail to connect MPOS, iRet=%d", iRet);
		DroidSetProgress(TYPE_MPOS, STEP_MPOS_CONNECT, DONE_FINISH, 0, 0, 0, 0, 0, STEP_MPOS_CONNECT);
		return 0;
	}
	MposReset();
	DroidSetProgress(TYPE_MPOS, STEP_MPOS_CONNECT, DONE_FINISH, 0, 0, 0, 0, 0, 0);

	LOGI("starting protocol...");


	// TMS handshake
	DroidSetProgress(TYPE_TMS, STEP_TMS_HANDSHAKE, DONE_START, 0, 0, 0, 0, 0, 0);
	iRet = ProTimsHandshake();
	LOGI("ProTimsHandshake, iRet=%d", iRet);
	if(0 != iRet)
	{
		LOGE("fail to TMS handshake, iRet=%d", iRet);
		DroidSetProgress(TYPE_TMS, STEP_TMS_HANDSHAKE, DONE_FINISH, 0, 0, 0, 0, 0, iRet);
		return iRet;
	}
	DroidSetProgress(TYPE_TMS, STEP_TMS_HANDSHAKE, DONE_FINISH, 0, 0, 0, 0, 0, 0);

	// terminal information
	strcpy(stTerminal.MonVer, "1.0.0");
	memset(&stTerminal, 0x00, sizeof(ST_TERMINAL));
	iRet = MposGetTermInfo(stTerminal.TermInfo);
	LOGI("MposGetTermInfo, iRet=%d", iRet);
	LogHexData(stTerminal.TermInfo, sizeof(stTerminal.TermInfo));
	if(0 != iRet)
	{
		LOGI("MPOS : fail to get terminal information");
		DroidSetProgress(TYPE_MPOS, STEP_MPOS_TERM_INFO, DONE_FINISH, 0, 0, 0, 0, 0, iRet);
		// when fail, continue
	}

	// SN
	iRet = MposReadSN(stTerminal.SN);
	LOGI("MposReadSN, iRet=%d", iRet);
	LogHexData(stTerminal.SN, sizeof(stTerminal.SN));
	if(0 != iRet)
	{
		LOGI("MPOS : fail to get terminal SN");
		DroidSetProgress(TYPE_MPOS, STEP_MPOS_SN, DONE_FINISH, 0, 0, 0, 0, 0, iRet);
		// when fail, continue
	}

    //开启此处，settasklist步骤失败
	//iRet = MposReadExSN(stTerminal.ExSN);
	//LOGI("MposReadExSN, iRet=%d", iRet);
	//LogHexData(stTerminal.ExSN, sizeof(stTerminal.ExSN));

	// version information
	iRet = MposReadVersion(stTerminal.VerInfo);
	LOGI("Terminal Version Information, iRet=%d", iRet);
	LogHexData(stTerminal.VerInfo, sizeof(stTerminal.VerInfo));
	if(0 != iRet)
	{
		LOGI("MPOS : fail to get terminal version information");
		DroidSetProgress(TYPE_MPOS, STEP_MPOS_VERN_INFO, DONE_FINISH, 0, 0, 0, 0, 0, iRet);
		// when fail, continue
	}

	// POS type
	stTerminal.PosType = GetPosType(stTerminal.TermInfo[0]);

	// terminal ID
	iRet = DroidGetTermId(stTerminal.TermID, LEN_TERMINAL_ID);
	LOGI("DroidGetTermId, iRet=%d, TermID=%.8s", iRet, stTerminal.TermID);

	if(8 != iRet || strncmp("00000000", stTerminal.TermID, 8)==0)
	{
		LOGE("DroidGetTermId, Invalid terminal ID");
		DroidSetProgress(TYPE_MPOS, STEP_TMS_TERM_ID, DONE_FINISH, 0, 0, 0, 0, 0, iRet);
		return STEP_TMS_TERM_ID;
	}


	// call mode
	memset(&stControl, 0x00, sizeof(ST_CONTROL));
	int iCallMode = DroidGetCallMode();
	if(iCallMode < 0)
	{
		iCallMode = COMM_TCPIP;
	}
	stControl.CallMode = iCallMode;

	// file path for save
	iRet = DroidGetPath(stControl.PathName, LEN_PATH);
	if(iRet <= 0 || iRet>LEN_PATH || strcmp("", stControl.PathName)==0)
	{
		strcpy(stControl.PathName, "/mnt/sdcard"); 	// defaut file path
	}

	// terminal authentication
	DroidSetProgress(TYPE_TMS, STEP_TMS_TERM_AUTH, DONE_START, 0, 0, 0, 0, 0, 0);
	iRet = ProTimsTermAuth(&stTerminal, &stControl);
	LOGI("ProTimsTermAuth, iRet=%d", iRet);
	if(0 != iRet)
	{
		LOGE("fail to TMS terminal authentication, iRet=%d", iRet);
		DroidSetProgress(TYPE_TMS, STEP_TMS_TERM_AUTH, DONE_FINISH, 0, 0, 0, 0, 0, iRet);
		return iRet;
	}
	DroidSetProgress(TYPE_TMS, STEP_TMS_TERM_AUTH, DONE_FINISH, 0, 0, 0, 0, 0, 0);

	// request upload
	DroidSetProgress(TYPE_TMS, STEP_TMS_REQ_UPLOAD, DONE_START, 0, 0, 0, 0, 0, 0);
	iRet = ProTimsReqUpload(stTerminal.SN, &iStatus);
	LOGI("ProTimsReqUpload, iRet=%d", iRet);
	if(0 != iRet)
	{
		LOGE("fail to TMS request upload, iRet=%d", iRet);
		DroidSetProgress(TYPE_TMS, STEP_TMS_REQ_UPLOAD, DONE_FINISH, 0, 0, 0, 0, 0, iRet);
		return iRet;
	}
	DroidSetProgress(TYPE_TMS, STEP_TMS_REQ_UPLOAD, DONE_FINISH, 0, 0, 0, 0, 0, 0);

	// upload
	if(1 == iStatus)
	{
		DroidSetProgress(TYPE_TMS, STEP_TMS_UPLOAD, DONE_START, 0, 0, 0, 0, 0, 0);
		iRet = ProTimsUploadFile(&stTerminal, sExTermInfo);
		LOGI("ProTimsUploadFile, iRet=%d", iRet);
		if(0 != iRet)
		{
			LOGE("fail to TMS upload, iRet=%d", iRet);
			DroidSetProgress(TYPE_TMS, STEP_TMS_UPLOAD, DONE_FINISH, 0, 0, 0, 0, 0, iRet);
			return iRet;
		}
		DroidSetProgress(TYPE_TMS, STEP_TMS_UPLOAD, DONE_FINISH, 0, 0, 0, 0, 0, 0);
	}

	// get multi-task(task table)
	stControl.CallMode   = 0;
	stControl.AuthResult = 0;
	int iRetry = PROTIMS_GETTASK_RETRY_TIMES;
	while(iRetry)
	{
		DroidSetProgress(TYPE_TMS, STEP_TMS_MULTI_TASK, DONE_START, 0, 0, 0, 0, 0, 0);
		iRet = ProTimsGetMultTask(sTaskBuff, stTerminal.SN, stControl.HostRandomEn, stControl.AuthResult, stControl.CallMode);
		LOGI("ProTimsGetMultTask, iRet=%d", iRet);
		if(0 == iRet)
		{
			break;
		}
		iRetry--;
	}
	if(iRetry<=0 || 0!=iRet)
	{
		LOGE("fail to TMS get multi task, iRet=%d", iRet);
		DroidSetProgress(TYPE_TMS, STEP_TMS_MULTI_TASK, DONE_FINISH, 0, 0, 0, 0, 0, iRet);
		return iRet;
	}

	// parse task table
	DroidSetProgress(TYPE_TMS, STEP_TMS_PARSE_TASK_TABLE, DONE_START, 0, 0, 0, 0, 0, 0);
	iRet = ProTimsParseRemoteTaskTable(sTaskBuff, stRemoteTaskTables, &iRemoteNum, stPukTaskTables, &iPukNum);
	LOGI("ProTimsParseRemoteTaskTable, iRet=%d, iRemoteNum=%d, iPukNum=%d", iRet, iRemoteNum, iPukNum);
	LogTaskTable(stRemoteTaskTables, iRemoteNum);
	LogTaskTable(stPukTaskTables, iPukNum);
	if(iRet <= 0)
	{
		LOGE("fail to TMS parse task table, iRet=%d", iRet);
		DroidSetProgress(TYPE_TMS, STEP_TMS_PARSE_TASK_TABLE, DONE_FINISH, 0, 0, 0, 0, 0, iRet);
		return iRet;
	}

	// parse task table step 2
	DroidSetProgress(TYPE_TMS, STEP_TMS_PARSE_TASK_TABLE2, DONE_START, 0, 0, 0, 0, 0, 0);
	iRet = ProTimsParseRemoteTaskTable2(stRemoteTaskTables, iRemoteNum, stPukTaskTables, iPukNum);
	LOGI("ProTimsParseRemoteTaskTable2, iRet=%d", iRet);
	if(iRet != (iRemoteNum+iPukNum))
	{
		LOGE("fail to TMS parse task table step 2, iRet=%d", iRet);
		DroidSetProgress(TYPE_TMS, STEP_TMS_PARSE_TASK_TABLE2, DONE_FINISH, 0, 0, 0, 0, 0, iRet);
		return iRet;
	}
	iRemoteNum = iRet;
	LogTaskTable(stRemoteTaskTables, iRemoteNum);

	// get last saved task table
	sprintf(szPathName, "%s", stControl.PathName);
	memset(stLastTaskTables, 0x00, sizeof(stLastTaskTables));
	iRet = PrTimsGetLastTaskTable(szPathName, stLastTaskTables);
	if(0 == iRet)
	{
		// remote task table is different from local task table
		DroidSetProgress(TYPE_TMS, STEP_TMS_COMPARE_TASK_LOCAL_TMS, DONE_START, 0, 0, 0, 0, 0, 0);
		if(ProTimsCompareTask(stRemoteTaskTables, stLastTaskTables))
		{
			// delete old temp downloaded file
			DroidSetProgress(TYPE_TMS, STEP_TMS_DELETE_TASK_LOCAL, DONE_START, 0, 0, 0, 0, 0, 0);
			ProTimsDeleteOldTmpFile(stControl.PathName, stRemoteTaskTables);
			DroidSetProgress(TYPE_TMS, STEP_TMS_DELETE_TASK_LOCAL, DONE_FINISH, 0, 0, 0, 0, 0, 0);
		}
	}

	// save local task table
	DroidSetProgress(TYPE_TMS, STEP_TMS_SAVE_TASK_LOCAL, DONE_START, 0, 0, 0, 0, 0, 0);
	iRet = ProTimsSaveTaskTable(szPathName, stRemoteTaskTables, iRemoteNum);
	LOGI("ProTimsSaveTaskTable, iRet=%d", iRet);
	if(0 != iRet)
	{
		LOGE("fail to TMS save task table, iRet=%d", iRet);
		DroidSetProgress(TYPE_TMS, STEP_TMS_SAVE_TASK_LOCAL, DONE_FINISH, 0, 0, 0, 0, 0, iRet);
		return iRet;
	}
	DroidSetProgress(TYPE_TMS, STEP_TMS_SAVE_TASK_LOCAL, DONE_FINISH, 0, 0, 0, 0, 0, 0);

	// calc the total size of all task
	stControl.AllTaskSize = 0;
	for(iCnt = 0; iCnt<iRemoteNum; iCnt++)
	{
		stControl.AllTaskSize += stRemoteTaskTables[iCnt].AllSize;
	}
	stControl.TaskNum 	   = iRemoteNum;
	stControl.CurTask 	   = 0;
	stControl.DownloadSize = 0;
	stControl.CurFileSize  = 0;
	LOGI("ProTimsGetFile, iRemoteNum=%d", iRemoteNum);
	LOGI("download, stControl.AllTaskSize=%d", stControl.AllTaskSize);

	// download file from TMS
	TmsReset();
	DroidSetProgress(TYPE_TMS, STEP_TMS_LOAD_FILE, DONE_START, stControl.TaskNum, 0, stControl.AllTaskSize, 0, 0, 0);
	iRet = ProTimsGetFile(stRemoteTaskTables, iRemoteNum, &stTerminal, &stControl);
	LOGI("ProTimsGetFile, iRet=%d", iRet);
	if(0 != iRet)
	{
		LOGE("fail to TMS download file, iRet=%d", iRet);
		DroidSetProgress(TYPE_TMS, STEP_TMS_LOAD_FILE, DONE_FINISH, 0, 0, 0, 0, 0, iRet);
		return iRet;
	}
	DroidSetProgress(TYPE_TMS, STEP_TMS_LOAD_FILE, DONE_FINISH, stControl.TaskNum, stControl.TaskNum, stControl.AllTaskSize, stControl.AllTaskSize, 0, 0);

	// finish communication with TMS
	iRet = ProTimsEnd(stTerminal.SN);
	LOGI("ProTimsEnd, iRet=%d", iRet);
	if(0 != iRet)
	{
		LOGI("TMS : fail to ProTimsEnd, iRet=%d", iRet);
		// when fail, continue
	}
	TmsClose();

	// set task table to MPOS
	stControl.CurTask = 0;
	stControl.DownloadSize = 0;
	DroidSetProgress(TYPE_MPOS, STEP_MPOS_SET_TASK, DONE_START, stControl.TaskNum, 0, stControl.AllTaskSize, 0, 0, 0);
	iRet = MposSetTaskList(stRemoteTaskTables, iRemoteNum);
	LOGI("MposSetTaskList, iRet=%d", iRet);
	if(0 != iRet)
	{
		LOGE("fail to MPOS set task, iRet=%d", iRet);
		DroidSetProgress(TYPE_MPOS, STEP_MPOS_SET_TASK, DONE_FINISH, stControl.TaskNum, 0, stControl.AllTaskSize, 0, 0, iRet);
		return iRet;
	}
	DroidSetProgress(TYPE_MPOS, STEP_MPOS_SET_TASK, DONE_FINISH, stControl.TaskNum, 0, stControl.AllTaskSize, 0, 0, 0);

	if(0 == iRet)
	{
		stControl.AllTaskSize = 0;
		for(iCnt=0; iCnt<iRemoteNum; iCnt++)
		{
			GenTmpFileName(stRemoteTaskTables[iCnt].TaskNo, szTmpName);
			sprintf(szFileName, "%s/%s", stControl.PathName, szTmpName);
			if( NULL==szFileName || strcmp(szFileName, "")==0 )
			{
				LOGE("MPOS : Unsupport File to download...");
				continue;
			}

			ulFileSize = GetFileSizeEX((unsigned char*)szFileName);
			stControl.AllTaskSize += ulFileSize;
		}
		if(0 >= stControl.AllTaskSize)
		{
			MposClose();
			LOGE("MPOS : NO File to download...");
			LOGW("finish   download...");
			return 0;
		}
		LOGE("MPOS : stControl.AllTaskSize=%d...", stControl.AllTaskSize);
		DroidSetProgress(TYPE_MPOS, STEP_MPOS_SAVE_FILE, DONE_START, stControl.TaskNum, 0, stControl.AllTaskSize, 0, 0, 0);
		iRet = MposSaveFileContent(stControl.PathName, stRemoteTaskTables, iRemoteNum, &stControl);
		LOGI("MposSaveFileContent, iRet=%d", iRet);
		if(0 != iRet)
		{
			LOGE("fail to MPOS save file content, iRet=%d", iRet);
			DroidSetProgress(TYPE_MPOS, STEP_MPOS_SAVE_FILE, DONE_FINISH, stControl.TaskNum, stControl.CurTask, stControl.AllTaskSize, stControl.DownloadSize, 0, iRet);
			return iRet;
		}
		DroidSetProgress(TYPE_MPOS, STEP_MPOS_SAVE_FILE, DONE_FINISH, stControl.TaskNum, stControl.CurTask, stControl.AllTaskSize, stControl.DownloadSize, 0, 0);
	}
	MposClose();

	LOGW("finish   download...");
	return 10;
}

JNIEXPORT void JNICALL Java_com_mpos_sdk_MposSDK_release
  (JNIEnv *env, jobject obj)
{
    JNIEnv   *sgEnv;
    LOGE("Java_com_mpos_sdk_MposSDK_release");
    //(*g_jvm)->AttachCurrentThread(g_jvm, &sgEnv, NULL);

	//(*sgEnv)->DeleteGlobalRef(sgEnv, sgObj);
	 LOGE("DetachCurrentThread...");
	//(*g_jvm)->DetachCurrentThread(g_jvm);
	LOGE("Java_com_mpos_sdk_MposSDK_release");

	return;
}

///////////////////////////////////////END JNI////////////////////////////////////////

///////////////////////////////////////INTERNAL///////////////////////////////////////
static jint ExecVoidIntMethod(jmethodID mdId, unsigned char *pszExcCls, unsigned char *pszExcMsg, unsigned char bVoid);
static jint ExecIntMethod(unsigned char bRecv, jmethodID mdId, unsigned char *pszExcCls, unsigned char *pszExcMsg, unsigned char *psDataBuff, unsigned int iDataLen, int iOffset);


//////////////////////////////START MPOS CALLBACK IMPLEMENT///////////////////////////
void MposReset()
{
	ExecVoidIntMethod(sgMdResetMpos, "java/lang/IllegalAceessException", "MPOS : fail to reset connection", 1);
}

void MposClose()
{
	ExecVoidIntMethod(sgMdCloseMpos, "java/lang/IllegalAceessException", "MPOS : fail to close connection", 1);
}

int MposConnect()
{
	return ExecVoidIntMethod(sgMdConnectMpos, "java/lang/IllegalAceessException", "MPOS : fail to set up connection", 0);
}

int MposRecv(unsigned char *ucRecv)
{
	return MposRecvs(ucRecv, 1);
}

int MposSend(unsigned char ucSend)
{
	unsigned char sBuff[2];
	sBuff[0] = ucSend;
	return MposSends(sBuff, 1);
}

int MposRecvs(unsigned char *psRecvBuff, int iRecvLen)
{
	return ExecIntMethod(1, sgMdRecvMpos, "java/lang/IllegalAceessException", "MPOS : fail to receive data",
			psRecvBuff, iRecvLen, 0);
}

int MposSends(unsigned char *psSendBuff, int iSendLen)
{
LOGE("jni MposSends");
	return ExecIntMethod(0, sgMdSendMpos, "java/lang/IllegalAceessException", "MPOS : fail to send data",
					psSendBuff, iSendLen, 0);
}

//////////////////////////////////END  MPOS CALLBACK IMPLEMENT/////////////////////////////////

//////////////////////////////////START TMS CALLBACK IMPLEMENT/////////////////////////////////
void TmsReset()
{
	ExecVoidIntMethod(sgMdResetTms, "java/lang/IllegalAceessException", "TMS : fail to reset connection", 1);
}

void TmsClose()
{
	ExecVoidIntMethod(sgMdCloseTms, "java/lang/IllegalAceessException", "TMS : fail to close connection", 1);
}

int TmsConnect()
{
	return ExecVoidIntMethod(sgMdConnectTms, "java/lang/IllegalAceessException", "TMS : fail to set up connection", 0);
}

int TmsRecvs(unsigned char *sRecvBuff, int iRecvLen)
{
	return ExecIntMethod(1, sgMdRecvTms, "java/io/IOException", "TMS : fail to receive data",
			sRecvBuff, iRecvLen, 0);
}

int TmsSends(unsigned char *sSendBuff, int iSendLen)
{
	return ExecIntMethod(0, sgMdSendTms, "java/lang/IllegalAceessException", "TMS : fail to send data",
				sSendBuff, iSendLen, 0);
}
//////////////////////////////////END TMS CALLBACK IMPLEMENT/////////////////////////////////

void DroidSetProgress(int iType, int iStep, int iDoneFlag, int iFileCount,
		int iCurFile, int iTotal, int iCurSize, int iCurFileSize, int iStatus)
{
    JNIEnv   *sgEnv;
    (*g_jvm)->AttachCurrentThread(g_jvm, &sgEnv, NULL);

LOGE("DroidSetProgress in");
	if(NULL == sgEnv)
	{
		return;
	}
	(*sgEnv)->ExceptionClear(sgEnv);
	(*sgEnv)->CallVoidMethod(sgEnv, sgObj, sgMdSetProgress, iType, iStep, iDoneFlag, iFileCount, iCurFile, iTotal, iCurSize, iCurFileSize, iStatus);
	if((*sgEnv)->ExceptionOccurred(sgEnv))
	{
		LOGE("fail to set progress");
		return;
	}
	LOGE("DroidSetProgress out");
	return;
}

int DroidGetTermId(unsigned char *pszOutTermId, int iMaxLen)
{
	int iLen = 0;
	ExecStringGetMethod(sgMdGetTermId, "java/lang/IllegalAceessException",
			            "Droid : fail to invoke getString method", pszOutTermId, 0, iMaxLen, &iLen);
	return iLen;
}

int DroidGetPath(unsigned char *pszOutPath, int iMaxLen)
{
	int iLen = 0;
	ExecStringGetMethod(sgMdGetPath, "java/lang/IllegalAceessException",
			            "Droid : fail to invoke getString method", pszOutPath, 0, iMaxLen, &iLen);
	return iLen;
}

int DroidGetCallMode()
{
	return ExecVoidIntMethod(sgMdGetCallMode, "java/lang/IllegalAceessException", "TMS : fail to get call mode", 0);
}

/////////////////////////////////////////INTENAL IMPLEMENT///////////////////////////////////
jint ExecVoidIntMethod(jmethodID mdId, unsigned char *pszExcCls, unsigned char *pszExcMsg, unsigned char bVoid)
{
	jthrowable exc;
	jint iRet = 0;

    JNIEnv   *sgEnv;
    (*g_jvm)->AttachCurrentThread(g_jvm, &sgEnv, NULL);


	if(NULL == sgEnv)
	{
		return -1000;
	}
	(*sgEnv)->ExceptionClear(sgEnv);
	if(bVoid)
	{
		(*sgEnv)->CallVoidMethod(sgEnv, sgObj, mdId);
	}
	else
	{
		iRet = (*sgEnv)->CallIntMethod(sgEnv, sgObj, mdId);
	}
	exc = (*sgEnv)->ExceptionOccurred(sgEnv);
	if(exc)
	{
		jclass  excClz;
		(*sgEnv)->ExceptionDescribe(sgEnv);
		(*sgEnv)->ExceptionClear(sgEnv);
		if(pszExcCls && strcmp(pszExcCls, "")!=0 )
		{
			excClz = (*sgEnv)->FindClass(sgEnv, pszExcCls);//得到对象的类句柄
		}
		else
		{
			excClz = (*sgEnv)->FindClass(sgEnv, "java/lang/IllegalAceessException");
		}
		if(NULL == excClz) {
			LOGI("exit   ExecVoidIntMethod...");
			if(bVoid)
			{
			    return -1;
				//return; // unable to find the exception class, give up
			}
			else
			{
				return -1001;
			}
		}
		if(pszExcMsg)
		{
			(*sgEnv)->ThrowNew(sgEnv, excClz, pszExcMsg);
		}
		else
		{
			(*sgEnv)->ThrowNew(sgEnv, excClz, "ExecVoidMethod, no message->null");
		}
	}
	return iRet;
}


jint ExecIntMethod(unsigned char bRecv, jmethodID mdId, unsigned char *pszExcCls, unsigned char *pszExcMsg, unsigned char *psDataBuff, unsigned int iDataLen, int iOffset)
{
	jthrowable exc;
	jint iRet = 0;

    JNIEnv   *sgEnv;
    (*g_jvm)->AttachCurrentThread(g_jvm, &sgEnv, NULL);

     LOGE("ExecIntMethod in");
	if(NULL == sgEnv || NULL==psDataBuff)
	{
		return -1000;
	}
	if(0 == iDataLen)
	{
		return 0;
	}
	(*sgEnv)->ExceptionClear(sgEnv);
	jbyteArray data = (*sgEnv)->NewByteArray(sgEnv, iDataLen);
	if(NULL == data)
	{
	    LOGE("DeleteLocalRef 735");
		(*sgEnv)->DeleteLocalRef(sgEnv, data);
		return -1000;
	}
	(*sgEnv)->ExceptionClear(sgEnv);

	(*sgEnv)->SetByteArrayRegion(sgEnv, data, iOffset, iDataLen, psDataBuff);
	exc = (*sgEnv)->ExceptionOccurred(sgEnv);
	if(exc)
	{
	    LOGE("DeleteLocalRef 744");
		(*sgEnv)->DeleteLocalRef(sgEnv, data);
		return -1000;
	}
	(*sgEnv)->ExceptionClear(sgEnv);
	//3 .调用这个方法
	iRet = (*sgEnv)->CallIntMethod(sgEnv, sgObj, mdId, data, 0, iDataLen);
	exc = (*sgEnv)->ExceptionOccurred(sgEnv);
	if(exc)
	{
		jclass  excClz;
		(*sgEnv)->ExceptionDescribe(sgEnv);
		(*sgEnv)->ExceptionClear(sgEnv);
		if(pszExcCls && strcmp(pszExcCls, "")!=0 )
		{
		 LOGE("java/io/IOException 759");
			excClz = (*sgEnv)->FindClass(sgEnv, pszExcCls);
			LOGE("java/io/IOException 761");
		}
		else
		{
			excClz = (*sgEnv)->FindClass(sgEnv, "java/lang/IllegalAceessException");
		}
		if(NULL == excClz) {
		LOGE("java/io/IOException 766");
			(*sgEnv)->DeleteLocalRef(sgEnv, data);
			return -1001;
		}
		if(pszExcMsg)
		{
		    LOGE("java/io/IOException 774");

			(*sgEnv)->ThrowNew(sgEnv, excClz, pszExcMsg);
			LOGE("java/io/IOException 776");
		}
		else
		{
			(*sgEnv)->ThrowNew(sgEnv, excClz, "ExecVoidMethod, no message->null");
		}
	}

	if(bRecv && iRet > 0)
	{
		jsize iArrLen = (*sgEnv)->GetArrayLength(sgEnv, data);
		(*sgEnv)->ExceptionClear(sgEnv);
		(*sgEnv)->GetByteArrayRegion(sgEnv, data, 0, iRet, psDataBuff);
		exc = (*sgEnv)->ExceptionOccurred(sgEnv);
		if (exc)
		{
		    LOGE("DeleteLocalRef 788");
			(*sgEnv)->DeleteLocalRef(sgEnv, data);
			return -2000;
		}
	}
	LOGE("DeleteLocalRef 793");
	(*sgEnv)->DeleteLocalRef(sgEnv, data);

	LOGE("ExecIntMethod out");
	return iRet;
}

int ExecStringGetMethod(jmethodID mdId, unsigned char *pszExcCls, unsigned char *pszExcMsg, unsigned char *psOutDataBuff, int iOffset, unsigned int iMaxLen, int *iOutLen)
{
	jthrowable exc;
	jstring    sRetVal;
	const char *pszRetVal = NULL;
	int   iValLen = 0;
	jint iRet = 0;

    JNIEnv   *sgEnv;
    (*g_jvm)->AttachCurrentThread(g_jvm, &sgEnv, NULL);

	if(NULL == sgEnv || NULL==psOutDataBuff)
	{
		return -1;
	}
	if(0 >= iMaxLen || psOutDataBuff==NULL || iOffset<0)
	{
		return -2;
	}

	(*sgEnv)->ExceptionClear(sgEnv);
	sRetVal = (*sgEnv)->CallObjectMethod(sgEnv, sgObj, mdId);
	exc = (*sgEnv)->ExceptionOccurred(sgEnv);
	if(exc)
	{
		jclass  excClz;
		(*sgEnv)->ExceptionDescribe(sgEnv);
		(*sgEnv)->ExceptionClear(sgEnv);
		if(pszExcCls && strcmp(pszExcCls, "")!=0 )
		{
			excClz = (*sgEnv)->FindClass(sgEnv, pszExcCls);
		}
		else
		{
			excClz = (*sgEnv)->FindClass(sgEnv, "java/lang/IllegalAceessException");
		}
		if(NULL == excClz)
		{
			return -1000;
		}
		if(pszExcMsg)
		{
			(*sgEnv)->ThrowNew(sgEnv, excClz, pszExcMsg);
		}
		else
		{
			(*sgEnv)->ThrowNew(sgEnv, excClz, "ExecVoidMethod, no message->null");
		}
	}

	pszRetVal = (*sgEnv)->GetStringUTFChars(sgEnv, sRetVal, NULL);//取得java传入过来的String对象,开辟内存
	if(NULL == pszRetVal)
	{
		*iOutLen = -1;
		return 0;
	}
	iValLen = strlen(pszRetVal);
	if(0 == iValLen)
	{
		*iOutLen = 0;
		return 0;
	}
	if(iValLen > iMaxLen)
	{
		iValLen  = iMaxLen;
		*iOutLen = iMaxLen;
	}
	else
	{
		*iOutLen = iValLen;
	}
	strncpy(psOutDataBuff+iOffset, pszRetVal, iValLen);
	(*sgEnv)->ReleaseStringUTFChars(sgEnv, sRetVal, pszRetVal);//用完释放掉掉內存
	pszRetVal = NULL;
	return 0;
}

JNIEXPORT jbyteArray JNICALL Java_com_mpos_sdk_MposSDK_getTermSN
  (JNIEnv * env, jobject obj){
    unsigned char termSn[8 + 1] = {0x00};
    jint iRet;
    LOGI("开始 MposReadSN");
    iRet = MposReadSN(termSn);
    LOGI("MposReadSN return:%d, termSn:%s", iRet, termSn);


    jbyteArray jarrRV = (*env)->NewByteArray(env,8);
    jbyte *jby = (*env)->GetByteArrayElements(env,jarrRV, 0);
    memcpy(jby, termSn, 8);
    (*env)->SetByteArrayRegion(env,jarrRV, 0, 8, jby);

    return jarrRV;
 }

 /*
  * Class:     com_mpos_sdk_MposSDK
  * Method:    getTermSn
  * Signature: ([B)I
  */
 JNIEXPORT jint JNICALL Java_com_mpos_sdk_MposSDK_getTermSn
   (JNIEnv *env, jobject obj, jbyteArray jarrRV){
       unsigned char termSn[8 + 1] = {0x00};
       jint iRet;
       LOGI("开始 MposReadSN");
       iRet = MposReadSN(termSn);
       LOGI("MposReadSN return:%d, termSn:%s", iRet, termSn);
       if(iRet != 0){
            return -1;
       }

       //jbyteArray jarrRV = (*env)->NewByteArray(env,8);
       jbyte *jby = (*env)->GetByteArrayElements(env,jarrRV, 0);
       memcpy(jby, termSn, 8);
       (*env)->SetByteArrayRegion(env,jarrRV, 0, 8, jby);

       return 0;
   }

 JNIEXPORT jbyteArray JNICALL Java_com_mpos_sdk_MposSDK_getTermExtSN
   (JNIEnv * env, jobject obj){
        unsigned char exSn[24 + 1] = {0x00};
        jint iRet;
        LOGI("开始 MposReadExSN");
        iRet = MposReadExSN(exSn);
        LOGI("MposReadSN return:%d, exSn:%s", iRet, exSn);


        jbyteArray jarrRV = (*env)->NewByteArray(env,24);
        jbyte *jby = (*env)->GetByteArrayElements(env,jarrRV, 0);
        memcpy(jby, exSn, 24);
        (*env)->SetByteArrayRegion(env,jarrRV, 0, 24, jby);

        return jarrRV;

}

JNIEXPORT jbyteArray JNICALL Java_com_mpos_sdk_MposSDK_getTermVerInfo
  (JNIEnv * env, jobject obj){
    unsigned char verInfo[8 + 1] = {0x00};
    jint iRet;
    LOGI("开始 MposReadVersion");
    iRet = MposReadVersion(verInfo);
    LOGI("MposReadVersion return:%d", iRet);

    jbyteArray jarrRV = (*env)->NewByteArray(env, 8);
    jbyte *jby = (*env)->GetByteArrayElements(env, jarrRV, 0);
    memcpy(jby, verInfo, 8);
    (*env)->SetByteArrayRegion(env, jarrRV, 0, 8, jby);

    return jarrRV;
}

JNIEXPORT jbyteArray JNICALL Java_com_mpos_sdk_MposSDK_getTerminalInfo
  (JNIEnv * env, jobject obj){
    unsigned char terInfo[30+ 1] = {0x00};
    jint iRet;
    LOGI("开始 MposGetTermInfo");
    iRet = MposGetTermInfo(terInfo);
    LOGI("MposGetTermInfo return:%d",iRet);


    jbyteArray jarrRV = (*env)->NewByteArray(env, 30);
    jbyte *jby = (*env)->GetByteArrayElements(env, jarrRV, 0);
    memcpy(jby, terInfo, 30);
    (*env)->SetByteArrayRegion(env, jarrRV, 0, 30, jby);

    return jarrRV;
}

/*
 * Class:     com_mpos_sdk_MposSDK
 * Method:    isUpdate
 * Signature: ()Z
 */
JNIEXPORT jint JNICALL Java_com_mpos_sdk_MposSDK_isUpdate
  (JNIEnv * env, jobject obj){
  	int iRet = 0;
  	int iCnt = 0;
  	int iStatus = 0;
  	ST_TERMINAL stTerminal;
  	ST_CONTROL  stControl;
  	unsigned char szTmpName[128];
  	unsigned char szPathName[128];
  	unsigned char szFileName[128];
  	unsigned char sExTermInfo[8192];
  	unsigned char sTaskBuff[RECV_BUFFER_SIZE];
  	unsigned long ulFileSize;
  	TASK_TABLE stRemoteTaskTables[PROTIMS_MAX_TASK];
  	TASK_TABLE stPukTaskTables[PROTIMS_MAX_TASK];
  	TASK_TABLE stLastTaskTables[PROTIMS_MAX_TASK];
  	int iRemoteNum = 0, iPukNum = 0;

  	LOGI("starting download...");

  	// connect TMS
  	//DroidSetProgress(TYPE_TMS, STEP_TMS_CONNECT, DONE_START, 0, 0, 0, 0, 0, 0);
  	iRet = TmsConnect();
  	LOGE("TmsConnect, iRet=%d", iRet);
  	if(0 == iRet) // 0-false
  	{
  		LOGE("fail to connect TMS, iRet=%d", 1);
  		//DroidSetProgress(TYPE_TMS, STEP_TMS_CONNECT, DONE_FINISH, 0, 0, 0, 0, 0, STEP_TMS_CONNECT);
  		return 0;
  	}
  	//DroidSetProgress(TYPE_TMS, STEP_TMS_CONNECT, DONE_FINISH, 0, 0, 0, 0, 0, 0);
  	TmsReset();


/*
  	// connect MPOS
  	//DroidSetProgress(TYPE_MPOS, STEP_MPOS_CONNECT, DONE_START, 0, 0, 0, 0, 0, 0);
  	LOGI("MposConnect befor");
  	iRet = MposConnect();
  	LOGI("MposConnect return:%d", iRet);
  	if(0 == iRet)// 0-false
  	{
  		LOGE("fail to connect MPOS, iRet=%d", iRet);
  		//DroidSetProgress(TYPE_MPOS, STEP_MPOS_CONNECT, DONE_FINISH, 0, 0, 0, 0, 0, STEP_MPOS_CONNECT);
  		return 0;
  	}
  	MposReset();
  	//DroidSetProgress(TYPE_MPOS, STEP_MPOS_CONNECT, DONE_FINISH, 0, 0, 0, 0, 0, 0);
*/
  	LOGI("starting protocol...");


  	// TMS handshake
  	//DroidSetProgress(TYPE_TMS, STEP_TMS_HANDSHAKE, DONE_START, 0, 0, 0, 0, 0, 0);
  	iRet = ProTimsHandshake();
  	LOGI("ProTimsHandshake, iRet=%d", iRet);
  	if(0 != iRet)
  	{
  		LOGE("fail to TMS handshake, iRet=%d", iRet);
  		//DroidSetProgress(TYPE_TMS, STEP_TMS_HANDSHAKE, DONE_FINISH, 0, 0, 0, 0, 0, iRet);
  		return iRet;
  	}
  	//DroidSetProgress(TYPE_TMS, STEP_TMS_HANDSHAKE, DONE_FINISH, 0, 0, 0, 0, 0, 0);


  	// terminal information
  	strcpy(stTerminal.MonVer, "1.0.0");
  	memset(&stTerminal, 0x00, sizeof(ST_TERMINAL));
 /*
  	iRet = MposGetTermInfo(stTerminal.TermInfo);
  	LOGI("MposGetTermInfo, iRet=%d", iRet);
  	LogHexData(stTerminal.TermInfo, sizeof(stTerminal.TermInfo));
  	if(0 != iRet)
  	{
  		LOGI("MPOS : fail to get terminal information");
  		//DroidSetProgress(TYPE_MPOS, STEP_MPOS_TERM_INFO, DONE_FINISH, 0, 0, 0, 0, 0, iRet);
  		// when fail, continue
  	}

  	// SN
  	iRet = MposReadSN(stTerminal.SN);
  	LOGI("MposReadSN, iRet=%d", iRet);
  	LogHexData(stTerminal.SN, sizeof(stTerminal.SN));
  	if(0 != iRet)
  	{
  		LOGI("MPOS : fail to get terminal SN");
  		//DroidSetProgress(TYPE_MPOS, STEP_MPOS_SN, DONE_FINISH, 0, 0, 0, 0, 0, iRet);
  		// when fail, continue
  	}

  	iRet = MposReadExSN(stTerminal.ExSN);
  	LOGI("MposReadExSN, iRet=%d", iRet);
  	LogHexData(stTerminal.ExSN, sizeof(stTerminal.ExSN));

  	// version information
  	iRet = MposReadVersion(stTerminal.VerInfo);
  	LOGI("Terminal Version Information, iRet=%d", iRet);
  	LogHexData(stTerminal.VerInfo, sizeof(stTerminal.VerInfo));
  	if(0 != iRet)
  	{
  		LOGI("MPOS : fail to get terminal version information");
  		//DroidSetProgress(TYPE_MPOS, STEP_MPOS_VERN_INFO, DONE_FINISH, 0, 0, 0, 0, 0, iRet);
  		// when fail, continue
  	}

*/
	LogHexData(stTerminal.TermInfo, sizeof(stTerminal.TermInfo));
	LogHexData(stTerminal.SN, sizeof(stTerminal.SN));
	LogHexData(stTerminal.ExSN, sizeof(stTerminal.ExSN));
	LogHexData(stTerminal.VerInfo, sizeof(stTerminal.VerInfo));

  	// POS type
  	//stTerminal.PosType = GetPosType(stTerminal.TermInfo[0]);

  	// terminal ID
  	iRet = DroidGetTermId(stTerminal.TermID, LEN_TERMINAL_ID);
  	LOGI("DroidGetTermId, iRet=%d, TermID=%.8s", iRet, stTerminal.TermID);

  	if(8 != iRet || strncmp("00000000", stTerminal.TermID, 8)==0)
  	{
  		LOGE("DroidGetTermId, Invalid terminal ID");
  		//DroidSetProgress(TYPE_MPOS, STEP_TMS_TERM_ID, DONE_FINISH, 0, 0, 0, 0, 0, iRet);
  		return STEP_TMS_TERM_ID;
  	}


  	// call mode
  	memset(&stControl, 0x00, sizeof(ST_CONTROL));
  	int iCallMode = DroidGetCallMode();
  	if(iCallMode < 0)
  	{
  		iCallMode = COMM_TCPIP;
  	}
  	stControl.CallMode = iCallMode;

  	// file path for save
  	iRet = DroidGetPath(stControl.PathName, LEN_PATH);
  	  	LOGI("stControl.PathName=%s", stControl.PathName);
  	if(iRet <= 0 || iRet>LEN_PATH || strcmp("", stControl.PathName)==0)
  	{
  		strcpy(stControl.PathName, "/mnt/sdcard"); 	// defaut file path
  	}

  	// terminal authentication
  	//DroidSetProgress(TYPE_TMS, STEP_TMS_TERM_AUTH, DONE_START, 0, 0, 0, 0, 0, 0);
  	iRet = ProTimsTermAuth(&stTerminal, &stControl);
  	LOGI("ProTimsTermAuth, iRet=%d", iRet);
  	LOGE("0000000000");
  	if(0 != iRet)
  	{
  		LOGE("fail to TMS terminal authentication, iRet=%d", iRet);
  		//DroidSetProgress(TYPE_TMS, STEP_TMS_TERM_AUTH, DONE_FINISH, 0, 0, 0, 0, 0, iRet);
  		return iRet;
  	}
  	//DroidSetProgress(TYPE_TMS, STEP_TMS_TERM_AUTH, DONE_FINISH, 0, 0, 0, 0, 0, 0);

  	// request upload
  	//DroidSetProgress(TYPE_TMS, STEP_TMS_REQ_UPLOAD, DONE_START, 0, 0, 0, 0, 0, 0);
  	iRet = ProTimsReqUpload(stTerminal.SN, &iStatus);
  	LOGI("ProTimsReqUpload, iRet=%d", iRet);
  	if(0 != iRet)
  	{
  		LOGE("fail to TMS request upload, iRet=%d", iRet);
  		//DroidSetProgress(TYPE_TMS, STEP_TMS_REQ_UPLOAD, DONE_FINISH, 0, 0, 0, 0, 0, iRet);
  		return iRet;
  	}
  	//DroidSetProgress(TYPE_TMS, STEP_TMS_REQ_UPLOAD, DONE_FINISH, 0, 0, 0, 0, 0, 0);

  	// upload
  	if(1 == iStatus)
  	{
  		//DroidSetProgress(TYPE_TMS, STEP_TMS_UPLOAD, DONE_START, 0, 0, 0, 0, 0, 0);
  		iRet = ProTimsUploadFile(&stTerminal, sExTermInfo);
  		LOGI("ProTimsUploadFile, iRet=%d", iRet);
  		if(0 != iRet)
  		{
  			LOGE("fail to TMS upload, iRet=%d", iRet);
  			//DroidSetProgress(TYPE_TMS, STEP_TMS_UPLOAD, DONE_FINISH, 0, 0, 0, 0, 0, iRet);
  			return iRet;
  		}
  		//DroidSetProgress(TYPE_TMS, STEP_TMS_UPLOAD, DONE_FINISH, 0, 0, 0, 0, 0, 0);
  	}

  	// get multi-task(task table)
  	stControl.CallMode   = 0;
  	stControl.AuthResult = 0;
  	int iRetry = PROTIMS_GETTASK_RETRY_TIMES;
  	while(iRetry)
  	{
  		//DroidSetProgress(TYPE_TMS, STEP_TMS_MULTI_TASK, DONE_START, 0, 0, 0, 0, 0, 0);
  		iRet = ProTimsGetMultTask(sTaskBuff, stTerminal.SN, stControl.HostRandomEn, stControl.AuthResult, stControl.CallMode);
  		LOGI("ProTimsGetMultTask, iRet=%d", iRet);
  		if(0 == iRet)
  		{
  			break;
  		}
  		iRetry--;
  	}
  	if(iRetry<=0 || 0!=iRet)
  	{
  		LOGE("fail to TMS get multi task, iRet=%d,iRetry=%d", iRet, iRetry);
  		//DroidSetProgress(TYPE_TMS, STEP_TMS_MULTI_TASK, DONE_FINISH, 0, 0, 0, 0, 0, iRet);
  		return iRet;
  	}

  	// parse task table
  	//DroidSetProgress(TYPE_TMS, STEP_TMS_PARSE_TASK_TABLE, DONE_START, 0, 0, 0, 0, 0, 0);
  	iRet = ProTimsParseRemoteTaskTable(sTaskBuff, stRemoteTaskTables, &iRemoteNum, stPukTaskTables, &iPukNum);
  	LOGI("ProTimsParseRemoteTaskTable, iRet=%d, iRemoteNum=%d, iPukNum=%d", iRet, iRemoteNum, iPukNum);
  	LogTaskTable(stRemoteTaskTables, iRemoteNum);
  	LogTaskTable(stPukTaskTables, iPukNum);
  	if(iRet <= 0)
  	{
  		LOGE("fail to TMS parse task table, iRet=%d", iRet);
  		//DroidSetProgress(TYPE_TMS, STEP_TMS_PARSE_TASK_TABLE, DONE_FINISH, 0, 0, 0, 0, 0, iRet);
  		return iRet;
  	}

  	// parse task table step 2
  	//DroidSetProgress(TYPE_TMS, STEP_TMS_PARSE_TASK_TABLE2, DONE_START, 0, 0, 0, 0, 0, 0);
  	iRet = ProTimsParseRemoteTaskTable2(stRemoteTaskTables, iRemoteNum, stPukTaskTables, iPukNum);
  	LOGI("ProTimsParseRemoteTaskTable2, iRet=%d", iRet);
  	if(iRet != (iRemoteNum+iPukNum))
  	{
  		LOGE("fail to TMS parse task table step 2, iRet=%d", iRet);
  		//DroidSetProgress(TYPE_TMS, STEP_TMS_PARSE_TASK_TABLE2, DONE_FINISH, 0, 0, 0, 0, 0, iRet);
  		return iRet;
  	}
  	iRemoteNum = iRet;
  	LogTaskTable(stRemoteTaskTables, iRemoteNum);

  	if(iRemoteNum > 0){
  	    return 10;//表示有更新
  	}else{
  	    return -1;//表示不可更新
  	}


 }




