#include "platform.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "xml.h"

#define XML_INFO_MAX				8192		// The max length of the xml document
#define XML_NAME_MAX				100			// The max length of the xml element's name

static int XmlFind(unsigned char *sDoc, int iDocLen, char *iFindFlag, int iFindFlagLen, int *iFindLocation);
static void XmlStrlwr(char *sTurned);

int XmlGetElement(unsigned char *sXmlDoc, int iInXmlDocLen, char *sEleName, unsigned char *sEleValue, int iVaMaxLen, int *iOutValueRealLen)
{
	char szStart[XML_NAME_MAX+4], szEnd[XML_NAME_MAX+5];
	int  iStartLen,  iFindLocation[2];
	int  iStartLoca, iEndLoca, iXmlDocLen;
	
	iXmlDocLen = iInXmlDocLen;	
	if (sXmlDoc[0]==0) 
	{
		return XML_NONE_DOCUMENT;
	}
	
	XmlStrlwr(sEleName);
	if (strcmp(sEleName, "root")==0)
	{
		return XML_ROOT_ELEMENT;
	}
	
	if(strlen(sEleName) > XML_NAME_MAX)
	{
		return XML_ELEMENT_NAME_MAX_LEN;
	}
	
	if( XmlFind(sXmlDoc, iXmlDocLen, "</root>", 7, iFindLocation) )
	{
		return XML_ROOT_END_TAG;
	}
	
	iXmlDocLen = iFindLocation[0]+7;
	if (iXmlDocLen > XML_INFO_MAX-1)
	{
		return XML_INFO_MAX;
	}
	
	strcpy(szStart, "<");
	strcat(szStart, sEleName);
	strcat(szStart, ">");
	iStartLen = strlen(szStart);	
	if (XmlFind(sXmlDoc, iXmlDocLen, szStart, iStartLen, iFindLocation))
	{
		return XML_DOCUMENT_MAX_LEN;
	}
	else if (iFindLocation[0] > iXmlDocLen)
	{
		return XML_DOCUMENT_MAX_LEN;
	}
	
	iStartLoca=iFindLocation[0];
	strcpy(szEnd, "</");
	strcat(szEnd, sEleName);
	strcat(szEnd, ">");
	iStartLen = strlen(szStart);
	if (XmlFind(sXmlDoc, iXmlDocLen, szEnd, strlen(szEnd), iFindLocation))
	{
		return XML_NONE_END_TAG;
	}
	else if (iFindLocation[0] > iXmlDocLen)
	{
		return XML_NONE_END_TAG;
	}
	
	iEndLoca=iFindLocation[0];	
	*iOutValueRealLen = iEndLoca - iStartLoca - strlen(szStart);
	if( *iOutValueRealLen <0 )
	{
		return XML_NONE_ELEMENT_VALUE;
	}
	
	if( *iOutValueRealLen > iVaMaxLen)
	{
		return XML_ELEMENT_VALUE_MAX_LEN;
	}
	
	memcpy(sEleValue, sXmlDoc+iStartLoca+iStartLen, *iOutValueRealLen);
	sEleValue[*iOutValueRealLen] = 0;
	
	return 0;
}

void XmlStrlwr(char *sTurned)
{
	int i = 0;
	while(sTurned[i] != 0)
	{
		if( (sTurned[i]>='A') && (sTurned[i]<='Z') )
		{
			sTurned[i]+=32;
		}
		i+=1;		
	}
}

int XmlFind(unsigned char *sDoc, int iDocLen, char *iFindFlag, int iFindFlagLen, int *iFindLocation)
{
	int  iFlag, i;
	char sTemp[200];
	
	iFlag = 0;
	for(i=0; (i<iDocLen-iFindFlagLen+1)&&(iFlag==0); i++)
	{
		memcpy(sTemp, sDoc+i, iFindFlagLen);	
		sTemp[iFindFlagLen] = 0;
		XmlStrlwr(sTemp);  
		if (memcmp(sTemp, iFindFlag, iFindFlagLen) == 0)
		{
	        iFlag = 1;
		}
	}
	
	if(iFlag)
	{
		*iFindLocation = i-1;
		return 0;
	}
	else
	{
		return 1;
	}
}


