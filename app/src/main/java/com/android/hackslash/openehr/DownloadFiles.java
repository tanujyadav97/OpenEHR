package com.android.hackslash.openehr;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadFiles {
    private String TAG = "Download Files";

    public DownloadFiles() {

    }

    public boolean download(int type) {
        int status = 0;

        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Android/data", "com.android.hackslash.openehr");
        try {
            if (!dir.exists()) {
                dir.mkdir();
                status = 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }

        if (status == 0 && type == 0)
            return true;
        else {
            String fileNamesString = downloadFileNames("https://nitd.000webhostapp.com/openehr/filenames.txt");
            if (fileNamesString.equals("")) {
                return false;
            } else {
                String[] fileNames = fileNamesString.split("###");
                for (int i = 0; i < fileNames.length; i++) {
                    URL url = null;
                    try {
                        url = new URL("https://nitd.000webhostapp.com/openehr/adlfiles/" + fileNames[i]);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }

                    if (!downloadFile(url, fileNames[i]))
                        return false;
                }
            }
        }
        return true;
    }

    public boolean downloadFile(URL url, String fileName) {
        try {

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);

            urlConnection.connect();

            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/Android/data/com.android.hackslash.openehr", fileName);

            FileOutputStream fileOutput = new FileOutputStream(file);

            InputStream inputStream = urlConnection.getInputStream();

            byte[] buffer = new byte[1024];
            int bufferLength = 0;

            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, bufferLength);
            }

            fileOutput.close();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error : " + e.toString());
            return false;
        }
    }

    public String downloadFileNames(String link) {
        String result = "";
        try {
            URL url = new URL(link);

            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String line = null;

            while ((line = in.readLine()) != null) {
                result += line + "###";
            }

            in.close();
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Error fetching the file names : " + e.toString());
            return result;
        }
    }
}