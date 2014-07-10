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
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.example.performancetest.TestRunner.TimeableCommand;
import com.example.performancetest.util.WaveFileUtils;

public class MainActivity extends Activity {
	// Adjusting the block process sample length will greatly affect the Renderscript performance, while it has much 
	// less of an impact on the other methods. In real-world usage, you might use a small block size of 1024 if you're
	// visualizing the results, or a larger buffer of 65536 might be appropriate if the data is only being streamed to
	// a file.
	private static final int SAMPLES_PER_BLOCK = 65536;
	private static final int ITERATIONS_PER_TEST = 10;	
	private static final String DEST_FOLDER_NAME = "PerformanceTest";
	
	private final Executor executor = Executors.newSingleThreadExecutor();	
	private final ResultPrinter resultPrinter = new ResultPrinter() {		
		public void print(final String text) {			
			results.post(new Runnable() {					
				@Override
				public void run() {
					results.setText(results.getText() + text);						
				}
			});							
		}
	};
	private final MtpScanner scanner = new MtpScanner() {				
		public void scan(File file) {
			// So we can copy it to PC.
			MainActivity.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));				
		}
	};
	
	private TextView results;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		final Button beginTests = (Button) findViewById(R.id.beginTests);
		results = (TextView) findViewById(R.id.resultsView);
		
		beginTests.setOnClickListener(new OnClickListener() {						
			public void onClick(View v) {
				results.setText("Starting...");				
				executor.execute(new Runnable() {					
					public void run() {
					    runTests();
					}
				});
			}
		});
	}
	
    public void runTests() {    	
        resultPrinter.print("Reading in wave data...\n");
        final short[] inAudioData;
		try {
			inAudioData = WaveFileUtils.readWaveFile44100_16Bit_Mono(getAssets().open("test_sample_44100.wav"));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}        
        new File(Environment.getExternalStorageDirectory(), DEST_FOLDER_NAME).mkdir();
        
        resultPrinter.print("Beginning tests...\n");
        final TestRunner testRunner 
			= new TestRunner(inAudioData, SAMPLES_PER_BLOCK, ITERATIONS_PER_TEST, resultPrinter, scanner);
        
        CommonTests.timeJava(testRunner, getFileForName("test_sample_44100_processed_java.wav"));
		CommonTests.timeJavaTuned(testRunner, getFileForName("test_sample_44100_processed_java_tuned.wav"));
		CommonTests.timeC(testRunner, getFileForName("test_sample_44100_processed_c.wav"));
		
		testRunner.timeAndPrintResults("Renderscript", getFileForName("test_sample_44100_processed_rs.wav"), 
				new TimeableCommand() {	
			final RenderScript rs = RenderScript.create(MainActivity.this);
			final Allocation inAllocation = Allocation.createSized(rs, Element.I16(rs), SAMPLES_PER_BLOCK);
	        final Allocation outAllocation = Allocation.createSized(rs, Element.I16(rs), SAMPLES_PER_BLOCK);
	    	final ScriptC_LowPassFilter filterScript = new ScriptC_LowPassFilter(rs);
	    	
			public void processBlock(short[] inBlock, short[] outBlock, int length) {
				inAllocation.copyFrom(inBlock);
				DspRenderscript.applyJustLowpass(inAllocation, outAllocation, length, filterScript);
				outAllocation.copyTo(outBlock);
			}
		});		
        
		resultPrinter.print("Done.\n");       
    }

	private File getFileForName(String name) {
		return new File(Environment.getExternalStorageDirectory() + "/" + DEST_FOLDER_NAME, name);
	}
}
