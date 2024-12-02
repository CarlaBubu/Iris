package com.example.testetcc10;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CadastroActivity extends AppCompatActivity {

    private EditText nomeEditText;
    private EditText emailEditText;
    private EditText senhaEditText;
    private EditText usernameEditText;
    private Button cadastrarButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        nomeEditText = findViewById(R.id.nome_cadastro);
        emailEditText = findViewById(R.id.email_cadastro);
        senhaEditText = findViewById(R.id.senha_cadastro);
        usernameEditText = findViewById(R.id.username_cadastro);
        cadastrarButton = findViewById(R.id.button_cadastrar);

        cadastrarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nome = nomeEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String senha = senhaEditText.getText().toString();
                String username = usernameEditText.getText().toString();

                cadastroUsuario(email, senha, nome, username);
            }
        });

        findViewById(R.id.button_fechar_cadastro).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void cadastroUsuario(String email, String senha, final String nome, final String username) {

        FirebaseAuth autenticacao = FirebaseAuth.getInstance();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("usuarios").child(username).get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                Toast.makeText(CadastroActivity.this, "Username já existe!", Toast.LENGTH_SHORT).show();
            } else {
                autenticacao.createUserWithEmailAndPassword(email, senha).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        String id = authResult.getUser().getUid();

                        databaseReference.child("usuarios").child(id).child("nome").setValue(nome);
                        databaseReference.child("usuarios").child(id).child("email").setValue(email);
                        databaseReference.child("usuarios").child(id).child("username").setValue(username);

                        Toast.makeText(CadastroActivity.this, "Sucesso: " + id, Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(CadastroActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        String erro = exception.getMessage();
                        Toast.makeText(CadastroActivity.this, "Erro: " + erro, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}
