package info.velt.mediacentral;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.Boolean;
import java.lang.String;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.PluginResult;

public class MediaCentral extends CordovaPlugin {

    private CallbackContext mainCallbackContext = null;
    private static final String LOG_TAG = "MediaCentral";
    private static final int CAMERA_ACTIVITY = 100;
    private static final int VIDEO_CAMERA_ACTIVITY = 101;
    private static final int IMAGE_GALLERY_ACTIVITY = 110;
    private static final int VIDEO_GALLERY_ACTIVITY = 111;
    private boolean hasStatus = false;
    private boolean mainStatus;
    private String mainResult;
    
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Log.d(LOG_TAG, getAppName());
        if (action.equals("camera")) {
            Log.d(LOG_TAG, "Launching camera");
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri uri = Uri.fromFile(new File(getImageFileName("mediacentral_tmp")));
            Log.d(LOG_TAG, "Putting EXTRA_OUTPUT " + uri.toString());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            callbackContext.success("Launching camera");
            this.cordova.startActivityForResult((CordovaPlugin) this, intent, CAMERA_ACTIVITY);
            return true;
        } if (action.equals("videocamera")) {
            Log.d(LOG_TAG, "Launching video camera");
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            callbackContext.success("Launching video camera");
            this.cordova.startActivityForResult((CordovaPlugin) this, intent, VIDEO_CAMERA_ACTIVITY);
            return true;
        } else if (action.equals("imagegallery")) {
            Log.d(LOG_TAG, "Opening image gallery");
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            callbackContext.success("Opening gallery");
            this.cordova.startActivityForResult((CordovaPlugin) this, intent, IMAGE_GALLERY_ACTIVITY);
            return true;
        } else if (action.equals("videogallery")) {
            Log.d(LOG_TAG, "Opening gallery");
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("video/*");
            callbackContext.success("Opening gallery");
            this.cordova.startActivityForResult((CordovaPlugin) this, intent, VIDEO_GALLERY_ACTIVITY);
            return true;
        } else if (action.equals("onimageloaded")) {
            Log.d(LOG_TAG, "Call to 'onimageloaded'");

            mainCallbackContext = callbackContext;
            Intent i = ((CordovaActivity)this.cordova.getActivity()).getIntent();

            if (i.hasExtra(Intent.EXTRA_STREAM)) {
                Log.d(LOG_TAG, "Intent type = "+i.getType());
                String[] typeparts = i.getType().split("/");
                String uri = ((Uri) i.getParcelableExtra(Intent.EXTRA_STREAM)).toString();
                callbackStatus(true, typeparts[0] + ";" + uri);
                return true;
            }

            if (hasStatus) {
                Log.d(LOG_TAG, "Cached status: " + mainResult);
                callbackStatus(mainStatus, mainResult);
            } else {
                Log.d(LOG_TAG, "No status yet");
                PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
            }
            return true;
        }
        return false;
    }

    private String getAppName() {
        try {
            PackageManager packageManager = cordova.getActivity().getPackageManager();
            ApplicationInfo ai = packageManager.getApplicationInfo(this.cordova.getActivity().getPackageName(), 0);
            CharSequence al = packageManager.getApplicationLabel(ai);
            return ((String) al);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            return "MediaCentral";
        }
    }

    private String getImageFileName(String filename) {
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), getAppName());
        storageDir.mkdirs();
        return storageDir.getAbsolutePath() + "/" + filename + ".jpg";
    }
        
    private void callbackStatus(boolean isSuccess, String resultMessage) {
        PluginResult result = null;
        hasStatus = false;
        Log.d(LOG_TAG, "Sending result " + Boolean.toString(isSuccess) + " " + resultMessage);
        if (isSuccess) {
            result = new PluginResult(PluginResult.Status.OK, resultMessage);
        } else {
            result = new PluginResult(PluginResult.Status.ERROR, resultMessage);
        }
        result.setKeepCallback(true);
        mainCallbackContext.sendPluginResult(result);
    }
    
    private void returnStatus(boolean isSuccess, String resultMessage) {
        if (mainCallbackContext != null) {
            callbackStatus(isSuccess, resultMessage);
        } else {
            hasStatus = true;
            mainStatus = isSuccess;
            mainResult = resultMessage;
        }
    }
    
    private void moveFile(String inputPath, String outputPath) {
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(inputPath);        
            out = new FileOutputStream(outputPath);
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            out.flush();
            out.close();
            out = null;
            
            new File(inputPath).delete();  

        }  catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d(LOG_TAG, "onActivityResult called");
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAMERA_ACTIVITY) {
                Log.d(LOG_TAG, "We are back from the photo camera activity");
                String timestamp = new SimpleDateFormat("dd.MM.yy-HH.mm.ss").format(new Date());
                String srcFile = getImageFileName("mediacentral_tmp");
                String destFile = getImageFileName(timestamp);
                Log.d(LOG_TAG, "Moving " + srcFile + " to " + destFile);
                moveFile(srcFile, destFile);
                returnStatus(true, "image;" + Uri.fromFile(new File(destFile)).toString());
            } else if (requestCode == VIDEO_CAMERA_ACTIVITY) {
                Log.d(LOG_TAG, "We are back from the photo camera activity");
                returnStatus(true, "video;" + intent.getData().toString());
            } else if (requestCode == IMAGE_GALLERY_ACTIVITY) {
                Log.d(LOG_TAG, "We are back from the gallery activity");
                returnStatus(true, "image;" + intent.getData().toString());
            } else if (requestCode == VIDEO_GALLERY_ACTIVITY) {
                Log.d(LOG_TAG, "We are back from the gallery activity");
                returnStatus(true, "video;" + intent.getData().toString());
            }
        } else {
            Log.d(LOG_TAG, "Activity returned no result");
            returnStatus(false, "Activity returned no result");
        }
    }

}