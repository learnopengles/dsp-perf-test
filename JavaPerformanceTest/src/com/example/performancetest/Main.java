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

import com.example.performancetest.util.WaveFileUtils;

public class Main {			
	private static final int SAMPLES_PER_BLOCK = 65536;
	private static final int ITERATIONS_PER_TEST = 50;
	private static final ResultPrinter RESULT_PRINTER = new ResultPrinter() {		
		@Override
		public void print(String text) {
			System.out.println(text);			
		}
	};
	private static final MtpScanner SCANNER = new MtpScanner() {		
		@Override
		public void scan(File file) {
			// No-op			
		}
	};
	
	private static void runTests() {
		RESULT_PRINTER.print("Reading in wave data...\n");
		// This is hard-coded to the test project structure.
		final short[] inAudioData = WaveFileUtils.readWaveFile44100_16Bit_Mono(
        		System.getProperty("user.dir") + "/../AndroidPerformanceTest/assets", "test_sample_44100.wav");
		RESULT_PRINTER.print("Beginning tests...\n");
		final TestRunner testRunner 
			= new TestRunner(inAudioData, SAMPLES_PER_BLOCK, ITERATIONS_PER_TEST, RESULT_PRINTER, SCANNER);
		
		CommonTests.timeJava(testRunner, getFileForName("test_sample_44100_processed_java.wav"));
		CommonTests.timeJavaTuned(testRunner, getFileForName("test_sample_44100_processed_java_tuned.wav"));
		CommonTests.timeC(testRunner, getFileForName("test_sample_44100_processed_c.wav"));
	}
	
	private static File getFileForName(String name) {
		return new File(System.getProperty("user.dir"), name);
	}
		
	// Should be executed from this project's root.
	public static void main(String [] args)
	{
		RESULT_PRINTER.print("Starting...\n");						
	    runTests();			
	}
}
