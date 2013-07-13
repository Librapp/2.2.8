package com.sumavision.talktv2.utils;

import java.io.File;
import java.io.IOException;

import android.media.MediaRecorder;
import android.util.Log;

import com.sumavision.talktv2.net.JSONMessageType;

/**
 * @author 郭鹏
 * @version 2.0
 * @description 录制声音
 * @createTime 2012-6-7
 * @changeLog
 */
public class AudioFileRecorderUtils {

	final MediaRecorder rec = new MediaRecorder();
	final String path;
	
	public AudioFileRecorderUtils(String path) {
		this.path = setPath(path);
	}
	
	private String setPath(String path){
		
		if(!path.startsWith("/")){
			path = JSONMessageType.AUDIO_SDCARD_FOLDER + path;
		}
		if(!path.contains(".")){
			path += ".mp3";
		}
		return path;
	}
	
	public void openRec() throws IOException{
		
		String state = android.os.Environment.getExternalStorageState();
		
		if(!state.equals(android.os.Environment.MEDIA_MOUNTED)){
			
			throw new IOException("SDCars is not exist, State is  " + state + ".");
		}
		
		File directory = new File(path).getParentFile();
		if(!directory.exists() && !directory.mkdirs()){
			throw new IOException("Path to file could not be created");
		}
		rec.setAudioSource(MediaRecorder.AudioSource.MIC);
//		rec.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
//		rec.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		rec.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
		rec.setAudioEncoder(3);
		Log.e("audio-file-path", path);
		rec.setOutputFile(path);
		rec.prepare();
		rec.start();
	}
	
	public void closeRec() throws IOException{
		
		rec.stop();
		rec.release();
	}
}
