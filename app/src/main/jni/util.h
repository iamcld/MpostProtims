#ifndef PAX_MPOS_PROTIMS_UTIL_H
#define PAX_MPOS_PROTIMS_UTIL_H

#ifdef __cplusplus
extern "C"{
#endif

unsigned long HexToULong(unsigned char *hex);
void ULongToHex(unsigned long ulData, unsigned char *psOutHex);
void Stn32BitCRC(unsigned char sOutCRC[4], unsigned char *sData, unsigned short usLen);
int  GenTmpFileName(int iTaskNo, unsigned char* pszOutTmpFileName);
int  IsSOFile(unsigned char* pszFileName);
void MyRand(unsigned char *sOutBuff);
void des(unsigned char *sInBuff, unsigned char *sOutBuff, unsigned char *sDesKey, int iMode);
void GenEDC(unsigned char *sPackage, unsigned char *sOutEdc);
unsigned char lrc (const char* psInData, int iOffset, int iLen);
int EnumFont(void *pstOutFonts, int iMaxFontNum);
unsigned char* GetCharSet(int iCharSet);
unsigned char* IsBold(int iBold);
unsigned char* IsItalic(int iItalic);
void MySleep(unsigned long ulDelay);
unsigned long MyGetTickCount();
void LogHexData(unsigned char *psInData, int iDataLen);
void LogTaskTable(void *psInTaskTable, int iTaskNum);
int GetPosType(unsigned char ucFlag);

#ifdef __cplusplus
}
#endif

#endif // PAX_MPOS_PROTIMS_UTIL_H
