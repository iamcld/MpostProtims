#ifndef PAX_MPOS_PROTIMS_ANDROID_CALLBACK_H_ 
#define PAX_MPOS_PROTIMS_ANDROID_CALLBACK_H_

#ifndef NULL
#ifdef __cplusplus
	#define NULL    0
#else
	#define NULL    ((void *)0)
#endif // __cplusplus
#endif // NULL

#define TYPE_TMS	1
#define TYPE_MPOS	2

#define DONE_START	1
#define DONE_DOING	2
#define DONE_FINISH	3

#define STEP_TMS_CONNECT				101
#define STEP_TMS_HANDSHAKE				102
#define STEP_TMS_TERM_AUTH				103
#define STEP_TMS_REQ_UPLOAD				104
#define STEP_TMS_UPLOAD					105
#define STEP_TMS_MULTI_TASK				106
#define STEP_TMS_PARSE_TASK_TABLE		107
#define STEP_TMS_PARSE_TASK_TABLE2		108
#define STEP_TMS_COMPARE_TASK_LOCAL_TMS	109
#define STEP_TMS_DELETE_TASK_LOCAL		110
#define STEP_TMS_SAVE_TASK_LOCAL		111
#define STEP_TMS_LOAD_FILE				112
#define STEP_TMS_TERM_ID				113

#define STEP_MPOS_CONNECT			201
#define STEP_MPOS_TERM_INFO			202
#define STEP_MPOS_SN				203
#define STEP_MPOS_EXSN				204
#define STEP_MPOS_VERN_INFO			205
#define STEP_MPOS_SET_TASK			206
#define STEP_MPOS_SAVE_FILE			207

#ifdef __cplusplus
extern "C"{
#endif

void MposReset();
void MposClose();
int  MposConnect();
int  MposRecv(unsigned char *uctRecv);
int  MposSend(unsigned char ucSend);
int  MposRecvs(unsigned char *psRecvBuff, int iRecvLen);
int  MposSends(unsigned char *psSendBuff, int iSendLen);

void TmsClose();
void TmsReset();
int  TmsConnect();
int  TmsSends(unsigned char *sSendBuff, int iSendLen);
int  TmsRecvs(unsigned char *sRecvBuff, int iRecvLen);

void DroidSetProgress(int iType, int iStep, int iDoneFlag, int iFileCount,
		int iCurFile, int iTotal, int iCurSize, int iCurFileSize, int iStatus);
int  DroidGetTermId(unsigned char *pszOutTermId, int iMaxLen);
int  DroidGetPath(unsigned char *pszOutPath, int iMaxLen);
int  DroidGetCallMode();

#ifdef __cplusplus
}
#endif

#endif // PAX_MPOS_PROTIMS_ANDROID_CALLBACK_H_
