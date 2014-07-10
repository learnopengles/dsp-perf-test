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

public class FilterState {
	static final int size = 16;
	
	static {
		checkSize();
	}
	
	@SuppressWarnings("all")
	static void checkSize() {
		if ((size & (size - 1)) != 0)
			throw new IllegalStateException("Size must be a power of two.");		
	}
	
	final double input[] = new double[size];
	final double output[] = new double[size];
	
	int current;
}