#ifndef PAX_MPOS_PROTIMS_PROTOCOL_MPOS_H_
#define PAX_MPOS_PROTIMS_PROTOCOL_MPOS_H_

enum MPOS_ERR_CODE{
	MPOS_OPEN = 201,
	MPOS_SEND,
	MPOS_RESET,
	MPOS_RECV,
	MPOS_BAUDRATE_INCORRECT,
	MPOS_COM_USB_CONNECT,
	MPOS_COM_USB_DISCONNECT,
	MPOS_WAIT_ACK_ERROR,
	MPOS_WAIT_ACK_NONE,
	MPOS_RECV_NAK,
	MPOS_RSP_DATA_LENGHTH_ERROR,
	MPOS_RSP_STX_ERROR,
	MPOS_RSP_CMD_TYPE_ERROR,
	MPOS_RSP_CMD_ERROR,
	MPOS_RSP_RSPCODE_LEN_ERROR,
	MPOS_RSP_LRC_ERROR,
	MPOS_RSP_RSPCODE_ERROR
};

enum MPOS_TASK_ERR_CODE{
	MPOS_MPOS_TASK_SAME = 1501,
	MPOS_MPOS_NO_TASK_FIRST,
	MPOS_COMPARE_TASK_ERROR,
	MPOS_ASK_TASK_CMD_ERROR,
	MPOS_SET_TASK_TEN_MORE,
	MPOS_FORGET_SET_TASK,
	MPOS_FILENO_NOT_SAME,
	MPOS_MPOS_SAVE_CONTENT_FAIL, 
	MPOS_NOTASK_ERROR,
	MPOS_SAVE_FILE_CONTENT_ERROR,
	MPOS_SAVE_FILE_CONTENT_READ_ERROR,
	MPOS_SAVE_FILE_CONTENT_NONE_FILENAME_ERROR,
	MPOS_SAVE_FILE_CONTENT_OPEN_FILE_ERROR,
};

#ifdef __cplusplus
extern "C"{
#endif

unsigned long MposGetFlashFreeSize(void);
int MposReadVersion(unsigned char* psOutVersion);
int MposGetTermInfo(unsigned char* psOutTerm);
int MposGetExTermInfo(unsigned char* psOutExTerm);
int MposReadSN(unsigned char* psOutSN);
int MposReadExSN(unsigned char* psOutExSN);
int MposCompareTask(void *pRemoteTaskTable, int iCmpTaskNum);
int MposSetTaskList(void *pstRemoteTaskTable, int iInTaskNum);
int MposSaveFileContent(unsigned char *szPathName, void *pRemoteTaskTable, int iInTaskNum, void *pstInControl);

#ifdef __cplusplus
}
#endif

#endif // PAX_MPOS_PROTIMS_PROTOCOL_MPOS_H_
