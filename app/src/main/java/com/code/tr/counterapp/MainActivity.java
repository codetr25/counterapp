package com.code.tr.counterapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {
    ImageView imgStart, imgReset;
    TextView txtCounter, txtSpeed;
    Switch swVibration, swSound, swAutomatic;
    int count = 0;
    double speed=1000; //1000 millisecons = 1 second
    boolean automatic, vibration, sound, counterStarted=false;
    Runnable runnable;
    Handler handler=null;
    MediaPlayer mpClick;
    Vibrator vibratorObj;
    SharedPreferences sharedPref;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        DecimalFormat decimal = new DecimalFormat("0.00");
        if(keyCode==KeyEvent.KEYCODE_VOLUME_UP){
            //Sound opening operation
            if (speed<3000)
                speed=speed+50;
            txtSpeed.setText("Speed: "+decimal.format(speed/1000)+" second");
            return true;
        }
        else if(keyCode==KeyEvent.KEYCODE_VOLUME_DOWN){
            //Sound muting operation
            if(speed>0)
                speed=speed-50;
            txtSpeed.setText("Speed: "+decimal.format(speed/1000)+" second");
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgStart = findViewById(R.id.imgStart);
        imgReset = findViewById(R.id.imgReset);
        txtCounter = findViewById(R.id.txtCounter);
        txtSpeed = findViewById(R.id.txtSpeed);
        swAutomatic = findViewById(R.id.swAutomatic);
        swSound = findViewById(R.id.swSound);
        swVibration = findViewById(R.id.swVibration);

        handler=new Handler();
        mpClick=MediaPlayer.create(this,R.raw.click);
        vibratorObj= (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        sharedPref = getPreferences(this.MODE_PRIVATE);

        //Let's retrieve the value we stored initially with the SharedPreferences object. If there's no value, let's write 0.
        count = sharedPref.getInt("count",0);
        txtCounter.setText(count+"");

        imgReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Alert dialog
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this)
                        .setMessage("Do you wand to reset the counter?")
                        .setPositiveButton("Yes", (dialog, id) -> {
                            count = 0;
                            txtCounter.setText(count + "");

                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putInt("count",0);
                            editor.apply();

                            Toast.makeText(MainActivity.this, "Counter reset", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("No", null);
                alert.create().show();

            }
        });
        swAutomatic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                automatic = isChecked;
            }
        });
        swVibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                vibration = isChecked;
            }
        });
        swSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sound = isChecked;
            }
        });
        imgStart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:{
                        imgStart.setImageResource(R.drawable.button_down);
                        if(automatic){
                            startTimer();
                            if(counterStarted){
                                stopTimer();
                                counterStarted=false;
                            }
                            else {
                                counterStarted=true;
                            }
                        }
                        else {
                            count++;
                            txtCounter.setText(count+"");

                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putInt("count",count);
                            editor.apply();

                            if(sound){
                                mpClick.start();
                            }
                            if(vibration){
                                if(vibratorObj!=null){
                                    vibratorObj.vibrate(100);//100 millisecond vibrate
                                    //Can be tested on real device
                                }
                            }
                        }
                    }
                    break;
                    case MotionEvent.ACTION_UP:{
                        imgStart.setImageResource(R.drawable.button_up);
                    }
                    break;
                }
                return true;
            }
        });

    }

    private void startTimer() {
        runnable=new Runnable() {
            @Override
            public void run() {
                count++;
                txtCounter.setText(count+"");

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("count",count);
                editor.apply();

                if(sound){
                    mpClick.start();
                }
                if(vibration){
                    if(vibratorObj!=null){
                        vibratorObj.vibrate(100);//100 millisecond vibrate
                        //Can be tested on real device
                    }
                }
                handler.postDelayed(this,(int)speed);
            }
        };
        handler.postDelayed(runnable,1000);//Wait 1 second at startup
    }
    private void stopTimer(){
        handler.removeCallbacksAndMessages(null); //stop timer
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();//Stop the timer when app closes
    }
}