#include "platform.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "file.h"
#include "compress.h"
#include "common.h"

int WriteRecvData(FILE *fp, unsigned char *sBuff, int iLen, int *piDestLen)
{
	int iDestLen = 0;
	int iRet     = 0;
	unsigned long ulLen = 0;

	if(0 == sBuff[0]) 
	{
		return 0;
	}
	else if (1 == sBuff[7])  // support decompress
	{
		iRet = Decompress(&sBuff[10], &sBuff[20000], iLen-2, &ulLen);
		iDestLen = (int)ulLen;
		if (0 == iRet)
		{
			if ( iDestLen != (sBuff[8]*256+sBuff[9]) )
			{
				return COMPRESS_DECOMPRESS_ERROR;
			}
			else if (iDestLen == (signed)fwrite(&sBuff[20000], sizeof(char), (int)iDestLen, fp))
			{
				*piDestLen = iDestLen;
				return 0;
			}
			else
			{
				return FILE_WRITE_FAILED_;
			}
		}
		else
		{
			return COMPRESS_DECOMPRESS_ERROR;
		}
	}
	else
	{
		if (iLen == (signed)fwrite(&sBuff[8], sizeof(char), iLen, fp))
		{
			*piDestLen = iLen;
			return 0;
		}
		else
		{
			return FILE_WRITE_FAILED_;
		}
	}
}

int GetFileSizeEX(unsigned char *szFileName)
{
	int  iLen = 0;
	FILE *fp = NULL;
	
	fp = fopen((char*)szFileName, "ab+");
	LOGE("GetFileSizeEX");
	if(NULL == fp)
	{ 
		LOGE("GetFileSizeEX FILE_OPEN_FAILED_=%d", FILE_OPEN_FAILED_);
		return -1;
	}

	fseek(fp, 0L, SEEK_END);
	iLen = ftell(fp);
	LOGE("GetFileSizeEX iLen=%d", iLen);

	if(iLen < 0)
	{
		LOGE("GetFileSizeEX FILE_TELL_FAILED_=%d", FILE_TELL_FAILED_);
		fclose(fp);
		return -1;
	}

	fclose(fp);
	return iLen;
}







