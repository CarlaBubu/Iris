package com.example.testetcc10;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class FirebaseHelper {
    private FirebaseDatabase database;

    public FirebaseHelper() {
        database = FirebaseDatabase.getInstance();
    }

    public interface OnPerfilAtualizadoListener {
        void onPerfilAtualizado(boolean sucesso);
    }

    public void atualizarPerfil(Usuario usuario, String novaSenha, OnPerfilAtualizadoListener listener) {
        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String, Object> dadosUsuario = new HashMap<>();

        if (usuario.getNome() != null) dadosUsuario.put("nome", usuario.getNome());
        if (usuario.getUsername() != null) dadosUsuario.put("username", usuario.getUsername());
        if (usuario.getDescricao() != null) dadosUsuario.put("descricao", usuario.getDescricao());
        if (usuario.getLeituraAtual() != null) dadosUsuario.put("leituraAtual", usuario.getLeituraAtual());
        if (usuario.getImageUrl() != null) dadosUsuario.put("imageUrl", usuario.getImageUrl());

        database.getReference().child("usuarios").child(id).updateChildren(dadosUsuario)
                .addOnSuccessListener(aVoid -> {
                    if (novaSenha != null && !novaSenha.isEmpty()) {
                        FirebaseAuth.getInstance().getCurrentUser().updatePassword(novaSenha)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        listener.onPerfilAtualizado(true);
                                    } else {
                                        listener.onPerfilAtualizado(false);
                                    }
                                });
                    } else {
                        listener.onPerfilAtualizado(true);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseHelper", "Erro ao atualizar perfil: ", e);
                    listener.onPerfilAtualizado(false);
                });
    }


}