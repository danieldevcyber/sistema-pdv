package com.pdv.model;

import java.math.BigDecimal;

public class ItemVenda {

    private Produto produto;
    private int quantidade;
    private BigDecimal precoUnitario;

    public ItemVenda(Produto produto, int quantidade) {
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoUnitario = produto.getPreco();
    }

    public BigDecimal getSubtotal() {
        return precoUnitario.multiply(new BigDecimal(quantidade));
    }

    // Getters e Setters
    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(BigDecimal precoUnitario) { this.precoUnitario = precoUnitario; }

    // Getters auxiliares para uso na TableView
    public String getNomeProduto() { return produto.getNome(); }
    public String getCodigoProduto() { return produto.getCodigo(); }
}
