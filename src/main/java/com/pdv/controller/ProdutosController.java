package com.pdv.controller;

import com.pdv.dao.ProdutoDAO;
import com.pdv.model.Produto;
import com.pdv.util.FormatUtil;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

public class ProdutosController implements Initializable {

    @FXML private TextField txtBusca;
    @FXML private TableView<Produto> tabelaProdutos;
    @FXML private TableColumn<Produto, Integer> colId;
    @FXML private TableColumn<Produto, String> colCodigo;
    @FXML private TableColumn<Produto, String> colNome;
    @FXML private TableColumn<Produto, String> colCategoria;
    @FXML private TableColumn<Produto, String> colPreco;
    @FXML private TableColumn<Produto, Integer> colEstoque;

    // Formulário
    @FXML private TextField txtCodigo;
    @FXML private TextField txtNome;
    @FXML private TextField txtCategoria;
    @FXML private TextField txtPreco;
    @FXML private TextField txtEstoque;
    @FXML private Button btnSalvar;
    @FXML private Button btnNovo;

    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final ObservableList<Produto> produtos = FXCollections.observableArrayList();
    private Produto produtoEditando = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTabela();
        carregarProdutos();

        tabelaProdutos.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, novo) -> { if (novo != null) preencherFormulario(novo); });
    }

    private void configurarTabela() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPreco.setCellValueFactory(data ->
                new SimpleStringProperty(FormatUtil.formatarMoeda(data.getValue().getPreco())));
        colEstoque.setCellValueFactory(new PropertyValueFactory<>("estoque"));
        tabelaProdutos.setItems(produtos);
    }

    private void carregarProdutos() {
        produtos.setAll(produtoDAO.listarTodos());
    }

    @FXML
    private void buscar() {
        String termo = txtBusca.getText().trim();
        if (termo.isEmpty()) {
            carregarProdutos();
        } else {
            produtos.setAll(produtoDAO.buscarPorNomeOuCodigo(termo));
        }
    }

    @FXML
    private void novoRegistro() {
        produtoEditando = null;
        limparFormulario();
        txtCodigo.requestFocus();
    }

    @FXML
    private void salvar() {
        if (!validarFormulario()) return;

        BigDecimal preco;
        try {
            preco = new BigDecimal(txtPreco.getText().replace(",", "."));
        } catch (NumberFormatException e) {
            mostrarAlerta("Preço inválido.", Alert.AlertType.WARNING);
            return;
        }

        int estoque;
        try {
            estoque = Integer.parseInt(txtEstoque.getText());
        } catch (NumberFormatException e) {
            mostrarAlerta("Estoque inválido.", Alert.AlertType.WARNING);
            return;
        }

        if (produtoEditando == null) {
            Produto novo = new Produto(
                    txtCodigo.getText().trim(),
                    txtNome.getText().trim(),
                    txtCategoria.getText().trim(),
                    preco, estoque
            );
            produtoDAO.salvar(novo);
        } else {
            produtoEditando.setCodigo(txtCodigo.getText().trim());
            produtoEditando.setNome(txtNome.getText().trim());
            produtoEditando.setCategoria(txtCategoria.getText().trim());
            produtoEditando.setPreco(preco);
            produtoEditando.setEstoque(estoque);
            produtoDAO.atualizar(produtoEditando);
        }

        carregarProdutos();
        limparFormulario();
        mostrarAlerta("Produto salvo com sucesso!", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void deletar() {
        Produto selecionado = tabelaProdutos.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarAlerta("Selecione um produto.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Excluir produto: " + selecionado.getNome() + "?");
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                produtoDAO.deletar(selecionado.getId());
                carregarProdutos();
                limparFormulario();
            }
        });
    }

    private void preencherFormulario(Produto p) {
        produtoEditando = p;
        txtCodigo.setText(p.getCodigo());
        txtNome.setText(p.getNome());
        txtCategoria.setText(p.getCategoria());
        txtPreco.setText(p.getPreco().toPlainString());
        txtEstoque.setText(String.valueOf(p.getEstoque()));
    }

    private void limparFormulario() {
        produtoEditando = null;
        txtCodigo.clear();
        txtNome.clear();
        txtCategoria.clear();
        txtPreco.clear();
        txtEstoque.clear();
        tabelaProdutos.getSelectionModel().clearSelection();
    }

    private boolean validarFormulario() {
        if (txtCodigo.getText().isBlank() || txtNome.getText().isBlank()
                || txtCategoria.getText().isBlank() || txtPreco.getText().isBlank()
                || txtEstoque.getText().isBlank()) {
            mostrarAlerta("Preencha todos os campos.", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private void mostrarAlerta(String msg, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
