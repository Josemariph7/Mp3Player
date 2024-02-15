package com.example.mp3player;

import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;


public class GrabacionActivity extends AppCompatActivity {

    private MediaRecorder grabacion;
    private String archivoSalida = null;
    private Button btn_recorder;
    private Button btn_volver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grabacion);

        btn_recorder = findViewById(R.id.btn_rec_grabacion);
        btn_recorder.setBackgroundResource(R.drawable.stop_rec);

        btn_volver = findViewById(R.id.btn_volver);
        btn_volver.setOnClickListener(this::volverPrincipal);
    }

    public void grabar(View view) {
        if (grabacion == null) {
            String nombreAleatorio = "audio_" + UUID.randomUUID().toString() + ".mp3"; // Genera un nombre aleatorio
            File musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
            File recordingsDir = new File(musicDir, "Recordings");
            if (!recordingsDir.exists()) {
                recordingsDir.mkdirs(); // Crea el directorio si no existe
            }
            archivoSalida = new File(recordingsDir, nombreAleatorio).getAbsolutePath();

            grabacion = new MediaRecorder();
            grabacion.setAudioSource(MediaRecorder.AudioSource.MIC);
            grabacion.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
            grabacion.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            grabacion.setOutputFile(archivoSalida);

            try {
                grabacion.prepare();
                grabacion.start();
            } catch (IOException e) {
                Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show();
            }

            btn_recorder.setBackgroundResource(R.drawable.rec);
            Toast.makeText(getApplicationContext(), "Grabando...", Toast.LENGTH_SHORT).show();
        } else {
            grabacion.stop();
            grabacion.release();
            grabacion = null;
            btn_recorder.setBackgroundResource(R.drawable.stop_rec);
            Toast.makeText(getApplicationContext(), "Grabación finalizada", Toast.LENGTH_SHORT).show();
        }
    }

    public void volverPrincipal(View view) {
        finish();
    }
}
