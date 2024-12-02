package com.example.testetcc10;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.AppCompatButton;
import android.widget.TextView;

import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class SalaAdapter extends RecyclerView.Adapter<SalaAdapter.SalaViewHolder> {
    private final List<Sala> salaList;
    private final Context context;

    public SalaAdapter(Context context, List<Sala> salaList) {
        this.context = context;
        this.salaList = salaList;
    }

    @NonNull
    @Override
    public SalaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sala, parent, false);
        return new SalaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SalaViewHolder holder, int position) {
        Sala sala = salaList.get(position);
        holder.nomeSala.setText(sala.getNomeLivro());
        holder.tipoSala.setText(sala.isPrivada() ? "Privada" : "Pública");

        holder.btEntrarSala.setOnClickListener(v-> {

            entrarNaSala(sala);
            
        });

        if (sala.isPrivada()) {
            holder.senhaEntrar.setVisibility(View.VISIBLE);
            holder.senhaEntrar.setEnabled(true);
        } else {
            holder.senhaEntrar.setVisibility(View.GONE);
            holder.senhaEntrar.setEnabled(false);
        }
    }

    private void verificarSenha(Sala sala, String senhaInput, SalaViewHolder holder) {
        FirebaseDatabase.getInstance().getReference().child("salas").child(sala.getId())
                .child("senha").get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String senhaFirebase = task.getResult().getValue(String.class);

                        if (senhaFirebase != null && senhaFirebase.equals(senhaInput)) {
                            entrarNaSala(sala);
                        } else {
                            Toast.makeText(context, "Senha incorreta.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Erro ao verificar senha.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void entrarNaSala(Sala sala) {
        Intent intent = new Intent(context, LeituraActivity.class);

        intent.putExtra("id", sala.getId());

        intent.putExtra("pdfUrl", sala.getPdfUrl());
        intent.putExtra("nomeLivro", sala.getNomeLivro());
        intent.putExtra("proprietario", sala.getProprietario());
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return salaList.size();
    }

    public static class SalaViewHolder extends RecyclerView.ViewHolder {
        TextView nomeSala, tipoSala;
        EditText senhaEntrar;
        Button btEntrarSala;

        public SalaViewHolder(View itemView) {
            super(itemView);
            nomeSala = itemView.findViewById(R.id.nome_sala);
            tipoSala = itemView.findViewById(R.id.tipo_sala);
            senhaEntrar = itemView.findViewById(R.id.senhaEntrar);

            btEntrarSala = itemView.findViewById(R.id.button_entrar_na_sala);
        }
    }
}

