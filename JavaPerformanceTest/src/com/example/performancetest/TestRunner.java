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
import java.text.NumberFormat;
import java.util.Arrays;

import com.example.performancetest.util.WaveFileUtils;

public class TestRunner {		
	private static final NumberFormat FORMAT = NumberFormat.getIntegerInstance();
	
	private final short[] inAudioData;
	private final short[] outAudioData;
	
	private final int samplesPerBlock;
	private final int iterationsPerTest;
	
	private final ResultPrinter resultPrinter;
	private final MtpScanner scanner;
	
	public TestRunner(
			final short[] inAudioData, int samplesPerBlock, int iterationsPerTest, ResultPrinter resultPrinter,
			MtpScanner scanner) {
		this.inAudioData = inAudioData;
		this.outAudioData = new short[inAudioData.length];
		this.samplesPerBlock = samplesPerBlock;
		this.iterationsPerTest = iterationsPerTest;
		this.resultPrinter = resultPrinter;
		this.scanner = scanner;
	}
	
	public void timeAndPrintResults(String testName, File fileForResult, TimeableCommand command) {
		Result result = timeCommand(command);
		print(testName, result);
		WaveFileUtils.writeWaveFile44100_16Bit_Mono(outAudioData, fileForResult);
		scanner.scan(fileForResult);
	}	
	
	private void print(final String text, final Result result) {
		final String description = text + ": "
								 + "best: " + FORMAT.format(result.bestResult) + ", "
								 + "mean: " + FORMAT.format(result.meanResult) + ", "
								 + "std dev: " + FORMAT.format(result.stdDev);
		
		resultPrinter.print(description + " shorts/second.\n");
	}
			
	private Result timeCommand(final TimeableCommand command) {
		final short[] inBlock = new short[samplesPerBlock];	
		final short[] outBlock = new short[samplesPerBlock];
		final int endSample = inAudioData.length - 1;        
        
        return timeIt(endSample, new Runnable() {
			public void run() {
				int pos = 0;
		        
		        while (pos < endSample) {
		        	final int samplesToProcess = Math.min(endSample - pos, samplesPerBlock);
		        	
		        	System.arraycopy(inAudioData, pos, inBlock, 0, samplesToProcess);		        	
		        	command.processBlock(inBlock, outBlock, samplesToProcess);
		        	System.arraycopy(outBlock, 0, outAudioData, pos, samplesToProcess);
		        			        			        	
		        	pos += samplesToProcess;
		        }				
			}
		});
    }

	private Result timeIt(long samplesPerIteration, Runnable runnable) {
		final long[] results = new long[iterationsPerTest];
		
		for (int i = 0; i < results.length; ++i) {
			final long startTimeNs = System.nanoTime();
			runnable.run();
			final long elapsedTimeNs = System.nanoTime() - startTimeNs;
			final long shortsPerSecond = (long) ((samplesPerIteration) / (elapsedTimeNs / 1000000000.0));
			results[i] = shortsPerSecond;
		}
		
		return new Result(results);
	}
	
	static interface TimeableCommand {
		void processBlock(short[] inBlock, short[] outBlock, int length);
	}
	
	static class Result {
		long bestResult;
		long meanResult;
		long stdDev;
		
		Result(long[] results) {
			bestResult = max(results);
			meanResult = mean(results);
			stdDev = stdDev(meanResult, results);
		}
		
		private static long max(long[] results) {
			long max = 0;
			
			for (long result : results)
				if (result > max)
					max = result;
			
			return max;
		}
		
		private static long mean(long[] results) {
			long sum = 0;
			
			for (long result : results)
				sum += result;
			
			return sum / results.length;
		}
		
		private static long stdDev(long mean, long[] results) {
			double sum = 0;
			
			for (long result : results)
				sum += (result - mean) * (result - mean);
			
			return (long) Math.sqrt(sum / (results.length - 1));
		}
	}
}
