package com.pdv.dao;

import com.pdv.model.ItemVenda;
import com.pdv.model.Produto;
import com.pdv.model.Venda;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VendaDAO {

    private final Connection conn;
    private final ProdutoDAO produtoDAO;

    public VendaDAO() {
        this.conn = DatabaseConnection.getInstance().getConnection();
        this.produtoDAO = new ProdutoDAO();
    }

    public void salvar(Venda venda) {
        String sqlVenda = """
            INSERT INTO vendas (data_hora, forma_pagamento, desconto, total, valor_pago)
            VALUES (?, ?, ?, ?, ?)
            """;

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sqlVenda, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setTimestamp(1, Timestamp.valueOf(venda.getDataHora()));
                stmt.setString(2, venda.getFormaPagamento().name());
                stmt.setBigDecimal(3, venda.getDesconto());
                stmt.setBigDecimal(4, venda.getTotal());
                stmt.setBigDecimal(5, venda.getValorPago());
                stmt.executeUpdate();

                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) venda.setId(keys.getInt(1));
                }
            }

            salvarItens(venda);
            atualizarEstoques(venda);

            conn.commit();
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            throw new RuntimeException("Erro ao registrar venda: " + e.getMessage(), e);
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private void salvarItens(Venda venda) throws SQLException {
        String sql = """
            INSERT INTO itens_venda (venda_id, produto_id, quantidade, preco_unitario)
            VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (ItemVenda item : venda.getItens()) {
                stmt.setInt(1, venda.getId());
                stmt.setInt(2, item.getProduto().getId());
                stmt.setInt(3, item.getQuantidade());
                stmt.setBigDecimal(4, item.getPrecoUnitario());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void atualizarEstoques(Venda venda) throws SQLException {
        String sql = "UPDATE produtos SET estoque = estoque - ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (ItemVenda item : venda.getItens()) {
                stmt.setInt(1, item.getQuantidade());
                stmt.setInt(2, item.getProduto().getId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public List<Venda> listarUltimas(int limite) {
        List<Venda> vendas = new ArrayList<>();
        String sql = "SELECT * FROM vendas ORDER BY data_hora DESC LIMIT ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limite);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    vendas.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar vendas: " + e.getMessage(), e);
        }
        return vendas;
    }

    public BigDecimal totalVendasHoje() {
        // PostgreSQL: CURRENT_DATE e cast de TIMESTAMPTZ para DATE
        String sql = """
            SELECT COALESCE(SUM(total), 0) FROM vendas
            WHERE data_hora::date = CURRENT_DATE
            """;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao calcular total do dia: " + e.getMessage(), e);
        }
        return BigDecimal.ZERO;
    }

    public int quantidadeVendasHoje() {
        // PostgreSQL: mesma sintaxe de cast
        String sql = "SELECT COUNT(*) FROM vendas WHERE data_hora::date = CURRENT_DATE";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar vendas: " + e.getMessage(), e);
        }
        return 0;
    }

    private Venda mapear(ResultSet rs) throws SQLException {
        Venda v = new Venda();
        v.setId(rs.getInt("id"));
        // PostgreSQL retorna TIMESTAMPTZ como Timestamp — usar getTimestamp() em vez de getString()
        v.setDataHora(rs.getTimestamp("data_hora").toLocalDateTime());
        v.setFormaPagamento(Venda.FormaPagamento.valueOf(rs.getString("forma_pagamento")));
        v.setDesconto(rs.getBigDecimal("desconto"));
        v.setValorPago(rs.getBigDecimal("valor_pago"));
        return v;
    }
}
