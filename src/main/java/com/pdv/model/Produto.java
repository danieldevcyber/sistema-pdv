package com.pdv.model;

import java.math.BigDecimal;

public class Produto {

    private int id;
    private String codigo;
    private String nome;
    private String categoria;
    private BigDecimal preco;
    private int estoque;

    public Produto() {}

    public Produto(String codigo, String nome, String categoria, BigDecimal preco, int estoque) {
        this.codigo = codigo;
        this.nome = nome;
        this.categoria = categoria;
        this.preco = preco;
        this.estoque = estoque;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }

    public int getEstoque() { return estoque; }
    public void setEstoque(int estoque) { this.estoque = estoque; }

    @Override
    public String toString() {
        return nome;
    }
}
