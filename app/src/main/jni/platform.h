#ifndef PAX_MPOS_PROTIMS_PLATFORM_H_
#define PAX_MPOS_PROTIMS_PLATFORM_H_

//#define MULTI_THREAD
//#define WINDOWS
#define DEBUG

#define VERSION 1.00.0001

#ifdef MULTI_THREAD
#ifdef WINDOWS
#ifndef TLS
#define TLS __declspec(thread)
#endif // TLS
#else  // WINDOWS
#define TLS __thread
#endif // WINDOWS
#else  // MULTI_THREAD
#ifndef TLS
#define TLS
#endif // TLS
#endif // MULTI_THREAD

#endif // PAX_MPOS_PROTIMS_PLATFORM_H_
