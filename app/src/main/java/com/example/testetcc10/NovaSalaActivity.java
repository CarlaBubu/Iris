package com.example.testetcc10;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class NovaSalaActivity extends AppCompatActivity {

    private static final int PICK_PDF_FILE = 1;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private Button buttonSala;
    private ImageView btAdicionarLivro;
    private EditText usernameProprietario;
    private EditText editTextNomeLivro;
    private EditText senhaNovaSala;
    private RadioGroup radioGroupSelecionar;
    private RadioButton radioButtonPublica;
    private RadioButton radioButtonPrivada;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nova_sala);

        storageReference = FirebaseStorage.getInstance().getReference("pdfs");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("salas");

        usernameProprietario = findViewById(R.id.username_proprietario);
        buttonSala = findViewById(R.id.button_salvar_sala);
        editTextNomeLivro = findViewById(R.id.nome_livro_nova_sala);
        btAdicionarLivro = findViewById(R.id.imageView_adicionar);
        senhaNovaSala = findViewById(R.id.senha_nova_sala);
        radioGroupSelecionar = findViewById(R.id.radioGroup_selecionar);
        radioButtonPublica = findViewById(R.id.radioButton_publica);
        radioButtonPrivada = findViewById(R.id.radioButton_priv);

        carregarNomeUsuario();

        findViewById(R.id.button_fechar_NovaSala).setOnClickListener(v -> finish());

        buttonSala.setEnabled(false);
        buttonSala.setOnClickListener(v -> {
            String nomeLivro = editTextNomeLivro.getText().toString();
            if (!nomeLivro.isEmpty()) {
                Intent intent = new Intent(NovaSalaActivity.this, LeituraActivity.class);
                intent.putExtra("nomeLivro", nomeLivro);
                startActivity(intent);
            } else {
                Toast.makeText(NovaSalaActivity.this, "Por favor, insira o nome do livro.", Toast.LENGTH_SHORT).show();
            }
        });


        radioGroupSelecionar.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButton_publica) {
                senhaNovaSala.setEnabled(false);
            } else if (checkedId == R.id.radioButton_priv) {
                senhaNovaSala.setEnabled(true);
            }
        });


        btAdicionarLivro.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, "Selecionar Arquivo"), PICK_PDF_FILE);
        });

    }

    private void carregarNomeUsuario() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("usuarios").child(userId);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Usuario usuario = dataSnapshot.getValue(Usuario.class);
                    if (usuario != null) {
                        usernameProprietario.setText(usuario.getUsername());
                        UserSession.nome = usuario.getUsername();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(NovaSalaActivity.this, "Erro ao carregar nome do usuário", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PDF_FILE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedFileUri = data.getData();
                if (selectedFileUri != null) {
                    uploadToFirebaseStorage(selectedFileUri);
                }
            }
        }
    }

    private void uploadToFirebaseStorage(Uri filePath) {
        if (filePath != null) {
            String fileName = getFileName(filePath);

            if (fileName != null) {
                StorageReference pdfReference = storageReference.child(fileName);

                UploadTask uploadTask = pdfReference.putFile(filePath);

                uploadTask.addOnFailureListener(exception -> {
                    Toast.makeText(getApplicationContext(), "Erro de upload: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                }).addOnSuccessListener(taskSnapshot -> {
                    pdfReference.getDownloadUrl().addOnSuccessListener(uri -> {


                        salvarSalaNoFirebase(fileName);

                        Toast.makeText(getApplicationContext(), "Upload OK: " + fileName, Toast.LENGTH_LONG).show();
                        buttonSala.setEnabled(true);
                    });
                });
            } else {
                Toast.makeText(this, "Não foi possível determinar o nome do arquivo.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void salvarSalaNoFirebase(String pdfUrl) {
        String nomeLivroStr = editTextNomeLivro.getText().toString();
        String senhaStr = senhaNovaSala.getText().toString();
        boolean isPrivada = radioButtonPrivada.isChecked();

        Sala sala = new Sala(nomeLivroStr, isPrivada, senhaStr, pdfUrl);

        sala.setProprietario(UserSession.nome);

        DatabaseReference salaReference = databaseReference.push();
        String salaId = salaReference.getKey();

        salaReference.setValue(sala).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(NovaSalaActivity.this, "Sala criada com sucesso! " + salaReference.getKey(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(NovaSalaActivity.this, "Erro ao criar sala.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFileName(Uri uri) {
        String result = null;

        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME);
                    result = cursor.getString(nameIndex);
                }
            }
        }

        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }

        return result;
    }

    private void verificarSenha(String nomeLivro) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Sala salaEncontrada = null;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Sala sala = snapshot.getValue(Sala.class);
                    if (sala != null && sala.getNomeLivro().equals(nomeLivro)) {
                        salaEncontrada = sala;
                        break;
                    }
                }
                if (salaEncontrada != null) {
                    if (salaEncontrada.isPrivada()) {
                        String senhaInput = senhaNovaSala.getText().toString();
                        if (salaEncontrada.verificarSenha(senhaInput)) {
                            Intent intent = new Intent(NovaSalaActivity.this, LeituraActivity.class);
                            intent.putExtra("pdfUrl", salaEncontrada.getPdfUrl());
                            intent.putExtra("nomeLivro", salaEncontrada.getNomeLivro());
                            startActivity(intent);
                        } else {
                            Toast.makeText(NovaSalaActivity.this, "Senha incorreta.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Intent intent = new Intent(NovaSalaActivity.this, LeituraActivity.class);
                        intent.putExtra("pdfUrl", salaEncontrada.getPdfUrl());
                        intent.putExtra("nomeLivro", salaEncontrada.getNomeLivro());
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(NovaSalaActivity.this, "Sala não encontrada.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(NovaSalaActivity.this, "Erro ao carregar salas.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
