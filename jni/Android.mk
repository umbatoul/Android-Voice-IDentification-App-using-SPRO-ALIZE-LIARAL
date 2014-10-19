# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# the purpose of this sample is to demonstrate how one can
# generate two distinct shared libraries and have them both
# uploaded in
#

LOCAL_PATH:= $(call my-dir)

ALIZE:=ALIZE_3.0

EIGEN:=Eigen
SPKDET:=LIA_SpkDet
SPKTOOLS:=LIA_SpkTools

#common_CFLAGS := -Iinctest
# first lib, which will be built statically
#
include $(CLEAR_VARS)

LOCAL_CPP_FEATURES += exceptions
#not sure if related through isnan() 
LOCAL_CPPFLAGS += -D_GLIBCXX_USE_C99_MATH=1
#LOCAL_MODULE    := libSPKVER

LOCAL_LDLIBS += -llog 

FILE_LIST := $(wildcard $(LOCAL_PATH)/$(ALIZE)/src/*.cpp) \
              $(wildcard $(LOCAL_PATH)/$(SPKDET)/ComputeNorm/src/*.cpp) \
              $(wildcard $(LOCAL_PATH)/$(SPKDET)/ComputeTest/src/*.cpp) \
              $(wildcard $(LOCAL_PATH)/$(SPKDET)/TrainWorld/src/*.cpp) \
              $(wildcard $(LOCAL_PATH)/$(SPKDET)/TrainTarget/src/*.cpp) \
              $(wildcard $(LOCAL_PATH)/$(SPKDET)/NormFeat/src/*.cpp) \
              $(wildcard $(LOCAL_PATH)/$(SPKDET)/EnergyDetector/src/*.cpp) \
              $(wildcard $(LOCAL_PATH)/$(SPKTOOLS)/src/*.cpp) 


LOCAL_SRC_FILES := $(FILE_LIST:$(LOCAL_PATH)/%=%)
LOCAL_C_INCLUDES += $(LOCAL_PATH)/$(ALIZE)/include
LOCAL_C_INCLUDES += $(LOCAL_PATH)/$(SPKTOOLS)/include
LOCAL_C_INCLUDES +=$(LOCAL_PATH)/$(EIGEN)
LOCAL_C_INCLUDES +=$(LOCAL_PATH)/$(SPKDET)/ComputeNorm/include
LOCAL_C_INCLUDES +=$(LOCAL_PATH)/$(SPKDET)/ComputeTest/include
LOCAL_C_INCLUDES +=$(LOCAL_PATH)/$(SPKDET)/TrainWorld/include
LOCAL_C_INCLUDES +=$(LOCAL_PATH)/$(SPKDET)/TrainTarget/include
LOCAL_C_INCLUDES +=$(LOCAL_PATH)/$(SPKDET)/NormFeat/include
LOCAL_C_INCLUDES +=$(LOCAL_PATH)/$(SPKDET)/EnergyDetector/include

#include $(BUILD_STATIC_LIBRARY)

# second lib, which will depend on and include the first one
#
#include $(CLEAR_VARS)

LOCAL_MODULE    := libvoicerecognize


#LOCAL_STATIC_LIBRARIES := libSPKVER

include $(BUILD_SHARED_LIBRARY)


# start spro lib

include $(CLEAR_VARS)
LOCAL_LDLIBS += -llog
SPRO=spro-4.0
FILE_LIST := $(wildcard $(LOCAL_PATH)/$(SPRO)/*.c) 
LOCAL_CFLAGS+= -DHAVE_CONFIG_H
LOCAL_SRC_FILES := $(FILE_LIST:$(LOCAL_PATH)/%=%)

LOCAL_C_INCLUDES += $(LOCAL_PATH)/$(SPRO)
LOCAL_MODULE    := libspro_4_0
include $(BUILD_SHARED_LIBRARY)
