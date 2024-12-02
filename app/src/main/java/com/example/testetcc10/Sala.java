package com.example.testetcc10;

public class Sala {

    private String id;
    private String nomeLivro;
    private boolean isPrivada;
    private String senhaSala;
    private String pdfUrl;
    private String link;
    private int paginaAtual = 1;
    private String proprietario;

    public Sala() {
    }

    public Sala(String nomeLivro, boolean isPrivada, String senhaSala, String pdfUrl, String link) {
        this.nomeLivro = nomeLivro;
        this.isPrivada = isPrivada;
        this.senhaSala = senhaSala;
        this.pdfUrl = pdfUrl;
        this.link = link;
    }

    public Sala(String nomeLivro, boolean isPrivada, String senhaSala, String pdfUrl) {
        this.nomeLivro = nomeLivro;
        this.isPrivada = isPrivada;
        this.senhaSala = senhaSala;
        this.pdfUrl = pdfUrl;
    }

    public boolean verificarSenha(String senha) {
        return this.senhaSala != null && this.senhaSala.equals(senha);
    }

    public String getProprietario() {
        return proprietario;
    }

    public void setProprietario(String proprietario) {
        this.proprietario = proprietario;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNomeLivro() {
        return nomeLivro;
    }

    public void setNomeLivro(String nomeLivro) {
        this.nomeLivro = nomeLivro;
    }

    public boolean isPrivada() {
        return isPrivada;
    }

    public void setPrivada(boolean isPrivada) {
        this.isPrivada = isPrivada;
    }

    public String getSenhaSala() {
        return senhaSala;
    }

    public void setSenhaSala(String senhaSala) {
        this.senhaSala = senhaSala;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public int getPaginaAtual() {
        return paginaAtual;
    }

    public void setPaginaAtual(int paginaAtual) {
        this.paginaAtual = paginaAtual;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
