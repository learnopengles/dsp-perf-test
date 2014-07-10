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
#pragma version(1)
#pragma rs java_package_name(com.example.performancetest)
#pragma rs_fp_relaxed

static const int size = 16;

// Renderscript conversion notes:
//
// - This code is essentially the same as the C++ code found in dsp.cpp except
//   for a few changes in apply_lowpass().
//
// - The performance gain compared to C++ comes from the on-device compilation.
//   This allows optimizations that are not possible when compiling C++ ahead of
//   time, as some portability must then be ensured.

// Renderscript conversion note:  In Renderscript, static variables are local
// to the execution of one script, so we don't need to create a struct to hold
// them.  If you run two scripts in parallel, they won't collide.  While I
// removed "FilterState" to simplify the code, having that struct would not
// reduce performance.

// Must use doubles for these arrays, otherwise can be noisy.
static double input[size];
static double output[size];
static unsigned int current = 0;

static const double int16_min_d = -32768.;
static const double int16_max_d = 32767.;

// Renderscript conversion note:  Renderscript initializes globals to 0.
// We could also use an init routine to do initialization, as done here as an
// example.  init() will be called once when the script is first loaded.
void init() {
    for (int i = 0; i < size; ++i) {
        input[i] = 0.0;
        output[i] = 0.0;
    }
    current = 0;
}

static int get_offset(int relative_offset) {
    return (current + relative_offset) % size;
}

static void push_sample(short sample) {
    input[get_offset(0)] = sample;
    current++;
}

static short get_output_sample() {
    // Renderscript conversion note: Renderscript has a clamp function so we
    // just use that one.
    return (short) clamp(output[get_offset(0)], int16_min_d, int16_max_d);
    // If targeting API 18 or lower, need to cast.
    // return (short) clamp(output[get_offset(0)], int16_min_d, int16_max_d);
}

// This is an implementation of a Chebyshev lowpass filter at 5000hz with ripple -0.50dB,
// 10th order, and for an input sample rate of 44100hz.

static void apply_onepass() {
    double* x = input;
    double* y = output;

    y[get_offset(0)] =
      (  1.0 * (1.0 / 6.928330802e+06) * (x[get_offset(-10)] + x[get_offset( -0)]))
    + ( 10.0 * (1.0 / 6.928330802e+06) * (x[get_offset( -9)] + x[get_offset( -1)]))
    + ( 45.0 * (1.0 / 6.928330802e+06) * (x[get_offset( -8)] + x[get_offset( -2)]))
    + (120.0 * (1.0 / 6.928330802e+06) * (x[get_offset( -7)] + x[get_offset( -3)]))
    + (210.0 * (1.0 / 6.928330802e+06) * (x[get_offset( -6)] + x[get_offset( -4)]))
    + (252.0 * (1.0 / 6.928330802e+06) *  x[get_offset( -5)])

    + (  -0.4441854896 * y[get_offset(-10)])
    + (   4.2144719035 * y[get_offset( -9)])
    + ( -18.5365677633 * y[get_offset( -8)])
    + (  49.7394321983 * y[get_offset( -7)])
    + ( -90.1491003509 * y[get_offset( -6)])
    + ( 115.3235358151 * y[get_offset( -5)])
    + (-105.4969191433 * y[get_offset( -4)])
    + (  68.1964705422 * y[get_offset( -3)])
    + ( -29.8484881821 * y[get_offset( -2)])
    + (   8.0012026712 * y[get_offset( -1)]);
}

// Renderscript conversion note: Because the algorithm maintains an ongoing
// state from one data item to the next, we can't use a Renderscript kernel.
// If we did, Renderscript would spawn multiple threads to process them in
// parallel, lacking that context.  As is done in C and Java, we do our own
// looping.
void apply_lowpass(rs_allocation in, rs_allocation out, int length) {
    for (uint32_t i = 0; i < length; i++) {
        push_sample(rsGetElementAt_short(in, i));
        apply_onepass();  
        rsSetElementAt_short(out, get_output_sample(), i);
    }
}
