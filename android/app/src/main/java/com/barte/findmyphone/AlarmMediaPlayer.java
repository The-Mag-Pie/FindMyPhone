package com.barte.findmyphone;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.IOException;

public class AlarmMediaPlayer extends MediaPlayer {

    public AlarmMediaPlayer(Context context) {
        super();

        try {
            setDataSource(context, Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.raw.find_phone_sound));
            setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            );
            setLooping(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() throws IllegalStateException {
        if (isPlaying()) return;
        try {
            prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.start();
    }

    @Override
    public void stop() throws IllegalStateException {
        if (isPlaying()) {
            super.stop();
        }
    }
}
