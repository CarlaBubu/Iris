package com.example.testetcc10;

public class Usuario {

    private String nome;
    private String username;
    private String descricao;
    private String leituraAtual;
    private String senha;
    private String imageUrl;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getLeituraAtual() {
        return leituraAtual;
    }

    public void setLeituraAtual(String leituraAtual) {
        this.leituraAtual = leituraAtual;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl() {
        this.imageUrl = imageUrl;
    }

    public boolean isValid() {
        return nome != null && !nome.isEmpty() &&
                username != null && !username.isEmpty() &&
                descricao != null &&
                leituraAtual != null &&
                senha != null && !senha.isEmpty();
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "nome='" + nome + '\'' +
                ", username='" + username + '\'' +
                ", descricao='" + descricao + '\'' +
                ", leituraAtual='" + leituraAtual + '\'' +
                ", senha='" + senha + '\'' +
                '}';
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
