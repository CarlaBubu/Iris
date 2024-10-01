package com.example.testetcc10;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText senhaEditText;
    private Button buttonAcessar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        emailEditText = findViewById(R.id.email_login);
        senhaEditText = findViewById(R.id.senha_login);
        buttonAcessar = findViewById(R.id.button_acessar);

        buttonAcessar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String senha = senhaEditText.getText().toString();

                loginUsuario(email, senha);
            }
        });

        Button buttonIrCadastro = findViewById(R.id.button_novo_cadastro);
        buttonIrCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), CadastroActivity.class));
            }
        });

        Button buttonEsqueciSenha = findViewById(R.id.button_fechar_salas);;
        buttonEsqueciSenha.setOnClickListener(new View.OnClickListener() {

            public void onClick(View e){
                startActivity(new Intent(getApplicationContext(), EsqueciSenhaActivity.class));
            }

        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loginUsuario(String email, String senha) {
        FirebaseAuth autenticacao = FirebaseAuth.getInstance();
        autenticacao.signInWithEmailAndPassword(email, senha).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                String email = authResult.getUser().getEmail();
                String id = authResult.getUser().getUid();
                Toast.makeText(MainActivity.this, "Login realizado com sucesso!", Toast.LENGTH_LONG).show();
                startActivity(new Intent(getApplicationContext(), SalasActivity.class));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                String erro = exception.getMessage();
                Toast.makeText(MainActivity.this, "Erro ao realizar login: " + erro, Toast.LENGTH_LONG).show();
            }
        });
    }
}