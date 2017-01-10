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
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.amazonaws.http.HttpClient;
import com.amazonaws.http.HttpRequest;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferType;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import javax.net.ssl.HttpsURLConnection;

import static android.app.Service.START_STICKY;

public class ExportActivity extends Activity{
    // S3 URL
    public static final String vidURL =
            "https://s3.amazonaws.com/androidmiraclemessages/";
    // TAG for logging;
    private static final String TAG = "UploadActivity";

    //Buttons and SharedPreferences
    Button submit;
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
    ViewFlipper export_vf;
    ImageView back, next_x, next_xx;
    TextView title, subtitle;

    // The TransferUtility is the primary class for managing transfer to S3
    private TransferUtility transferUtility;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);
        transferUtility = Util.getTransferUtility(this);
        myRef = database.getReference("clients");

        sharedpreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE);

        export_vf = (ViewFlipper) findViewById(R.id.export_vf);
        title = (TextView) findViewById(R.id.title);
        subtitle = (TextView) findViewById(R.id.subtitle);
        next_x = (ImageView) findViewById(R.id.next_x);
        next_x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ExportActivity.this, "Next chapters clicked!", Toast.LENGTH_SHORT).show();
            }
        });
        next_xx = (ImageView) findViewById(R.id.next_xx);
        next_xx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ExportActivity.this, "Track progress clicked!", Toast.LENGTH_SHORT).show();
            }
        });

        String s  = sharedpreferences.getString(FileLoc, null).toString();

        submit = (Button) findViewById(R.id.submit);
        back = (ImageView) findViewById(R.id.back);
//        feedback = (TextView) findViewById(R.id.feedback);

        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                export_vf.setInAnimation(v.getContext(), R.anim.slide_in_from_right);
                export_vf.setOutAnimation(v.getContext(), R.anim.slide_out_to_left);
                if(export_vf.getDisplayedChild() == 0) {
                    Toast.makeText(ExportActivity.this, "Upload started", Toast.LENGTH_LONG).show();
                    addNotification();
                    beginUpload(sharedpreferences.getString(FileLoc, null).toString());
                    submit.setText("Next steps");
                    title.setText("Message sent");
                    subtitle.setText("Thank you!");
                    back.setVisibility(View.VISIBLE);
                    export_vf.showNext();
                }
                else if(export_vf.getDisplayedChild() == 1) {
                    submit.setText("Home");
                    title.setText("What's next");
                    subtitle.setText("Follow-up");
                    export_vf.showNext();
                }
                else {
                    startActivity(new Intent(ExportActivity.this, PreCameraActivity.class));
                    finish();
                }
            }

        });

        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                export_vf.setInAnimation(v.getContext(), R.anim.slide_in_from_left);
                export_vf.setOutAnimation(v.getContext(), R.anim.slide_out_to_right);
                if(export_vf.getDisplayedChild() == 1) {
                    submit.setText("Submit");
                    title.setText("Upload");
                    subtitle.setText("Ready to send the message?");
                    back.setVisibility(View.INVISIBLE);
                    export_vf.showPrevious();
                }
                else if(export_vf.getDisplayedChild() == 2) {
                    submit.setText("Next steps");
                    title.setText("Message sent");
                    subtitle.setText("Thank you!");
                    export_vf.showPrevious();
                }
            }
        });

        final String appPackageName = getPackageName().toString();
//        feedback.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                try {
//                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
//                } catch (android.content.ActivityNotFoundException notFoundException) {
//                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
//                }
//            }
//        });
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//    }

    private void beginUpload(String filePath) {
        if (filePath == null) {
            Toast.makeText(this, "Could not find the filepath of the selected file",
                    Toast.LENGTH_LONG).show();
            return;
        }
        file = new File(filePath);
        observer = transferUtility.upload(com.miraclemessages.Constants.BUCKET_NAME,
                sharedpreferences.getString(Name, null).toString() + "_" +
                        sharedpreferences.getString(Email, null).toString() +
                        "/" + file.getName(), file);

//        Log.v("STATE: ", observer.getState().toString());
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                Log.v("MUFFIN:", observer.getState().toString());

                if(state.toString().equals("COMPLETED")) {
                    Intent i = new Intent(getApplicationContext(), ExportActivity.class);
                    PendingIntent pendingIntent =
                            PendingIntent.getActivity(getApplicationContext(), 0, i, 0);

                    builder.setContentTitle("Miracle Message Uploaded!")
                            .setContentText("Thank you! :-)")
                            .setProgress(0, 0, false)
                            .setOngoing(false)
                            .setContentIntent(pendingIntent);

                    manager.notify(UPLOAD_NOTIFICATION_ID, builder.build());

                    DatabaseReference usersRef = myRef.push();

                    long unixTime = System.currentTimeMillis() / 1000L;
                    String currentTime = unixTime + "";
                    // Store to Firebase upon completion
                    usersRef.setValue(new Client(sharedpreferences.getString("about_one_reach", null)
                            , sharedpreferences.getString("about_one_live", null)
                            , sharedpreferences.getString("about_one_birth", null)
                            , sharedpreferences.getString("about_one_hometown", null)
                            , sharedpreferences.getString("about_one_name", null)
                            , sharedpreferences.getString("about_one_years", null)
                            , currentTime
                            , sharedpreferences.getString("about_two_birth", null)
                            , sharedpreferences.getString("about_two_location", null)
                            , sharedpreferences.getString("about_two_years", null)
                            , sharedpreferences.getString("about_two_name", null)
                            , sharedpreferences.getString("about_two_other", null)
                            , sharedpreferences.getString("about_two_relationship", null)
                            , sharedpreferences.getString(Email, null)
                            , sharedpreferences.getString(Location, null)
                            , sharedpreferences.getString(Name, null)
                            , sharedpreferences.getString(Phone, null)
                            , vidURL + sharedpreferences.getString(Name, null).toString() + "_" +
                            sharedpreferences.getString(Email, null).toString() +
                            "/" + file.getName()));

                    RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                    String nameURL = sharedpreferences.getString(Name, null).replaceAll("\\s+", "%20");
                    String hook = "https://hooks.zapier.com/hooks/catch/1838547/tsx0t0/?email="
                            + sharedpreferences.getString(Email, null) + "&name="
                            + nameURL + "&video="
                            + vidURL + nameURL + "_" +
                            sharedpreferences.getString(Email, null).toString() +
                            "/" + file.getName();

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, hook, new Response.Listener<String>() {
                        public void onResponse(String response) {
                            Log.v("YAS", "BISH");
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.v("No", "BISH");
                        }
                    });
                    queue.add(stringRequest);
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                Intent i = new Intent(getApplicationContext(), ExportActivity.class);
                PendingIntent pendingIntent =
                        PendingIntent.getActivity(getApplicationContext(), 0, i, 0);
                Log.v("CURRENTO:", bytesCurrent + "");
                Log.v("TOTALO:", bytesTotal + "");
                Log.v("VALUEEE: ", ((int)(((float)bytesCurrent/bytesTotal)*100) + ""));
                builder.setContentText("Progress: " + (int)(((float)bytesCurrent/bytesTotal)*100) + "%" +
                        " (Tap to reupload video if needed)")
                        .setProgress((int) bytesTotal, (int)(bytesCurrent), false)
                        .setContentIntent(pendingIntent);
                manager.notify(UPLOAD_NOTIFICATION_ID, builder.build());
            }

            @Override
            public void onError(int id, Exception ex) {
                Intent i = new Intent(getApplicationContext(), ExportActivity.class);
                PendingIntent pendingIntent =
                        PendingIntent.getActivity(getApplicationContext(), 0, i, 0);

                builder.setContentTitle("Network status changed")
                        .setContentText("Please tap here and re-upload.")
                        .setOngoing(true)
                        .setContentIntent(pendingIntent)
                        .setProgress(0, 0, false);

                manager.notify(UPLOAD_NOTIFICATION_ID, builder.build());
            }
        });
    }

    public static class Client {

        public String client_contact_info;
        public String client_current_city;
        public String client_dob;
        public String client_hometown;
        public String client_name;
        public String client_years_homeless;

        public String created_at;

        public String recipient_dob;
        public String recipient_last_location;
        public String recipient_last_seen;
        public String recipient_name;
        public String recipient_other_info;
        public String recipient_relationship;

        public String volunteer_email;
        public String volunteer_location;
        public String volunteer_name;
        public String volunteer_phone;
        public String uploadedURL;

        public Client(String client_contact_info,
                    String client_current_city,
                    String client_dob,
                    String client_hometown,
                    String client_name,
                    String client_years_homeless,
                    String created_at,
                    String recipient_dob,
                    String recipient_last_location,
                    String recipient_last_seen,
                    String recipient_name,
                    String recipient_other_info,
                    String recipient_relationship,
                    String volunteer_email,
                    String volunteer_location,
                    String volunteer_name,
                    String volunteer_phone,
                    String uploadedURL) {
            this.client_contact_info = client_contact_info;
            this.client_current_city = client_current_city;
            this.client_dob = client_dob;
            this.client_hometown = client_hometown;
            this.client_name = client_name;
            this.client_years_homeless = client_years_homeless;
            this.created_at = created_at;
            this.recipient_dob = recipient_dob;
            this.recipient_last_location = recipient_last_location;
            this.recipient_last_seen = recipient_last_seen;
            this.recipient_name = recipient_name;
            this.recipient_other_info = recipient_other_info;
            this.recipient_relationship = recipient_relationship;
            this.volunteer_email = volunteer_email;
            this.volunteer_location = volunteer_location;
            this.volunteer_name = volunteer_name;
            this.volunteer_phone = volunteer_phone;
            this.uploadedURL = uploadedURL;
        }
    }

    public void onBackPressed(){
        export_vf.setInAnimation(getApplicationContext(), R.anim.slide_in_from_left);
        export_vf.setOutAnimation(getApplicationContext(), R.anim.slide_out_to_right);
        if(export_vf.getDisplayedChild() == 1) {
            submit.setText("Submit");
            title.setText("Upload");
            subtitle.setText("Ready to send the message?");
            back.setVisibility(View.INVISIBLE);
            export_vf.showPrevious();
        }
        else if(export_vf.getDisplayedChild() == 2) {
            submit.setText("Next steps");
            title.setText("Message sent");
            subtitle.setText("Thank you!");
            export_vf.showPrevious();
        }

    }

    private void addNotification() {
        builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.mmicon)
                        .setContentTitle("Uploading Miracle Message...")
                        .setOngoing(true);

        // Add as notification
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(UPLOAD_NOTIFICATION_ID, builder.build());
    }
}