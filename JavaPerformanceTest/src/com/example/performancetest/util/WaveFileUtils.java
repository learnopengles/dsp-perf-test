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
package com.example.performancetest.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class WaveFileUtils {	
	public static short[] readWaveFile44100_16Bit_Mono(String dir, String name) {		
		try {
			return readWaveFile44100_16Bit_Mono(new BufferedInputStream(new FileInputStream(new File(dir, name))));
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
			return null;
		}		
	}
	
	public static short[] readWaveFile44100_16Bit_Mono(InputStream is) {		
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();				
		final byte[] buffer = new byte[4096];
		
		try {						
			try {				
				is.skip(44);	// Skip over the header.
				
				int read;
				while ((read = is.read(buffer)) > 0) {
					baos.write(buffer, 0, read);
				}
			} finally {				
				is.close();				
			}		
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		final short[] audioData = new short[baos.size() / 2];
		final ByteBuffer byteBuffer = ByteBuffer.wrap(baos.toByteArray()).order(ByteOrder.LITTLE_ENDIAN);
		byteBuffer.asShortBuffer().get(audioData);
		return audioData;
	}
	
	public static void writeWaveFile44100_16Bit_Mono(short[] data, File file) {
		final ByteBuffer buffer = ByteBuffer
			.allocate(data.length * 2)
			.order(ByteOrder.LITTLE_ENDIAN);
		
		buffer.asShortBuffer().put(data).position(0);		
		
		try {			
			final FileOutputStream os = new FileOutputStream(file);
			final ByteBuffer converter = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
			
			// The header
			os.write((byte) 'R');
			os.write((byte) 'I');
			os.write((byte) 'F');
			os.write((byte) 'F');
			
			// The chunk size
			final int chunkSize = 44 + (data.length * 2) - 8;	
			converter.limit(4);
			converter.putInt(chunkSize).position(0);
			os.write(converter.array());
					
			// The format
			os.write((byte) 'W');
			os.write((byte) 'A');
			os.write((byte) 'V');
			os.write((byte) 'E');
			
			// The "fmt" subchunk for the wave format.
			os.write((byte) 'f');
			os.write((byte) 'm');
			os.write((byte) 't');
			os.write((byte) ' ');
			
			// 16" is the size of the rest of the subchunk.
			converter.limit(4);
			converter.putInt(16).position(0);
			os.write(converter.array());
			
			// "1" indicates PCM.
			converter.limit(2);
			converter.putShort((short)1).position(0);
			os.write(converter.array(), 0, 2);			
			
			// # of channels.
			converter.limit(2);
			converter.putShort((short)1).position(0);
			os.write(converter.array(), 0, 2);

			// Sample rate.
			converter.limit(4);
			converter.putInt(44100).position(0);
			os.write(converter.array());
			
			// Byte rate
			converter.limit(4);
			converter.putInt(88200).position(0);
			os.write(converter.array());
			
			// Block align
			converter.limit(2);
			converter.putShort((short)2).position(0);
			os.write(converter.array(), 0, 2);			

			// Bits per sample
			converter.limit(2);
			converter.putShort((short)16).position(0);
			os.write(converter.array(), 0, 2);

			// Data subchunk
			os.write((byte) 'd');
			os.write((byte) 'a');
			os.write((byte) 't');
			os.write((byte) 'a');

			// Data subchunk size.
			converter.limit(4);
			converter.putInt(data.length * 2).position(0);
			os.write(converter.array());
			
			// Data
			try {
				os.write(buffer.array());
			} finally {
				os.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
}
