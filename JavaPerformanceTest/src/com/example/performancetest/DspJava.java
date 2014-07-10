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
package com.example.performancetest;

public class DspJava {	
	static final short clamp(int input) {
		return (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, input));
	}
	
	static final int getOffset(FilterState filterState, int relativeOffset) {
		// The two below lines should be equivalent; however, the various Java runtimes don't seem to optimize
		// the below so it's done manually.
		// return ((filterState.current + relativeOffset) % FilterState.size + FilterState.size) % FilterState.size;    
		return (filterState.current + relativeOffset) & (FilterState.size - 1);				
	}	

	static final void pushSample(FilterState filterState, short sample) {
		filterState.input[getOffset(filterState, 0)] = sample;
		++filterState.current;
	}	

	static final short getOutputSample(FilterState filterState) {
		return clamp((int)filterState.output[getOffset(filterState, 0)]);
	}

	// This is an implementation of a Chebyshev lowpass filter at 5000hz with ripple -0.50dB,
	// 10th order, and for an input sample rate of 44100hz.
	// See mkfilter (http://www-users.cs.york.ac.uk/~fisher/mkfilter/) for generating new filters.
	static final void applyLowpass(FilterState filterState) {	     
		final double[] x = filterState.input;
		final double[] y = filterState.output;
		
		y[getOffset(filterState, 0)] =
		   (  1.0 * (1.0 / 6.928330802e+06) * (x[getOffset(filterState, -10)] + x[getOffset(filterState,  -0)]))
		 + ( 10.0 * (1.0 / 6.928330802e+06) * (x[getOffset(filterState,  -9)] + x[getOffset(filterState,  -1)]))
		 + ( 45.0 * (1.0 / 6.928330802e+06) * (x[getOffset(filterState,  -8)] + x[getOffset(filterState,  -2)]))
		 + (120.0 * (1.0 / 6.928330802e+06) * (x[getOffset(filterState,  -7)] + x[getOffset(filterState,  -3)]))
		 + (210.0 * (1.0 / 6.928330802e+06) * (x[getOffset(filterState,  -6)] + x[getOffset(filterState,  -4)]))
		 + (252.0 * (1.0 / 6.928330802e+06) *  x[getOffset(filterState,  -5)])
		
		 + (  -0.4441854896 * y[getOffset(filterState, -10)])
		 + (   4.2144719035 * y[getOffset(filterState,  -9)])
		 + ( -18.5365677633 * y[getOffset(filterState,  -8)])
		 + (  49.7394321983 * y[getOffset(filterState,  -7)])
		 + ( -90.1491003509 * y[getOffset(filterState,  -6)])
		 + ( 115.3235358151 * y[getOffset(filterState,  -5)])
		 + (-105.4969191433 * y[getOffset(filterState,  -4)])
		 + (  68.1964705422 * y[getOffset(filterState,  -3)])
		 + ( -29.8484881821 * y[getOffset(filterState,  -2)])
		 + (   8.0012026712 * y[getOffset(filterState,  -1)]);
	}

	public static final void applyLowpass(FilterState filterState, short[] input, short[] output, int length) {				
		for (int i = 0; i < length; ++i) {
			pushSample(filterState, input[i]);
			applyLowpass(filterState);
			output[i] = getOutputSample(filterState);
		}
	}
}
