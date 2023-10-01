package com.example.indoortracking;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

import com.example.indoortracking.databinding.ActivitySettingsBinding;

public class settingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;
    private SharedPreferences sharedPreferences;
    private SharedPreferences getsharedPreferences;
    int routeColor;
    int navColor;
    boolean voice;
    int lineType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences=getSharedPreferences("settings", Context.MODE_PRIVATE);
        getsharedPreferences=getSharedPreferences("settings", Context.MODE_PRIVATE);

        routeColor=getsharedPreferences.getInt("routeColor",0);
        navColor=getsharedPreferences.getInt("navColor",0);
        voice=getsharedPreferences.getBoolean("voice", true);
        lineType=getsharedPreferences.getInt("lineType", 0);

        setupUI();
    }

    private void setupUI() {

        if(routeColor==1) {
            binding.blueRadio.setChecked(true);
        }
        else if(routeColor==2) {
            binding.greenButton.setChecked(true);
        }
        else if(routeColor==1) {
            binding.redRadio.setChecked(true);
        }
        else {
            binding.blackRadio.setChecked(true);
        }

        if(navColor==1) {
            binding.blueRadio2.setChecked(true);
        }
        else if(navColor==2) {
            binding.greenButton2.setChecked(true);
        }
        else if(navColor==3) {
            binding.blackRadio2.setChecked(true);
        }
        else {
            binding.redRadio2.setChecked(true);
        }

        if(!voice) {
            binding.switchVoice.setChecked(false);
        }
        else {
            binding.switchVoice.setChecked(true);
        }

        if(lineType==1) {
            binding.switchLine.setChecked(true);
        }
        else {
            binding.switchVoice.setChecked(false);
        }
        binding.close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
            }
        });
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        binding.routeRadio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (binding.redRadio.isChecked()) {
                    myEdit.putInt("routeColor", 0);
                    myEdit.apply();
                }
                else if (binding.blueRadio.isChecked()) {
                    myEdit.putInt("routeColor", 1);
                    myEdit.apply();
                }
                else if (binding.greenButton.isChecked()) {
                    myEdit.putInt("routeColor", 2);
                    myEdit.apply();
                }

                else if (binding.blackRadio.isChecked()) {
                    myEdit.putInt("routeColor", 3);
                    myEdit.apply();
                }
            }
        });

        binding.routeRadio2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (binding.redRadio2.isChecked()) {
                    myEdit.putInt("navColor", 0);
                    myEdit.apply();
                }
                else if (binding.blueRadio2.isChecked()) {
                    myEdit.putInt("navColor", 1);
                    myEdit.apply();
                }
                else if (binding.greenButton2.isChecked()) {
                    myEdit.putInt("navColor", 2);
                    myEdit.apply();
                }

                else if (binding.blackRadio2.isChecked()) {
                    myEdit.putInt("navColor", 3);
                    myEdit.apply();
                }
            }
        });

        binding.switchVoice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                myEdit.putBoolean("voice", b);
                myEdit.apply();
            }
        });

         binding.switchLine.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
             @Override
             public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                 if(b) {
                     myEdit.putInt("lineType", 1);
                     myEdit.apply();
                 }
                 else {
                     myEdit.putInt("lineType", 0);
                     myEdit.apply();
                 }
             }
         });


    }
}