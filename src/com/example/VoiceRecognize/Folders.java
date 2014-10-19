package com.example.VoiceRecognize;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
//import com.example.VoiceRecognize.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
//import java.util.Date;



import android.content.Context;
import android.os.Environment;
import android.widget.Toast;


public enum Folders {
	PARENT, RECORDER, DATA, PRM, LBL,
	WAV, OBAK,    
	GMM, RES, LST, CFG, NDX, EXTSTOR;

	protected static boolean verifyImpostor(Context C, BufferedWriter bw) {

		String targseg = com.example.VoiceRecognize.VoiceRecognize.target_seg_res;
		String [] norms = {".ztnorm"}; 

		boolean releasedTool=false;
		boolean retval = false;
		float tol = com.example.VoiceRecognize.VoiceRecognize.TOLERANCE;
		String [] baseNames = {targseg};
		FileInputStream fis;
		try {
			for (String base:baseNames)
				for (String ext:norms)
				{
					File f = new File(base + ext);
					fis = new FileInputStream(f);
					BufferedReader obr = new BufferedReader(new InputStreamReader(fis));
					String rLine; 
					float basescore = 0.0f, score = 0.0f;
					while ((rLine = obr.readLine()) != null)  
					{
						String[] segmentedLine = rLine.split("\\s+");
						if (segmentedLine[1].equals(segmentedLine[3])) {
							basescore = Float.parseFloat(segmentedLine[4]);
						}
						else {
							score = Float.parseFloat(segmentedLine[4]);
							String logmsg = "score="+score +";scoreRatio="+ score/basescore + ";Tolerance=" + tol; 
							if (com.example.VoiceRecognize.VoiceRecognize.experimentalSetup) {
								String un = com.example.VoiceRecognize.VoiceRecognize.username;
								logmsg = un +";" + logmsg;
								writeToLogFile(bw, logmsg);
							}
							Toast.makeText(C,logmsg, Toast.LENGTH_LONG).show();

							if (releasedTool)
								if (score/basescore >= tol) { 
									retval = true;
									break;
								}
						}
					}

					obr.close();
					fis.close();
				}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return retval;
	}

	protected static void BackUpOwnerFiles() 
	{
		String dPath = Folders.OBAK.toString(); 
		String rPath = Folders.RES.getPath();

		File file = new File(rPath);
		File destf = new File(dPath);
		if(!destf.exists()){
			if (!destf.mkdirs()) {
				AppLog.logString("cannot create folder "+ dPath);
			}
		}
		for (File resf:file.listFiles()) 
		{
			if (resf != null) 	
				try {										
					File dfile = new File(dPath, resf.getName());
					com.example.VoiceRecognize.VoiceRecognize.copyFiles(resf, dfile);
				} catch(IOException e) {
					AppLog.logString("Failed to copy asset file: " + resf);
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

	// this returns a relative path from parent directory to the subdir
	@Override
	public String toString() {
		switch (this) {
		case PARENT:
			return "ACCEPTO";
		case LBL:
			return DATA.toString()+"/lbl";
		case PRM:
			return DATA.toString()+"/prm";
		case WAV:
			return DATA.toString()+"/wav";

		case RECORDER:
			return PARENT.toString()+"/AudioRecorder";
		case GMM:
			return PARENT.toString()+"/gmm";
		case RES:
			return PARENT.toString()+"/res";

		case DATA:
			return PARENT.toString()+"/data";
		case CFG:
			return PARENT.toString()+"/cfg";            	  
		case NDX:
			return PARENT.toString()+"/ndx";
		case LST:
			return PARENT.toString()+"/lst";
		case OBAK:
			return EXTSTOR.toString()+"/ownerbakup";
		case EXTSTOR:
			return Environment.getExternalStorageDirectory().getPath();
		}
		return super.toString();
	}
	// returns full path to enum dir.
	public String getPath() {
		return com.example.VoiceRecognize.VoiceRecognize.filepath +"/"+this.toString() + File.separator;
	}

	// just checking if a gmm file exists for the particular impostor
	// if missing, it adds the name to the list to  generate gmm for it
	protected static void Verify_Imp_gmm(String S, ArrayList<String> C) 
	{
		File gmmfile = new File(Folders.GMM.getPath() + File.separator + S + ".gmm");
		if (!gmmfile.exists())
			C.add(S);
	}

	protected static void setup() {
		boolean success = true;

		File intfd = null;
		try
		{
			intfd = com.example.VoiceRecognize.VoiceRecognize.CC.getFilesDir();// /data/app.../files
			if (!intfd.exists())
				intfd.mkdirs();
			for (Folders f:Folders.values()) {
				File file;
				file = new File(f.getPath());
				if(!file.exists()){
					if (!file.mkdirs()) {
						AppLog.logString("cannot create folder "+ f.toString());
						success  = false;
					}
				}
			}
			if (!success)
				System.exit(-1);
		}
		catch (Exception e) {
			e.printStackTrace();
		} 
	}

	protected static BufferedWriter createFileOnDevice() {
		return createFileOnDevice(true);
	}

	private static BufferedWriter createFileOnDevice(Boolean append) {
		/*
		 * Function to initially create the log file and it also writes the time of creation to file.
		 */
		File Root = Environment.getExternalStorageDirectory();
		if(Root.canWrite()){
			try {
			File  LogFile = new File(Root, "AcceptoLog.txt");

			BufferedWriter bw = com.example.VoiceRecognize.VoiceRecognize.bwLogOut;
			if (bw == null) {
				FileWriter LogWriter = new FileWriter(LogFile, append);
				bw = new BufferedWriter(LogWriter);
			}
			Calendar calendar = Calendar.getInstance();
			int hours = calendar.get(Calendar.HOUR_OF_DAY);
			int minutes = calendar.get(Calendar.MINUTE);
			bw.write("Logged at" + String.valueOf(hours) +":"+ String.valueOf(minutes)+"\n");
			return bw;
			}
			 catch (IOException e) {
					// TODO Auto-generated catch block
					AppLog.logString(e.toString());
					e.printStackTrace();
			 }
		}
		return null;
	}

	protected static void writeToLogFile(BufferedWriter bw, String message){
		try {
			bw.write(message+"\n");
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}    
}
