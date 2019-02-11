package com.android.hackslash.openehr;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadFiles {
    private String TAG = "Download Files";

    public DownloadFiles() {
    }

    public boolean download(int type) throws IOException {
        int status = 1;
        final String[] filenames = new String[1];

        Log.i("dfgdfgdfgdfg",Integer.toString(status));


            final FirebaseStorage[] storage = {FirebaseStorage.getInstance()};

            StorageReference storageRef = storage[0].getReferenceFromUrl("gs://openehr-bb9fc.appspot.com/");
            StorageReference  islandRef = storageRef.child("filenames.txt");
           final File localFile = File.createTempFile("images", "txt");
            islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.e("firebase ",";local tem file created  created " +localFile.toString());

                  StringBuilder fileNamesString =  downloadFileNames(localFile);
                    try {
                        doTheWork(fileNamesString);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e("firebase ",";local tem file not created  created " +exception.toString());
                }
            });


return true;



    }

    public void doTheWork(StringBuilder fileNamesString) throws IOException {
        String  file = fileNamesString.toString();
        if (file.equals("")) {
                return;
        } else {
            String fileNames[]=file.split("###");
            for (int i = 0; i < fileNames.length; i++) {
                URL url = null;
                Log.e("hhhhhhhhhhhhhhEEEEEEEE",fileNames[i]);
                if (!downloadFile( fileNames[i])){
                        return;
                }
            }
        }
    }

    public boolean downloadFile( final String fileName) throws IOException {

        final FirebaseStorage[] storage = {FirebaseStorage.getInstance()};

        StorageReference storageRef = storage[0].getReferenceFromUrl("gs://openehr-bb9fc.appspot.com/");
        StorageReference  islandRef = storageRef.child(fileName);
        final File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Android/data/com.android.hackslash.openehr", fileName);
        islandRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.e("firebase ",";local tem file created  created " +file.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("firebase ",";local tem file not created  created " +exception.toString());
            }
        });
        return true;
    }
    public StringBuilder downloadFileNames(File localFile) {
        StringBuilder result = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(localFile));
            String line;

            while ((line = br.readLine()) != null) {
                result=result.append(line);
                result=result.append("###");
            }
            br.close();
        }
        catch (IOException e) {
        }
        Log.e("firebase ",result.toString());
        return result;
    }
}
