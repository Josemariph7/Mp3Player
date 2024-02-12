package com.example.mp3player;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.util.Locale;

public class Mp3Player extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    private Handler handler = new Handler();
    private boolean isRepeatEnabled = false;
    private ImageView imageView;
    private VideoView videoView;
    private TextView currentTimeTextView, totalTimeTextView;
    private MediaRecorder mediaRecorder = null;
    private String audioFile = null;
    private int[] canciones = {R.raw.video1, R.raw.song1, R.raw.song2};
    private int[] imagenes = {R.drawable.foto1, R.drawable.foto2};
    private int cancionActual = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaPlayer = new MediaPlayer();
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        imageView = (ImageView) findViewById(R.id.imageView);
        videoView = (VideoView) findViewById(R.id.videoView);
        currentTimeTextView = (TextView) findViewById(R.id.currentTime);
        totalTimeTextView = (TextView) findViewById(R.id.totalTime);

        ImageButton btnPlay = findViewById(R.id.btnPlay); // Asegúrate de que el ID corresponde al playButton
        ImageButton btnPause = findViewById(R.id.btnPause); // Asegúrate de que el ID corresponde al pauseButton
        ImageButton btnStop = findViewById(R.id.btnStop); // Asegúrate de que el ID corresponde al stopButton
        ImageButton btnNext = findViewById(R.id.btnNext); // Asegúrate de que el ID corresponde al nextButton
        ImageButton btnPrevious = findViewById(R.id.btnPrevious); // Asegúrate de que el ID corresponde al previousButton
        ImageButton btnRepeat = findViewById(R.id.btnRepeat); // Asegúrate de que el ID corresponde al repeatButton
        ImageButton btnRecord = findViewById(R.id.botonGrabar); // Asegúrate de que el ID corresponde al recordButton

        // Asignar funcionalidades a los botones
        btnPlay.setOnClickListener(v -> playCancion());
        btnPause.setOnClickListener(v -> pauseCancion());
        btnStop.setOnClickListener(v -> stopCancion());
        btnNext.setOnClickListener(v -> siguienteCancion());
        btnPrevious.setOnClickListener(v -> anteriorCancion());

        btnRecord.setOnClickListener(v -> {
            if (mediaRecorder == null) {
                startRecording();
            } else {
                stopRecording();
            }
        });

        btnRepeat.setOnClickListener(v -> {
            isRepeatEnabled = !isRepeatEnabled;
        });

        mediaPlayer.setOnCompletionListener(mp -> {
            if (isRepeatEnabled==true) {
                playCancion();
            } else {
                siguienteCancion();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        cargarCancionActual();
        updateSeekBar();
    }

    private void cargarCancionActual() {
        if (mediaPlayer.isPlaying() || videoView.isPlaying()) {
            mediaPlayer.stop();
            videoView.stopPlayback();
        }
        mediaPlayer.reset();
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + canciones[cancionActual]);

        if (canciones[cancionActual] == R.raw.video1) { // Asumiendo que video1 es tu archivo de video
            videoView.setVideoURI(uri);
            videoView.setOnPreparedListener(mp -> {
                videoView.start();
            });
            imageView.setVisibility(ImageView.GONE);
            videoView.setVisibility(VideoView.VISIBLE);
        } else {
            try {
                mediaPlayer.setDataSource(this, uri);
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(mp -> {
                    mp.start();
                    seekBar.setMax(mediaPlayer.getDuration());
                });
                imageView.setVisibility(ImageView.VISIBLE);
                videoView.setVisibility(VideoView.GONE);

                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(this, uri);
                byte[] art = retriever.getEmbeddedPicture();
                if (art != null) {
                    imageView.setImageBitmap(BitmapFactory.decodeByteArray(art, 0, art.length));
                } else {
                    // Aquí debes asegurarte de que el índice cancionActual siempre sea válido para el array imagenes.
                    imageView.setImageResource(imagenes[cancionActual % imagenes.length]);
                }
                retriever.release();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void playCancion() {
        if (!mediaPlayer.isPlaying() && cancionActual != R.raw.video1) {
            mediaPlayer.start();
        } else if (!videoView.isPlaying() && cancionActual == R.raw.video1) {
            videoView.start();
        }
        updateSeekBar();
    }

    private void pauseCancion() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else if (videoView.isPlaying()) {
            videoView.pause();
        }
    }

    private void stopCancion() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        } else if (videoView.isPlaying()) {
            videoView.stopPlayback();
        }
        seekBar.setProgress(videoView.getCurrentPosition());
        mediaPlayer.reset();
        //cargarCancionActual();
    }

    private void siguienteCancion() {
        cancionActual = (cancionActual + 1) % canciones.length;
        cargarCancionActual();
    }

    private void anteriorCancion() {
        cancionActual = (cancionActual - 1 + canciones.length) % canciones.length;
        cargarCancionActual();
    }

    private void startRecording() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        audioFile = getExternalCacheDir().getAbsolutePath() + "/audioRecord.3gp";
        mediaRecorder.setOutputFile(audioFile);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
    }

    private void updateSeekBar() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    currentTimeTextView.setText(formatTime(mediaPlayer.getCurrentPosition()));
                    totalTimeTextView.setText(formatTime(mediaPlayer.getDuration()));
                } else if (videoView != null && videoView.isPlaying()) {
                    seekBar.setProgress(videoView.getCurrentPosition());
                    currentTimeTextView.setText(formatTime(videoView.getCurrentPosition()));
                    totalTimeTextView.setText(formatTime(videoView.getDuration()));
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    private String formatTime(int millis) {
        int seconds = millis / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;

        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        if (videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        if (videoView != null) {
            videoView.stopPlayback();
        }
        if (mediaRecorder != null) {
            mediaRecorder.release();
        }
        handler.removeCallbacksAndMessages(null);
    }
}
