 int size = env->GetArrayLength(stringArrays);
 int argc = size+1;
 char* argv[argc];
 argv[0]=new char[3];
 strcpy(argv[0],"NF");

 for (int i=0; i < size; ++i) 
   {
     jstring string = (jstring) env->GetObjectArrayElement(stringArrays, i);
     const char* myarray = env->GetStringUTFChars(string, 0);
     argv[i+1] = new char[strlen(myarray)+1];
     strcpy(argv[i+1], myarray);
     /* #ifdef __ANDROID_API__
     ** __android_log_print(ANDROID_LOG_DEBUG, "NF", "NormFeat argv[%i] = %s\n", i, argv[i]);
     ** #endif
     */
     env->ReleaseStringUTFChars(string, myarray);
     env->DeleteLocalRef(string);
   }
