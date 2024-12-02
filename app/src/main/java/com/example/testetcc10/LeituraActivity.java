package com.example.testetcc10;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.File;

public class LeituraActivity extends AppCompatActivity {

    private ImageView imgPdf;
    private PdfRenderer renderer;
    private int pageCount = 0;
    private int currentPage = 0;
    private PdfRenderer.Page pagina;
    private TextView textViewNomeLivro;

    private ScaleGestureDetector scaleGestureDetector;
    private float scaleFactor = 1.0f;

    FirebaseStorage storage;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    DatabaseReference salaDeLeituraReference;

    private boolean arquivoCarregado = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leitura);

       Button voltarPagina = findViewById(R.id.bt_voltar_pagina);
       Button avancarPagina = findViewById(R.id.bt_avancar_pagina);

        imgPdf = findViewById(R.id.imgPdf);
        textViewNomeLivro = findViewById(R.id.textView_livro);


        FirebaseApp.initializeApp(this);
        storageReference = FirebaseStorage.getInstance().getReference("pdfs");
        databaseReference = FirebaseDatabase.getInstance().getReference("salas");

        Intent intent = getIntent();

        String idSala = intent.getStringExtra("id");
        String pdfUrl = intent.getStringExtra("pdfUrl");
        String nomeLivroStr = intent.getStringExtra("nomeLivro");
        String proprietario = intent.getStringExtra("proprietario");

        Log.d(">", "Id da sala: " +  idSala);

        Toast.makeText(this, pdfUrl, Toast.LENGTH_SHORT).show();

        salaDeLeituraReference = FirebaseDatabase.getInstance().getReference("salas").child(idSala);

        if(!proprietario.equals(UserSession.nome)) {
            voltarPagina.setEnabled(false);
            avancarPagina.setEnabled(false);
        }


        if (nomeLivroStr != null) {
            textViewNomeLivro.setText(nomeLivroStr);
        }

        if (pdfUrl != null) {
            downloadFromFirebaseStorage(pdfUrl);
        }

        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        imgPdf.setOnTouchListener((v, event) -> {
            scaleGestureDetector.onTouchEvent(event);
            return true;
        });

        findViewById(R.id.button_sair_sala).setOnClickListener(v -> finish());

        findViewById(R.id.bt_voltar_pagina).setOnClickListener(v -> {
            currentPage--;
            if (currentPage < 0) currentPage = 0;
            abrePagina(currentPage);
        });

        findViewById(R.id.bt_avancar_pagina).setOnClickListener(v -> {
            currentPage++;
            if (currentPage >= pageCount) currentPage = pageCount - 1;
            abrePagina(currentPage);
        });

        aguardaNotificacao();

    }

    private void aguardaNotificacao() {

        salaDeLeituraReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Sala dadosSala = dataSnapshot.getValue(Sala.class);
                    abrePagina(dadosSala.getPaginaAtual());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private void downloadFromFirebaseStorage(String nomeArquivoNoFirebase) {
        StorageReference fileRef = storageReference.child(nomeArquivoNoFirebase);

        File directory = new File(getExternalFilesDir(null), "downloads");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File localFile = new File(directory, "downloaded.pdf");

        fileRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(this, "Arquivo baixado com sucesso!", Toast.LENGTH_SHORT).show();
            exibeArquivo(Uri.fromFile(localFile));
        }).addOnFailureListener(e -> {
            arquivoCarregado = true;
            Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
        });
    }

    private void exibeArquivo(Uri filePath) {
        try {
            Log.d(">", filePath.toString());

            ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(filePath, "r");
            if (pfd == null) {
                return;
            }
            this.renderer = new PdfRenderer(pfd);
            this.pageCount = renderer.getPageCount();
            abrePagina(currentPage);
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao abrir o PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private void abrePagina(int nroPagina) {
        if (!arquivoCarregado) {
            Log.w("PdfRenderer", "Arquivo carregado");
            return;
        }

        if (renderer == null) {
            Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
            return;
        }


        try {
            if (pagina != null) {
                pagina.close();
            }
            salaDeLeituraReference.child("paginaAtual").setValue(nroPagina);

            pagina = renderer.openPage(nroPagina);

            int width = (int) (pagina.getWidth() * scaleFactor);
            int height = (int) (pagina.getHeight() * scaleFactor);

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            pagina.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            imgPdf.setImageBitmap(bitmap);
            imgPdf.invalidate();
        } catch (Exception e) {
            Toast.makeText(this, " " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(1.5f, Math.min(scaleFactor, 5.0f));
            abrePagina(currentPage);
            return true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pagina != null) {
            pagina.close();
        }
        if (renderer != null) {
            renderer.close();
        }
    }

}
