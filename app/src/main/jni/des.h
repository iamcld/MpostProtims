#ifndef PAX_MPOS_PROTIMS_DES_H_
#define PAX_MPOS_PROTIMS_DES_H_

#define MASK_DES_KEYLEN		0xF0
#define MASK_DES_MODE		0x0F
#define DES_ENCRYPT			1	// 8 bytes key
#define DES_DECRYPT			2
#define ENCRYPT				DES_ENCRYPT
#define DECRYPT				DES_DECRYPT
#define TRI_ENCCLS			3	// 16 bytes key
#define TRI_DECCLS			4
#define TRI_ENCSTD			5	// 24 bytes key
#define TRI_DECSTD			6

#ifdef __cplusplus
extern "C"{
#endif

void vDes(int iMode, unsigned char *psSource, unsigned char *psKey, unsigned char *psResult);

#ifdef __cplusplus
}
#endif

#endif // PAX_MPOS_PROTIMS_DES_H_ 
