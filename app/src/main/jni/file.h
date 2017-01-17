#ifndef PAX_MPOS_PROTIMS_FILE_H_
#define PAX_MPOS_PROTIMS_FILE_H_

enum FILE_ERR_CODE{
	FILE_NOEXIST = 701,
	FILE_EXIST,
	FILE_TELL_FAILED_,
	FILE_SEEK_FAILED_,
	FILE_OPEN_FAILED_,
	FILE_READ_FAILED_,
	FILE_WRITE_FAILED_,
	FILE_NOENOUGH_SPACE,
	FILE_REMOVE_FAILED_,
	FILE_INVALID_NAME,
	FILE_MAX_FILESIZE_LIMIT
};

#ifdef __cplusplus
extern "C"{
#endif

int WriteRecvData(FILE *fp, unsigned char *sBuff, int iLen, int *piDestLen);
int GetFileSizeEX(unsigned char *szFileName);


#ifdef __cplusplus
}
#endif

#endif // PAX_MPOS_PROTIMS_FILE_H_
