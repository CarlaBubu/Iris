package com.example.testetcc10;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class SalasActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SalaAdapter salaAdapter;
    private List<Sala> salaList;
    private DatabaseReference salasRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_salas);

        recyclerView = findViewById(R.id.recycler_view_salas);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        salaList = new ArrayList<>();
        salaAdapter = new SalaAdapter(this, salaList);
        recyclerView.setAdapter(salaAdapter);

        findViewById(R.id.button_fechar_salas).setOnClickListener(v -> finish());

        Button buttonNovaSala = findViewById(R.id.button_nova_sala);
        buttonNovaSala.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), NovaSalaActivity.class)));

        Button buttonPerfilSala = findViewById(R.id.button_perfil_salas);
        buttonPerfilSala.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), PerfilActivity.class)));

        FirebaseDatabase firebase = FirebaseDatabase.getInstance();
        salasRef = firebase.getReference().child("salas");

        carregarSalas();

    }

    private void carregarSalas() {

        salasRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                salaList.clear();

                if (dataSnapshot.exists()) {

                    List<Sala> temp = new ArrayList<>();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        Sala sala = snapshot.getValue(Sala.class);

                        sala.setId(snapshot.getKey());

                        if (sala != null) {
                            temp.add(sala);
                        }
                    }

                    for(Sala sala: temp){
                        if(sala.getProprietario().equals(UserSession.nome))
                        {
                            salaList.add(sala);
                        }
                        else
                        {
                            if(!sala.isPrivada())
                                salaList.add(sala);
                        }
                    }

                    salaAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SalasActivity.this, " " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onSalaClick(Sala sala) {
        if (sala.isPrivada()) {
            showSenhaDialog(sala);
        } else {
            entrarNaSala(sala);
        }
    }

    private void showSenhaDialog(Sala sala) {
        EditText inputSenha = new EditText(SalasActivity.this);
        inputSenha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        new AlertDialog.Builder(SalasActivity.this)
                .setTitle("Digite a senha da sala")
                .setView(inputSenha)
                .setPositiveButton("Entrar", (dialog, which) -> {
                    String senhaInput = inputSenha.getText().toString();
                    if (sala.verificarSenha(senhaInput)) {
                        entrarNaSala(sala);
                    } else {
                        Toast.makeText(SalasActivity.this, "Senha incorreta.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void entrarNaSala(Sala sala) {
        Intent intent = new Intent(SalasActivity.this, LeituraActivity.class);
        intent.putExtra("linkSala", sala.getLink());
        startActivity(intent);
    }
}
