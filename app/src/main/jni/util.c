#include "platform.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#if !defined(WIN32) && !defined(WINDOWS)
#include <sys/time.h>
#include <unistd.h>
#endif
#include "common.h"
#include "util.h"

unsigned long HexToULong(unsigned char *psHex)
{
	unsigned long ulData = 0;
	unsigned long ulTmp  = 0;
	
	ulData = 0;
	ulTmp  = 1;
	ulData += psHex[3]*ulTmp;
	ulTmp  *= 256;
	ulData += psHex[2]*ulTmp;
	ulTmp  *= 256;
	ulData += psHex[1]*ulTmp;
	ulTmp  *= 256;
	ulData += psHex[0]*ulTmp;

	return ulData;
}

void ULongToHex(unsigned long ulData, unsigned char *psOutHex)
{
	unsigned short nHigh, nLow;	
	nHigh = (unsigned short)(ulData/65536);
	nLow  = (unsigned short)(ulData%65536);
	psOutHex[0] = nHigh/256;
	psOutHex[1] = nHigh%256;
	psOutHex[2] = nLow/256;
	psOutHex[3] = nLow %256;
}

void Stn32BitCRC(unsigned char sOutCRC[4], unsigned char *sData, unsigned short usLen)
{
	unsigned long  ulRSL, ulTL;
	unsigned short i;
	unsigned char  ucTemp, k;
	
	ulRSL = 0xffffffffL;
	for (i = 0; i < usLen; i++)
	{
		ucTemp = (unsigned char )ulRSL; 	
		ucTemp = ucTemp^sData[i];		
		ulTL = (unsigned long)ucTemp;		
		for (k = 0; k < 8; k++)
		{
			if (ulTL & 1)
			{
				ulTL = 0xedb88320L^(ulTL>>1);
			}
			else
			{
				ulTL = ulTL>>1;
			}
		}
		ulRSL = ulTL^(ulRSL>>8);
	}	
	ulRSL ^= 0xffffffffL;
	sOutCRC[0] = (unsigned char )(ulRSL>>24);
	sOutCRC[1] = (unsigned char )(ulRSL>>16);
	sOutCRC[2] = (unsigned char )(ulRSL>>8);
	sOutCRC[3] = (unsigned char )(ulRSL);
}

int GenTmpFileName(int iTaskNo, unsigned char* pszOutTmpFileName)
{
	if (pszOutTmpFileName == NULL)
	{
		return -1;
	}
	sprintf(pszOutTmpFileName, "PROTIMS_%d.LOG", iTaskNo);
	return 0;
}

void  GetTime(unsigned char *psOutTime)
{
#if defined(WIN32) || defined(WINDOWS)
	SYSTEMTIME systime;	
	unsigned char sHour[2] = {0};
	unsigned char sMin[2]  = {0};
	unsigned char sSec[2]  = {0};
	unsigned char sTime[8] = {0};

	GetLocalTime(&systime);	
	if(systime.wHour < 10)
	{
		sprintf(sHour, "%s%d", "0", systime.wHour);
	}
	else
	{
		sprintf(sHour, "%d", systime.wHour);
	}
	
	if(systime.wMinute < 10)
	{
		sprintf(sMin, "%s%d", "0", systime.wMinute);
	}
	else
	{
		sprintf(sMin, "%d", systime.wMinute);
	}
	if(systime.wSecond < 10)
	{
		sprintf(sSec, "%s%d", "0", systime.wSecond);
	}
	else
	{
		sprintf(sSec, "%d", systime.wSecond);
	}
	sprintf((char*)psOutTime, "%s:%s:%s", sHour, sMin, sSec);
	return;
#else
	struct tm *pstDateTime = NULL;
	time_t tmTime;
	time(&tmTime);
	pstDateTime = localtime(&tmTime);
	sprintf((char*)psOutTime, "%2.2d:%2.2d:%2.2d", pstDateTime->tm_hour, pstDateTime->tm_min, pstDateTime->tm_sec);
#endif // WIN32 || WINDOWS
}

int IsSOFile(unsigned char* pszFileName)
{
	int  iLen;
	unsigned char *p;
	unsigned char sTemp[4] = {0};
	
	if(pszFileName == NULL)
	{
		return 0;
	}
	p = pszFileName;
	iLen = strlen(pszFileName);
	memcpy(sTemp, p+iLen-3, 3);
	if(0 == strncasecmp(sTemp, ".so", 3))
	{
		return 1;
	}
	
	return 0;
}

void MyRand(unsigned char *sOutBuff)
{
	LOGE("1.MyRand");
	GetTime(sOutBuff);
	LOGE("2.MyRand");
	vDes(1, sOutBuff, (unsigned char *)"\x16\x89\x88\xAA\x66\x58\x51\x8F", sOutBuff);
}

void des(unsigned char *sInBuff, unsigned char *sOutBuff, unsigned char *sDesKey, int iMode)
{
	iMode = 1;
	vDes(iMode, sInBuff, sDesKey, sOutBuff);
}

void GenEDC(unsigned char *sPackage, unsigned char *sOutEdc)
{
	unsigned short usLen = 0;	
	usLen = sPackage[1]*256 + sPackage[2] + 3;	
	Stn32BitCRC(sOutEdc, sPackage, usLen);
}

unsigned char lrc (const char* psInData, int iOffset, int iLen)
 {
	unsigned char lrc = 0;
	int  i   = 0;
	for (i=0; i<iLen; i++) {
		lrc ^= psInData[i + iOffset];
	}
	return lrc;
}

int EnumFont(void *pstOutFonts, int iMaxFontNum)
{
	return 0;
}

unsigned char* IsBold(int iBold)
{
	return iBold ? "BOLD" : "";
}

unsigned char* IsItalic(int iItalic)
{
	return iItalic ? "ITALIC" : "";
}

unsigned char* GetCharSet(int iCharSet)
{
	switch(iCharSet)
	{
	case CHARSET_WEST:
		return "WEST";	
	case CHARSET_TAI:
		return "TAI";	
	case CHARSET_MID_EUROPE:
		return "EUROPE";		
	case CHARSET_VIETNAM:
		return "VIETNAM";		
	case CHARSET_GREEK:
		return "GREEK";		
	case CHARSET_BALTIC:
		return "BALTIC";		
	case CHARSET_TURKEY:
		return "TURKEY";		
	case CHARSET_HEBREW:
		return "HEBREW";		
	case CHARSET_RUSSIAN:
		return "RUSSIAN";		
	case CHARSET_GB2312:
		return "GB2312";		
	case CHARSET_GBK:
		return "GBK";		
	case CHARSET_GB18030:
		return "GB18030";		
	case CHARSET_BIG5:
		return "BIG5";		
	case CHARSET_SHIFT_JIS:
		return "JIS";		
	case CHARSET_KOREAN:
		return "KOREAN";			
	case CHARSET_ARABIA:
		return "ARABIA";		
	case CHARSET_DIY:
		return "DIY";				
	default:
		break;
	}
	return "UNKNOWN";
}

void MySleep(unsigned long ulDelay)
{
#ifdef CLD_DEBUF
#if defined(WIN32) || defined(WINDOWS)
	Sleep(ulDelay);
#else
	LOGI("MySleep Start");
	usleep(ulDelay*1000);
	LOGI("MySleep End");
#endif
#endif
}

unsigned long MyGetTickCount()
{
#if defined(WIN32) || defined(WINDOWS)
	return GetTickCount();
#else
	struct timeval tv;
	if( 0 != gettimeofday(&tv, NULL) )
	{
		return 0;
	}
	return (tv.tv_sec * 1000) + (tv.tv_usec / 1000);
#endif     
}

void LogHexData(unsigned char *psInData, int iDataLen)
{
#if !defined(DEBUG)
	return;
#else
	int iCnt;

	if(iDataLen <= 0)
	{
		return;
	}
	if(NULL == psInData)
	{
		return;
	}

	for(iCnt=0; iCnt<iDataLen;)
	{
		if(iDataLen-iCnt>=8)
		{
			LOGI("%02X %02X %02X %02X %02X %02X %02X %02X",
				psInData[iCnt],    psInData[iCnt+1], psInData[iCnt+2],  psInData[iCnt+3],
				psInData[iCnt+4],  psInData[iCnt+5], psInData[iCnt+6],  psInData[iCnt+7]);
			iCnt += 8;
		}
		else
		{
			int iNum = iDataLen-iCnt;
			switch(iNum)
			{
			case 7:
				LOGI("%02X %02X %02X %02X %02X %02X %02X", psInData[iCnt],
						psInData[iCnt + 1], psInData[iCnt + 2],
						psInData[iCnt + 3], psInData[iCnt + 4],
						psInData[iCnt + 5], psInData[iCnt + 6]);
				iCnt += 7;
				break;
			case 6:
				LOGI("%02X %02X %02X %02X %02X %02X", psInData[iCnt],
						psInData[iCnt + 1], psInData[iCnt + 2], psInData[iCnt + 3],
						psInData[iCnt + 4], psInData[iCnt + 5]);
				iCnt += 6;
				break;
			case 5:
				LOGI("%02X %02X %02X %02X %02X", psInData[iCnt],
						psInData[iCnt + 1], psInData[iCnt + 2], psInData[iCnt + 3],
						psInData[iCnt + 4]);
				iCnt +=5;
				break;
			case 4:
				LOGI("%02X %02X %02X %02X", psInData[iCnt],
						psInData[iCnt + 1], psInData[iCnt + 2], psInData[iCnt + 3]);
				iCnt += 4;
				break;
			case 3:
				LOGI("%02X %02X %02X", psInData[iCnt], psInData[iCnt + 1], psInData[iCnt + 2]);
				iCnt += 3;
				break;
			case 2:
				LOGI("%02X %02X", psInData[iCnt], psInData[iCnt + 1]);
				iCnt += 2;
				break;
			case 1:
				LOGI("%02X", psInData[iCnt]);
				iCnt++;
				break;
			default:
				break;
			}
		}
	}
#endif // DEBUG
}

int GetPosType(unsigned char ucFlag)
{
	switch (ucFlag)
	{
	case 0x23:
		return 32;
	default:
		return 32;
	}
}


void LogTaskTable(void *psInTaskTable, int iTaskNum)
{
#if !defined(DEBUG)
	return;
#else // DeBUG
	int iCnt = 0;
	TASK_TABLE * psTaskTable = (TASK_TABLE *)psInTaskTable;
	if(iTaskNum <= 0 || NULL == psInTaskTable)
	{
		return;
	}

	LOGI("********STSRT LIST TASK TABLE********");
	for(iCnt=0; iCnt<iTaskNum; iCnt++)
	{
		LOGI("AllSize=%d", psTaskTable[iCnt].AllSize);
		LOGI("TaskNo=%d", psTaskTable[iCnt].TaskNo);
		LOGI("TaskType=%d", psTaskTable[iCnt].TaskType);
		LOGI("FileName=%32.32s", psTaskTable[iCnt].FileName);
		LOGI("AppName=%32.32s", psTaskTable[iCnt].AppName);
		LOGI("vern=%s", psTaskTable[iCnt].vern);
		LOGI("ForceUpdate=%d", psTaskTable[iCnt].ForceUpdate);
		LOGI("done=%d", psTaskTable[iCnt].done);
		LOGI("++++++++++++++++++++++++++++++++");
	}
	LOGI("********END LIST TASK TABLE********");
#endif // DEBUG
}



