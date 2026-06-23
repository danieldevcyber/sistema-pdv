package com.pdv.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    // ─── Configuração da conexão ────────────────────────────────────────────
    private static final String HOST   = "localhost";
    private static final String PORT   = "5432";
    private static final String DB     = "pdv_db";
    private static final String USER   = "seu_usuario";
    private static final String PASS   = "sua_senha";

    private static final String URL =
            "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DB;

    // ─── Singleton ──────────────────────────────────────────────────────────
    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        initDatabase();
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASS);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao conectar ao banco de dados: " + e.getMessage(), e);
        }
        return connection;
    }

    // ─── Inicialização ──────────────────────────────────────────────────────
    private void initDatabase() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASS);
            criarTabelas();
            inserirDadosIniciais();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inicializar banco de dados: " + e.getMessage(), e);
        }
    }

    private void criarTabelas() throws SQLException {
        // SERIAL   = auto-increment do PostgreSQL (substitui AUTOINCREMENT do SQLite)
        // NUMERIC  = tipo preciso para dinheiro (substitui REAL)
        // TIMESTAMPTZ = timestamp com fuso horário
        String sqlProdutos = """
            CREATE TABLE IF NOT EXISTS produtos (
                id        SERIAL       PRIMARY KEY,
                codigo    VARCHAR(20)  NOT NULL UNIQUE,
                nome      VARCHAR(150) NOT NULL,
                categoria VARCHAR(80)  NOT NULL,
                preco     NUMERIC(10,2) NOT NULL,
                estoque   INTEGER      NOT NULL DEFAULT 0
            )
            """;

        String sqlVendas = """
            CREATE TABLE IF NOT EXISTS vendas (
                id              SERIAL        PRIMARY KEY,
                data_hora       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                forma_pagamento VARCHAR(30)   NOT NULL,
                desconto        NUMERIC(10,2) NOT NULL DEFAULT 0,
                total           NUMERIC(10,2) NOT NULL,
                valor_pago      NUMERIC(10,2) NOT NULL
            )
            """;

        String sqlItensVenda = """
            CREATE TABLE IF NOT EXISTS itens_venda (
                id             SERIAL        PRIMARY KEY,
                venda_id       INTEGER       NOT NULL REFERENCES vendas(id),
                produto_id     INTEGER       NOT NULL REFERENCES produtos(id),
                quantidade     INTEGER       NOT NULL,
                preco_unitario NUMERIC(10,2) NOT NULL
            )
            """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sqlProdutos);
            stmt.execute(sqlVendas);
            stmt.execute(sqlItensVenda);
        }
    }

    private void inserirDadosIniciais() throws SQLException {
        var rs = connection.createStatement()
                           .executeQuery("SELECT COUNT(*) FROM produtos");
        rs.next();
        if (rs.getInt(1) > 0) return;

        String sql = """
            INSERT INTO produtos (codigo, nome, categoria, preco, estoque) VALUES
            ('001', 'Coca-Cola 2L',          'Bebidas',     8.99,  50),
            ('002', 'Pepsi 2L',              'Bebidas',     7.99,  30),
            ('003', 'Água Mineral 500ml',    'Bebidas',     2.50, 100),
            ('004', 'Suco de Laranja 1L',    'Bebidas',     6.50,  40),
            ('005', 'Pão de Forma',          'Padaria',     5.99,  25),
            ('006', 'Leite Integral 1L',     'Laticínios',  4.99,  60),
            ('007', 'Arroz Branco 5kg',      'Grãos',      24.90,  20),
            ('008', 'Feijão Preto 1kg',      'Grãos',       8.50,  35),
            ('009', 'Óleo de Soja 900ml',    'Condimentos', 6.99,  45),
            ('010', 'Macarrão Espaguete 500g','Massas',     3.99,  80),
            ('011', 'Molho de Tomate 340g',  'Condimentos', 3.50,  60),
            ('012', 'Café Torrado 500g',     'Bebidas',    14.99,  30),
            ('013', 'Açúcar Refinado 1kg',   'Condimentos', 4.50,  50),
            ('014', 'Biscoito Recheado 140g','Snacks',      3.99,  70),
            ('015', 'Chocolate 90g',         'Snacks',      4.99,  55)
            """;

        connection.createStatement().execute(sql);
    }
}
