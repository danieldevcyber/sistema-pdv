package com.pdv.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Venda {

    public enum FormaPagamento {
        DINHEIRO("Dinheiro"),
        CARTAO_CREDITO("Cartão de Crédito"),
        CARTAO_DEBITO("Cartão de Débito"),
        PIX("PIX");

        private final String descricao;
        FormaPagamento(String descricao) { this.descricao = descricao; }

        @Override
        public String toString() { return descricao; }
    }

    private int id;
    private LocalDateTime dataHora;
    private List<ItemVenda> itens;
    private FormaPagamento formaPagamento;
    private BigDecimal desconto;
    private BigDecimal valorPago;

    public Venda() {
        this.itens = new ArrayList<>();
        this.dataHora = LocalDateTime.now();
        this.desconto = BigDecimal.ZERO;
        this.valorPago = BigDecimal.ZERO;
    }

    public BigDecimal getSubtotal() {
        return itens.stream()
                .map(ItemVenda::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotal() {
        return getSubtotal().subtract(desconto);
    }

    public BigDecimal getTroco() {
        BigDecimal troco = valorPago.subtract(getTotal());
        return troco.compareTo(BigDecimal.ZERO) > 0 ? troco : BigDecimal.ZERO;
    }

    public void adicionarItem(ItemVenda item) {
        // Se o produto já existe, incrementa a quantidade
        for (ItemVenda existente : itens) {
            if (existente.getProduto().getId() == item.getProduto().getId()) {
                existente.setQuantidade(existente.getQuantidade() + item.getQuantidade());
                return;
            }
        }
        itens.add(item);
    }

    public void removerItem(ItemVenda item) {
        itens.remove(item);
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public List<ItemVenda> getItens() { return itens; }

    public FormaPagamento getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(FormaPagamento formaPagamento) { this.formaPagamento = formaPagamento; }

    public BigDecimal getDesconto() { return desconto; }
    public void setDesconto(BigDecimal desconto) { this.desconto = desconto; }

    public BigDecimal getValorPago() { return valorPago; }
    public void setValorPago(BigDecimal valorPago) { this.valorPago = valorPago; }
}
