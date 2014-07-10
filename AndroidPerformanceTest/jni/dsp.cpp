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
#include "dsp.h"
#include <algorithm>
#include <cstdint>
#include <limits>

static constexpr int int16_min = std::numeric_limits<int16_t>::min();
static constexpr int int16_max = std::numeric_limits<int16_t>::max();

// Note: For the C code, integer & double clamping seem to operate at the same speed. Using floats slows things down somewhat.
static inline int16_t clamp(int input)
{
     return std::max(int16_min, std::min(int16_max, input));
}

static inline int get_offset(const FilterState& filter_state, int relative_offset)
{
     static_assert(!(FilterState::size & (FilterState::size - 1)), "size must be a power of two.");
     return (filter_state.current + relative_offset) % filter_state.size;
}

static inline void push_sample(FilterState& filter_state, int16_t sample)
{
     filter_state.input[get_offset(filter_state, 0)] = sample;
     ++filter_state.current;
}

static inline int16_t get_output_sample(const FilterState& filter_state)
{
     return clamp(filter_state.output[get_offset(filter_state, 0)]);
}

// This is an implementation of a Chebyshev lowpass filter at 5000hz with ripple -0.50dB,
// 10th order, and for an input sample rate of 44100hz.
static inline void apply_lowpass(FilterState& filter_state)
{
     static_assert(FilterState::size >= 10, "FilterState::size must be at least 10.");

     double* x = filter_state.input;
     double* y = filter_state.output;

     y[get_offset(filter_state, 0)] =
       (  1.0 * (1.0 / 6.928330802e+06) * (x[get_offset(filter_state, -10)] + x[get_offset(filter_state,  -0)]))
     + ( 10.0 * (1.0 / 6.928330802e+06) * (x[get_offset(filter_state,  -9)] + x[get_offset(filter_state,  -1)]))
     + ( 45.0 * (1.0 / 6.928330802e+06) * (x[get_offset(filter_state,  -8)] + x[get_offset(filter_state,  -2)]))
     + (120.0 * (1.0 / 6.928330802e+06) * (x[get_offset(filter_state,  -7)] + x[get_offset(filter_state,  -3)]))
     + (210.0 * (1.0 / 6.928330802e+06) * (x[get_offset(filter_state,  -6)] + x[get_offset(filter_state,  -4)]))
     + (252.0 * (1.0 / 6.928330802e+06) *  x[get_offset(filter_state,  -5)])

     + (  -0.4441854896 * y[get_offset(filter_state, -10)])
     + (   4.2144719035 * y[get_offset(filter_state,  -9)])
     + ( -18.5365677633 * y[get_offset(filter_state,  -8)])
     + (  49.7394321983 * y[get_offset(filter_state,  -7)])
     + ( -90.1491003509 * y[get_offset(filter_state,  -6)])
     + ( 115.3235358151 * y[get_offset(filter_state,  -5)])
     + (-105.4969191433 * y[get_offset(filter_state,  -4)])
     + (  68.1964705422 * y[get_offset(filter_state,  -3)])
     + ( -29.8484881821 * y[get_offset(filter_state,  -2)])
     + (   8.0012026712 * y[get_offset(filter_state,  -1)]);
}

void apply_lowpass(FilterState& filter_state, const int16_t* input, int16_t* output, int length)
{
     for (int i = 0; i < length; ++i) {
          push_sample(filter_state, input[i]);
          apply_lowpass(filter_state);
          output[i] = get_output_sample(filter_state);
     }
}
