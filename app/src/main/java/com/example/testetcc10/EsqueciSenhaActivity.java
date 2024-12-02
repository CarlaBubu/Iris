package com.example.testetcc10;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class EsqueciSenhaActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText senhaNovaEditText;
    private EditText confirmarSenhaNovaEditText;
    private Button recuperarButton;
    private Button voltarButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_esqueci_senha);

        usernameEditText = findViewById(R.id.editTextRecuperar);
        emailEditText = findViewById(R.id.editEmailAddressRecuperar);
        senhaNovaEditText = findViewById(R.id.TextPassword_senhaNova);
        confirmarSenhaNovaEditText = findViewById(R.id.TextPassword_confirmar);
        recuperarButton = findViewById(R.id.button_recuperar);
        voltarButton = findViewById(R.id.button_voltarRecuperar);

        recuperarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String senhaNova = senhaNovaEditText.getText().toString();
                String confirmarSenhaNova = confirmarSenhaNovaEditText.getText().toString();

                if (senhaNova.equals(confirmarSenhaNova)) {
                    recuperarSenha(email, senhaNova);
                } else {
                    Toast.makeText(EsqueciSenhaActivity.this, "Senhas não conferem", Toast.LENGTH_SHORT).show();
                }
            }
        });

        voltarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void recuperarSenha(String email, String senhaNova) {
        FirebaseAuth autenticacao = FirebaseAuth.getInstance();
        autenticacao.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(EsqueciSenhaActivity.this, "E-mail de recuperação enviado com sucesso", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EsqueciSenhaActivity.this, "Erro ao enviar e-mail de recuperação", Toast.LENGTH_SHORT).show();
            }
        });
    }
}