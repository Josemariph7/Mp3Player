package com.example.mp3player;

import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;

public class Mp3Player extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    private Handler handler = new Handler();
    private Runnable runnable;
    private CheckBox chkRepeat;
    private ImageView imageView;
    private VideoView videoView;
    private MediaMetadataRetriever metaRetriever;
    private int[] canciones = {R.raw.video1,R.raw.song1, R.raw.song2};
    private int[] imagenes = {R.drawable.foto1, R.drawable.foto2};
    private int cancionActual = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaPlayer = new MediaPlayer();
        seekBar = findViewById(R.id.seekBar);
        chkRepeat = findViewById(R.id.chkRepeat);
        imageView = findViewById(R.id.imageView);
        videoView = findViewById(R.id.videoView);

        findViewById(R.id.btnPlay).setOnClickListener(v -> playCancion());
        findViewById(R.id.btnPause).setOnClickListener(v -> pauseCancion());
        findViewById(R.id.btnStop).setOnClickListener(v -> stopCancion());
        findViewById(R.id.btnNext).setOnClickListener(v -> siguienteCancion());
        findViewById(R.id.btnPrevious).setOnClickListener(v -> anteriorCancion());

        mediaPlayer.setOnCompletionListener(mp -> {
            if (chkRepeat.isChecked()) {
                mediaPlayer.seekTo(0);
                mediaPlayer.start();
            } else {
                siguienteCancion();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        initSeekBar();
        cargarCancionActual();
    }

    private void cargarCancionActual() {
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + canciones[cancionActual]);
        String nombreRecurso = getResources().getResourceEntryName(canciones[cancionActual]);
        if (nombreRecurso.endsWith(".mp4")) {
            imageView.setVisibility(ImageView.GONE);
            videoView.setVisibility(VideoView.VISIBLE);
            videoView.setVideoURI(uri);
            videoView.setOnPreparedListener(mp -> videoView.start());
        } else {
            mediaPlayer.reset();
            mediaPlayer = MediaPlayer.create(getApplicationContext(), canciones[cancionActual]);
            mediaPlayer.start();
            imageView.setVisibility(ImageView.VISIBLE);
            videoView.setVisibility(VideoView.GONE);
            metaRetriever = new MediaMetadataRetriever();
            metaRetriever.setDataSource(this, uri);
            byte[] art = metaRetriever.getEmbeddedPicture();
            if (art != null) {
                imageView.setImageBitmap(BitmapFactory.decodeByteArray(art, 0, art.length));
            } else {
                imageView.setImageResource(imagenes[cancionActual]);
            }
        }
        seekBar.setMax(mediaPlayer != null ? mediaPlayer.getDuration() : videoView.getDuration());
    }

    private void playCancion() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        } else if (videoView != null && !videoView.isPlaying()) {
            videoView.start();
        }
    }

    private void pauseCancion() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
    }

    private void stopCancion() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        } else if (videoView != null && videoView.isPlaying()) {
            videoView.stopPlayback();
        }
        cargarCancionActual();
    }

    private void siguienteCancion() {
        cancionActual = (cancionActual + 1) % canciones.length;
        cargarCancionActual();
    }

    private void anteriorCancion() {
        cancionActual = (cancionActual - 1 + canciones.length) % canciones.length;
        cargarCancionActual();
    }

    private void initSeekBar() {
        runnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                } else if (videoView != null && videoView.isPlaying()) {
                    seekBar.setProgress(videoView.getCurrentPosition());
                }
                handler.postDelayed(this, 500);
            }
        };
        handler.postDelayed(runnable, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (videoView != null) {
            videoView.stopPlayback();
        }
        handler.removeCallbacks(runnable);
    }
}

