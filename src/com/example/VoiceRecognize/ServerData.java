package com.example.VoiceRecognize;

import java.io.File;
//import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
//import java.io.ObjectOutputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.AssetManager;

public class ServerData {


	private static Context CC;
	private static boolean Update;
	protected ServerData(Context cc, boolean upd) {
		CC = cc;
		Update = upd;
	}

	protected  void copyToLocal()  
	{
		String [] list;	
		AssetManager assetManager;
		try 
		{  
			assetManager = CC.getAssets();

			list = com.example.VoiceRecognize.VoiceRecognize.AssetsDirList; //assetManager.list("");
			if (list!=null) {
				////////////////////////////////////////////////
				for (String sub:list) 
				{
					if (sub != null) 
						for(String filename : assetManager.list(sub)) {
							try {										
								String DestPath =             //= CC.getFilesDir().getPath() + File.separator + 
										Folders.valueOf(sub.toUpperCase()).getPath();
								CopyFromAssetsToStorage(CC, Update, sub, filename, DestPath);
							} catch(IOException e) {
								AppLog.logString("Failed to copy asset file: " + filename);
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}


	private static void CopyFromAssetsToStorage(Context Context, boolean Update, String SourceDir,  
			String SourceFile, String DestPath) throws IOException {
		String relativepath = SourceDir+ File.separator + SourceFile;
		String DestFile     = DestPath + File.separator + SourceFile;
		File mDDir = new File (DestPath);
		if (!mDDir.exists())
			mDDir.mkdirs();


		if (new File(DestFile).exists())//nDFile.exists())
			if (!Update)
				return;


		InputStream IS = Context.getAssets().open(relativepath);
		OutputStream OS = new FileOutputStream(DestFile);
		copyStream(IS, OS);
		OS.flush();
		OS.close();
		IS.close();

	}	

	private static void copyStream(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while((read = in.read(buffer)) != -1){
			out.write(buffer, 0, read);
		}
	}

}
