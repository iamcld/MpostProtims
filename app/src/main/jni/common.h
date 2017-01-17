#ifndef PAX_MPOS_PROTIMS_COMMMON_H_
#define PAX_MPOS_PROTIMS_COMMMON_H_

#include "platform.h"
#if !defined(WIN32) && !defined(WINDOWS)
#include <jni.h>
#include "android/log.h"
#endif // WINDOWS

#ifndef NULL
#ifdef __cplusplus
	#define NULL    0
#else
	#define NULL    ((void *)0)
#endif // __cplusplus
#endif // NULL

static const char *TAG = "C-TAG";
#if !defined(WIN32) && !defined(WINDOWS) && defined(DEBUG)
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt,##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt,##args)
#define LOGW(fmt, args...) __android_log_print(ANDROID_LOG_WARN,  TAG, fmt,##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt,##args)
#else
#define LOGI(fmt, args...)
#define LOGD(fmt, args...)
#define LOGW(fmt, args...)
#define LOGE(fmt, args...)
#endif // WINDOWS

// communication buffer size
#define PACKAGE_LENGTH	 	8192
#define SEND_BUFFER_SIZE	PACKAGE_LENGTH
#define RECV_BUFFER_SIZE	PACKAGE_LENGTH
#define FILE_CONTENT_SIZE	1024


// charset
#define CHARSET_WEST				0x01      // USA/UK/Western European       
#define CHARSET_TAI					0x02      // Thailand                        
#define CHARSET_MID_EUROPE			0x03      // Central Europe                           
#define CHARSET_VIETNAM				0x04      // Vietnam                           
#define CHARSET_GREEK				0x05      // Greece                          
#define CHARSET_BALTIC				0x06      // Baltic Sea                      
#define CHARSET_TURKEY				0x07      // Turkey                        
#define CHARSET_HEBREW				0x08      // Hebrew                          
#define CHARSET_RUSSIAN				0x09      // Russia                         
#define CHARSET_GB2312				0x0A      // simplified Chinese 2312      
#define CHARSET_GBK					0x0B      // simplified Chinese GBK      
#define CHARSET_GB18030				0x0C      // simplified Chinese GB18030      
#define CHARSET_BIG5				0x0D      // Chinese Traditional         
#define CHARSET_SHIFT_JIS			0x0E      // Japan                         
#define CHARSET_KOREAN				0x0F      // korea                          
#define CHARSET_ARABIA				0x10      // Arab                         
#define CHARSET_DIY					0x11      // custom

// retry times
#define PROTIMS_GETTASK_RETRY_TIMES 3

// download flag
#define PROTIMS_NONE_STATUS						0
#define PROTIMS_DOWNLOADING_STATUS				1
#define PROTIMS_DOWNLOADED_STATUS				2
#define PROTIMS_UPDATED_STATUS					3

// task max num
#define PROTIMS_MAX_TASK            32	 	  // max task number limit

// download type
#define PROTIMS_DOWNLOAD_MONITOR         0x00    // monitor
#define PROTIMS_CREATE_FILE_SYSTEM       0x01    // create file system
#define PROTIMS_DOWNLOAD_FONT            0x02    // font
#define PROTIMS_DELETE_APPLICATION       0x03    // delete application
#define PROTIMS_DOWNLOAD_APPLICATION     0x04    // application
#define PROTIMS_DOWNLOAD_PARA_FILE       0x05    // parameter file 
#define PROTIMS_DELETE_PARA_FILE         0x06    // delete parameter file
#define PROTIMS_DELETE_ALL_APPLICATION   0x07    // delete all application(include parameter file and log file)
#define PROTIMS_WNET_FIRMWARE_UPDATE     0x09    // update wifi module 
#define PROTIMS_DOWNLOAD_DLL             0x0A	 // so
#define PROTIMS_DELLETE_DLL              0x0B    // delete so
#define PROTIMS_DOWNLOAD_USPUK			 0x10	 // USPUK
#define PROTIMS_DOWNLOAD_UAPUK			 0x11	 // UAPUK
#define PROTIMS_DOWNLOAD_PUBFILE		 0x12	 // public file
#define PROTIMS_DELETE_PUBFILE			 0x13	 // delete public file

#define LEN_TERMINAL_ID					8		 // length of terminal id
#define LEN_TERMINAL_SN_CUR				8		 // length of terminal SN for current use	
#define LEN_TERMINAL_SN_EXT				24		 // length of terminal SN for extend use	
#define LEN_TERMINAL_VERSION			8		 // length of terminal version information	
#define LEN_TERMINAL_MONITOR_VER		9		 // length of terminal monitor version 
#define LEN_TERMINAL_RANDOM				8		 // length of terminal or host random number
#define LEN_TERMINAL_INFORMATION		30		 // length of terminal information
#define LEN_TERMINAL_MAC_ADDR			7		 // length of terminal MAC address
#define LEN_PATH						128		 // length of file path
#define LEN_FILE_NAME					33		 // length of file name

typedef enum 
{
	COMM_SERIAL = 0,
	COMM_MODEM,	
	COMM_TCPIP,	
	COMM_GPRS,	
	COMM_CDMA,
	COMM_PPP,
	COMM_WIFI,
	COMM_WCDMA,
	COMM_UNKNOWN
}COMM_MODE;

typedef struct{
	int CharSet;    // charset
	int Width;      // the width of font
	int Height;     // the height of font
	int Bold;       // 0-normal, 1-bold
	int Italic;     // 0-normal, 1-italic
}ST_FONT;

typedef struct
{
	unsigned long AllSize;			// current task size
	unsigned char TaskNo;			// task no.
	unsigned char TaskType;			// task type
	unsigned char FileName[33];		// file name
	unsigned char AppName[33];		// application name
	unsigned char vern[21];			// version
	unsigned char ForceUpdate;		// flag for force update
	unsigned char Rev[5];			// just use for byte alignment
	unsigned char done;				// status flag: 0-not downloaded, 1-downloading, 2-finish dowbload, 3-finish update
}TASK_TABLE;

typedef struct
{
	int PosType;														// terminal POS type
	unsigned char TermID[LEN_TERMINAL_ID + 1];							// terminal id/download task id
	unsigned char SN[LEN_TERMINAL_SN_CUR + LEN_TERMINAL_SN_EXT + 1];	// terminal serial number
	unsigned char ExSN[LEN_TERMINAL_SN_CUR + LEN_TERMINAL_SN_EXT + 1];	// terminal extend serial number
	unsigned char VerInfo[LEN_TERMINAL_VERSION + 1];					// terminal version information
	unsigned char MonVer[LEN_TERMINAL_MONITOR_VER + 1];					// terminal monitor version 
	unsigned char TermInfo[LEN_TERMINAL_INFORMATION];					// terminal information
	unsigned char MacAddr[LEN_TERMINAL_MAC_ADDR];						// terminal MAC address 
}ST_TERMINAL;

typedef struct
{
	int TaskNum;
	int CurTask;
	int UploadStatus;													// upload file status
	COMM_MODE CommModeTms;												// TMS communication mode
	COMM_MODE CommModeMpos;												// MPOS communication mode
	unsigned char HostRandom[LEN_TERMINAL_RANDOM + 1];					// host random number
	unsigned char TermRandom[LEN_TERMINAL_RANDOM + 1];					// terminal random number
	unsigned char HostRandomEn[LEN_TERMINAL_RANDOM + 1];				// host random number(encrypted)
	unsigned char TermRandomEn[LEN_TERMINAL_RANDOM + 1];				// terminal random number(encrypted)
	unsigned char PathName[LEN_PATH];
	unsigned char FileName[LEN_PATH];
	unsigned char CallMode;												// call mode, 0-monitor call
	unsigned char FirstLoadFile;										// flag for if first download file
	unsigned char AuthResult;
	unsigned long AllTaskSize;
	unsigned long DownloadSize;
	unsigned long CurFileSize;
}ST_CONTROL;

#endif // PAX_MPOS_PROTIMS_COMMMON_H_
