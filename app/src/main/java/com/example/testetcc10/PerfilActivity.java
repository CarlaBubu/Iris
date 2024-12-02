package com.example.testetcc10;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.BitmapShader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.bumptech.glide.Glide;

public class PerfilActivity extends AppCompatActivity {

    private TextView nomeTextView;
    private TextView usernameTextView;
    private TextView descricaoTextView;
    private TextView leituraAtualTextView;
    private ImageView imageViewIcon;
    private Button buttonEditarPerfil;
    private Button buttonVoltar;
    private Button buttonSalas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        nomeTextView = findViewById(R.id.textView_nome);
        usernameTextView = findViewById(R.id.textView_username);
        descricaoTextView = findViewById(R.id.textView_descricao_perfil);
        leituraAtualTextView = findViewById(R.id.textView_leitura_atual);
        imageViewIcon = findViewById(R.id.imageView_icon1);
        buttonEditarPerfil = findViewById(R.id.button_editar_perfil);
        buttonVoltar = findViewById(R.id.button_fechar_perfil);
        buttonSalas = findViewById(R.id.button_salas_perfil);

        carregarDadosPerfil();

        buttonSalas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SalasActivity.class));
            }
        });

        buttonEditarPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(PerfilActivity.this, EditarPerfilActivity.class);
            startActivityForResult(intent, 1);
        });

        buttonVoltar.setOnClickListener(v -> finish());
    }


    private void carregarDadosPerfil() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("usuarios").child(userId);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Usuario usuario = dataSnapshot.getValue(Usuario.class);
                    if (usuario != null) {
                        nomeTextView.setText(usuario.getNome());
                        usernameTextView.setText(usuario.getUsername());
                        descricaoTextView.setText(usuario.getDescricao());
                        leituraAtualTextView.setText(usuario.getLeituraAtual());

                        String imageUrl = usuario.getImageUrl();
                        if (imageUrl != null) {
                            Glide.with(PerfilActivity.this)
                                    .asBitmap()
                                    .load(imageUrl)
                                    .into(new CustomTarget<Bitmap>() {
                                        @Override
                                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                            imageViewIcon.setImageDrawable(getCircularBitmapDrawable(resource));
                                        }

                                        @Override
                                        public void onLoadCleared(@Nullable Drawable placeholder) {
                                        }
                                    });
                        }
                    }
                } else {
                    Toast.makeText(PerfilActivity.this, "Dados do usuário não encontrados.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PerfilActivity.this, "Erro ao carregar dados.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private BitmapDrawable getCircularBitmapDrawable(Bitmap bitmap) {
        Bitmap circularBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(circularBitmap);
        Paint paint = new Paint();
        paint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        paint.setAntiAlias(true);
        float radius = Math.min(bitmap.getWidth() / 2f, bitmap.getHeight() / 2f);
        canvas.drawCircle(bitmap.getWidth() / 2f, bitmap.getHeight() / 2f, radius, paint);
        return new BitmapDrawable(getResources(), circularBitmap);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            carregarDadosPerfil();
        }
    }

}
