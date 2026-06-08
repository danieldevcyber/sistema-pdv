package com.pdv.dao;

import com.pdv.model.Produto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProdutoDAO {

    private final Connection conn;

    public ProdutoDAO() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    public List<Produto> listarTodos() {
        List<Produto> produtos = new ArrayList<>();
        String sql = "SELECT * FROM produtos ORDER BY nome";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                produtos.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar produtos: " + e.getMessage(), e);
        }
        return produtos;
    }

    public List<Produto> buscarPorNomeOuCodigo(String termo) {
        List<Produto> produtos = new ArrayList<>();
        String sql = "SELECT * FROM produtos WHERE LOWER(nome) LIKE ? OR codigo LIKE ? ORDER BY nome";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            String like = "%" + termo.toLowerCase() + "%";
            stmt.setString(1, like);
            stmt.setString(2, like);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    produtos.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar produtos: " + e.getMessage(), e);
        }
        return produtos;
    }

    public Optional<Produto> buscarPorCodigo(String codigo) {
        String sql = "SELECT * FROM produtos WHERE codigo = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar produto: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<Produto> buscarPorId(int id) {
        String sql = "SELECT * FROM produtos WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar produto: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public void salvar(Produto produto) {
        String sql = "INSERT INTO produtos (codigo, nome, categoria, preco, estoque) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, produto.getCodigo());
            stmt.setString(2, produto.getNome());
            stmt.setString(3, produto.getCategoria());
            stmt.setBigDecimal(4, produto.getPreco());
            stmt.setInt(5, produto.getEstoque());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) produto.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar produto: " + e.getMessage(), e);
        }
    }

    public void atualizar(Produto produto) {
        String sql = "UPDATE produtos SET codigo=?, nome=?, categoria=?, preco=?, estoque=? WHERE id=?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, produto.getCodigo());
            stmt.setString(2, produto.getNome());
            stmt.setString(3, produto.getCategoria());
            stmt.setBigDecimal(4, produto.getPreco());
            stmt.setInt(5, produto.getEstoque());
            stmt.setInt(6, produto.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar produto: " + e.getMessage(), e);
        }
    }

    public void atualizarEstoque(int produtoId, int novoEstoque) {
        String sql = "UPDATE produtos SET estoque = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, novoEstoque);
            stmt.setInt(2, produtoId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar estoque: " + e.getMessage(), e);
        }
    }

    public void deletar(int id) {
        String sql = "DELETE FROM produtos WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar produto: " + e.getMessage(), e);
        }
    }

    private Produto mapear(ResultSet rs) throws SQLException {
        Produto p = new Produto();
        p.setId(rs.getInt("id"));
        p.setCodigo(rs.getString("codigo"));
        p.setNome(rs.getString("nome"));
        p.setCategoria(rs.getString("categoria"));
        p.setPreco(rs.getBigDecimal("preco"));
        p.setEstoque(rs.getInt("estoque"));
        return p;
    }
}
