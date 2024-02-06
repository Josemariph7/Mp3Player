package com.example.mp3player;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SeekBar;
import androidx.appcompat.app.AppCompatActivity;

public class Mp3Player extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    private Handler handler = new Handler();
    private Runnable runnable;
    private CheckBox chkRepeat;
    private ImageView imageView;
    private int[] canciones = {R.raw.song1, R.raw.song2};
    private int[] imagenes = {R.drawable.foto1, R.drawable.foto2};
    private int cancionActual = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaPlayer = MediaPlayer.create(this, canciones[cancionActual]);
        seekBar = findViewById(R.id.seekBar);
        seekBar.setMax(mediaPlayer.getDuration());
        chkRepeat = findViewById(R.id.chkRepeat);
        imageView = findViewById(R.id.imageView);

        findViewById(R.id.btnPlay).setOnClickListener(v -> playCancion());
        findViewById(R.id.btnPause).setOnClickListener(v -> pauseCancion());
        findViewById(R.id.btnStop).setOnClickListener(v -> stopCancion());
        findViewById(R.id.btnNext).setOnClickListener(v -> {
            siguienteCancion();
            actualizarImagen();
        });
        findViewById(R.id.btnPrevious).setOnClickListener(v -> {
            anteriorCancion();
            actualizarImagen();
        });

        mediaPlayer.setOnCompletionListener(mp -> {
            if (chkRepeat.isChecked()) {
                playCancion();
            } else {
                siguienteCancion();
                actualizarImagen();
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
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        initSeekBar();
        actualizarImagen();
    }

    private void actualizarImagen() {
        imageView.setImageResource(imagenes[cancionActual]);
    }

    private void playCancion() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void pauseCancion() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    private void stopCancion() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.reset();
        mediaPlayer = MediaPlayer.create(getApplicationContext(), canciones[cancionActual]);
        seekBar.setMax(mediaPlayer.getDuration());
        seekBar.setProgress(0);
    }

    private void siguienteCancion() {
        cancionActual = (cancionActual + 1) % canciones.length;
        cambiarCancion();
    }

    private void anteriorCancion() {
        cancionActual = (cancionActual - 1 + canciones.length) % canciones.length;
        cambiarCancion();
    }

    private void cambiarCancion() {
        mediaPlayer.release();
        mediaPlayer = MediaPlayer.create(getApplicationContext(), canciones[cancionActual]);
        playCancion();
        seekBar.setMax(mediaPlayer.getDuration());
        seekBar.setProgress(0);
    }

    private void initSeekBar() {
        runnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                }
                handler.postDelayed(this, 500);
            }
        };
        handler.postDelayed(runnable, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacks(runnable);
    }
}

