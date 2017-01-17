#ifndef PAX_MPOS_PROTIMS_PROTOCOL_PROTIMS_H_
#define PAX_MPOS_PROTIMS_PROTOCOL_PROTIMS_H_

enum PROTIMS_PROTOCOL_ERR_CODE
{
	PROTIMS_HANDSHAKE_ERROR = 1201,
	PROTIMS_TERM_AUTH_ERROR,
	PROTIMS_REQ_UPLOAD_ERROR,
	PROTIMS_UPLOAD_INFO_ERROR,
	PROTIMS_GET_TASK_ERROR,
	PROTIMS_TASK_PARSE_ERROR,
	PROTIMS_DOWNLOAD_ERROR,
	PROTIMS_UPDATE_ERROR,
	PROTIMS_SET_TASK_ERROR,
	PROTIMS_READ_TASK_LOG_ERROR,
	PROTIMS_SAVE_FILE_CONTENT_ERROR,
	PROTIMS_POS_TYPE,
	PROTIMS_MPOS_SAVE_CONTENT_FAIL, 
	PROTIMS_UNSUPPORT_DOWNLOAD_TYPE,
	PROTIMS_READ_PROTIMS_LOG_ERROR, 
	PROTIMS_DELETE_OLD_TMP_ERROR, 
	PROTIMS_WRITE_PROTIMS_LOG_ERROR, 
	PROTIMS_LOAD_FILE_EX_ERROR, 
	PROTIMS_WRITE_DONE_FLAG_ERROR, 
	PROTIMS_SNED_END_PACK_ERROR,
};

enum PROTIMS_PROTOCOL_COMM_ERR_CODE{
	PROTIMS_USERCANCEL = 601,
	PROTIMS_COMM_PARA_INCORRECT,
	PROTIMS_COMM_TIMEOUT_,
	PROTIMS_COMM_WNET_RECV_ERROR,
	PROTIMS_COMM_VERIFY_ERROR,
	PROTIMS_SERVER_DEALERROR,
	PROTIMS_TIMEOUT,
	PROTIMS_TID_ERROR,
	PROTIMS_TSN_ERROR,
	PROTIMS_NOTASK_ERROR,
};

enum PROTIMS_PROTOCOL_WNET_ERR_CODE{
	PROTIMS_WLINIT = 501,
	PROTIMS_WLOPENPORT,
	PROTIMS_WLGETSINGAL,
	PROTIMS_WLLOGINDIAL,
	PROTIMS_WLCLOSEPORT,
	PROTIMS_WNET_DNSRESLOVE,
	PROTIMS_WNET_CONNECT_FAILED,
	PROTIMS_WNET_NETRECV
};

#ifdef __cplusplus
extern "C"{
#endif

int ProTimsHandshake(void);
int ProTimsTermAuth(void *pstInTerminal, void *pstInOutControl);
int ProTimsReqUpload(unsigned char *psSN, int *ReqStatus);
int ProTimsUploadFile(void *pstInTerminal, unsigned char *psExTermInfo);
int ProTimsGetMultTask(unsigned char *sOutBuff, unsigned char *psSN, unsigned char *sHostRandomEn, unsigned char bAuthResult, unsigned char bCallMode);
int ProTimsParseRemoteTaskTable(unsigned char *psTaskBuff, void *pstInRemoteTaskTable, int *iOutRemoteNum, void *pstInPukTaskTable, int *iOutPukNum);
int ProTimsParseRemoteTaskTable2(void *pstRemoteTaskTable, int iTaskNum, void *pstPukTaskTable, int iPukTaskNum);
int ProTimsCompareTask(void* pstInRemoteTaskTable, void* pstInLocalTaskTable);
int PrTimsGetLastTaskTable(unsigned char *szPathName, void* pstOutTaskTable);
int ProTimsDeleteOldTmpFile(unsigned char *szPathName, void* pstInTaskTable);
int ProTimsSaveTaskTable(unsigned char *szPathName, void *pstInRemoteTaskTable, int iTaskNum);
int ProTimsEnd(unsigned char *psSN);
int ProTimsGetFile(void *pstInRemoteTaskTable, int iTaskNum, void *pstInTerminal, void *pstInControl);
int ProTimsLoadFile(unsigned char ucTaskNo, unsigned long fsize,  void *pstInTerminal, void *pstInControl);

#ifdef __cplusplus
}
#endif

#endif // PAX_MPOS_PROTIMS_PROTOCOL_PROTIMS_H_
