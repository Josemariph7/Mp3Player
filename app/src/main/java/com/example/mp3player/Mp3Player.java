package com.example.mp3player;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
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
    private List<Integer> medios = new ArrayList<>();
    private int medioActual = 0;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        } else {
            initializeApp();
        }
    }

    private void initializeApp() {
        mediaPlayer = new MediaPlayer();
        seekBar = findViewById(R.id.seekBar);
        imageView = findViewById(R.id.imageView);
        videoView = findViewById(R.id.videoView);
        currentTimeTextView = findViewById(R.id.currentTime);
        totalTimeTextView = findViewById(R.id.totalTime);

        ImageButton btnPlay = findViewById(R.id.btnPlay);
        ImageButton btnPause = findViewById(R.id.btnPause);
        ImageButton btnStop = findViewById(R.id.btnStop);
        ImageButton btnNext = findViewById(R.id.btnNext);
        ImageButton btnPrevious = findViewById(R.id.btnPrevious);
        ImageButton btnRepeat = findViewById(R.id.btnRepeat);
        ImageButton btnRecord = findViewById(R.id.botonGrabar);

        btnPlay.setOnClickListener(v -> playMedio());
        btnPause.setOnClickListener(v -> pauseMedio());
        btnStop.setOnClickListener(v -> stopMedio());
        btnNext.setOnClickListener(v -> siguienteMedio());
        btnPrevious.setOnClickListener(v -> anteriorMedio());
        btnRecord.setOnClickListener(v -> {
            if (mediaRecorder == null) {
                startRecording();
            } else {
                stopRecording();
            }
        });
        btnRepeat.setOnClickListener(v -> {
            isRepeatEnabled = !isRepeatEnabled;
            String message = isRepeatEnabled ? "Repetición activada" : "Repetición desactivada";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });

        mediaPlayer.setOnCompletionListener(mp -> {
            if (isRepeatEnabled) {
                playMedio();
            } else {
                siguienteMedio();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (videoView.isPlaying()) {
                        videoView.seekTo(progress);
                    } else {
                        mediaPlayer.seekTo(progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(updateSeekBarTask);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                handler.postDelayed(updateSeekBarTask, 1000);
            }
        });

        cargarListaMedios();
        cargarMedioActual();
        updateSeekBar();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (permissionToRecordAccepted) {
                    initializeApp();
                } else {
                    Toast.makeText(this, "Permiso para grabar audio denegado", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void cargarListaMedios() {
        Field[] fields = R.raw.class.getFields();
        for (Field field : fields) {
            try {
                int resourceId = field.getInt(null);
                medios.add(resourceId);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private String getfileExtension(Uri uri)
    {
        String extension;
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        extension= mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
        return extension;
    }


    private void cargarMedioActual() {
        if (mediaPlayer.isPlaying() || videoView.isPlaying()) {
            mediaPlayer.stop();
            videoView.stopPlayback();
        }
        mediaPlayer.reset();
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + medios.get(medioActual));
        String resourceName = getResources().getResourceEntryName(medios.get(medioActual));
        if (resourceName.contains("video")) {
            videoView.setVideoURI(uri);
            videoView.setOnPreparedListener(mp -> {
                if (!videoView.isPlaying()) {
                    videoView.start();
                }
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
                    // Si no hay imagen de los metadatos, carga una imagen predeterminada
                    // imageView.setImageResource(R.drawable.default_image);
                }
                retriever.release();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void playMedio() {
        if (medios.get(medioActual) == R.raw.video1) {
            videoView.start();
        } else {
            mediaPlayer.start();
        }
        updateSeekBar();
    }

    private void pauseMedio() {
        if (videoView.isPlaying()) {
            videoView.pause();
        } else if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    private void stopMedio() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        } else if (videoView.isPlaying()) {
            videoView.stopPlayback();
        }
        seekBar.setProgress(0);
        mediaPlayer.reset();
    }

    private void siguienteMedio() {
        medioActual = (medioActual + 1) % medios.size();
        cargarMedioActual();
    }

    private void anteriorMedio() {
        medioActual = (medioActual - 1 + medios.size()) % medios.size();
        cargarMedioActual();
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

        File src = new File(audioFile);
        String fileName = "audio_" + System.currentTimeMillis() + ".3gp";
        File dst = new File(getFilesDir(), fileName);
        try {
            copy(src, dst);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copy(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
    }

    private void updateSeekBar() {
        handler.postDelayed(updateSeekBarTask, 1000);
    }

    private Runnable updateSeekBarTask = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                seekBar.setMax(mediaPlayer.getDuration()); // Ajustar la duración máxima de la barra de progreso
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                currentTimeTextView.setText(formatTime(mediaPlayer.getCurrentPosition()));
                totalTimeTextView.setText(formatTime(mediaPlayer.getDuration()));
            } else if (videoView != null && videoView.isPlaying()) {
                seekBar.setMax(videoView.getDuration()); // Ajustar la duración máxima de la barra de progreso
                seekBar.setProgress(videoView.getCurrentPosition());
                currentTimeTextView.setText(formatTime(videoView.getCurrentPosition()));
                totalTimeTextView.setText(formatTime(videoView.getDuration()));
            }
            handler.postDelayed(this, 1000);
        }
    };

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

    private Drawable getDrawableConColor(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(color);
        return drawable;
    }
}
