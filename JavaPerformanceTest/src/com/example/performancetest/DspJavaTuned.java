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

// This is a manually-inlined version of DspJava
public class DspJavaTuned {
	public static final void applyLowpass(FilterState filterState, short[] input, short[] output, int length) {				
		for (int i = 0; i < length; ++i) {
			filterState.input[(filterState.current + 0) & (FilterState.size - 1)] = input[i];
			++filterState.current;
			final double[] x = filterState.input;
			final double[] y = filterState.output;
			
			y[(filterState.current + 0) & (FilterState.size - 1)] = 
			   (  1.0 * (1.0 / 6.928330802e+06) * (x[(filterState.current + -10) & (FilterState.size - 1)] + x[(filterState.current + -0) & (FilterState.size - 1)]))
			 + ( 10.0 * (1.0 / 6.928330802e+06) * (x[(filterState.current + -9) & (FilterState.size - 1)] + x[(filterState.current + -1) & (FilterState.size - 1)]))
			 + ( 45.0 * (1.0 / 6.928330802e+06) * (x[(filterState.current + -8) & (FilterState.size - 1)] + x[(filterState.current + -2) & (FilterState.size - 1)]))
			 + (120.0 * (1.0 / 6.928330802e+06) * (x[(filterState.current + -7) & (FilterState.size - 1)] + x[(filterState.current + -3) & (FilterState.size - 1)]))
			 + (210.0 * (1.0 / 6.928330802e+06) * (x[(filterState.current + -6) & (FilterState.size - 1)] + x[(filterState.current + -4) & (FilterState.size - 1)]))
			 + (252.0 * (1.0 / 6.928330802e+06) *  x[(filterState.current + -5) & (FilterState.size - 1)])
			
			 + (  -0.4441854896 * y[(filterState.current + -10) & (FilterState.size - 1)])
			 + (   4.2144719035 * y[(filterState.current + -9) & (FilterState.size - 1)])
			 + ( -18.5365677633 * y[(filterState.current + -8) & (FilterState.size - 1)])
			 + (  49.7394321983 * y[(filterState.current + -7) & (FilterState.size - 1)])
			 + ( -90.1491003509 * y[(filterState.current + -6) & (FilterState.size - 1)])
			 + ( 115.3235358151 * y[(filterState.current + -5) & (FilterState.size - 1)])
			 + (-105.4969191433 * y[(filterState.current + -4) & (FilterState.size - 1)])
			 + (  68.1964705422 * y[(filterState.current + -3) & (FilterState.size - 1)])
			 + ( -29.8484881821 * y[(filterState.current + -2) & (FilterState.size - 1)])
			 + (   8.0012026712 * y[(filterState.current + -1) & (FilterState.size - 1)]);
			output[i] = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, (int)filterState.output[(filterState.current + 0) & (FilterState.size - 1)]));			
		}
	}	
}
