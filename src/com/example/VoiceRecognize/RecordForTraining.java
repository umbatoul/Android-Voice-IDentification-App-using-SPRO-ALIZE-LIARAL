package com.example.VoiceRecognize;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.example.VoiceRecognize.R;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class RecordForTraining extends Activity {
	public static final boolean DEBUGMODE = com.example.VoiceRecognize.VoiceRecognize.DEBUGMODE;
	private static final int RECORDER_BPP = 16;
	private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
	private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
	private static final int RECORDER_SAMPLERATE = 8000;// 16000;
	private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO; //STEREO;
	private static final int MY_RECORDER_CHANNELS = 1;
	private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

	private AudioRecord recorder = null;
	private int bufferSize = 0;
	private int buffCountForDuration;
	private int fixedDurationSeconds  = 30; //TODO: create a preferences entry for user to choose
	private Thread recordingThread = null;
	private boolean isRecording = false;
	private boolean variableDuration = false; //TODO: change to true for deployment or create a preferences entry for user to choose
	private String recfile;


	//public native int Sfbsep(String S1, String S2);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		recfile = this.getIntent().getStringExtra("filename");
		setContentView(R.layout.trainrec);
		TextView ttv = ((TextView)findViewById(R.id.tv_title));
		ttv.setText(this.getIntent().getStringExtra("titleMsg"));
		
		TextView itv = ((TextView)findViewById(R.id.tv_info));
		itv.setText(this.getIntent().getStringExtra("infoMsg"));

		setButtonHandlers();
		enableButtons(false);

 

		bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING);
		buffCountForDuration = fixedDurationSeconds * (RECORDER_BPP * RECORDER_SAMPLERATE *MY_RECORDER_CHANNELS /8) / bufferSize;
	}


	private void setButtonHandlers() {
		((Button)findViewById(R.id.btnStart)).setOnClickListener(btnClick);
		((Button)findViewById(R.id.btnStop)).setOnClickListener(btnClick);
		//((Button)findViewById(R.id.btnSkip)).setOnClickListener(btnClick);
	}

	private void enableButton(int id,boolean isEnable){
		((Button)findViewById(id)).setEnabled(isEnable);
	}

	private void enableButtons(boolean isRecording) {
		enableButton(R.id.btnStart,!isRecording);
		enableButton(R.id.btnStop,isRecording);
		//enableButton(R.id.btnSkip,!isRecording);
	}

	private String getFilename(){
		//String filepath = Environment.getExternalStorageDirectory().getPath();
		//File file = new File(filepath,AUDIO_RECORDER_FOLDER);
		String fPath = Folders.RECORDER.getPath();    
		File file = new File(fPath);//filepath,AUDIO_RECORDER_FOLDER);
		//        File file = new File(com.example.VoiceRecognize.VoiceRecognize.filepath, Folders.RECORDER.toString());
		if(!file.exists()){
			file.mkdirs();
		}
		//String value = com.example.VoiceRecognize.VoiceRecognize.target;//unm.getText().toString();
		String Fnm; 

		Fnm = file.getAbsolutePath() + File.separator + recfile + AUDIO_RECORDER_FILE_EXT_WAV;

		if (DEBUGMODE) AppLog.logString("File name +++++++++++++++++++++: " + Fnm);
		return Fnm;
	}

	private String getTempFilename(){
		String tempPath = Folders.RECORDER.getPath();
		File file = new File(tempPath);
		if(!file.exists()){
			file.mkdirs();
		}
		return (file.getAbsolutePath() + File.separator + AUDIO_RECORDER_TEMP_FILE);
	}

	private static int[] mSampleRates = new int[] { 8000, 11025, 22050, 44100 };
	private AudioRecord findAudioRecord() {
		for (int rate : mSampleRates) {
			for (short audioFormat : new short[] { AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT }) {
				for (short channelConfig : new short[] { AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO }) {
					try {
						if (DEBUGMODE) AppLog.logString("Attempting rate " + rate + "Hz, bits: " + audioFormat + ", channel: "
								+ channelConfig);
						int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);

						if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
							// check if we can instantiate and have a success
							AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, rate, channelConfig, audioFormat, bufferSize);

							if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
								return recorder;
						}
					} catch (Exception e) {
						if (DEBUGMODE) AppLog.logString("" +rate + "Exception, keep trying." + e);
					}
				}
			}
		}
		return null;
	}


	private void startRecording(){
		try
		{
			recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
					RECORDER_SAMPLERATE, RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING, bufferSize);
			if (recorder == null)
				recorder = findAudioRecord();

			recorder.startRecording();

			isRecording = true;

			recordingThread = new Thread(new Runnable() {                        
				@Override
				public void run() {     	
					writeAudioDataToFile();
				}
			}, "AudioRecorder Thread");

			recordingThread.start();
		}
		catch(Exception e) {
			if (DEBUGMODE) AppLog.logString("Audio Recording problem");
			e.printStackTrace();
		}
	}

	private void writeAudioDataToFile(){
		byte data[] = new byte[bufferSize];
		String filename = getTempFilename();
		FileOutputStream os = null;
		deleteTempFile();
		try {
			os = new FileOutputStream(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int read = 0;
		short buffCount = 0;
		if(null != os){
			while(isRecording && (variableDuration||(buffCount<buffCountForDuration))){
				read = recorder.read(data, 0, bufferSize);
				buffCount++;
				if(AudioRecord.ERROR_INVALID_OPERATION != read){
					try {
						os.write(data);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			if (isRecording) { 
				stopRecording();
			}

			try {
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void stopRecording(){
		if(null != recorder){
			isRecording = false;

			recorder.stop();
			recorder.release();

			recorder = null;
			recordingThread = null;
		}
		String S = getFilename();
		String tempFN = getTempFilename();
		copyWaveFile(tempFN, S);
		deleteTempFile();
		if (DEBUGMODE) AppLog.logString("File name: " + getTempFilename() + "       "+S);

		Intent returnIntent = new Intent();
		setResult(RESULT_OK,returnIntent);     
		finish();
	}

	private void deleteTempFile() {
		File file = new File(getTempFilename());

		file.delete();
	}

	private void copyWaveFile(String inFilename,String outFilename){
		FileInputStream in = null;
		FileOutputStream out = null;
		long totalAudioLen = 0;
		long totalDataLen = 0;
		totalDataLen = totalAudioLen + 36;
		long longSampleRate = 0;
		longSampleRate= RECORDER_SAMPLERATE;
		int channels = 0;
		channels = MY_RECORDER_CHANNELS;
		long byteRate = 0;
		byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels/8;

		byte[] data = new byte[bufferSize];

		try {
			in = new FileInputStream(inFilename);
			out = new FileOutputStream(outFilename);
			totalAudioLen = in.getChannel().size();
			totalDataLen = totalAudioLen + 36;

			if (DEBUGMODE) AppLog.logString("File size: " + totalDataLen);

			WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
					longSampleRate, channels, byteRate);

			while(in.read(data) != -1){
				out.write(data);
			}

			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void WriteWaveFileHeader(
			FileOutputStream out, long totalAudioLen,
			long totalDataLen, long longSampleRate, int channels,
			long byteRate) throws IOException {

		byte[] header = new byte[44];

		header[0] = 'R';  // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f';  // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1;  // format = 1
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) (MY_RECORDER_CHANNELS  * RECORDER_BPP / 8);
		header[33] = 0;
		header[34] = RECORDER_BPP;  // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

		out.write(header, 0, 44);
	}

	private View.OnClickListener btnClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch(v.getId()){
			case R.id.btnStart:{
				if (DEBUGMODE) AppLog.logString("Start Recording");

				enableButtons(true);
				startRecording();

				break;
			}
			case R.id.btnStop:{
				if (DEBUGMODE) AppLog.logString("Stop Recording");

				enableButtons(false);
				stopRecording();

				break;
			}
			}
		}
	}; 
}










