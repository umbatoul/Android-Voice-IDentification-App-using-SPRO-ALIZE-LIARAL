/* 
 * Given from the server are:
 *  world.gmm, and 
 *  normalized *.prm and *.lbl: a number of analyzed files used as basic impostors for training
 * the model and establishing thresholds. 
 * 
 * These may either be packaged with the app as raw resources.
 * Alternatively, (not implemented here) these may be received 
 * from the server as updates or expansion files. In that case,
 * whenever there is an update, the app should ask the owner
 * to retrain.
 * Given in the package as fixed resources are a number if cfg 
 * files 
 * TODO: copy these supplied files to an external cache: getExternalCacheDir()
 * currently they are just stored in ExternalStorage (sdcard)
 * The user's files should be stored privately in the app internal storage.
 */

/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.VoiceRecognize;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
//import java.io.InputStream;
//import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
//import android.content.res.AssetManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
//import android.widget.EditText;
import android.os.Bundle;
//import android.os.Environment;
import android.preference.PreferenceManager;

import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class VoiceRecognize extends Activity
{
	public final static boolean DEBUGMODE= false; // one file suffices 
	public final static int NUM_TRAIN_FILES = 1; // one file suffices 
	
	public String[] impListAll;
	public int      impListAllSize;
	public String[] seglist;
	public int      seglistsize;
	protected static String  target, impostor, fileToProcess, ndxFile1;
	private static Boolean verifying;
	
	protected static final String AUDIO_RECORDER_FILE_EXT_TMPPRM = ".tmp.prm";
	protected static final String AUDIO_RECORDER_FILE_EXT_ETMPPRM = ".enr.tmp.prm";
	protected static final String AUDIO_RECORDER_FILE_EXT_NORMPRM = ".norm.prm";
	protected static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
	public static String filepath;//Environment.getExternalStorageDirectory().getPath();
	
	protected static final String [] AssetsDirList = {"prm", "lbl", "gmm", "cfg"};
	protected static  String prm_path = null; // Folders.PRM.getPath();	
	protected static  String lbl_path = null; // Folders.LBL.getPath();

	protected static  String cfg_path = null; // Folders.CFG.getPath(); //filepath + "/" + AUDIO_CFG_FOLDER;
	protected static  String ndx_path = null; // Folders.NDX.getPath(); //filepath + "/" + AUDIO_NDX_FOLDER;
	protected static  String gmm_path = null; // Folders.GMM.getPath(); //filepath + "/" + AUDIO_GMM_FOLDER;
	protected static  String res_path = null; // Folders.RES.getPath();
	protected String gARGS[];

	protected static  String train_target_cfg         = null; // cfg_path + "/TrainTarget.cfg";

	protected static  String compute_test_train_cfg   = null; // cfg_path + "/ComputeTest_GMM.cfg";
	protected static  String NormFeat_energy_Spro_cfg = null; // cfg_path + "/NormFeat_energy_SPro.cfg";
	protected static  String NormFeat_Spro_cfg        = null; // cfg_path + "/NormFeat_SPro.cfg";
	protected static  String EnergyDetector_Spro_cfg  = null; // cfg_path + "/EnergyDetector_SPro.cfg";

	protected static  String compute_testZ_cfg        = null; // cfg_path + "/ComputeTestZNorm.cfg";
	protected static  String compute_testT_cfg        = null; // cfg_path + "/ComputeTestTNorm.cfg";
	protected static String compute_testZT_cfg        = null;
	
	protected static  String target_seg_res      = null; // res_path + "/target-seg_gmm.res";
	protected static  String target_imp_res      = null; // res_path + "/target-imp_gmm.res";
	protected static  String imp_seg_res         = null; // res_path + "/imp-seg_gmm.res";
	protected static  String imp_imp_res         = null; // res_path + "/imp-imp_gmm.res";

	protected static  String trainModel_ndx      = null; // ndx_path + "/trainModel.ndx";
	protected static  String target_seg_ndx      = null; // ndx_path + "/computetest_gmm_target-seg.ndx";
	protected static  String trainImp_ndx        = null; // ndx_path + "/trainImp.ndx";
	protected static  String trainImp1_ndx        = null; 
	protected static  String trainImpGMM_ndx     = null;
	protected static  String imp_imp_ndx         = null; // ndx_path + "/computetest_gmm_imp-imp.ndx";
	protected static  String target_imp_ndx      = null; // ndx_path + "/computetest_gmm_target-imp.ndx";
	protected static  String imp_seg_ndx         = null; // ndx_path + "/computetest_gmm_imp-seg.ndx";
	protected static  String world_gmm           = null; // gmm_path + "/world.gmm";

	protected static Context CC = null;
	// TODO: add edit text or spinner view to allow user to change tolerance level
	// this value represents the max allowed deviation from the owner's normalized scores
	protected static float TOLERANCE = 0.5f;

	
	private static boolean serverUpdate = true;
	private ServerData SD;

	protected static BufferedWriter bwLogOut = null;
	protected static final boolean experimentalSetup = true;
	protected static String username = null;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		final Intent intent_ut    = new Intent(this, RecordForTraining.class);
		final Intent intent_idtfy = new Intent(this, RecordForTraining.class);

		target   = getResources().getText(R.string.train_user).toString();
		impostor = getResources().getText(R.string.test_user).toString();
		CC       = this.getApplicationContext(); //this.getBaseContext();
		filepath = CC.getFilesDir().getPath();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if( Float.parseFloat(getString(R.string.version)) > prefs.getFloat("LastInstalledVersion", (float) 0.0 ) ) {
			serverUpdate = true;
		}
		else serverUpdate = false;

		// Here I copy necessary files bundled with the apk from assets to the 
		// app internal storage. I call it serverdata because I am assuming these
		// files get created externally and served if updates to the world.gmm
		// or new impostor files are needed.
		// The cfg files are not expected to change
		if (serverUpdate) {
			SD = new ServerData(CC, serverUpdate);
			SD.copyToLocal();
			serverUpdate = false;
		}


		////////////////////////////////////////////////
		/// generating .ndx files
		String targlist [];
		targlist    = new String [] {target, impostor};
		

		String imp1List [];
		imp1List    = new String [1];
		imp1List[0] = impostor;

		
		Folders.setup();
		setFPaths();
		System.loadLibrary("spro_4_0");
		System.loadLibrary("voicerecognize");

		impListAll     = get_impListAll();

		// TODO: create these files only once - check if ndx folder is empty or out of date with impListAll
		// or move up to the check server update index
		create_generic_ndx_files(impListAll, null, true, trainImp_ndx, null); 
		create_generic_ndx_files(imp1List, imp1List, false, trainImp1_ndx, null);
		create_generic_ndx_files(impListAll, impListAll, true, imp_imp_ndx, null); //"imp_imp.ndx", "computetest_gmm_imp-imp.ndx");

		create_generic_ndx_files(targlist, impListAll, false, imp_seg_ndx, null); //"imp_seg.ndx", "computetest_gmm_imp-seg.ndx");
		create_generic_ndx_files(impListAll, targlist, false, target_imp_ndx, null);

		create_generic_ndx_files(targlist, targlist, false, target_seg_ndx, null);
		create_generic_ndx_files(targlist, null, true, trainModel_ndx, null);
		
		// END NDX Generation
		//////////////////////////////////////////////////////////////////////////////////

		setContentView(R.layout.main);

		Button btn_ut = (Button) findViewById(R.id.btnTrainRec);
		Button btn_idtfy = (Button) findViewById(R.id.btnIdentify);
		;
		if (experimentalSetup)
			bwLogOut = Folders.createFileOnDevice();

		//TODO: disable btn_idtfy if owner not trained

		
		/////////////////////////////
		final EditText et = (EditText) findViewById(R.id.username);

		btn_ut.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// to start training
				username = et.getText().toString();
				verifying = false;
				fileToProcess = target;
				ndxFile1 = trainModel_ndx;
				intent_ut.putExtra("titleMsg", getResources().getText(R.string.titleLearn).toString());
				intent_ut.putExtra("infoMsg", getResources().getText(R.string.infoLearn).toString());
				intent_ut.putExtra("filename", target);
				startActivityForResult(intent_ut, 1);
			}
		});



		btn_idtfy.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// to start training
				username = et.getText().toString();
				verifying = true;
				fileToProcess = impostor;
				ndxFile1 = trainImp1_ndx;
				intent_idtfy.putExtra("titleMsg", getResources().getText(R.string.titleVerif).toString());
				intent_idtfy.putExtra("infoMsg", getResources().getText(R.string.infoVerif).toString());
				intent_idtfy.putExtra("filename", impostor);
				startActivityForResult(intent_idtfy, 1);
			}
		});  
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		long startTime, endTime;
		if (requestCode == 1) {
			if (DEBUGMODE) AppLog.logString("HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH");
			if(resultCode == RESULT_OK){
				//feature extraction .wav to .tmp.prm
				
				// *******************************************************************************
				// 	Common part for target and impostor
				startTime = System.currentTimeMillis();
				if (DEBUGMODE) AppLog.logString("Input name is "+ fileToProcess);
				String input = Folders.RECORDER.getPath() +"/" + fileToProcess + AUDIO_RECORDER_FILE_EXT_WAV;
				if (DEBUGMODE) AppLog.logString("Input name is "+input);
				String result= getPrmFilename(fileToProcess, AUDIO_RECORDER_FILE_EXT_TMPPRM, true); 

				Sfbsep(input, result);
				if (DEBUGMODE) AppLog.logString("Result name is "+result);
				// label generation code requires that there is no .lbl file present
				// for that particular .prm. Here we delete previous lbl file for 
				// target or impostor
				cleanup_lbl_dir( lbl_path, fileToProcess);
				
				if (DEBUGMODE) AppLog.logString("config file name "+NormFeat_energy_Spro_cfg  +" Prm path name "+prm_path+ "   data as owner");	
				if (DEBUGMODE) AppLog.logString("The lbl path" +lbl_path+"/");

				gARGS = new String [] {"--inputFeatureFilename", fileToProcess, 
						"--featureFilesPath", prm_path,
						"--config", NormFeat_energy_Spro_cfg};
				NormFeat1(gARGS);


				gARGS = new String [] {"--inputFeatureFilename", fileToProcess, 
						"--featureFilesPath", prm_path,
						"--labelFilesPath", lbl_path,
						"--config", EnergyDetector_Spro_cfg};
				EnergyDetector1(gARGS);

				gARGS = new String [] {"--inputFeatureFilename", fileToProcess, 
						"--featureFilesPath", prm_path,
						"--labelFilesPath", lbl_path,
						"--config", NormFeat_Spro_cfg};
				NormFeat1(gARGS);

				if (DEBUGMODE) AppLog.logString("Training Target ........................... "+train_target_cfg);
				
				gARGS = new String [] {"--targetIdList", ndxFile1, 
						"--featureFilesPath", prm_path,
						"--labelFilesPath", lbl_path, 
						"--mixtureFilesPath", gmm_path,
						"--inputWorldFilename", world_gmm,
						"--config", train_target_cfg};

				TrainTarget1(gARGS);

				/////**********        end common part  *****************************//






				// specific to training : target-seg score to take tolerance amount
				//ComputeTest 
				if (verifying)
				{
					gARGS = new String [] {"--targetIdList", trainModel_ndx, 
							"--featureFilesPath", prm_path,
							"--labelFilesPath", lbl_path, 
							"--mixtureFilesPath", gmm_path,
							"--config", compute_test_train_cfg,
							"--ndxFilename" , target_seg_ndx,
							"--inputWorldFilename", world_gmm,
							"--outputFilename", target_seg_res};
					if (DEBUGMODE) AppLog.logString("ComputeTest ...GMM target_seg ............ ");
					
					ComputeTest1(gARGS);

					// no need to do backup unless the verification logic is revised 
					
					gARGS = new String [] {"--targetIdList", trainModel_ndx, 
							"--featureFilesPath", prm_path,
							"--labelFilesPath", lbl_path, 
							"--mixtureFilesPath", gmm_path,
							"--config", compute_testZ_cfg,
							"--inputWorldFilename", world_gmm,
							"--ndxFilename" , target_imp_ndx, //"ndx/computetest_gmm_target-imp.ndx",
							"--outputFilename", target_imp_res};
					if (DEBUGMODE) AppLog.logString("ComputeTest Z........target_imp......... ");
					
					ComputeTest1(gARGS);

					
					gARGS = new String [] {"--targetIdList", trainImp_ndx, //"ndx/trainImp.ndx", 
							"--featureFilesPath", prm_path,
							"--labelFilesPath", lbl_path, 
							"--mixtureFilesPath", gmm_path,
							"--config", compute_testT_cfg,
							"--inputWorldFilename", world_gmm,
							"--ndxFilename" , imp_seg_ndx, //"ndx/computetest_gmm_imp-seg.ndx",
							"--outputFilename", imp_seg_res};
					if (DEBUGMODE) AppLog.logString("ComputeTest T...imp_seg........... ");
					ComputeTest1(gARGS);


					
					///******************************************************
					if (true) {
						gARGS = new String [] {"--targetIdList", trainImp_ndx, //"ndx/trainImp.ndx", 
								"--featureFilesPath", prm_path,
								"--labelFilesPath", lbl_path, 
								"--mixtureFilesPath", gmm_path,
								"--config", compute_testZT_cfg,
								"--inputWorldFilename", world_gmm,
								"--ndxFilename" , imp_imp_ndx, 
								"--outputFilename", imp_imp_res};
						if (DEBUGMODE) AppLog.logString("ComputeTest *** impimp *** ");
						ComputeTest1(gARGS);
	
						gARGS = null;
						gARGS = new String [] {   //"--config", compute_normZT_cfg,
								"--testNistFile", target_seg_res, //"res/target-seg_gmm.res",
								"--normType", "ztnorm",
								"--tnormNistFile", imp_seg_res, //"res/imp-seg_gmm.res",
								"--znormNistFile", target_imp_res, //"res/target-imp_gmm.res",
								"--ztnormNistFile", imp_imp_res, //"res/imp-imp_gmm.res",
								"--outputFileBaseName", target_seg_res, //"res/target-seg_gmm.res"
						};				
						if (DEBUGMODE) AppLog.logString("ComputeNorm .....res.ztnorm.............. ");
						ComputeNorm1(gARGS);

						
					} 
					//**********************************************************/
					if (experimentalSetup) {
						endTime = System.currentTimeMillis();
						Folders.writeToLogFile(bwLogOut, "verif time=" + (endTime - startTime) + " ms");
					}
					if (Folders.verifyImpostor(getBaseContext(), bwLogOut))
						Toast.makeText(getApplicationContext(), getResources().getText(R.string.verif_1).toString(), 
								Toast.LENGTH_LONG).show();
					else
						Toast.makeText(getApplicationContext(), getResources().getText(R.string.verif_0).toString(), 
								Toast.LENGTH_LONG).show();
				}

			}

			else if (resultCode == RESULT_CANCELED) 
			{
				Toast.makeText(getApplicationContext(), getResources().getText(R.string.op_cancel).toString(), 
						Toast.LENGTH_LONG).show();
				//Write your code if there's no result
			}
		}
	}//onActivityResult

	private static void setFPaths() {
		prm_path = Folders.PRM.getPath();	
		lbl_path = Folders.LBL.getPath();

		cfg_path = Folders.CFG.getPath(); //filepath + "/" + AUDIO_CFG_FOLDER;
		ndx_path = Folders.NDX.getPath(); //filepath + "/" + AUDIO_NDX_FOLDER;
		gmm_path = Folders.GMM.getPath(); //filepath + "/" + AUDIO_GMM_FOLDER;
		res_path = Folders.RES.getPath();
		train_target_cfg         = cfg_path + "TrainTarget.cfg";

		compute_test_train_cfg   = cfg_path + "ComputeTest_GMM.cfg";
		NormFeat_energy_Spro_cfg = cfg_path + "NormFeat_energy_SPro.cfg";
		NormFeat_Spro_cfg        = cfg_path + "NormFeat_SPro.cfg";
		EnergyDetector_Spro_cfg  = cfg_path + "EnergyDetector_SPro.cfg";

		compute_testZ_cfg        = cfg_path + "ComputeTestZNorm.cfg";
		compute_testT_cfg        = cfg_path + "ComputeTestTNorm.cfg";
		compute_testZT_cfg        = cfg_path + "ComputeTestZTNorm.cfg";

		target_seg_res      = res_path + "target-seg_gmm.res";
		target_imp_res      = res_path + "target-imp_gmm.res";
		imp_seg_res         = res_path + "imp-seg_gmm.res";
		imp_imp_res         = res_path + "imp-imp_gmm.res";

		trainModel_ndx      = ndx_path + "trainModel.ndx";
		target_seg_ndx      = ndx_path + "computetest_gmm_target-seg.ndx";
		trainImp_ndx        = ndx_path + "trainImp.ndx";
		trainImp1_ndx       = ndx_path + "trainImp1.ndx";
		trainImpGMM_ndx     = ndx_path + "trainImpGMM.ndx";
		imp_imp_ndx         = ndx_path + "computetest_gmm_imp-imp.ndx";
		target_imp_ndx      = ndx_path + "computetest_gmm_target-imp.ndx";
		imp_seg_ndx         = ndx_path + "computetest_gmm_imp-seg.ndx";
		world_gmm           = "world";//gmm_path + "world.gmm";

	}
	protected static void copyFiles(File source, File dest)
			throws IOException {
		FileChannel inputChannel = null;
		FileChannel outputChannel = null;
		try {
			inputChannel = new FileInputStream(source).getChannel();
			outputChannel = new FileOutputStream(dest).getChannel();
			outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
		} finally {
			inputChannel.close();
			outputChannel.close();
		}
	}


	protected String getPrmFilename(String value, String ext, boolean full){

		File file;
		file = new File(Folders.PRM.getPath());//filepath,AUDIO_PRMT_FOLDER);

		if(!file.exists()){
			file.mkdirs();
		}

		return ((full ? file.getAbsolutePath() + "/" : "") + value + ext);
	}


	protected String[] get_impListAll() { 

		File filelabel;

		ArrayList<String> imp_list = new ArrayList<String>();
		ArrayList<String> imp_crgmm_list = new ArrayList<String>();
		String arr[] = null;
		try
		{
			filelabel = new File(Folders.LBL.getPath());
			if (filelabel==null || filelabel.listFiles()==null) {
				System.err.println("label dir empty!");
				System.exit(-1);
			}
			for(File filel: filelabel.listFiles()) {
				// TODO MAYBE: if multiple owners are needed:
				//      account for more training files: unm1, unm2, unm3
				String S = filel.getName().substring (0,filel.getName().indexOf('.'));
				if (!S.equals(target) && !S.equals(impostor)) { //unm.getText().toString())) 
					imp_list.add(S);
					Folders.Verify_Imp_gmm(S, imp_crgmm_list);
				}
			}
			if (!imp_crgmm_list.isEmpty())
				Create_Imp_gmm(imp_crgmm_list);

			arr = imp_list.toArray(new String[imp_list.size()]);
			impListAllSize = arr.length;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return arr;
	}



	protected void cleanup_lbl_dir( String dirpath, String lblfile){
		File filedir = new File(dirpath);
		if(!filedir.exists()){
			filedir.mkdirs();
		}
		else {
			if (lblfile==null) //delete all
				for(File file: filedir.listFiles()) {
					file.delete();
				}
			else {
				String fname = filedir.getAbsolutePath() + "/" + lblfile + ".lbl";
				File f = new File(fname);
				f.delete();
			}
		}
	}

	// generic ndx file generator
	protected void create_generic_ndx_files(String [] list1, String [] list2, boolean uniqueline, String f1, String f2) {

		try {
			File file1 = new File(f1);
			File file2 = null;
			if (f2 != null)
				file2 = new File(f2);
			OutputStream out = new FileOutputStream(file1);

			final PrintStream printStream = new PrintStream(out);
			int sz1 = list1.length;
			int sz2=0;
			if (list2 != null)
				sz2 = list2.length;
			for(int i = 0; i < sz1; i++) {
				String S = list1[i];
				for(int j = 0; j < sz2; j++) { 
					if (uniqueline && (j == i))
						continue;
					else
						S = S+ "  "+ list2[j];
				}
				if (sz2 == 0)
					S = S + " " + S;
				S = S +"\n";
				printStream.print(S);
			}            
			printStream.close();
			out.close();
			if (file2!=null)
				copyFiles(file1, file2);
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void Create_Imp_gmm(ArrayList<String> imp_crgmm_list) 
	{
		String 	[] arr = imp_crgmm_list.toArray(new String[imp_crgmm_list.size()]);
		create_generic_ndx_files(arr, null, true, trainImpGMM_ndx, null);

		if (DEBUGMODE) AppLog.logString("Training other...................... "+train_target_cfg);
		String [] iARGS = new String [] {"--targetIdList", trainImpGMM_ndx, 
				"--featureFilesPath", prm_path,
				"--labelFilesPath", lbl_path, 
				"--mixtureFilesPath", gmm_path,
				"--inputWorldFilename", world_gmm,
				"--config", train_target_cfg};

		TrainTarget1(iARGS);	
	}


	@Override
	public void onPause()
	{  //TODO: perhaps move this code to be called once whenever there is an updated version
		SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putFloat("LastInstalledVersion", Float.parseFloat(getString(R.string.version)) );
		editor.commit();
		super.onPause();
	}


	public native int ComputeNorm1(String [] S);
	public native int ComputeTest1(String [] S);
	public native int NormFeat1(String [] S);
	public native int EnergyDetector1(String [] S1);
	public native int TrainTarget1(String [] S1);    
	public native int Sfbsep(String S1, String S2);



@Override
	public void onResume() {
		if (experimentalSetup)
			bwLogOut = Folders.createFileOnDevice();
		super.onResume();
	}
@Override
public void onDestroy() {
	if (bwLogOut!=null)
		try {
			bwLogOut.flush();
			bwLogOut.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	super.onDestroy();
}
}