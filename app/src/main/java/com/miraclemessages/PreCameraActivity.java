package com.miraclemessages;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class PreCameraActivity extends Activity {

    private ViewFlipper viewFlipper, buttonViewFlipper;
    Button skip, begin, changeUser;
    TextView vName, vEmail, vPhone, vLocation, homeLabel, bfLabel, switchLoc;
    SharedPreferences sharedpreferences;
    ImageView smallIcon;
    ImageView next, back;
    LinearLayout pcBack;
    public static final String myPreferences = "MyPreferences";
    public static final String Name = "name";
    public static final String Email = "email";
    public static final String Phone = "phone";
    public static final String Location = "location";
    Animation animFadeOut, animFadeIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_precamera);

        pcBack = (LinearLayout) findViewById(R.id.precamera_background);
        pcBack.getBackground().setAlpha(0);

        sharedpreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE);
        viewFlipper = (ViewFlipper) findViewById(R.id.viewflipper);
        buttonViewFlipper = (ViewFlipper) findViewById(R.id.buttonviewflipper);
        next = (ImageView) findViewById(R.id.next);
        back = (ImageView)findViewById(R.id.back);
        skip = (Button) findViewById(R.id.skip);
        changeUser = (Button) findViewById(R.id.logout);
        vName = (TextView)findViewById(R.id.volunteer_name);
        vEmail = (TextView)findViewById(R.id.volunteer_email);
//        vPhone = (TextView) findViewById(R.id.volunteer_phone);
        vLocation = (TextView) findViewById(R.id.volunteer_location);
        switchLoc = (TextView) findViewById(R.id.switch_location);
        homeLabel = (TextView) findViewById(R.id.home_label);
        bfLabel = (TextView) findViewById(R.id.bf_label);
        begin = (Button) findViewById(R.id.begin);
        smallIcon = (ImageView) findViewById(R.id.small_icon);

        vName.setText(sharedpreferences.getString(Name, null));
        vEmail.setText(sharedpreferences.getString(Email, null));
//        vPhone.setText(sharedpreferences.getString(Phone, null));
        vLocation.setText(sharedpreferences.getString(Location, null));
        homeLabel.setText("Welcome, " + sharedpreferences.getString(Name, null) + "!");

        animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_out);
        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
        animFadeOut.setDuration(100);
        animFadeIn.setDuration(600);

        switchLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Volunteer Chapter Location:");

                // Set up the input
                final EditText input = new EditText(v.getContext());
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);

                LinearLayout layout = new LinearLayout(v.getContext());
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setGravity(Gravity.CENTER_HORIZONTAL);
                input.setSingleLine(true);
                layout.setPadding(100, 0, 100, 0);
                input.setHint("ie. City, State");
                layout.addView(input);

                builder.setView(layout);

                // Set up the buttons
                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(input.getText().toString().equals("")) {
                            Toast.makeText(PreCameraActivity.this, "Must input chapter location", Toast.LENGTH_LONG).show();
                        }
                        else {
                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putString(Location, input.getText().toString());
                            editor.commit();
                            Toast.makeText(PreCameraActivity.this, "New Chapter Location Saved!", Toast.LENGTH_LONG).show();
                            vLocation.setText(sharedpreferences.getString(Location, null));
                        }
                    }
                });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

                builder.show();
            }
        });



        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PreCameraActivity.this, Camera2Activity.class));
                finish();
            }
        });

        begin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bfLabel.setAnimation(animFadeIn);
                bfLabel.getAnimation().start();
                bfLabel.setVisibility(View.VISIBLE);
                smallIcon.setAnimation(animFadeIn);
                smallIcon.getAnimation().start();
                smallIcon.setVisibility(View.VISIBLE);
                viewFlipper.setInAnimation(v.getContext(), R.anim.slide_in_from_right);
                viewFlipper.setOutAnimation(v.getContext(), R.anim.slide_out_to_left);
                pcBack.setBackgroundResource(R.drawable.a_xxxhdpi);
                pcBack.getBackground().setAlpha(90);
                viewFlipper.showNext();
                buttonViewFlipper.setInAnimation(v.getContext(), R.anim.slide_in_from_right);
                buttonViewFlipper.setOutAnimation(v.getContext(), R.anim.slide_out_to_left);
                buttonViewFlipper.showNext();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                if(next.getText().equals("Record")) {
                if(viewFlipper.getDisplayedChild() == viewFlipper.getChildCount()-1) {
                    startActivity(new Intent(PreCameraActivity.this, Camera2Activity.class));
                    finish();
                }
                Log.v("Next Click: ", viewFlipper.getDisplayedChild() + " " + (viewFlipper.getChildCount() - 1));
                if (viewFlipper.getDisplayedChild() < viewFlipper.getChildCount() - 1) {
                    viewFlipper.setInAnimation(v.getContext(), R.anim.slide_in_from_right);
                    viewFlipper.setOutAnimation(v.getContext(), R.anim.slide_out_to_left);
                    if(viewFlipper.getDisplayedChild() == 1)
                        pcBack.setBackgroundResource(R.drawable.b_xxxhdpi);
                    else if(viewFlipper.getDisplayedChild() == 2)
                        pcBack.setBackgroundResource(R.drawable.c_xxxhdpi);
                    else if(viewFlipper.getDisplayedChild() == 3)
                        pcBack.setBackgroundResource(R.drawable.d_xxxhdpi);
                    else if(viewFlipper.getDisplayedChild() == 4)
                        pcBack.setBackgroundResource(R.drawable.e_xxxhdpi);
                    pcBack.getBackground().setAlpha(90);
                    viewFlipper.showNext();
//                    if(viewFlipper.getDisplayedChild() == viewFlipper.getChildCount() - 1)
//                        next.setText("Record");
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.v("Back Click: ", viewFlipper.getDisplayedChild() + " " + 0);
                if (viewFlipper.getDisplayedChild() > 0) {
                    if(viewFlipper.getDisplayedChild() == 1) {
                        bfLabel.setAnimation(animFadeOut);
                        bfLabel.getAnimation().start();
                        bfLabel.setVisibility(View.INVISIBLE);
                        smallIcon.setAnimation(animFadeOut);
                        smallIcon.getAnimation().start();
                        smallIcon.setVisibility(View.GONE);
                    }
                    viewFlipper.setInAnimation(v.getContext(), R.anim.slide_in_from_left);
                    viewFlipper.setOutAnimation(v.getContext(), R.anim.slide_out_to_right);
                    if(viewFlipper.getDisplayedChild() == 2)
                        pcBack.setBackgroundResource(R.drawable.a_xxxhdpi);
                    else if(viewFlipper.getDisplayedChild() == 3)
                        pcBack.setBackgroundResource(R.drawable.b_xxxhdpi);
                    else if(viewFlipper.getDisplayedChild() == 4)
                        pcBack.setBackgroundResource(R.drawable.c_xxxhdpi);
                    else if(viewFlipper.getDisplayedChild() == 5)
                        pcBack.setBackgroundResource(R.drawable.d_xxxhdpi);
                    pcBack.getBackground().setAlpha(90);
                    if(viewFlipper.getDisplayedChild() == 1) {
                        pcBack.setBackgroundColor(Color.parseColor("#93E2FF"));
                        pcBack.getBackground().setAlpha(0);
                    }
                    viewFlipper.showPrevious();
//                    if(next.getText().equals("Record"))
//                        next.setText("Next");
                    if(viewFlipper.getDisplayedChild() == 0) {
                        buttonViewFlipper.setInAnimation(v.getContext(), R.anim.slide_in_from_left);
                        buttonViewFlipper.setOutAnimation(v.getContext(), R.anim.slide_out_to_right);
                        buttonViewFlipper.showPrevious();
                    }
                }
            }
        });

        changeUser.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v){
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.clear();
                editor.commit();
                startActivity(new Intent(PreCameraActivity.this, MainActivity.class));
                finish();
            }
        });
    }


}
