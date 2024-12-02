package com.example.testetcc10;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.BitmapShader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EditarPerfilActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText nomeEditText;
    private EditText usernameEditText;
    private EditText descricaoEditText;
    private EditText leituraAtualEditText;
    private EditText senhaEditText;
    private ImageView imageViewIcon;
    private Button buttonSalvarPerfil;
    private Button buttonFecharPerfil;
    private Uri imageUri;
    private Button buttonExcluir;

    private FirebaseHelper firebaseHelper;
    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        nomeEditText = findViewById(R.id.editText_nome2);
        usernameEditText = findViewById(R.id.editText_username2);
        descricaoEditText = findViewById(R.id.editText_descricao);
        leituraAtualEditText = findViewById(R.id.editText_leitura);
        senhaEditText = findViewById(R.id.editText_senha_edit);
        imageViewIcon = findViewById(R.id.imageView_icon_edit);
        buttonSalvarPerfil = findViewById(R.id.button_salvar_edicao);
        buttonFecharPerfil = findViewById(R.id.button_voltar_editarPerfil);
        buttonExcluir = findViewById(R.id.button_excluir);

        firebaseHelper = new FirebaseHelper();
        usuario = new Usuario();

        carregarDadosPerfil();

        imageViewIcon.setOnClickListener(v -> openFileChooser());

        buttonSalvarPerfil.setOnClickListener(v -> salvarPerfil());

        buttonFecharPerfil.setOnClickListener(v -> finish());

        buttonExcluir.setOnClickListener(v -> confirmarExclusao());
    }

    private void carregarDadosPerfil() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("usuarios").child(userId);
        userRef.get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                nomeEditText.setText(dataSnapshot.child("nome").getValue(String.class));
                usernameEditText.setText(dataSnapshot.child("username").getValue(String.class));
                descricaoEditText.setText(dataSnapshot.child("descricao").getValue(String.class));
                leituraAtualEditText.setText(dataSnapshot.child("leituraAtual").getValue(String.class));

                String imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);
                if (imageUrl != null) {
                    Glide.with(this)
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
            } else {
                Toast.makeText(EditarPerfilActivity.this, "Dados do usuário não encontrados.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Selecione uma imagem"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this)
                    .asBitmap()
                    .load(imageUri)
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

    private void salvarPerfil() {
        usuario.setNome(nomeEditText.getText().toString());
        usuario.setUsername(usernameEditText.getText().toString());
        usuario.setDescricao(descricaoEditText.getText().toString());
        usuario.setLeituraAtual(leituraAtualEditText.getText().toString());

        String novaSenha = senhaEditText.getText().toString();

        if (imageUri != null) {
            uploadImageToFirebase(novaSenha);
        } else {
            atualizarPerfil(novaSenha);
        }
    }

    private void uploadImageToFirebase(String novaSenha) {
        StorageReference fileReference = FirebaseStorage.getInstance().getReference("uploads/" + System.currentTimeMillis() + ".jpg");
        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    usuario.setImageUrl(imageUrl);
                    atualizarPerfil(novaSenha);
                }))
                .addOnFailureListener(e -> Toast.makeText(EditarPerfilActivity.this, "Erro ao fazer upload da imagem.", Toast.LENGTH_SHORT).show());
    }

    private void atualizarPerfil(String novaSenha) {
        firebaseHelper.atualizarPerfil(usuario, novaSenha, sucesso -> {
            if (sucesso) {
                Toast.makeText(EditarPerfilActivity.this, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.putExtra("updated", true);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(EditarPerfilActivity.this, "Erro ao atualizar perfil!", Toast.LENGTH_SHORT).show();
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

    private void confirmarExclusao() {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Conta")
                .setMessage("Você tem certeza que deseja excluir sua conta?")
                .setPositiveButton("Sim", (dialog, which) -> excluirUsuario())
                .setNegativeButton("Não", null)
                .show();
    }

    private void excluirUsuario() {
        FirebaseUser usuarioAtual = FirebaseAuth.getInstance().getCurrentUser();
        if (usuarioAtual != null) {
            String userId = usuarioAtual.getUid();
            FirebaseDatabase.getInstance().getReference("usuarios").child(userId).removeValue()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            usuarioAtual.delete()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Toast.makeText(EditarPerfilActivity.this, "Conta excluída com sucesso!", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(EditarPerfilActivity.this, MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(EditarPerfilActivity.this, "Erro ao excluir conta: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(EditarPerfilActivity.this, "Erro ao excluir dados do usuário: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
