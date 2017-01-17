LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := protocol
LOCAL_LDLIBS    := -lm -llog 
LOCAL_SRC_FILES := com_mpos_sdk_MposSDK.c compress.c des.c file.c mpos.c protims.c util.c xml.c

include $(BUILD_SHARED_LIBRARY)