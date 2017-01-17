#include "platform.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "common.h"
#include "mpos.h"
#include "callback.h"

#define MPOS_CMD_TYPE1		0x90
#define MPOS_CMD_TYPE2		0x91
#define MPOS_STX			0x02
#define MPOS_ACK			0x06
#define MPOS_NAK			0x15

static int WaitACK(void);
static int MposAskTaskCmd(void);
static int MposAskTaskRecvs(unsigned char* pszOutTaskBuff, int *iOutRecvLens);
static int MposSendPack(unsigned char cmdType, unsigned char cmd, unsigned char *sDataBuff, int iSendLen, int iCount);
static int ProcessRsp(unsigned char cmdType, unsigned char cmd, int iCount);

unsigned long MposGetFlashFreeSize(void)
{
	return 0;
}

int MposReadVersion(unsigned char* psOutVersion)
{
	int iRet = 0, iRecvs = 0, iLen = 0, iRspCode = 0, i = 0;
	unsigned char sSendRecvBuff[19]  = {0};

	sSendRecvBuff[0] = 0x02;		// identifier
	sSendRecvBuff[1] = 0x90;		// command code
	sSendRecvBuff[2] = 0x3d;		// sub command code
	sSendRecvBuff[3] = 0;			// high 8 bits of the length of data 
	sSendRecvBuff[4] = 0;			// low  8 bits of the length of data 
	sSendRecvBuff[5] = lrc((char*)sSendRecvBuff+1 , 0, 4); // LRC
    LOGE("jni MposReadVersion MposSends befor");
	iRet = MposSends(sSendRecvBuff, 6);
	LOGE("jni MposReadVersion MposSends return %d",iRet);

	if(6 != iRet)
	{
		LOGE("MPOS : MposReadVersion, iRet=%d", iRet);
		return iRet;
	}
LOGE("jni 46");
	MySleep(10);
	LOGE("jni 48");
	iRet = WaitACK();
	LOGE("jni 50");
	if(0 != iRet)
	{
		LOGE("MPOS : MposReadVersion, WaitACK, iRet=%d", iRet);
		return iRet;
	}
	LOGE("jni 56");
	memset(sSendRecvBuff, 0x00, 19);
	iRecvs = MposRecvs(sSendRecvBuff, 18); // data field only has the response code, no data content
LOGE("jni 59");
	if(18 != iRecvs)
	{
		if(!MposSend(0x15))
		{
			return MPOS_SEND;
		}
		return MPOS_RSP_DATA_LENGHTH_ERROR;
	}

	if(MPOS_STX != sSendRecvBuff[0])
	{
		if(!MposSend(0x15))
		{
			return MPOS_SEND;
		}
		return MPOS_RSP_STX_ERROR;
	}

	if(0x90 != sSendRecvBuff[1])
	{ 
		if(!MposSend(0x15))
		{
			return MPOS_SEND;
		}
		return MPOS_RSP_CMD_TYPE_ERROR;
	}

	if(0x3d != sSendRecvBuff[2])
	{
		if(!MposSend(0x15))
		{
			return MPOS_SEND;
		}
		return MPOS_RSP_CMD_ERROR;
	}

	iLen = sSendRecvBuff[3]*256 + sSendRecvBuff[4];
	if(12 != iLen) 
	{
		if(!MposSend( 0x15))
		{
			return MPOS_SEND;
		}
		return MPOS_RSP_RSPCODE_LEN_ERROR;
	}

	if(lrc((char*)sSendRecvBuff+1, 0, 16) != sSendRecvBuff[17])
	{
		if(!MposSend( 0x15))
		{
			return MPOS_SEND;
		}
		return MPOS_RSP_LRC_ERROR;
	}

	iRspCode =  (sSendRecvBuff[5] << 24) + (sSendRecvBuff[6] << 16) + (sSendRecvBuff[7] << 8) + sSendRecvBuff[8];
	if(0 != iRspCode) 
	{
		if(!MposSend(0x15))
		{
			return MPOS_SEND;
		}
		return MPOS_RSP_RSPCODE_ERROR;
	}

	memcpy(psOutVersion, sSendRecvBuff+9, 8);
	MposReset();
	if(!MposSend(0x06))
	{
		return MPOS_SEND;
	}

	return 0;
}

int MposGetTermInfo(unsigned char* psOutTerm)
{
	int iRet = 0, iRecvs = 0, iLen = 0, iRspCode = 0, i = 0;
	unsigned char sSendRecvBuff[64]  = {0};

	sSendRecvBuff[0] = 0x02;		// identifier
	sSendRecvBuff[1] = 0x90;		// command type
	sSendRecvBuff[2] = 0x3e;		// sub command code
	sSendRecvBuff[3] = 0;			// high 8 bits of the length of data 
	sSendRecvBuff[4] = 0;			// low  8 bits of the length of data 
	sSendRecvBuff[5] = lrc((char*)sSendRecvBuff+1 , 0, 4); // LRC

    LOGE("jni MposSends befor");
	iRet = MposSends(sSendRecvBuff, 6);
	LOGE("jni MposSends return :%d", iRet);
	if(6 != iRet)
	{
	    LOGE("0000000");
		MposReset();
		LOGE("111111111");
		return MPOS_RSP_DATA_LENGHTH_ERROR;
	}

	LOGE("MySleep befor");
	MySleep(10);
	LOGE("WaitACK befor");
	iRet = WaitACK();
	LOGE("MposGetTermInfo, WaitACK iRet=%d", iRet);
	if(0 != iRet)
	{
		MposReset();
		return iRet;
	}
	
	memset(sSendRecvBuff, 0x00, 64);
	LOGE("MposRecvs befor");
	iRecvs = MposRecvs(sSendRecvBuff, 40); 
    LOGE("MposRecvs return:%d", iRecvs);
	if( 40!=iRecvs && 28!=iRecvs)
	{
		MposReset();
		return MPOS_RSP_DATA_LENGHTH_ERROR;
	}
	if(28 == iRecvs)
	{
		iRecvs = MposRecvs(sSendRecvBuff+28, 12); 
	}

	if(MPOS_STX != sSendRecvBuff[0])
	{
		if(!MposSend(0x15))
		{
			return MPOS_SEND;
		}
		MySleep(3);
		MposReset();
		return MPOS_RSP_STX_ERROR;
	}

	if(0x90 != sSendRecvBuff[1])
	{ 
		if(!MposSend(0x15))
		{
			return MPOS_SEND;
		}
		MySleep(3);
		MposReset();
		return MPOS_RSP_CMD_TYPE_ERROR;
	}

	if(0x3e != sSendRecvBuff[2])
	{
		if(!MposSend(0x15))
		{
			return MPOS_SEND;
		}
		MySleep(3);
		MposReset();
		return MPOS_RSP_CMD_ERROR;
	}

	iLen = sSendRecvBuff[3]*256 + sSendRecvBuff[4];
	if((unsigned char)lrc((char*)sSendRecvBuff+1, 0, iLen+4) != sSendRecvBuff[iLen+5])
	{
		if(!MposSend(0x15))
		{
			return MPOS_SEND;
		}
		MySleep(3);
		MposReset();
		return MPOS_RSP_LRC_ERROR;
	}

	iRspCode =  (sSendRecvBuff[5] << 24) + (sSendRecvBuff[6] << 16) + (sSendRecvBuff[7] << 8) + sSendRecvBuff[8];
	if(0 != iRspCode) 
	{
		if(!MposSend(0x15))
		{
			return MPOS_SEND;
		}
		MySleep(3);
		MposReset();
		return MPOS_RSP_RSPCODE_ERROR;
	}

	memcpy(psOutTerm, sSendRecvBuff+9, 30);
	if(!MposSend(0x06))
	{
		MposReset();
		return MPOS_SEND;
	}
	LOGE("MposGetTermInfo end");
	MposReset();
	return 0;
}

int MposGetExTermInfo(unsigned char* psOutExTerm)
{
	return 0;
}

int MposReadSN(unsigned char* psOutSN)
{
	int iRet   = 0;
	int iRecvs = 0;
	int iLen   = 0;
	int iSNLen = 0;
	int iRspCode = 0;
	int i = 0;
	unsigned char sSendRecvBuff[40]  = {0};

	sSendRecvBuff[0] = 0x02;		
	sSendRecvBuff[1] = 0x90;		
	sSendRecvBuff[2] = 0x34;		
	sSendRecvBuff[3] = 0;			
	sSendRecvBuff[4] = 0;			
	sSendRecvBuff[5] = lrc((char*)sSendRecvBuff+1 , 0, 4); 

	iRet = MposSends(sSendRecvBuff, 6);
	if(6 != iRet)
	{
		return iRet;
	}
	
	MySleep(10);
	iRet = WaitACK();
	if(0 != iRet)
	{
		return iRet;
	}
	
	memset(sSendRecvBuff, 0x00, 40);
	iRecvs = MposRecvs(sSendRecvBuff, 10); 
	if(MPOS_STX != sSendRecvBuff[0])
	{
		if(!MposSend(0x15))
		{
			return MPOS_SEND;
		}
		return MPOS_RSP_STX_ERROR;
	}

	if(0x90 != sSendRecvBuff[1])
	{ 
		if(!MposSend(0x15))
		{
			return MPOS_SEND;
		}
		return MPOS_RSP_CMD_TYPE_ERROR;
	}

	if(0x34 != sSendRecvBuff[2])
	{
		if(!MposSend(0x15))
		{
			return MPOS_SEND;
		}
		return MPOS_RSP_CMD_ERROR;
	}

	if(10 != iRecvs)
	{
		if(!MposSend(0x15))
		{
			return MPOS_SEND;
		}
		return MPOS_RSP_DATA_LENGHTH_ERROR;
	}

	iRspCode =  (sSendRecvBuff[5] << 24) + (sSendRecvBuff[6] << 16) + (sSendRecvBuff[7] << 8) + sSendRecvBuff[8];
	if(0 != iRspCode) 
	{
		if(!MposSend(0x15))
		{
			return MPOS_SEND;
		}
		return MPOS_RSP_RSPCODE_ERROR;
	}

	iLen   = sSendRecvBuff[3]*256 + sSendRecvBuff[4];
	iSNLen = sSendRecvBuff[9];
	iRecvs = MposRecvs(sSendRecvBuff+10, iSNLen+1); 
	if( (iSNLen+1) != iRecvs)
	{
		if(!MposSend(0x15))
		{
			return MPOS_SEND;
		}
		return MPOS_RSP_DATA_LENGHTH_ERROR;
	}

	if((unsigned char)lrc((char*)sSendRecvBuff+1, 0, iLen+4) != sSendRecvBuff[iLen+5])
	{
		if(!MposSend(0x15))
		{
			return MPOS_SEND;
		}
		return MPOS_RSP_LRC_ERROR;
	}

	memcpy(psOutSN, sSendRecvBuff+10, iSNLen);
	MposReset();
	if(!MposSend(0x06))
	{
		return MPOS_SEND;
	}

	return 0;
}

int MposReadExSN(unsigned char* psOutExSN)
{
	int iRet   = 0;
	int iRecvs = 0;
	int iLen   = 0;
	int iSNLen = 0;
	int iRspCode = 0;
	int i = 0;
	unsigned char sSendRecvBuff[40]  = {0};

	sSendRecvBuff[0] = 0x02;		
	sSendRecvBuff[1] = 0x90;		
	sSendRecvBuff[2] = 0x35;		
	sSendRecvBuff[3] = 0;			
	sSendRecvBuff[4] = 0;			
	sSendRecvBuff[5] = lrc((char*)sSendRecvBuff+1 , 0, 4); 

	iRet = MposSends(sSendRecvBuff, 6);
	if(6 != iRet)
	{
		MySleep(3);
		MposReset();
		return iRet;
	}
	
	MySleep(10);
	iRet = WaitACK();
	if(0 != iRet)
	{
		MySleep(3);
		MposReset();
		return iRet;
	}
	
	memset(sSendRecvBuff, 0x00, 40);
	iRecvs = MposRecvs(sSendRecvBuff, 10); 

	if(MPOS_STX != sSendRecvBuff[0])
	{
		MySleep(3);
		MposReset();
		return MPOS_RSP_STX_ERROR;
	}

	if(0x90 != sSendRecvBuff[1])
	{ 
		MySleep(3);
		MposReset();
		return MPOS_RSP_CMD_TYPE_ERROR;
	}

	if(0x35 != sSendRecvBuff[2])
	{
		MySleep(3);
		MposReset();
		return MPOS_RSP_CMD_ERROR;
	}

	if(10 != iRecvs)
	{
		MySleep(3);
		MposReset();
		return MPOS_RSP_DATA_LENGHTH_ERROR;
	}

	iRspCode =  (sSendRecvBuff[5] << 24) + (sSendRecvBuff[6] << 16) + (sSendRecvBuff[7] << 8) + sSendRecvBuff[8];
	LOGE("MposReadExSN, iRspCode=%d", iRspCode);
	if(0 != iRspCode) 
	{
		MySleep(3);
		MposReset();
		return MPOS_RSP_RSPCODE_ERROR;
	}

	iLen   = sSendRecvBuff[3]*256 + sSendRecvBuff[4];
	iSNLen = sSendRecvBuff[9];
	iRecvs = MposRecvs(sSendRecvBuff+10, iSNLen+1); 
	if( (iSNLen+1) != iRecvs)
	{
		MySleep(3);
		MposReset();
		return MPOS_RSP_DATA_LENGHTH_ERROR;
	}

	if((unsigned char)lrc((char*)sSendRecvBuff+1, 0, iLen+4) != sSendRecvBuff[iLen+5])
	{
		MySleep(3);
		MposReset();
		return MPOS_RSP_LRC_ERROR;
	}

	memcpy(psOutExSN, sSendRecvBuff+10, iSNLen);
	if(!MposSend(0x06))
	{
		MposReset();
		return MPOS_SEND;
	}

	MposReset();
	return 0;
}

int MposCompareTask(void *pInRemoteTaskTable, int iCmpTaskNum)
{
	TASK_TABLE CurTaskTable[PROTIMS_MAX_TASK] = {{0}};
	unsigned char szTaskBuff[10 * 100] = {0};
	int iTaskInx = 0, iBuffInx = 0, iRet = 0, iLen = 0, iRecvs = 0;
	int iTotal = 0, iTaskNum = 0, iCurTaskNum = 0, iCmpNum = 0, iTmpLen = 0, iRspCode = 0;
	int i = 0, j =0;
	int bFirst = 1;
	TASK_TABLE *pRemoteTaskTable = (TASK_TABLE *)pInRemoteTaskTable;
	
	iRet = MposAskTaskCmd();	
	if(0 != iRet)
	{
		return MPOS_ASK_TASK_CMD_ERROR;
	}
	
	while(1)
	{	
		iLen		= 0;
		iTmpLen		= 0;
		iRecvs		= 0;
		iCurTaskNum = 0;	
		MySleep(10);
		iRet = MposAskTaskRecvs(szTaskBuff, &iRecvs);	// szTaskBuff(package) consist of STX + cmd + sub cmd + len + response code + data + lrc
		if(MPOS_MPOS_NO_TASK_FIRST == iRet) // need to set task to MPOS
		{
			return 0;
		}
		if(0 != iRet)
		{
			return iRet;
		}
		
		iLen   = szTaskBuff[3]*256 + szTaskBuff[4] - 4;
		if(bFirst) // task total number,only read from the first package
		{
			iTaskNum= szTaskBuff[9]*256  + szTaskBuff[10];
			bFirst = 0;
		}
		
		iCurTaskNum = szTaskBuff[11]*256 + szTaskBuff[12];
		iBuffInx = 13;
		for(iTaskInx=iTotal; iTaskInx<(iTotal+iCurTaskNum); iTaskInx++)
		{
			CurTaskTable[iTaskInx].TaskType = szTaskBuff[iBuffInx]; // task type
			iBuffInx++;

			CurTaskTable[iTaskInx].TaskNo = szTaskBuff[iBuffInx];  // task NO.
			iBuffInx++;

			iTmpLen = szTaskBuff[iBuffInx]*256 + szTaskBuff[iBuffInx+1]; // file name length
			iBuffInx += 2;	
			if(iTmpLen > 32)
			{
				iTmpLen = 32;
			}
			if(iTmpLen > 0)
			{
				strncpy( (char*)CurTaskTable[iTaskInx].FileName, (char*)szTaskBuff+iBuffInx, iTmpLen); // file name
				iBuffInx += iTmpLen;
			}
		
			iTmpLen = szTaskBuff[iBuffInx]*256 + szTaskBuff[iBuffInx+1]; // app name length
			iBuffInx += 2;
			if(iTmpLen > 32)
			{
				iTmpLen = 32;
			}
			if(iTmpLen > 0)
			{
				strncpy( (char*)CurTaskTable[iTaskInx].AppName, (char*)szTaskBuff+iBuffInx, iTmpLen);
				iBuffInx += iTmpLen;
			}
			
			iTmpLen = szTaskBuff[iBuffInx]*256 + szTaskBuff[iBuffInx+1]; // version length
			iBuffInx += 2;
			if(iTmpLen > 20)
			{
				iTmpLen = 20;
			}
			if(iTmpLen > 0)
			{
				strncpy( (char*)CurTaskTable[iTaskInx].vern, (char*)szTaskBuff+iBuffInx, iTmpLen);
				iBuffInx += iTmpLen;
			}
			
			CurTaskTable[iTaskInx].ForceUpdate = szTaskBuff[iBuffInx];
			iBuffInx++;
		}
		iTotal += iCurTaskNum;
		if(iTaskNum == iTotal)
		{
			break;
		}
	}

	for(i=0; i<iCmpTaskNum; i++)
	{
		for(j=0; j<iTaskNum; j++)
		{
			if(CurTaskTable[j].TaskType == pRemoteTaskTable[i].TaskType)
			{

				CurTaskTable[j].AllSize = pRemoteTaskTable[i].AllSize;
				if(memcmp(&CurTaskTable[j], &pRemoteTaskTable[i], sizeof(TASK_TABLE) - sizeof(char))) // not eqaul,compare next
				{
					continue; 	
				}
				else
				{
					iCmpNum++;
				}
			}
		}
	}

	MposReset();
	if(iCmpNum != iCmpTaskNum) // not equal,need set task to MPOS
	{
		return 0;
	}
	
	return MPOS_MPOS_TASK_SAME; // same task,no need set task to MPOS
}

int MposSetTaskList(void *pstInRemoteTaskTable, int iInTaskNum)
{
	int iTaskNum = 0, iCurTaskNum = 0, iTatol = 0, iRet = 0;
	int i = 0, iTmp = 0, iLen = 0, iTask = 0;
	unsigned char sSendBuff[SEND_BUFFER_SIZE];
	TASK_TABLE *pstRemoteTaskTable = (TASK_TABLE *)pstInRemoteTaskTable;

	iTaskNum = iInTaskNum;
	iTask    = iInTaskNum;
	if (iTaskNum == 0)
	{
		LOGE("MposSetTaskList, MPOS_NOTASK_ERROR=%d", MPOS_NOTASK_ERROR);
		return MPOS_NOTASK_ERROR;
	}

    LOGE("iTaskNum=%d",iTaskNum);
	for(i=0; i<iTaskNum; i++)
	{
		switch(pstRemoteTaskTable[i].TaskType) 
		{
		case PROTIMS_CREATE_FILE_SYSTEM:
		case PROTIMS_DELETE_APPLICATION:
		case PROTIMS_DELLETE_DLL:
		case PROTIMS_DELETE_PUBFILE:
		case PROTIMS_DELETE_ALL_APPLICATION:
			iTask--;
			continue;
		}
	}

	if(iTask <= 0)
	{
		LOGE("MposSetTaskList, MPOS_NOTASK_ERROR=%d", MPOS_NOTASK_ERROR);
		return MPOS_NOTASK_ERROR;
	}

	MposReset(); // Added by lirz 20150302

	i = 0;
	while(iTatol != iTask)
	{
		memset(sSendBuff, 0x00 , SEND_BUFFER_SIZE);
		iCurTaskNum = 0;
		iTmp = 4;

		for( ; i<iTask ; )
		{
			// file type
			sSendBuff[iTmp] = pstRemoteTaskTable[i].TaskType;
			iTmp++;
		
			// file no
			sSendBuff[iTmp] = pstRemoteTaskTable[i].TaskNo;
			iTmp++;
			
			// length of file name
			iLen = strlen((char *)pstRemoteTaskTable[i].FileName);
			sSendBuff[iTmp] = iLen / 256;
			iTmp++;
			sSendBuff[iTmp] = iLen % 256;
			iTmp++;
			
			// file name
			if(iLen > 0)
			{
				strncpy((char*)sSendBuff+iTmp, (char*)pstRemoteTaskTable[i].FileName, iLen);
				iTmp += iLen;
			}
			
			// length of application name
			iLen = strlen((char *)pstRemoteTaskTable[i].AppName);
			sSendBuff[iTmp] = iLen / 256;
			iTmp++;
			sSendBuff[iTmp] = iLen % 256;
			iTmp++;
			
			// application name
			if(iLen > 0)
			{
				strncpy((char*)sSendBuff+iTmp, (char*)pstRemoteTaskTable[i].AppName, iLen);
				iTmp += iLen;
			}

			// length of version
			iLen = strlen((char*)pstRemoteTaskTable[i].vern);
			sSendBuff[iTmp] = iLen / 256;
			iTmp++;
			sSendBuff[iTmp] = iLen % 256;
			iTmp++;
			
			// version
			if(iLen > 0)
			{
				strncpy((char*)sSendBuff+iTmp, (char*)pstRemoteTaskTable[i].vern, iLen);
				iTmp += iLen;
			}
			
			sSendBuff[iTmp] = pstRemoteTaskTable[i].ForceUpdate;
			iTmp++;

			iTatol++;
			iCurTaskNum++;
			i++;
			if(i >= 10)
			{
				break;
			}
		}

		sSendBuff[0] = iTask / 256;
		sSendBuff[1] = iTask % 256;
		sSendBuff[2] = iCurTaskNum / 256;
		sSendBuff[3] = iCurTaskNum % 256;

		LogHexData(sSendBuff, 0, iTmp);
		MySleep(5); // 50->5 Modified by lirz 20150301
		iRet = MposSendPack(0x91, 0x41, sSendBuff, iTmp, 0);
		if (0 != iRet) 
		{
			LOGE("MposSetTaskList, MposSendPack iRet=%d", iRet);
			return iRet;
		}
	}
	
	return 0;
}

int MposSaveFileContent(unsigned char *szPathName, void *pInRemoteTaskTable, int iInTaskNum, void *pstInControl)
{
	int iTaskNum = 0, iRet = 0, i = 0, iTimes = 0;
	unsigned long ulTotal = 0, ulFileSize = 0;
	size_t iLen = 0;
	char szTmpName[128] = {0};
	char szFileName[128] = {0};
	unsigned char sSendBuff[SEND_BUFFER_SIZE];
	FILE* fp = NULL;
	TASK_TABLE *pRemoteTaskTable = (TASK_TABLE *)pInRemoteTaskTable;
	ST_CONTROL *pstControl = (ST_CONTROL *)pstInControl;

	iTaskNum = iInTaskNum;
	if (iTaskNum == 0)
	{
		LOGE("MposSaveFileContent, MPOS_NOTASK_ERROR=%d", MPOS_NOTASK_ERROR);
		return MPOS_NOTASK_ERROR;
	}
	
	memset(sSendBuff, 0x00 , SEND_BUFFER_SIZE);
	pstControl->DownloadSize = 0;
	for(i=0; i<iTaskNum; i++)
	{
		memset(szFileName , 0x00, 33);
		memset(sSendBuff, 0x00, SEND_BUFFER_SIZE);
		fp = NULL;
		ulTotal = 0;
		
		pstControl->CurTask = i+1;

		// file type
		if( pRemoteTaskTable[i].TaskType!=PROTIMS_DOWNLOAD_APPLICATION &&
			pRemoteTaskTable[i].TaskType!=PROTIMS_DOWNLOAD_DLL &&
			pRemoteTaskTable[i].TaskType!=PROTIMS_DOWNLOAD_PARA_FILE &&
			pRemoteTaskTable[i].TaskType!=PROTIMS_DOWNLOAD_FONT &&
			pRemoteTaskTable[i].TaskType!=PROTIMS_DOWNLOAD_MONITOR &&
			pRemoteTaskTable[i].TaskType!=PROTIMS_DOWNLOAD_USPUK &&
			pRemoteTaskTable[i].TaskType!=PROTIMS_DOWNLOAD_UAPUK &&
			pRemoteTaskTable[i].TaskType!=PROTIMS_DOWNLOAD_PUBFILE ) 
		{
			continue;
		}
	
		// file no
		sSendBuff[0] = pRemoteTaskTable[i].TaskNo;
		GenTmpFileName(pRemoteTaskTable[i].TaskNo, szTmpName);
		sprintf(szFileName, "%s/%s", szPathName, szTmpName);
		if( NULL==szFileName || strcmp(szFileName, "")==0 )
		{
			LOGE("MposSaveFileContent, MPOS_SAVE_FILE_CONTENT_NONE_FILENAME_ERROR=%d", MPOS_SAVE_FILE_CONTENT_NONE_FILENAME_ERROR);
			DroidSetProgress(TYPE_MPOS, STEP_MPOS_SAVE_FILE, DONE_DOING, pstControl->TaskNum, pstControl->CurTask, pstControl->AllTaskSize, pstControl->DownloadSize, 0, MPOS_SAVE_FILE_CONTENT_NONE_FILENAME_ERROR);
			return MPOS_SAVE_FILE_CONTENT_NONE_FILENAME_ERROR;
		}

		ulFileSize = GetFileSizeEX((unsigned char*)szFileName);	
		LOGI("MposSaveFileContent GetFileSizeEX, ulFileSize=%d", ulFileSize);
		// file size
		sSendBuff[1] = (unsigned char)((ulFileSize >> 24) & 0xff);
		sSendBuff[2] = (unsigned char)((ulFileSize >> 16) & 0xff);
		sSendBuff[3] = (unsigned char)((ulFileSize >> 8) & 0xff);
		sSendBuff[4] = (unsigned char)(ulFileSize & 0xff);

		LOGI("MposSaveFileContent szFileName, szFileName=%s", szFileName);
		fp = fopen(szFileName, "rb");
		if (NULL == fp) 
		{
			LOGE("MposSaveFileContent, MPOS_SAVE_FILE_CONTENT_OPEN_FILE_ERROR=%d", MPOS_SAVE_FILE_CONTENT_OPEN_FILE_ERROR);
			DroidSetProgress(TYPE_MPOS, STEP_MPOS_SAVE_FILE, DONE_DOING, pstControl->TaskNum, pstControl->CurTask, pstControl->AllTaskSize, pstControl->DownloadSize, ulFileSize, MPOS_SAVE_FILE_CONTENT_OPEN_FILE_ERROR);
			return MPOS_SAVE_FILE_CONTENT_OPEN_FILE_ERROR;
		}

		iTimes = (ulFileSize/FILE_CONTENT_SIZE) + 1;
		while(iTimes > 0)
		{
			
			iLen = fread(sSendBuff+13, sizeof(char), FILE_CONTENT_SIZE, fp);
			//LOGI("MposSaveFileContent, pRemoteTaskTable[i].TaskType=%d", pRemoteTaskTable[i].TaskType);
			//LOGI("MposSaveFileContent, fread iLen=%d", iLen);
			//LogHexData(sSendBuff+13, iLen);
			if(0 == iLen)
			{
				fclose(fp);
				DroidSetProgress(TYPE_MPOS, STEP_MPOS_SAVE_FILE, DONE_DOING, pstControl->TaskNum, pstControl->CurTask, pstControl->AllTaskSize, pstControl->DownloadSize, ulFileSize, MPOS_SAVE_FILE_CONTENT_READ_ERROR);
				return MPOS_SAVE_FILE_CONTENT_READ_ERROR;
			}

			// file offset
			sSendBuff[5] = (unsigned char)((ulTotal >> 24) & 0xff);
			sSendBuff[6] = (unsigned char)((ulTotal >> 16) & 0xff);
			sSendBuff[7] = (unsigned char)((ulTotal >> 8) & 0xff);
			sSendBuff[8] = (unsigned char)(ulTotal & 0xff);
			
			// current file content
			LOGI("MposSaveFileContent, Current Size=%d", iLen);
			sSendBuff[9]  = (iLen >> 24) & 0xff;
			sSendBuff[10] = (iLen >> 16) & 0xff;
			sSendBuff[11] = (iLen >> 8) & 0xff;
			sSendBuff[12] = iLen & 0xff;

			MySleep(5); // 500->5 , Modified by lirz 20150301
			iRet = MposSendPack(0x91, 0x42, sSendBuff, 13+iLen, iTimes);
			if(MPOS_FORGET_SET_TASK == iRet)
			{
				LOGE("MposSaveFileContent, MPOS_FORGET_SET_TASK=%d", MPOS_FORGET_SET_TASK);
				fclose(fp);
				DroidSetProgress(TYPE_MPOS, STEP_MPOS_SAVE_FILE, DONE_DOING, pstControl->TaskNum, pstControl->CurTask, pstControl->AllTaskSize, pstControl->DownloadSize, ulFileSize, MPOS_FORGET_SET_TASK);
				return MPOS_FORGET_SET_TASK;
			}

			if(0 != iRet)
			{
				fclose(fp);
				DroidSetProgress(TYPE_MPOS, STEP_MPOS_SAVE_FILE, DONE_DOING, pstControl->TaskNum, pstControl->CurTask, pstControl->AllTaskSize, pstControl->DownloadSize, ulFileSize, iRet);
				return iRet;
			}

			ulTotal += iLen;
			pstControl->DownloadSize += iLen;
			DroidSetProgress(TYPE_MPOS, STEP_MPOS_SAVE_FILE, DONE_DOING, pstControl->TaskNum, pstControl->CurTask, pstControl->AllTaskSize, pstControl->DownloadSize, ulFileSize, 0);
			iTimes--;
		}

		if(ulTotal != ulFileSize)
		{
			fclose(fp);
			fp = NULL;
			LOGE("MposSaveFileContent, MPOS_SAVE_FILE_CONTENT_ERROR=%d", MPOS_SAVE_FILE_CONTENT_ERROR);
			DroidSetProgress(TYPE_MPOS, STEP_MPOS_SAVE_FILE, DONE_DOING, pstControl->TaskNum, pstControl->CurTask, pstControl->AllTaskSize, pstControl->DownloadSize, ulFileSize, MPOS_SAVE_FILE_CONTENT_ERROR);
			return MPOS_SAVE_FILE_CONTENT_ERROR;
		}

		if (0 != iRet) 
		{
			fclose(fp);
			fp = NULL;
			LOGE("MposSaveFileContent, iRet=%d", iRet);
			DroidSetProgress(TYPE_MPOS, STEP_MPOS_SAVE_FILE, DONE_DOING, pstControl->TaskNum, pstControl->CurTask, pstControl->AllTaskSize, pstControl->DownloadSize, ulFileSize, iRet);
			return iRet;
		}
		
		fclose(fp);
		fp = NULL;
	}

	return 0;
}

/////////////////////////////////////////////////INTERNAL IMPLEMENT///////////////////////////////////////////////////
int MposAskTaskCmd(void)
{
	int iRet   = 0;
	unsigned char sSendRecvBuff[6]  = {0};

	sSendRecvBuff[0] = 0x02;		
	sSendRecvBuff[1] = 0x91;		
	sSendRecvBuff[2] = 0x43;		
	sSendRecvBuff[3] = 0;			
	sSendRecvBuff[4] = 0;			
	sSendRecvBuff[5] = lrc((char*)sSendRecvBuff+1 , 0, 4); 

	iRet = MposSends(sSendRecvBuff, 6);
	if(6 != iRet)
	{
		return iRet;
	}
	
	MySleep(10);
	iRet = WaitACK();
	if(0 != iRet)
	{
		return iRet;
	}

	return 0;
}

int MposAskTaskRecvs(unsigned char* pszOutTaskBuff, int *iOutRecvLens)
{
	int iRet   = 0;
	int iRecvs = 0;
	int iLen   = 0;
	int iSNLen = 0;
	int iRspCode = 0;
	unsigned char *pszTaskBuff = pszOutTaskBuff;
	
	iRecvs = MposRecvs(pszTaskBuff, 9); // 9 bytes - STX(1 byte:0x02),cmd(1 byte),sub cmd(1 byte),len(2 byte),return code(4 byte)
	if(MPOS_STX != pszTaskBuff[0])
	{
		if(!MposSend(0x15))
		{
			return MPOS_SEND;
		}
		return MPOS_RSP_STX_ERROR;
	}

	if(0x91 != pszTaskBuff[1])
	{ 
		if(!MposSend(0x15))
		{
			return MPOS_SEND;
		}
		return MPOS_RSP_CMD_TYPE_ERROR;
	}

	if(0x43 != pszTaskBuff[2])
	{
		if(!MposSend(0x15))
		{
			return MPOS_SEND;
		}
		return MPOS_RSP_CMD_ERROR;
	}

	if(9 != iRecvs)
	{
		if(!MposSend(0x15))
		{
			return MPOS_SEND;
		}
		return MPOS_RSP_DATA_LENGHTH_ERROR;
	}

	iRspCode =  (pszTaskBuff[5] << 24) + (pszTaskBuff[6] << 16) + (pszTaskBuff[7] << 8) + pszTaskBuff[8];
	iLen   = pszTaskBuff[3]*256 + pszTaskBuff[4];
	iRecvs = MposRecvs(pszTaskBuff+9, iLen-3); // iLen-3 : iLen - 4(response code) + 1(lrc)
	if( (iLen-3) != iRecvs)
	{
		if(!MposSend(0x15))
		{
			return MPOS_SEND;
		}
		return MPOS_RSP_DATA_LENGHTH_ERROR;
	}

	if((unsigned char)lrc((char*)pszTaskBuff+1, 0, iLen+4) != pszTaskBuff[iLen+5]) // iLen+4 : iLen + cmd(1) + sub cmd(1) + len(2)
	{
		return MPOS_RSP_LRC_ERROR;
	}

	if(0x01 == iRspCode)
	{
		if(!MposSend(0x06))
		{
			return MPOS_SEND;
		}
		return MPOS_MPOS_NO_TASK_FIRST;
	}

	if( 0 != iRspCode ) // 0x00-success,0x01-MPOS has no task the first time : need set task to MPOS
	{
		if(!MposSend(0x15))
		{
			return MPOS_SEND;
		}
		return MPOS_RSP_RSPCODE_ERROR;
	}

	if(!MposSend(0x06))
	{
		return MPOS_SEND;
	}

	*iOutRecvLens = iRecvs + 9;

	return 0;
}

int MposSendPack(unsigned char cmdType, unsigned char cmd, unsigned char *sDataBuff, int iSendLen, int iCount)
{
	int iTimes = 4; // after send one package, if not receive ACK/NAK in 2000ms，retry 4 times
	int iRet   = 0;
	unsigned long ulEnd   = 0;
	unsigned long ulStart = 0;
	unsigned char sSendBuff[SEND_BUFFER_SIZE];
	
	memset(sSendBuff, 0x00, SEND_BUFFER_SIZE); 
	sSendBuff[0] = 0x02;		
	sSendBuff[1] = cmdType;    
	sSendBuff[2] = cmd;		
	sSendBuff[3] = (unsigned char)(iSendLen/256);  
	sSendBuff[4] = (unsigned char)(iSendLen%256);  
	memcpy(sSendBuff+5, sDataBuff, iSendLen);
	sSendBuff[5+iSendLen] = lrc((char*)sSendBuff+1 , 0, iSendLen+4); // LRC
	iSendLen += 6;
	sSendBuff[iSendLen] = '\0';
	ulStart = MyGetTickCount();
	ulEnd   = ulStart + 5000;
	while( iTimes>0 && ulStart<ulEnd )
	{

		iRet = MposSends(sSendBuff, iSendLen);
		LOGI("MposSendPack, MposSends iSendLen=%d", iSendLen);
		if(iSendLen != iRet)
		{
			LOGE("MposSendPack, MposSends iRet=%d", iRet);
			return iRet;
		}
		if(cmdType==0x91 && cmd==0x41)
		{
			MySleep(10);
		}
		else
		{
			MySleep(10);
		}

		iRet = WaitACK();
		if(0 != iRet)
		{
			LOGE("MposSendPack, WaitACK=%d", iRet);
			return iRet;
		}		
		if(0 == iRet)
		{
			break;
		}
		iTimes--;
		ulStart = MyGetTickCount();
	}

	iRet = ProcessRsp(cmdType, cmd, iCount);
	if(0 != iRet)
	{
		LOGE("MposSendPack, ProcessRsp=%d", iRet);
		return iRet;
	}

	return 0;
}

int WaitACK(void)
{
	unsigned char pszRecv[2] = {0};
	//int  iTimes     = 3;
	int iTimes = 1;
	
	while(!MposRecv(pszRecv))
	{
		iTimes--;
		if(iTimes<=0)
		{
			return MPOS_WAIT_ACK_ERROR;
		}	
	}
	if(pszRecv[0] == (char)MPOS_NAK)
	{
		return MPOS_RECV_NAK;
	}
	
	if(pszRecv[0] != MPOS_ACK)
	{
		return MPOS_WAIT_ACK_NONE;
	}

	return 0;	
}

int ProcessRsp(unsigned char cmdType, unsigned char cmd, int iCount)
{
	int iLen	 = 0;
	int iRspCode = 0;
	int iRecvs   = 0;
	int i = 0;
	unsigned char sRecvBuff[10];

	memset(sRecvBuff, 0x00, 10); 
	MySleep(10);
	iRecvs = MposRecvs(sRecvBuff, 10);


	if(10 != iRecvs)
	{
	    //LOGE("ProcessRsp MposRecvs %d", iRecvs);

		if(!MposSend(0x15))
		{
			return MPOS_SEND;
		}
		LOGE("ProcessRsp MposRecvs Error, MPOS_RSP_DATA_LENGHTH_ERROR", MPOS_RSP_DATA_LENGHTH_ERROR);
		return MPOS_RSP_DATA_LENGHTH_ERROR;
	}

	if(!MposSend(0x06))
	{
		return MPOS_SEND;
	}

	if(MPOS_STX != sRecvBuff[0])
	{
		if(!MposSend(0x15))
		{
			return MPOS_SEND;
		}
		LOGE("ProcessRsp MPOS_RSP_STX_ERROR", MPOS_RSP_STX_ERROR);
		return MPOS_RSP_STX_ERROR;
	}

	if(cmdType != sRecvBuff[1])
	{ 
		if(!MposSend(0x15))
		{
			return MPOS_SEND;
		}
		LOGE("ProcessRsp MPOS_RSP_CMD_TYPE_ERROR", MPOS_RSP_CMD_TYPE_ERROR);
		return MPOS_RSP_CMD_TYPE_ERROR;
	}

	if(cmd != sRecvBuff[2])
	{
		if(!MposSend(0x15))
		{
			return MPOS_SEND;
		}
		LOGE("ProcessRsp MPOS_RSP_CMD_ERROR", MPOS_RSP_CMD_ERROR);
		return MPOS_RSP_CMD_ERROR;
	}

	iLen = sRecvBuff[3]*256 + sRecvBuff[4];
	if(4 != iLen) 
	{
		if(!MposSend(0x15))
		{
			return MPOS_SEND;
		}
		LOGE("ProcessRsp MPOS_RSP_RSPCODE_LEN_ERROR", MPOS_RSP_RSPCODE_LEN_ERROR);
		return MPOS_RSP_RSPCODE_LEN_ERROR;
	}

	if(lrc((char*)sRecvBuff+1, 0, 8) != sRecvBuff[9])
	{
		if(!MposSend(0x15))
		{
			return MPOS_SEND;
		}
		LOGE("ProcessRsp MPOS_RSP_LRC_ERROR", MPOS_RSP_LRC_ERROR);
		return MPOS_RSP_LRC_ERROR;
	}

	iRspCode =  (sRecvBuff[5] << 24) + (sRecvBuff[6] << 16) + (sRecvBuff[7] << 8) + sRecvBuff[8];
	if(0 != iRspCode) 
	{
		if( cmdType==0x91 && cmd==0x41 && 0x04==iRspCode ) // 9141:set task,0x04-task number more than 10
		{
			LOGW("ProcessRsp PROTIMS_SET_TASK_TEN_MORE");
			return 0; // return PROTIMS_SET_TASK_TEN_MORE;
		}
		if( cmdType==0x91 && cmd==0x42 && 0x02==iRspCode ) // 9142:set content,before set content must set task
		{
			LOGE("ProcessRsp MPOS_FORGET_SET_TASK", MPOS_FORGET_SET_TASK);
			return MPOS_FORGET_SET_TASK;
		}
		if( cmdType==0x91 && cmd==0x42 && 0x03==iRspCode ) // 9142:set content,settask fileno is not same as setcontent fileno
		{
			if(!MposSend(0x15))
			{
				return MPOS_SEND;
			}
			LOGE("ProcessRsp MPOS_FILENO_NOT_SAME=%d", MPOS_FILENO_NOT_SAME);
			return MPOS_FILENO_NOT_SAME;
		}

		if(1 == iCount)
		{
			if(!MposSend(0x06))
			{
				return MPOS_SEND;
			}
			return 0;
		}

		if( cmdType==0x91 && cmd==0x42 && 1!=iCount && (0x05==iRspCode || 0x06==iRspCode || 0x07==iRspCode || 0x08==iRspCode) ) // 9142:set content,settask fileno is not same as setcontent fileno
		{
			if(!MposSend(0x15))
			{
				return MPOS_SEND;
			}
			LOGE("iRspCode=%d", iRspCode);
			LOGE("ProcessRsp MPOS_MPOS_SAVE_CONTENT_FAIL=%d", MPOS_MPOS_SAVE_CONTENT_FAIL);
			return MPOS_MPOS_SAVE_CONTENT_FAIL;
		}
		
		if(!MposSend(0x15))
		{
			return MPOS_SEND;
		}
		LOGE("ProcessRsp MPOS_RSP_RSPCODE_ERROR=%d", MPOS_RSP_RSPCODE_ERROR);
		return MPOS_RSP_RSPCODE_ERROR;
	}

	if(!MposSend(0x06))
	{
		return MPOS_SEND;
	}

	return 0;
}

