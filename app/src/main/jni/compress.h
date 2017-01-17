#ifndef PAX_MPOS_PROTIMS_COMPRESS_H_
#define PAX_MPOS_PROTIMS_COMPRESS_H_

#ifndef NULL
#ifdef __cplusplus
	#define NULL    0
#else
	#define NULL    ((void *)0)
#endif // __cplusplus
#endif // NULL

enum COMPRESS_ERR_CODE{
	COMPRESS_DECOMPRESS_ERROR = 1601
};

#ifdef __cplusplus
extern "C"{
#endif

int Decompress(unsigned char *srcbuf, unsigned char *dstbuf, unsigned long srclen, unsigned long *dstlen);

#ifdef __cplusplus
}
#endif

#endif // PAX_MPOS_PROTIMS_COMPRESS_H_