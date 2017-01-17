#ifndef PAX_MPOS_PROTIMS_XML_H_
#define PAX_MPOS_PROTIMS_XML_H_

enum {
	XML_NONE_DOCUMENT = 1401,
	XML_ROOT_ELEMENT,
	XML_ELEMENT_NAME_MAX_LEN,
	XML_ROOT_END_TAG,
	XML_NONE_END_TAG,
	XML_INFO_MAX,
	XML_DOCUMENT_MAX_LEN,
	XML_NONE_ELEMENT_VALUE,
	XML_ELEMENT_VALUE_MAX_LEN
};

#ifdef __cplusplus
extern "C"{
#endif

int XmlGetElement(unsigned char *sXmlDoc, int iInXmlDocLen, char *sEleName, unsigned char *sEleValue, int iVaMaxLen, int *iOutValueRealLen);

#ifdef __cplusplus
}
#endif

#endif // PAX_MPOS_PROTIMS_XML_H_