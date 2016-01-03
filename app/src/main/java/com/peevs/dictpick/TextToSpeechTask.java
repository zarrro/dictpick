package com.peevs.dictpick;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by zarrro on 19.10.2015 г..
 */
public class TextToSpeechTask extends AsyncTask<Void, Void, Void> {

    // TODO: more modes can be added like - play stream
    public enum Mode {
        DOWNLOAD_AND_PLAY, DOWNLOAD_ONLY, PLAY_ONLY;
    }

    private static final String TAG = TextToSpeechTask.class.getSimpleName();

    private final Language lang;

    private final File filesDir;
    private final String text;

    private final boolean forceDownload;
    private final Mode mode;

    private static class MediaPlayerCleanup implements MediaPlayer.OnCompletionListener {

        private InputStream datasourceInputStream;

        MediaPlayerCleanup(InputStream datasourceInputStream) {
            this.datasourceInputStream = datasourceInputStream;
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            mp.release();
            if (datasourceInputStream != null) {
                try {
                    datasourceInputStream.close();
                } catch (IOException e) {
                    Log.wtf("MediaPlayerCleanup", e);
                }
            }
        }
    }

    public TextToSpeechTask(String text, Language lang, File filesDir) {
        this.text = text;
        this.lang = lang;
        this.filesDir = filesDir;
        // the defaults
        this.forceDownload = false;
        this.mode = Mode.DOWNLOAD_AND_PLAY;
    }

    public TextToSpeechTask(String text, Language lang, File file, Mode mode, boolean forceDownload) {
        this.text = text;
        this.lang = lang;
        this.filesDir = file;
        this.mode = mode;
        this.forceDownload = forceDownload;
    }

    @Override
    protected Void doInBackground(Void... v) {
        File speechFile = getSpeechFile(filesDir, this.text, this.lang);
        if (mode == Mode.DOWNLOAD_ONLY || mode == Mode.DOWNLOAD_AND_PLAY) {
            OutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(speechFile);
                Log.i(TAG, "Download text to speech speechFile: " + speechFile.getAbsolutePath());
                Translator.textToSpeach(text, lang, outputStream);
            } catch (IOException e) {
                Log.e(TAG, "Failed to store text to speech filesDir ", e);
                speechFile.delete();
            } finally {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Log.wtf(TAG, e);
                }
            }
        }
        if (mode == Mode.PLAY_ONLY || mode == Mode.DOWNLOAD_AND_PLAY) {
            if (speechFile.exists() && speechFile.isFile()) {
                try {
                    FileInputStream fin = new FileInputStream(speechFile);
                    MediaPlayer mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
                    mediaPlayer.setDataSource(fin.getFD());
                    mediaPlayer.setOnCompletionListener(new MediaPlayerCleanup(fin));
                    // no need to use prepareAsync as we are in background thread here
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    Log.e(TAG, "Error with playing the speech filesDir, delete it to cleanup", e);
                    speechFile.delete();
                }
            }
        }
        return null;
    }

    public File getSpeechFile(File filesDir, String text, Language lang) {
        StringBuilder sb = new StringBuilder();
        sb.append("tts-");
        sb.append(text.hashCode());
        sb.append(lang.toString().hashCode());
        sb.append(".mp3");
        return new File(this.filesDir, sb.toString());
    }
}



