package com.example.audiorouting;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import android.widget.EditText;
import android.speech.tts.TextToSpeech;

import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{
    private static final String TAG = "AudioDevices";

    private SoundPool soundPool;
    private int soundId;
    private Button playButton;
    private TextView audioOutputView;

    private Switch audioSwitch;
    private Button speakerListButton;

    private EditText textInput;
    private Button speakButton;
    private TextToSpeech textToSpeech;

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.getDefault());

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Language not supported.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Initialization failed.", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        audioOutputView = findViewById(R.id.audioOutputs);
        speakerListButton = findViewById(R.id.speakers_Button);
        playButton = findViewById(R.id.play_button);
        textInput = findViewById(R.id.text_input);
        speakButton = findViewById(R.id.speak_button);

        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        //make internal speaker as a default speaker
        audioManager.setSpeakerphoneOn(true);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        soundId = soundPool.load(this, R.raw.sound_file, 1);
        textToSpeech = new TextToSpeech(this, this);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f);
            }
        });

        speakerListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioManager != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
                        StringBuilder stringBuilder = new StringBuilder();
                        for (AudioDeviceInfo device : devices) {
                            String deviceType = getDeviceTypeString(device.getType());
                            String deviceProductName = device.getProductName().toString();
                            Log.d(TAG, "Device Type: " + deviceType + ", Product Name: " + deviceProductName);
                            stringBuilder.append("Device Type: ").append(deviceType).append(", Product Name: ").append(deviceProductName).append("\n");
                        }
                        audioOutputView.setText(stringBuilder);
                    }
                } else {
                    Log.d(TAG, "Audio device listing requires Android M or above.");
                }
            }
        });

        Switch audioSwitch = findViewById(R.id.audio_switch);
        audioSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Switch is checked, route audio to external speaker (USB-C audio device)
                    audioManager.setSpeakerphoneOn(false);
                    audioManager.setMode(AudioManager.MODE_NORMAL);

                } else {
                    // Switch is unchecked, route audio to internal speaker
                    audioManager.setSpeakerphoneOn(true);
                    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

                }
            }
        });
        speakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = textInput.getText().toString().trim();
                if (!text.isEmpty()) {
                    speakText(text);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundPool.release();
        soundPool = null;
    }

    private String getDeviceTypeString(int deviceType) {
        switch (deviceType) {
            case AudioDeviceInfo.TYPE_AUX_LINE:
                return "Auxiliary Line";
            case AudioDeviceInfo.TYPE_BLUETOOTH_A2DP:
                return "Bluetooth A2DP";
            case AudioDeviceInfo.TYPE_BLUETOOTH_SCO:
                return "Bluetooth SCO";
            case AudioDeviceInfo.TYPE_BUILTIN_EARPIECE:
                return "Built-in Earpiece";
            case AudioDeviceInfo.TYPE_BUILTIN_MIC:
                return "Built-in Microphone";
            case AudioDeviceInfo.TYPE_BUS:
                return "Bus";
            case AudioDeviceInfo.TYPE_DOCK:
                return "Dock";
            case AudioDeviceInfo.TYPE_FM:
                return "FM";
            case AudioDeviceInfo.TYPE_HDMI:
                return "HDMI";
            case AudioDeviceInfo.TYPE_HDMI_ARC:
                return "HDMI ARC";
            case AudioDeviceInfo.TYPE_HEARING_AID:
                return "Hearing Aid";
            case AudioDeviceInfo.TYPE_IP:
                return "IP";
            case AudioDeviceInfo.TYPE_LINE_ANALOG:
                return "Analog Line";
            case AudioDeviceInfo.TYPE_USB_DEVICE:
                return "USB Device";
            case AudioDeviceInfo.TYPE_USB_HEADSET:
                return "USB Headset";
            case AudioDeviceInfo.TYPE_WIRED_HEADPHONES:
                return "Wired Headphones";
            case AudioDeviceInfo.TYPE_WIRED_HEADSET:
                return "Wired Headset";
            default:
                return "Unknown";
        }
    }
    private void speakText(String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

}
