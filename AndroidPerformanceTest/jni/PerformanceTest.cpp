/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
#include <vector>
#include <jni.h>
#include "dsp.h"

#ifdef __cplusplus
extern "C" {
#endif

static FilterState filter_state{};
static auto in_arr = std::vector<jshort>();
static auto out_arr = std::vector<jshort>();

JNIEXPORT void JNICALL Java_com_example_performancetest_DspC_apply_1lowpass(JNIEnv * env, jclass cls, jshortArray in, jshortArray out, jint length) {
	in_arr.reserve(length);
	out_arr.reserve(length);
	env->GetShortArrayRegion(in, 0, length, in_arr.data());
	apply_lowpass(filter_state, in_arr.data(), out_arr.data(), length);
	env->SetShortArrayRegion(out, 0, length, out_arr.data());
}

#ifdef __cplusplus
}
#endif
