package com.miraclemessages;

import android.app.Activity;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferType;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExportActivity extends Activity{
    // Indicates that no upload is currently selected
    private static final int INDEX_NOT_CHECKED = -1;

    // TAG for logging;
    private static final String TAG = "UploadActivity";

    //Buttons and SharedPreferences
    Button submit, back;
    SharedPreferences sharedpreferences;
    TextView feedback;
    public static final String myPreferences = "MyPreferences";
    public static final String Name = "name";
    public static final String Email = "email";
    public static final String Phone = "phone";
    public static final String Location = "location";
    public static final String FileLoc = "file";
    public static final String ACCOUNT_KEY = "accountName";
    private String mChosenAccountName;
    private static int UPLOAD_NOTIFICATION_ID = 1001;
    File file;
    TransferObserver observer;
    NotificationCompat.Builder builder;
    NotificationManager manager;
    // The TransferUtility is the primary class for managing transfer to S3
    private TransferUtility transferUtility;

    // The SimpleAdapter adapts the data about transfers to rows in the UI
    private SimpleAdapter simpleAdapter;

    // A List of all transfers
    private List<TransferObserver> observers;

    /**
     * This map is used to provide data to the SimpleAdapter above. See the
     * fillMap() function for how it relates observers to rows in the displayed
     * activity.
     */
    private ArrayList<HashMap<String, Object>> transferRecordMaps;

    // Which row in the UI is currently checked (if any)
    private int checkedIndex;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);
        transferUtility = Util.getTransferUtility(this);
        sharedpreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE);


            String s  = sharedpreferences.getString(FileLoc, null).toString();
            Log.v("DEREK: ", s);


            submit = (Button) findViewById(R.id.submit);
            back = (Button) findViewById(R.id.homepage);
            feedback = (TextView) findViewById(R.id.feedback);

            submit.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Log.v("OY:", "M8");
                    Toast.makeText(ExportActivity.this, "Upload started", Toast.LENGTH_LONG).show();
                    addNotification();
                    beginUpload(sharedpreferences.getString(FileLoc, null).toString());
                }

            });

            back.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    startActivity(new Intent(ExportActivity.this, PreCameraActivity.class));
                    finish();
                }
            });
        }

    @Override
    protected void onPause() {
        super.onPause();
        // Clear transfer listeners to prevent memory leak, or
        // else this activity won't be garbage collected.
        if (observers != null && !observers.isEmpty()) {
            for (TransferObserver observer : observers) {
                observer.cleanTransferListener();
            }
        }
    }

    private void beginUpload(String filePath) {
        if (filePath == null) {
            Toast.makeText(this, "Could not find the filepath of the selected file",
                    Toast.LENGTH_LONG).show();
            return;
        }
        file = new File(filePath);
        observer = transferUtility.upload(com.miraclemessages.Constants.BUCKET_NAME, file.getName(),
                file);
//        Log.v("STATE: ", observer.getState().toString());
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                Log.v("MUFFIN:", observer.getState().toString());
                if(observer.getState().toString().equals("COMPLETED")){
                    builder.setContentText("Miracle Message Uploaded!")
                    .setProgress(0, 0, false)
                    .setOngoing(false);
                    manager.notify(UPLOAD_NOTIFICATION_ID, builder.build());
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                Log.v("VALUEEE: ", ((bytesCurrent)/bytesTotal)*100 + "");
                builder.setContentText("Progress: " + ((bytesCurrent)/bytesTotal)*100 + "%")
                        .setProgress((int) bytesTotal, (int)((bytesCurrent)), false);
                manager.notify(UPLOAD_NOTIFICATION_ID, builder.build());
            }

            @Override
            public void onError(int id, Exception ex) {

            }
        });
    }

    private void addNotification() {
            builder =
                new NotificationCompat.Builder(this)
                        //CHANGE THE SMALL ICON LATER
                        .setSmallIcon(R.drawable.mmicon)
                        .setContentTitle("Uploading Miracle Message...")
                        .setOngoing(true);

             // Add as notification
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(UPLOAD_NOTIFICATION_ID, builder.build());
    }

}