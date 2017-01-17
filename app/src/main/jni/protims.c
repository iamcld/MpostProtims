#include "platform.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "common.h"
#include "protims.h"
#include "file.h"
#include "util.h"
#include "callback.h"

// ini
#define MAX_FILE_BUFFER				51200

// zip flag
#define PROTIMS_NO_ZIP               0  		// unzip
#define PROTIMS_IS_ZIP               1  		// zip

#define PROTIMS_VER  							"PX02"
#define LIB_VER  								"003" 

// ProTims protocol command code
#define PROTIMS_MSG_MODEM_OK              0x05
#define PROTIMS_MSG_LOAD_DATA             0x36
#define PROTIMS_MSG_AUTH_TERMINAL         0x37
#define PROTIMS_MSG_GET_TASK_TABLE        0x38
#define PROTIMS_MSG_DOWNLOAD_OK           0x3A
#define PROTIMS_MSG_SIN_AUTH_FLAG         0x3B  // sign auth
#define PROTIMS_MSG_GET_MULTITASK_TABLE   0X3C  // request to get multi-task(3c, multi-package)
#define PROTIMS_MSG_NOTIFY_UPDATE_REQ     0x3d   
#define PROTIMS_MSG_GET_PARA_INFO         0x3e  // query parameter
#define PROTIMS_MSG_SAVE_PARA_INFO_STATUS 0x3f  // get status of save parameter
#define PROTIMS_MSG_SPECIAL_COMMAND       0xF0  // special command
#define PROTIMS_MSG_UPLOAD_REQ        	  0x40
#define PROTIMS_MSG_UPLOAD_DATA           0x41
#define PROTIMS_MSG_GET_RSAKEY			  0x51
#define PROTIMS_MSG_GET_MAINKEY			  0x52
#define PROTIMS_MSG_GET_KEYSTATE		  0x53

// communication retry times and timeout setting
#define PROTIMS_RETRY_NUMBER					2
#define PROTIMS_RECON_NUM						5			// wifi reconnect times
#define PROTIMS_HANDSHAKE_RETRY_TIMES			15			// handshake retry times
#define PROTIMS_DATA_RETRY_TIMES				3			// send data retry times
#define PROTIMS_GET_PLAN_RETRY_TIMES			10			// get plan retry times
#define PROTIMS_RECV_CONTROL_BYTE_TIMEOUT		2			// receive control package timeout : 200ms
#define PROTIMS_RECV_BYTE_TIMEOUT				80			// receive byte timeout
#define PROTIMS_TOTAL_SEND_PACKET_TIME_OUT		600			// send package timeout : 60s
#define PROTIMS_TOTAL_RECEV_PACKET_TIME_OUT		600			// receive package timeout : 60s
#define PROTIMS_ONE_RECEV_PACKET_TIME_OUT		300

static void Bublesort(TASK_TABLE *stTaskTables, int iTaskNum);
static int  ProTimsSendPack(unsigned char cmd, unsigned char *sInSendBuff,  int iSendLen);
static int  ProTimsRecvPack(unsigned char cmd, unsigned char *sOutRecvBuff, int *iOutRecvLen);
static int  ProTimsRecvControlPack(unsigned char cmd, unsigned char *sOutRecvBuff, int *iRecvLen);
static int  ProTimsSendRecv(unsigned char cmd, unsigned char *sSendBuff, int iSendLen, unsigned char *sRecvBuff, int *iRecvLen);
static int  GetLastTaskTable(unsigned char *szFileName, void* pstOutTaskTable);

int ProTimsHandshake(void)
{
	int	i		 = 0;
	int iRet	 = 0;
	int iRecvLen = 0;
	unsigned char sBuff[20] = {0};
	
	LOGE("ProTimsHandshake, starting...");
	for(i=0; i<PROTIMS_HANDSHAKE_RETRY_TIMES; i++)
	{
		TmsReset();
		memset(sBuff, 0x00, 20);	
		memcpy(sBuff, "READY", 5);		
		sBuff[5] = 0x08;				
		sBuff[6] = 0x01;	 // new TMS version 08 01
		
		iRet = ProTimsSendPack(PROTIMS_MSG_SPECIAL_COMMAND, sBuff, 7);
		
		if (iRet != 0)
		{
			continue;
		}
		
		memset(sBuff, 0x00, sizeof(sBuff));
		iRet = ProTimsRecvControlPack(PROTIMS_MSG_SPECIAL_COMMAND, sBuff, &iRecvLen);
		LOGE("ProTimsHandshake iRet=%d", iRet);
		
		if ((0 == iRet) && (7 == iRecvLen))
		{	
			if(0 == memcmp(sBuff+5,"\x88\x01",2)) // new TMS version
			{
				// do nothing
			}
			
			if (0 == memcmp(sBuff, "READY", 5))
			{
				break;
			}
		}
	}
	
	if (i >= PROTIMS_HANDSHAKE_RETRY_TIMES)
	{
		iRet = ProTimsSendPack(PROTIMS_MSG_SPECIAL_COMMAND, (unsigned char *)"EOT", 3);		// if fail to handshake, disconnect with TMS
		return PROTIMS_HANDSHAKE_ERROR;
	}
	
	LOGE("ProTimsHandshake, finish...");
	return 0;
}

int ProTimsTermAuth(void *pstInTerminal, void *pstInOutControl)
{
	unsigned char sRecvBuff[RECV_BUFFER_SIZE] = {0};
	unsigned char sOutDes[8]	    = {0};
	unsigned char sPackage[1024]	= {0};
	int i = 0, iRet = 0, iRecvLen = 0;	
	ST_TERMINAL *pstTerminal = (ST_TERMINAL *)pstInTerminal;
	ST_CONTROL  *pstControl  = (ST_CONTROL *)pstInOutControl;
	
	memset(sPackage, 0x00, 1024);
	i = 0;
	if( pstTerminal->SN==NULL || strcmp((char*)pstTerminal->SN, "")==0 )
	{
		memcpy(sPackage+i, "00000000", 8); // SN
	}
	else
	{
		memcpy(sPackage+i, pstTerminal->SN, 8);  // SN
	}
	i += 20;	
	sPackage[i]   = (unsigned char)((8*1024)/256);
	sPackage[i+1] = (unsigned char)((8*1024)%256);	
	i += 3;	
	memcpy(sPackage+i, pstTerminal->TermID, strlen((char*)pstTerminal->TermID)); // Task ID
	i += 10;	
	if(0 >= pstTerminal->PosType)
	{
		sPackage[i++] = 32;
	}
	else
	{
		sPackage[i++] = pstTerminal->PosType;
	}
	 
	// monitor version
	if(NULL == pstTerminal->MonVer)
	{
		sprintf((char*)(sPackage+i), "1.0.0");			
	}
	else
	{
		sprintf((char*)(sPackage+i), "%.6", pstTerminal->MonVer);
	}
	i += 6;
	
	sprintf((char*)(sPackage+i), "%X", pstTerminal->VerInfo[0]);	// BIOS version
	i += 6;	
	sprintf((char*)(sPackage+i), "%X", pstTerminal->VerInfo[1]);	// main borad hardware version
	i += 6;
	sprintf((char*)(sPackage+i), "%X", pstTerminal->VerInfo[2]);	// main borad solfware version
	i += 4;
	sprintf((char*)(sPackage+i), "%X", pstTerminal->VerInfo[3]);	// modern version
	i += 4;
	
	sPackage[i++] = pstControl->CommModeTms;   // communication type
	sPackage[i++] = 0x00;	
	
	MyRand(pstControl->TermRandom);
	memcpy(sPackage+i, pstControl->TermRandom, 8);
	i += 8;
	
	iRet = ProTimsSendRecv(PROTIMS_MSG_AUTH_TERMINAL, sPackage, i, sRecvBuff, &iRecvLen);	
	if (0 != iRet)
	{
		LOGE("2.ProTimsTermAuth, ProTimsSendRecv, iRet=%d", iRet);
		return iRet;
	}
	
	memcpy(sPackage, sRecvBuff, iRecvLen);
	// Shenzhen UnionPay's ProTims use fixed key to download: byte csFixedAuthPwd[9] = "\xf0\x90\x88\xdf\xac\x23\x89\x68";
	// the first 8 bytes of sPackage is the random number which is sent by server(TMS)
	// des data(Packet[70~77]) with key index(Packet[69]);
	// encrytp to save to psHostRandomEn
	des(sPackage, pstControl->HostRandomEn, (unsigned char *)"\xf0\x90\x88\xdf\xac\x23\x89\x68\x00", 1); 
	// des local host random number																 
	des(pstControl->TermRandom, sOutDes, (unsigned char *)"\xf0\x90\x88\xdf\xac\x23\x89\x68\x00", 1); 
	// sPackage+8 local host random number(encrypted)
	memcpy(pstControl->TermRandomEn, sPackage+8, 8);
	if (0 != memcmp(sOutDes, pstControl->TermRandomEn, 8))
	{
		return PROTIMS_TERM_AUTH_ERROR;
	}
	
	return 0;
}

int ProTimsReqUpload(unsigned char *psSN, int *ReqStatus)
{
	unsigned char sPackage[32]  = {0};
	unsigned char sRecvBuff[64] = {0};
	int i = 0, iRet	= 0, iRecvLen = 0;
		
	memset(sPackage, 0x00, 32);				
	i = 0;
	memcpy(sPackage+i, psSN, 8);   
	i += 20;	
	iRet = ProTimsSendRecv(PROTIMS_MSG_UPLOAD_REQ, sPackage, i, sRecvBuff, &iRecvLen);		
	if (0 != iRet)
	{
		return iRet;
	}	
	if (iRecvLen != 1)
	{
		return PROTIMS_COMM_WNET_RECV_ERROR;
	}
	
	// get req status from TMS
	*ReqStatus = sRecvBuff[0];
		
	return 0;
}

int ProTimsUploadFile(void *pstInTerminal, unsigned char *psExTermInfo)
{
	unsigned char sRecvBuff[RECV_BUFFER_SIZE] = {0};	
	unsigned char sPackage[1024] = {0};
	int iRet = 0, iRecvLen = 0, iRetryCount = 0, iXMLLen = 0, iValueLen = 0;
	unsigned long ulFlashFreeSize = 0;
	unsigned char sEleValue[32] = {0};
	ST_FONT stFont[20] = {0};
	int i = 0, j= 0;
	ST_TERMINAL *pstTerminal = pstInTerminal;

	memset(sPackage, 0x00, sizeof(sPackage));	
	i = 0;	
	sPackage[0] = 1; // sPackage total
	sPackage[1] = 0; // sPackage num
	i += 2;	
	sPackage[i] = pstTerminal->PosType; 
	i += 1;
	sPackage[i] = pstTerminal->VerInfo[0]; // BOIS version
	i += 1;
	sprintf((char*)(sPackage+i), "%d.%d", pstTerminal->VerInfo[1], pstTerminal->VerInfo[2]); // monitor version
	i += 6;
	sPackage[i] = pstTerminal->TermInfo[5]; // PCI INFO
	i += 1;
	sPackage[i] = pstTerminal->TermInfo[14]; // FONT INFO
	i += 1;
	memset(sEleValue, 0x00, sizeof(sEleValue));
	iRet = XmlGetElement(psExTermInfo, iXMLLen, "ddrsize", sEleValue, sizeof(sEleValue), &iValueLen);
	memcpy((char*)(sPackage+i), sEleValue, iValueLen); // SdramSize
	i += 10;
	memcpy((char*)(sPackage+i), sEleValue, iValueLen); // DDRSize
	i += 10;
	memset(sEleValue, 0x00, sizeof(sEleValue));
	iRet = XmlGetElement(psExTermInfo, iXMLLen, "flashsize", sEleValue, sizeof(sEleValue), &iValueLen);
	memcpy((char*)(sPackage+i), sEleValue, iValueLen); // total FLASH Size
	i += 10;

	ulFlashFreeSize = MposGetFlashFreeSize();
	if(0 == ulFlashFreeSize)
	{
		ulFlashFreeSize = 124*1025; // 124M
	}
	sprintf((char*)(sPackage+i), "%ld", ulFlashFreeSize);  // free FLASH Size
	i += 10;

	sPackage[i++] = pstTerminal->TermInfo[16];  // MSR
	sPackage[i++] = pstTerminal->TermInfo[1];	// PRN
	sPackage[i++] = pstTerminal->TermInfo[15];	// ICC
	sPackage[i++] = pstTerminal->TermInfo[12];	// RF

	memset(sEleValue,0x00,sizeof(sEleValue));
	iRet = XmlGetElement(psExTermInfo, iXMLLen, "wnetver", sEleValue, sizeof(sEleValue), &iValueLen);
	memcpy((char*)(sPackage+i), sEleValue, iValueLen); // WNETVer
	i += 10;

	memset(sEleValue, 0x00, sizeof(sEleValue));
	iRet = XmlGetElement(psExTermInfo, iXMLLen, "imei", sEleValue, sizeof(sEleValue), &iValueLen);
	memcpy((char*)(sPackage+i), sEleValue, iValueLen); // IMEI
	i += 20;

	sPackage[i++] = pstTerminal->TermInfo[8];	// LAN
	sPackage[i++] = pstTerminal->TermInfo[11];	// WIFI
	sPackage[i++] = pstTerminal->TermInfo[2];	// MODEM
	sPackage[i++] = pstTerminal->TermInfo[9];	// GPRS
	sPackage[i++] = pstTerminal->TermInfo[10];	// CDMA
	sPackage[i++] = pstTerminal->TermInfo[18];	// WCDMA
	sPackage[i++] = pstTerminal->TermInfo[6];	// USBH
	sPackage[i++] = pstTerminal->TermInfo[7];	// USBD

	memcpy(sPackage+i, pstTerminal->SN, 8);	// SN
	i += 20;	
	memcpy(sPackage+i, pstTerminal->ExSN, 8); 	// EXSN
	i += 20;
	sprintf((char*)(sPackage+i), "%02x%02x%02x%02x%02x%02x", pstTerminal->MacAddr[0], pstTerminal->MacAddr[1], pstTerminal->MacAddr[2], pstTerminal->MacAddr[3], pstTerminal->MacAddr[4], pstTerminal->MacAddr[5]);
	i += 20;
	if(pstTerminal->TermInfo[0] == 0x14 || pstTerminal->TermInfo[0] == 0x15 || pstTerminal->TermInfo[0] == 0x16)
	{
		// Sxxx
		memcpy((char *)(sPackage+i), "ARM11 32BIT", strlen("ARM11 32BIT")); // CPU
	}
	else
	{
		if(pstTerminal->VerInfo[1] < 3)
		{
			memcpy((char *)(sPackage+i), "ARM9 32BIT", strlen("ARM9 32BIT"));
		}
		else
		{
			memcpy((char *)(sPackage+i), "ARM11 32BIT", strlen("ARM11 32BIT"));
		}
	}
	i += 20;
	sprintf((char *)sPackage+i,"%s-%s", LIB_VER, PROTIMS_VER); // TMS VER
	i += 20;
	
	iRet = EnumFont(&stFont[0], 20);
	if(iRet >= 0)
	{
		sprintf((char*)(sPackage+i), "NUMBER:%d", iRet); // FONT NUMBER
		i += 10;

		for(j = 0; j < iRet; j++)
		{
			// FONT INFO
			sprintf((char*)(sPackage+i), "%d-%s [%d*%d] %s %s", j+1,
			        GetCharSet(stFont[j].CharSet),
					stFont[j].Width, 
					stFont[j].Height,
					IsBold(stFont[j].Bold),
					IsItalic(stFont[j].Italic));
			i += 32;
		}	
	}

	if(i > sizeof(sPackage))
	{
		i = sizeof(sPackage);
	}
	
	for(iRetryCount = 0; iRetryCount <= PROTIMS_DATA_RETRY_TIMES; iRetryCount++)
	{
		iRet = ProTimsSendRecv(PROTIMS_MSG_UPLOAD_DATA, sPackage, i, sRecvBuff, &iRecvLen);

		if (0 != iRet)
		{
			continue;
		}
		if (sPackage[0] != sRecvBuff[0] || sPackage[1] != sRecvBuff[1] || iRecvLen != 3)
		{
			iRet = -1;
			continue;
		}		
		switch(sRecvBuff[2])	//	0-success, 1-retransmit, 2-terminate
		{
			case 0:
				return 0;
			case 2:
				return PROTIMS_COMM_WNET_RECV_ERROR;
			case 1:
			default:
				iRet = PROTIMS_COMM_WNET_RECV_ERROR;
				break;
		}

	}
	
	return iRet;
}

int ProTimsGetMultTask(unsigned char *sOutBuff, unsigned char *psSN, unsigned char *sHostRandomEn, unsigned char bAuthResult, unsigned char bCallMode)
{	
	//unsigned char sRecvBuff[RECV_BUFFER_SIZE] = {0};
	unsigned char sRecvBuff[9000] = {0};
	unsigned char sPackage[8192] = {0};
	char szTempFileName[128] = {0};
	int i    = 0;
	int iRet = 0;
	int iRecvLen = 0;
	int iRcvFileLen = 0; 
	FILE *fp = NULL;

	LOGE("ProTimsGetMultTask in");
	LOGE("ProTimsGetMultTask in,bAuthResult=%d", bAuthResult);
	memset(sPackage, 0x00, sizeof(sPackage));	
	i = 0;
	sPackage[i++] = bAuthResult;
	memcpy(sPackage+i, sHostRandomEn, 8); 
	i += 8;
	
	sPackage[i++] = bCallMode; // 0-monitor call
	sPackage[i++] = 0;
	sPackage[i++] = 0;

	memset(sRecvBuff, 0x00, sizeof(sRecvBuff));
	iRet = ProTimsSendRecv(PROTIMS_MSG_GET_MULTITASK_TABLE, sPackage, i, sRecvBuff, &iRecvLen);
    LOGE("ProTimsSendRecv out------1----,iRet=%d",iRet);

	if(0 != iRet)
	{
		LOGE("ProTimsGetMultTask ProTimsSendRecv, iRet=%d", iRet);
		return iRet;
	}

	sprintf(szTempFileName, "mnt/sdcard/MULTI_TASK_%8.8s.TMP", psSN);

	fp = fopen(szTempFileName, "wb+");
	if (NULL == fp)
	{
		LOGE("ProTimsGetMultTask, FILE_OPEN_FAILED_=%d", FILE_OPEN_FAILED_);
		return FILE_OPEN_FAILED_;
	}

	if (fwrite(sRecvBuff, sizeof(char), iRecvLen, fp) == -1)
	{
		LOGE("ProTimsGetMultTask FILE_WRITE_FAILED_=%d", FILE_WRITE_FAILED_);
		return FILE_WRITE_FAILED_;
	}
	
	iRcvFileLen = iRecvLen;


	LOGE("sRecvBuff[6]=%d, sRecvBuff[7]=%d",sRecvBuff[6], sRecvBuff[7]);

//cld test
/*
	while (sRecvBuff[6] != sRecvBuff[7] + 1 )
	{
		i = 0;
		sPackage[i++] = bAuthResult;
		memcpy(sPackage+i, sHostRandomEn, 8);
		i += 8;
		sPackage[i++] = bCallMode;						// 0-monitor call TMS
		sPackage[i++] = sRecvBuff[6];
		sPackage[i++] = sRecvBuff[7] + 1 ;
		memset(sRecvBuff, 0x00, sizeof(sRecvBuff));
		iRet = ProTimsSendRecv(PROTIMS_MSG_GET_MULTITASK_TABLE, sPackage, i, sRecvBuff, &iRecvLen);
        LOGE("ProTimsSendRecv out------2----,iRet=%d",iRet);

		if(0!= iRet)
		{
			return iRet;
		}

		fseek(fp, 0L, SEEK_END);

		if (fwrite(&sRecvBuff[8], sizeof(char), iRecvLen-8, fp) == -1)
		{
			return FILE_WRITE_FAILED_;
		}
		iRcvFileLen += iRecvLen-8;	
	}
*/

	i = 0;
	sOutBuff[i++] = PROTIMS_MSG_GET_MULTITASK_TABLE;
	sOutBuff[i++] = (iRcvFileLen+1)/256;
	sOutBuff[i++] = (iRcvFileLen+1)%256;
	sOutBuff[i++] = 0;	

	fseek(fp, 0L, SEEK_SET);	
	fread(&sOutBuff[i], sizeof(char), iRecvLen, fp);
	// memcpy(&sOutBuff[i], sRecvBuff, iRecvLen);
	fclose(fp);
	remove(szTempFileName);

    LOGE("ProTimsGetMultTask out");
	return 0;
}

int ProTimsParseRemoteTaskTable(unsigned char *psTaskBuff, void *pstInRemoteTaskTable, int *iOutRemoteNum, void *pstInPukTaskTable, int *iOutPukNum)
{
	int i = 10,	iTaskNum = 0; 
	int iBuffLen = psTaskBuff[1]*256 + psTaskBuff[2] + 3;
	TASK_TABLE *pstRemoteTaskTable = (TASK_TABLE *)pstInRemoteTaskTable;
	TASK_TABLE *pstPukTaskTable    = (TASK_TABLE *)pstInPukTaskTable;
	unsigned char szAppName[33];
	
	*iOutPukNum    = 0;
	*iOutRemoteNum = 0;
	memset(pstRemoteTaskTable, 0, sizeof(TASK_TABLE)*PROTIMS_MAX_TASK);
	memset(pstPukTaskTable,    0, sizeof(TASK_TABLE)*PROTIMS_MAX_TASK);
	
	i = 12; // only new version use 3c to get task	
	// parse task 
	while(i < iBuffLen)	
	{
		pstRemoteTaskTable[iTaskNum].TaskNo   = psTaskBuff[i]*256 + psTaskBuff[i+1];
		i += 2;
		pstRemoteTaskTable[iTaskNum].TaskType = psTaskBuff[i++];
		pstRemoteTaskTable[iTaskNum].AllSize = 0;

		switch(pstRemoteTaskTable[iTaskNum].TaskType)
		{
		case PROTIMS_DOWNLOAD_MONITOR:  // monitor
			memcpy(pstRemoteTaskTable[iTaskNum].vern, psTaskBuff+i, 20); // version
			i += 20;
			pstRemoteTaskTable[iTaskNum].AllSize = HexToULong(psTaskBuff+i); // task size
			i += 4;
			*iOutRemoteNum = *iOutRemoteNum+1;
			break;
		case PROTIMS_DOWNLOAD_FONT:     // font
			memcpy(pstRemoteTaskTable[iTaskNum].vern, psTaskBuff+i, 20); // version
			i += 20;
			pstRemoteTaskTable[iTaskNum].AllSize = HexToULong(psTaskBuff+i); // task size
			i += 4; 
			*iOutRemoteNum = *iOutRemoteNum+1;
			break;
		case PROTIMS_DELETE_APPLICATION: // delete application
			i += 52;
			continue;			
		case PROTIMS_DOWNLOAD_APPLICATION: // application
			memcpy(pstRemoteTaskTable[iTaskNum].vern, psTaskBuff+i, 20); 			// version
			i += 20;
			pstRemoteTaskTable[iTaskNum].AllSize = HexToULong(psTaskBuff+i); 		// task size
			i += 4;
//			pstRemoteTaskTable[iTaskNum].LoadAddr = HexToULong(psTaskBuff+i); 	// load address, 0-application manager, 1-normal application
			i += 4;
//			pstRemoteTaskTable[iTaskNum].MainEntry = HexToULong(psTaskBuff+i); 	// main entry address
			i += 4;
//			pstRemoteTaskTable[iTaskNum].EventEntry = HexToULong(pBuff+i); 		// event entry address
			i += 4;
			memcpy(pstRemoteTaskTable[iTaskNum].AppName, (char *)psTaskBuff+i, 32); // application name
			sprintf(szAppName, "%32.32s", (char *)psTaskBuff+i);
			LOGE("APP Name=%s", szAppName);
			i += 32;
			*iOutRemoteNum = *iOutRemoteNum+1;
			break;
		case PROTIMS_CREATE_FILE_SYSTEM:  // create file system
			continue;
		case PROTIMS_DOWNLOAD_PARA_FILE:  // parameter file
			pstRemoteTaskTable[iTaskNum].AllSize = HexToULong(psTaskBuff+i); // task size
			i += 4;
			memcpy(pstRemoteTaskTable[iTaskNum].AppName, (char *)psTaskBuff+i, 32); // application
			i += 32;
			memcpy(pstRemoteTaskTable[iTaskNum].FileName, (char *)psTaskBuff+i, 30); // parameter file
			i += 30;
			memcpy(pstRemoteTaskTable[iTaskNum].vern, (char *)psTaskBuff+i, 20); // version
			i += 20;

			if(0 == IsSOFile((char*)pstRemoteTaskTable[iTaskNum].FileName) && strlen((char *)pstRemoteTaskTable[iTaskNum].FileName) > 16)
			{
				pstRemoteTaskTable[iTaskNum].FileName[16] = 0;
			}	
			*iOutRemoteNum = *iOutRemoteNum+1;
			break;
		case PROTIMS_WNET_FIRMWARE_UPDATE:  // wifi driver
			i += 20;
			continue;
		case PROTIMS_DOWNLOAD_DLL:
			memcpy(pstRemoteTaskTable[iTaskNum].vern, (char *)psTaskBuff+i, 20); // version
			i += 20;
			pstRemoteTaskTable[iTaskNum].AllSize = HexToULong(psTaskBuff+i); // task size
			i += 4;
			memcpy(pstRemoteTaskTable[iTaskNum].AppName, (char *)psTaskBuff+i, 32); // dll file name
			i += 32;
			pstRemoteTaskTable[iTaskNum].ForceUpdate = psTaskBuff[i];
			i++;
			*iOutRemoteNum = *iOutRemoteNum+1;
			break;
		case PROTIMS_DELLETE_DLL:
			memcpy(pstRemoteTaskTable[iTaskNum].vern, (char *)psTaskBuff+i, 20); // version
			i += 20;
			memcpy(pstRemoteTaskTable[iTaskNum].AppName, (char *)psTaskBuff+i, 32); // dll name
			i += 32;
			*iOutRemoteNum = *iOutRemoteNum+1;
			break;
		case PROTIMS_DOWNLOAD_USPUK:
			memcpy(pstRemoteTaskTable[iTaskNum].vern, (char *)psTaskBuff+i, 20); // version
			i += 20;
			pstRemoteTaskTable[iTaskNum].AllSize = HexToULong(psTaskBuff+i); // task size
			i += 4;
			memcpy(pstRemoteTaskTable[iTaskNum].AppName, (char *)psTaskBuff+i, 32); // dll name
			i += 32;
			memcpy(&pstPukTaskTable[*iOutPukNum], &pstRemoteTaskTable[iTaskNum], sizeof(TASK_TABLE));
			*iOutPukNum = *iOutPukNum+1;
			break;
		case PROTIMS_DOWNLOAD_UAPUK:
			memcpy(pstRemoteTaskTable[iTaskNum].vern, (char *)psTaskBuff+i, 20); // version
			i += 20;
			pstRemoteTaskTable[iTaskNum].AllSize = HexToULong(psTaskBuff+i); // task size
			i += 4;
			memcpy(pstRemoteTaskTable[iTaskNum].AppName, (char *)psTaskBuff+i, 32); // PUK file name
			i += 32;
			*iOutRemoteNum = *iOutRemoteNum+1;
			break;
		case PROTIMS_DOWNLOAD_PUBFILE:
			memcpy(pstRemoteTaskTable[iTaskNum].vern, (char *)psTaskBuff+i, 20); // version
			i += 20;
			pstRemoteTaskTable[iTaskNum].AllSize = HexToULong(psTaskBuff+i); // task size
			i += 4;
			memcpy(pstRemoteTaskTable[iTaskNum].AppName, (char *)psTaskBuff+i, 32); // File name
			i += 32;	
			*iOutRemoteNum = *iOutRemoteNum+1;
			break;
		case PROTIMS_DELETE_PUBFILE:
			memcpy(pstRemoteTaskTable[iTaskNum].vern, (char *)psTaskBuff+i, 20); // version
			i += 20;
			memcpy(pstRemoteTaskTable[iTaskNum].AppName, (char *)psTaskBuff+i, 32); // File name
			i += 32;
			*iOutRemoteNum = *iOutRemoteNum+1;
			break;			
		case PROTIMS_DELETE_ALL_APPLICATION:	// delete all application
			continue;
		default:
			return PROTIMS_COMM_WNET_RECV_ERROR;
		}

		iTaskNum++;  

		if(iTaskNum >= PROTIMS_MAX_TASK)
		{
			return iTaskNum;
		}
	}
	
	return iTaskNum;
}

int ProTimsParseRemoteTaskTable2(void *pstInRemoteTaskTable, int iTaskNum, void *pstInPukTaskTable, int iPukTaskNum)
{
	int iRet = 0, i = 0, iTotal = 0;
	TASK_TABLE stTmpTaskTable[PROTIMS_MAX_TASK] = {{0}};
	TASK_TABLE *pstRemoteTaskTable = (TASK_TABLE *)pstInRemoteTaskTable;
	TASK_TABLE *pstPukTaskTable	   = (TASK_TABLE *)pstInPukTaskTable;
	
	if(iTaskNum < 2 || iTaskNum > PROTIMS_MAX_TASK) // less than 2 puk task
	{
		return iTaskNum;
	}
	
	for(i=0; i<iTaskNum; i++)
	{
		if (PROTIMS_DOWNLOAD_USPUK !=pstRemoteTaskTable[i].TaskType)
		{
			memcpy(&stTmpTaskTable[iTotal], &pstRemoteTaskTable[i], sizeof(TASK_TABLE));
			iTotal++;
		}	
	}
	
	Bublesort(pstRemoteTaskTable, iTaskNum);
	for(i = 0; i < iPukTaskNum; i++)
	{
		memcpy(&stTmpTaskTable[iTotal], &pstPukTaskTable[i], sizeof(TASK_TABLE));
		iTotal++;
	}
	
	memset(pstRemoteTaskTable, 0, sizeof(TASK_TABLE)*PROTIMS_MAX_TASK); // recopy again	
	for(i = 0; i < iTotal; i++ )
	{
		memcpy(&pstRemoteTaskTable[i], &stTmpTaskTable[i], sizeof(TASK_TABLE));
	}
	
	return iTotal;
}

int ProTimsLoadFile(unsigned char ucTaskNo, unsigned long fsize,  void *pstInTerminal, void *pstInControl)
{
	const int DATA_OFFSET    = 8;
	const int ADDRESS_OFFSET = 31;
	FILE *fp = NULL;
	int	i = 0, k = 0, iRet = 0;
	int	iRetryTimes = 0; // retry download times
	int	iRecvLen    = 0; // receive length
	int	iDestLen    = 0; // the length of decompressed data
	int iSendCount  = 0;
	int	iRecvCount  = 0;
	int	iCheckCount = 0;
	int	iPacketNum  = 0;
	unsigned long ulLoadSize; // the downloaded size
	unsigned char sBuff[40];
	unsigned char szTmpName[33];
	unsigned char szFileName[128];
	unsigned char sRecvBuff[RECV_BUFFER_SIZE];
	ST_CONTROL *pstControl = (ST_CONTROL *)pstInControl;
	ST_TERMINAL *pstTerminal = (ST_TERMINAL *)pstInTerminal;

	iRetryTimes = 3;
	memset(sBuff, 0, 40);

	GenTmpFileName(ucTaskNo, szTmpName);
	sprintf(szFileName, "%s/%s", pstControl->PathName, szTmpName);
	ulLoadSize = GetFileSizeEX((unsigned char *)szFileName);
	LOGI("ProTimsLoadFile, pstControl->AllTaskSize=%d", pstControl->AllTaskSize);
	LOGI("ProTimsLoadFile, ulLoadSize=%d", ulLoadSize);
	LOGI("ProTimsLoadFile, fsize=%d", fsize);
	if(ulLoadSize <= 0)
	{
		fp = fopen(szFileName, "ab+");
		LOGE("ProTimsLoadFile");
		if (NULL == fp)
		{
			DroidSetProgress(TYPE_TMS, STEP_TMS_LOAD_FILE, DONE_DOING, pstControl->TaskNum, pstControl->CurTask, pstControl->AllTaskSize, 0, fsize, FILE_OPEN_FAILED_);
			return FILE_OPEN_FAILED_;
		}
		ulLoadSize = 0;
		fseek(fp, 0L, SEEK_CUR);
	}
	else
	{
	    LOGI("szFileName%s", szFileName);
		fp = fopen(szFileName, "ab+");
		if(NULL == fp)
		{
		    LOGE("open %s fail",szFileName);
			DroidSetProgress(TYPE_TMS, STEP_TMS_LOAD_FILE, DONE_DOING, pstControl->TaskNum, pstControl->CurTask, pstControl->AllTaskSize, ulLoadSize, fsize, FILE_OPEN_FAILED_);
			return FILE_OPEN_FAILED_;
		}
	}

	iRecvLen = 0;
	if(strcmp(pstControl->FileName, szFileName))
	{
		strcpy(pstControl->FileName, szFileName);
		pstControl->DownloadSize += ulLoadSize;
		DroidSetProgress(TYPE_TMS, STEP_TMS_LOAD_FILE, DONE_DOING, pstControl->TaskNum, pstControl->CurTask, pstControl->AllTaskSize, pstControl->DownloadSize, fsize, 0);
	}

	iRet = 0;
	iSendCount  = 0;
	iRecvCount  = 0;
	iCheckCount = 0;


	sRecvBuff[0] = 0;
	while (1)
	{
		if((unsigned long)ulLoadSize >= fsize)
		{
			break;
		}

		i = 0;
		memcpy(&sBuff[i], pstTerminal->SN, 8);
		LOGI("ProTimsLoadFile, pstTerminal->SN=%s", pstTerminal->SN);
		i += 23;
		memcpy(&sBuff[i], pstControl->TermRandomEn+4, 4);
		LogHexData(pstControl->TermRandomEn, 8);
		memcpy(&sBuff[i+4], pstControl->HostRandomEn+4, 4);
		LogHexData(pstControl->HostRandomEn, 8);
		i += 8;
		sBuff[i++] = (unsigned char)(ucTaskNo/256);
		sBuff[i++] = (unsigned char)(ucTaskNo%256);
		sBuff[i++] = (unsigned char)(pstControl->DownloadSize*100L/pstControl->AllTaskSize);

		if(1 == pstControl->FirstLoadFile)
		{
			pstControl->FirstLoadFile = 0;
		}
		ULongToHex(ulLoadSize, sBuff+i);
		i += 4;
		sBuff[i++] = PROTIMS_IS_ZIP;

		TmsReset();
		LogHexData(sBuff, i);
		//MySleep(3); // 100->3, Modified by lirz 20150301
		iRet = ProTimsSendPack(PROTIMS_MSG_LOAD_DATA, sBuff, i);

		if(0 != iRet)
		{
			LOGE("ProTimsLoadFile, retry ProTimsSendPack");
			iSendCount++;
			if(iSendCount < iRetryTimes)
			{
				continue;
			}
			else
			{
				DroidSetProgress(TYPE_TMS, STEP_TMS_LOAD_FILE, DONE_DOING, pstControl->TaskNum, pstControl->CurTask, pstControl->AllTaskSize, pstControl->DownloadSize, fsize, iRet);
				fclose(fp);
				return iRet;
			}
		}

		iSendCount = 0;
		if(iRecvLen > 0)
		{
			LOGW("ProTimsLoadFile, WriteRecvData, iRecvLen=%d", iRecvLen);
			LOGE("ProTimsLoadFile, WriteRecvData, iRecvLen=%d", iRecvLen);
			iRet = WriteRecvData(fp, sRecvBuff, iRecvLen, &iDestLen);
			LOGW("ProTimsLoadFile, WriteRecvData, iDestLen=%d", iDestLen);
			if (0 != iRet)
			{
				fclose(fp);
				LOGE("ProTimsLoadFile, WriteRecvData error");
				DroidSetProgress(TYPE_TMS, STEP_TMS_LOAD_FILE, DONE_DOING, pstControl->TaskNum, pstControl->CurTask, pstControl->AllTaskSize, pstControl->DownloadSize, fsize, iRet);
				return iRet;
			}
			LOGW("ProTimsLoadFile, WriteRecvData, pstControl->DownloadSize=%d", pstControl->DownloadSize);
			pstControl->DownloadSize += iDestLen;
			LOGW("ProTimsLoadFile, WriteRecvData, pstControl->DownloadSize=%d", pstControl->DownloadSize);
			iRecvLen = 0;
			DroidSetProgress(TYPE_TMS, STEP_TMS_LOAD_FILE, DONE_DOING, pstControl->TaskNum, pstControl->CurTask, pstControl->AllTaskSize, pstControl->DownloadSize, fsize, iRet);
		}

		memset(sRecvBuff, 0x00, sizeof(sRecvBuff));
		iRet = ProTimsRecvPack(PROTIMS_MSG_LOAD_DATA, sRecvBuff, &iRecvLen);
		LOGE("ProTimsLoadFile, ProTimsRecvPack, iRecvLen=%d", iRecvLen);
		if(0 != iRet)
		{
			LOGE("ProTimsLoadFile, retry ProTimsRecvPack");
			iRecvCount++;
			if (iRecvCount < iRetryTimes)
			{
				continue;
			}
			else
			{
				fclose(fp);
				DroidSetProgress(TYPE_TMS, STEP_TMS_LOAD_FILE, DONE_DOING, pstControl->TaskNum, pstControl->CurTask, pstControl->AllTaskSize, pstControl->DownloadSize, fsize, iRet);
				return iRet;
			}

		}
		else
		{
			iRecvLen -= DATA_OFFSET;
			for(k=0; k<DATA_OFFSET-1; k++)
			{
				if(sBuff[ADDRESS_OFFSET+k] != sRecvBuff[k])
				{
					fclose(fp);
					DroidSetProgress(TYPE_TMS, STEP_TMS_LOAD_FILE, DONE_DOING, pstControl->TaskNum, pstControl->CurTask, pstControl->AllTaskSize, pstControl->DownloadSize, fsize, 0);
					break;
				}
			}
			if((DATA_OFFSET-1) == k)
			{
				sRecvBuff[0] = 1;
				if (iRecvLen > 0)
				{
					if((ulLoadSize+PACKAGE_LENGTH) >= fsize)
					{
						LOGE("ProTimsLoadFile, WriteRecvData, iRecvLen=%d", iRecvLen);
						iRet = WriteRecvData(fp, sRecvBuff, iRecvLen, &iDestLen);
						LOGE("ProTimsLoadFile, WriteRecvData, iDestLen=%d", iDestLen);
						fclose(fp);
						pstControl->DownloadSize += iDestLen;
						DroidSetProgress(TYPE_TMS, STEP_TMS_LOAD_FILE, DONE_DOING, pstControl->TaskNum, pstControl->CurTask, pstControl->AllTaskSize, pstControl->DownloadSize, fsize, iRet);
						return iRet;
					}

					iRecvCount  = 0;
					iCheckCount = 0;
					ulLoadSize += PACKAGE_LENGTH;
					iPacketNum++;
				}
			}
			else
			{
				sRecvBuff[0] = 0;
				iCheckCount++;
				iRecvLen = 0;
				if(iCheckCount > iRetryTimes)
				{
					fclose(fp);
					DroidSetProgress(TYPE_TMS, STEP_TMS_LOAD_FILE, DONE_DOING, pstControl->TaskNum, pstControl->CurTask, pstControl->AllTaskSize, pstControl->DownloadSize, fsize, PROTIMS_COMM_WNET_RECV_ERROR);
					return PROTIMS_COMM_WNET_RECV_ERROR;
				}
			}
		} // end else
	} // end of while (1)

	fclose(fp);
	DroidSetProgress(TYPE_TMS, STEP_TMS_LOAD_FILE, DONE_DOING, pstControl->TaskNum, pstControl->CurTask, pstControl->AllTaskSize, pstControl->DownloadSize, fsize, 0);
	return iRet;
}

int ProTimsEnd(unsigned char *psSN)
{
	unsigned char sSendBuff[23];
	memset(sSendBuff, 0x00, 23);
	memcpy(sSendBuff, psSN, 8);
	return ProTimsSendPack(PROTIMS_MSG_DOWNLOAD_OK, sSendBuff, 23);
}

int ProTimsCompareTask(void* pstInRemoteTaskTable, void* pstInLocalTaskTable)
{
	int iCnt = 0;
	TASK_TABLE* pstRemoteTaskTable = (TASK_TABLE *)pstInRemoteTaskTable;
	TASK_TABLE* pstLocalTaskTable  = (TASK_TABLE *)pstInLocalTaskTable;
	for(iCnt = 0; iCnt<PROTIMS_MAX_TASK; iCnt++)
	{
		 // not eqaul,return 1
		if(memcmp(&pstRemoteTaskTable[iCnt], &pstLocalTaskTable[iCnt], sizeof(TASK_TABLE) - sizeof(char)))
		{
			return 1;
		}
	}

	return 0;
}

//////////////////////////////////INTERNAL//////////////////////////////////////
void Bublesort(TASK_TABLE *stTaskTables, int iTaskNum)
{
	int i,j;
	TASK_TABLE stTempTable = {0};
	
	for(j=0; j<iTaskNum-1; j++)
	{
		for(i=0; i<iTaskNum-1-j; i++)
		{
			if(strcmp((char*)stTaskTables[i].vern, (char*)stTaskTables[i+1].vern)> 0)
			{
				memcpy(&stTempTable,       &stTaskTables[i],   sizeof(TASK_TABLE));
				memcpy(&stTaskTables[i],   &stTaskTables[i+1], sizeof(TASK_TABLE));
				memcpy(&stTaskTables[i+1], &stTempTable, 	   sizeof(TASK_TABLE));
			}
		}
	}
	return;
}

int ProTimsRecvPack(unsigned char cmd, unsigned char *sOutRecvBuff, int *iOutRecvLen)
{
	int i = 0;
	int iDataLen   = 0;
	int iRecvCount =0;
	int iHaveRecvLen = 0;
	int iRet = 0;
	int iWnetRecvLen = -1;
	unsigned char sEdc[4] = {0};
	unsigned char sRecvBuff[RECV_BUFFER_SIZE] = {0};

	while(1)	
	{
		// recv the first package    
		iWnetRecvLen = TmsRecvs(sRecvBuff, sizeof(sRecvBuff));
		LOGE("iWnetRecvLen = %d", iWnetRecvLen);
		if(iWnetRecvLen < 0)
		{        
			if (iWnetRecvLen == -13)
			{
			    LOGE("iWnetRecvLen continue");
				continue;
			}
			else
			{
				LOGE("1.ProTimsRecvPack PROTIMS_WNET_NETRECV=%d", PROTIMS_WNET_NETRECV);
				return PROTIMS_WNET_NETRECV;
			}
		}
		else 
		{
			break;
		}
	}

LOGE("000000000000");
	iHaveRecvLen = 0;
	if ((0x02 != sRecvBuff[0]) || (cmd != sRecvBuff[1]))
	{
		LOGE("2.ProTimsRecvPack PROTIMS_WNET_NETRECV=%d", PROTIMS_COMM_WNET_RECV_ERROR);
		return PROTIMS_COMM_WNET_RECV_ERROR;	
	}
	
	// return code, 0-success
	if (0 != sRecvBuff[4])
	{
		LOGE("ProTimsRecvPack, PROTIMS_SERVER_DEALERROR=%d", PROTIMS_SERVER_DEALERROR);
		return PROTIMS_SERVER_DEALERROR; 
	}
LOGE("0000000000011111111111");
	iDataLen =sRecvBuff[2]*256 + sRecvBuff[3];
	iHaveRecvLen = iWnetRecvLen;	
LOGE("111111111111111");
	// receive data, in the process of translation, 
	// the data maybe divied into multi-package, 
	// so need re-combine the data packages.
	while(iHaveRecvLen < (iDataLen+8))
	{		
		iWnetRecvLen = TmsRecvs(sRecvBuff+iHaveRecvLen, sizeof(sRecvBuff)-iHaveRecvLen);
		iRecvCount++;  
		if(iWnetRecvLen < 0)
		{        
			if((iWnetRecvLen==-13)&&(iRecvCount < PROTIMS_DATA_RETRY_TIMES) )
			{
				continue;
			}
			else
			{
				LOGE("3.ProTimsRecvPack PROTIMS_WNET_NETRECV=%d", PROTIMS_WNET_NETRECV);
				return PROTIMS_WNET_NETRECV;
			}
		}
		iHaveRecvLen += iWnetRecvLen;
	}

LOGE("22222222222222");
	// wrong length of the received data
	if(iHaveRecvLen != (iDataLen+8))
	{
		LOGE("4.ProTimsRecvPack PROTIMS_COMM_WNET_RECV_ERROR=%d", PROTIMS_COMM_WNET_RECV_ERROR);
		return PROTIMS_COMM_WNET_RECV_ERROR;
	}

LOGE("333333333333");
	GenEDC(sRecvBuff+1, sEdc);
LOGE("34444444444444");
	// length of data + 4(identifier, command code, content data length(2bytes)) + iDataLen
	// verify data CRC
	if (0 != memcmp(sRecvBuff+4+iDataLen, sEdc, 4)) 
	{
		LOGE("5.ProTimsRecvPack PROTIMS_COMM_VERIFY_ERROR=%d", PROTIMS_COMM_VERIFY_ERROR);
		return PROTIMS_COMM_VERIFY_ERROR;
	}
	LOGE("55555555555");
	memcpy(sOutRecvBuff, sRecvBuff+5, iDataLen-1);
	*iOutRecvLen = iDataLen - 1;
LOGE("55555555555");
	return 0;
}

int ProTimsSendPack(unsigned char cmd, unsigned char *sInSendBuff, int iSendLen)
{
	unsigned char sSendBuff[SEND_BUFFER_SIZE];
	int iLen = 0;
	
	memset(sSendBuff, 0x00, SEND_BUFFER_SIZE);
	sSendBuff[0] = 0x02;  // identifier
	sSendBuff[1] = cmd;   // command code
	sSendBuff[2] = (unsigned char)(iSendLen/256);  // high 8 bit of the data length
	sSendBuff[3] = (unsigned char)(iSendLen%256);  // low  8 bit of the data length
	memcpy(sSendBuff+4, sInSendBuff, iSendLen);
	GenEDC(sSendBuff+1, sSendBuff+iSendLen+4);
	iSendLen += 8;

	iLen = TmsSends(sSendBuff, iSendLen);
	if(iLen != iSendLen){
		LOGE("TmsSends Failed, %d, %d",iLen, iSendLen);
		return iLen;
	}

	return 0;
}

int ProTimsRecvControlPack(unsigned char cmd, unsigned char *sOutRecvBuff, int *iRecvLen)
{
	int	i	  = 0;
	int iRet = 0;
	unsigned char sEdc[4]	   = {0};
	unsigned char sRecvBuff[256] = {0};
	
	*iRecvLen = 0;
	memset(sRecvBuff, 0x00, sizeof(sRecvBuff));
	iRet = TmsRecvs(sRecvBuff, sizeof(sRecvBuff));
	LOGE("1.ProTimsRecvControlPack iRet=%d, %d, %d", iRet, sRecvBuff[0], sRecvBuff[1]);
	if ( (0x02!=sRecvBuff[0]) || (cmd!=sRecvBuff[1]) ) 
	{
		LOGE("1.ProTimsRecvControlPack PROTIMS_COMM_WNET_RECV_ERROR=%d",PROTIMS_COMM_WNET_RECV_ERROR);
		return PROTIMS_COMM_WNET_RECV_ERROR;
	}

	*iRecvLen = sRecvBuff[2] * 256 + sRecvBuff[3];
	LOGE("2.ProTimsRecvControlPack *iRecvLen=%d", *iRecvLen);
	// verifiy the data length of package
	if (*iRecvLen != (iRet - 8)) 
	{
		LOGE("2.ProTimsRecvControlPack PROTIMS_COMM_WNET_RECV_ERROR=%d",PROTIMS_COMM_WNET_RECV_ERROR);
		return PROTIMS_COMM_WNET_RECV_ERROR;
	}

	GenEDC(sRecvBuff+1, sEdc);
	// check data package verify code
	if (memcmp(sRecvBuff+4+*iRecvLen, sEdc, 4))  
	{
		LOGE("ProTimsRecvControlPack PROTIMS_COMM_VERIFY_ERROR=%d", PROTIMS_COMM_VERIFY_ERROR);
		return PROTIMS_COMM_VERIFY_ERROR;
	}
	memcpy(sOutRecvBuff, sRecvBuff+4, *iRecvLen);

	return 0;
}

int ProTimsSendRecv(unsigned char cmd, unsigned char *sSendBuff, int iSendLen, unsigned char *sRecvBuff, int *iRecvLen)
{
	int i, iRet;

	for (i=0; i<3; i++)
	{
		TmsReset();
		iRet = 0;
		iRet = ProTimsSendPack(cmd, sSendBuff, iSendLen);
        LOGE("ProTimsSendPack return:%d",iRet);
		if (0 != iRet)
		{
		    LOGE("ProTimsSendPack continue");
			continue;
		}

		iRet = ProTimsRecvPack(cmd, sRecvBuff, iRecvLen);
        LOGE("ProTimsRecvPack return:%d",iRet);
		if(0 == iRet)
		{
		    LOGE("ProTimsRecvPack Failed:%d", iRet);
			return iRet;  
		}
	}

	LOGE("ProTimsSendRecv Failed:%d", iRet);
	return iRet;
}

int PrTimsGetLastTaskTable(unsigned char *szPathName, void* pstOutTaskTable)
{
	int iRet = 0;
	int iFileSize = 0;
	FILE *fp = NULL;
	unsigned char szFileName[128];

	sprintf(szFileName, "%s/PROTIMS_TASK.LOG", szPathName);
	iFileSize = GetFileSizeEX(szFileName);
	if(0 == iFileSize)
	{
		return 0;
	}
	if(0 > iFileSize)
	{
		return FILE_READ_FAILED_;
	}

	fp = fopen(szFileName, "rb");
	if(NULL == fp)
	{
		return 0;
	}

	if((iRet = fseek(fp, 0L, SEEK_SET)) < 0)
	{
		fclose(fp);
		return FILE_SEEK_FAILED_;
	}

	if( (iRet=fread((unsigned char*) pstOutTaskTable, sizeof(char), PROTIMS_MAX_TASK*sizeof(TASK_TABLE), fp))%sizeof(TASK_TABLE) != 0)
	{
		fclose(fp);
		return FILE_READ_FAILED_;
	}

	fclose(fp);
	return 0;
}

int ProTimsDeleteOldTmpFile(unsigned char *szPathName, void* pstInTaskTable)
{
	char szTmpName[33] = {0};
	char szFileName[128] = {0};
	int iCnt = 0;
	TASK_TABLE *pstTaskTable = (TASK_TABLE *)pstInTaskTable;

	for(iCnt = 0; iCnt<PROTIMS_MAX_TASK; iCnt++)
	{
		GenTmpFileName(pstTaskTable[iCnt].TaskNo, szTmpName);
		sprintf(szFileName, "%s/%.32s", szPathName, szTmpName);
		remove(szFileName);
	}

	return 0;
}

int ProTimsSaveTaskTable(unsigned char *szPathName, void *pstInRemoteTaskTable, int iTaskNum)
{
	int iCnt = 0;
	int iRet = 0;
	FILE *fp = NULL;
	unsigned char szFileName[128];
	TASK_TABLE *pstRemoteTaskTable = (TASK_TABLE *)pstInRemoteTaskTable;

	sprintf(szFileName, "%s/PROTIMS_TASK.LOG", szPathName);
	fp = fopen(szFileName, "wb");
	if (NULL == fp)
	{
		return FILE_OPEN_FAILED_;
	}

	if ((iRet = fseek(fp, 0L, SEEK_SET)) < 0)
	{
		fclose(fp);
		return FILE_SEEK_FAILED_;
	}

	for(iCnt=0 ; iCnt<iTaskNum; iCnt++)
	{
		if((iRet = fwrite(&pstRemoteTaskTable[iCnt], sizeof(TASK_TABLE), 1, fp)) < 0)
		{
			fclose(fp);
			return FILE_WRITE_FAILED_;
		}
	}

	fclose(fp);
	return 0;
}

int ProTimsGetFile(void *pstInRemoteTaskTable, int iTaskNum, void *pstInTerminal, void *pstInControl)
{
	int iCnt = 0;
	int iRet = 0;
	TASK_TABLE *pstRemoteTaskTable = (TASK_TABLE *)pstInRemoteTaskTable;
	ST_TERMINAL *pstTerminal = (ST_TERMINAL *)pstInTerminal;
	ST_CONTROL	*pstControl  = (ST_CONTROL *)pstInControl;

	LOGI("ProTimsGetFile, iTaskNum=%d", iTaskNum);
	for(iCnt = 0; iCnt < iTaskNum; iCnt++)
	{
		switch (pstRemoteTaskTable[iCnt].TaskType)
		{
			case PROTIMS_CREATE_FILE_SYSTEM:
			case PROTIMS_DELETE_APPLICATION:
			case PROTIMS_DELLETE_DLL:
			case PROTIMS_DELETE_PUBFILE:
			case PROTIMS_DELETE_ALL_APPLICATION:
				continue;
			case PROTIMS_DOWNLOAD_APPLICATION:
			case PROTIMS_DOWNLOAD_DLL:
			case PROTIMS_DOWNLOAD_PARA_FILE:
			case PROTIMS_DOWNLOAD_FONT:
			case PROTIMS_DOWNLOAD_MONITOR:
			case PROTIMS_DOWNLOAD_USPUK:
			case PROTIMS_DOWNLOAD_UAPUK:
			case PROTIMS_DOWNLOAD_PUBFILE:
				pstRemoteTaskTable[iCnt].done = PROTIMS_DOWNLOADING_STATUS;
				break;
			default:
				break;
		}

		if (pstRemoteTaskTable[iCnt].done == PROTIMS_DOWNLOADING_STATUS)
		{
			LOGW("ProTimsGetFile, iCnt=%d, TaskNo=%d, AllSize=%d", iCnt,
					pstRemoteTaskTable[iCnt].TaskNo,
					pstRemoteTaskTable[iCnt].AllSize);
			MySleep(2); // 20-2 Modified by lirz 20150302
			pstControl->CurTask = iCnt+1;
			iRet = ProTimsLoadFile(pstRemoteTaskTable[iCnt].TaskNo, pstRemoteTaskTable[iCnt].AllSize, pstTerminal, pstControl);
			LOGW("ProTimsLoadFile, iRet=%d", iRet);
			if (0 != iRet)
			{
				TmsClose();
				return PROTIMS_LOAD_FILE_EX_ERROR;
			}

			pstRemoteTaskTable[iCnt].done = PROTIMS_DOWNLOADED_STATUS;
		}
	}

	return 0;
}


