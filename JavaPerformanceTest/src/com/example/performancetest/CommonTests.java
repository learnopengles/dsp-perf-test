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

import java.io.File;

import com.example.performancetest.TestRunner.TimeableCommand;

public class CommonTests {
	public static void timeJava(TestRunner testRunner, File file) {
    	testRunner.timeAndPrintResults("Java", file, new TimeableCommand() {	
    		final FilterState filterState = new FilterState();
    		public void processBlock(short[] inBlock, short[] outBlock, int length) {
    			DspJava.applyLowpass(filterState, inBlock, outBlock, length);				
    		}
    	});
    }
    
    public static void timeJavaTuned(TestRunner testRunner, File file) {
    	testRunner.timeAndPrintResults("Java (tuned)", file, new TimeableCommand() {	
    		final FilterState filterState = new FilterState();
    		public void processBlock(short[] inBlock, short[] outBlock, int length) {
    			DspJavaTuned.applyLowpass(filterState, inBlock, outBlock, length);				
    		}
    	});
    }
    
    public static void timeC(TestRunner testRunner, File file) {
    	testRunner.timeAndPrintResults("C", file, new TimeableCommand() {				
			public void processBlock(short[] inBlock, short[] outBlock, int length) {
				DspC.apply_lowpass(inBlock, outBlock, length);				
			}
    	});
    }
}
