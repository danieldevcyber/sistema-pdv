package com.pdv.controller;

import com.pdv.dao.ProdutoDAO;
import com.pdv.dao.VendaDAO;
import com.pdv.model.ItemVenda;
import com.pdv.model.Produto;
import com.pdv.model.Venda;
import com.pdv.util.FormatUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class PDVController implements Initializable {

    // Barra de busca
    @FXML private TextField txtBusca;
    @FXML private TextField txtQuantidade;

    // Tabela de itens da venda
    @FXML private TableView<ItemVenda> tabelaItens;
    @FXML private TableColumn<ItemVenda, String> colCodigo;
    @FXML private TableColumn<ItemVenda, String> colNome;
    @FXML private TableColumn<ItemVenda, Integer> colQtd;
    @FXML private TableColumn<ItemVenda, String> colPrecoUnit;
    @FXML private TableColumn<ItemVenda, String> colSubtotal;

    // Totais
    @FXML private Label lblSubtotal;
    @FXML private Label lblDesconto;
    @FXML private Label lblTotal;

    // Painel de pagamento
    @FXML private ComboBox<Venda.FormaPagamento> cmbFormaPagamento;
    @FXML private TextField txtDesconto;
    @FXML private TextField txtValorPago;
    @FXML private Label lblTroco;

    // Dashboard
    @FXML private Label lblVendasHoje;
    @FXML private Label lblTotalDia;

    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final VendaDAO vendaDAO = new VendaDAO();

    private Venda vendaAtual = new Venda();
    private final ObservableList<ItemVenda> itensObservable = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTabela();
        configurarFormaPagamento();
        configurarCampos();
        atualizarDashboard();
        novaVenda();
    }

    private void configurarTabela() {
        colCodigo.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCodigoProduto()));
        colNome.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNomeProduto()));
        colQtd.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        colPrecoUnit.setCellValueFactory(data ->
                new SimpleStringProperty(FormatUtil.formatarMoeda(data.getValue().getPrecoUnitario())));
        colSubtotal.setCellValueFactory(data ->
                new SimpleStringProperty(FormatUtil.formatarMoeda(data.getValue().getSubtotal())));

        tabelaItens.setItems(itensObservable);

        // Permite remover item com Delete
        tabelaItens.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DELETE) {
                removerItemSelecionado();
            }
        });
    }

    private void configurarFormaPagamento() {
        cmbFormaPagamento.setItems(FXCollections.observableArrayList(Venda.FormaPagamento.values()));
        cmbFormaPagamento.getSelectionModel().selectFirst();
    }

    private void configurarCampos() {
        // Enter na busca adiciona produto
        txtBusca.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) adicionarProduto();
        });

        // Atualiza troco ao digitar valor pago
        txtValorPago.setOnKeyReleased(e -> calcularTroco());

        // Atualiza totais ao mudar desconto
        txtDesconto.setOnKeyReleased(e -> atualizarTotais());
    }

    @FXML
    private void adicionarProduto() {
        String busca = txtBusca.getText().trim();
        if (busca.isEmpty()) return;

        // Tenta encontrar por código exato primeiro
        Optional<Produto> opt = produtoDAO.buscarPorCodigo(busca);

        if (opt.isEmpty()) {
            // Busca por nome
            var lista = produtoDAO.buscarPorNomeOuCodigo(busca);
            if (lista.isEmpty()) {
                mostrarAlerta("Produto não encontrado: " + busca, Alert.AlertType.WARNING);
                return;
            } else if (lista.size() == 1) {
                opt = Optional.of(lista.get(0));
            } else {
                opt = selecionarProduto(lista);
            }
        }

        opt.ifPresent(produto -> {
            int qtd = parseQuantidade();
            if (qtd <= 0) {
                mostrarAlerta("Quantidade inválida.", Alert.AlertType.WARNING);
                return;
            }
            if (produto.getEstoque() < qtd) {
                mostrarAlerta("Estoque insuficiente! Disponível: " + produto.getEstoque(), Alert.AlertType.WARNING);
                return;
            }

            vendaAtual.adicionarItem(new ItemVenda(produto, qtd));
            atualizarTabela();
        });

        txtBusca.clear();
        txtQuantidade.setText("1");
        txtBusca.requestFocus();
    }

    private Optional<Produto> selecionarProduto(java.util.List<Produto> lista) {
        ChoiceDialog<Produto> dialog = new ChoiceDialog<>(lista.get(0), lista);
        dialog.setTitle("Selecionar Produto");
        dialog.setHeaderText("Múltiplos produtos encontrados");
        dialog.setContentText("Escolha o produto:");
        return dialog.showAndWait();
    }

    @FXML
    private void removerItemSelecionado() {
        ItemVenda selecionado = tabelaItens.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            vendaAtual.removerItem(selecionado);
            atualizarTabela();
        }
    }

    @FXML
    private void finalizarVenda() {
        if (vendaAtual.getItens().isEmpty()) {
            mostrarAlerta("Adicione pelo menos um produto.", Alert.AlertType.WARNING);
            return;
        }

        BigDecimal valorPago = FormatUtil.parseMoeda(txtValorPago.getText());
        Venda.FormaPagamento forma = cmbFormaPagamento.getValue();

        if (forma == Venda.FormaPagamento.DINHEIRO && valorPago.compareTo(vendaAtual.getTotal()) < 0) {
            mostrarAlerta("Valor pago insuficiente!", Alert.AlertType.WARNING);
            return;
        }

        vendaAtual.setFormaPagamento(forma);
        vendaAtual.setDesconto(FormatUtil.parseMoeda(txtDesconto.getText()));
        vendaAtual.setValorPago(valorPago);

        // Confirmação
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Venda");
        confirm.setHeaderText("Finalizar venda?");
        confirm.setContentText("Total: " + FormatUtil.formatarMoeda(vendaAtual.getTotal())
                + "\nForma: " + forma
                + "\nTroco: " + FormatUtil.formatarMoeda(vendaAtual.getTroco()));

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                vendaDAO.salvar(vendaAtual);
                mostrarAlerta("Venda registrada com sucesso!\nTroco: "
                        + FormatUtil.formatarMoeda(vendaAtual.getTroco()), Alert.AlertType.INFORMATION);
                atualizarDashboard();
                novaVenda();
            }
        });
    }

    @FXML
    private void novaVenda() {
        vendaAtual = new Venda();
        itensObservable.clear();
        txtBusca.clear();
        txtQuantidade.setText("1");
        txtDesconto.setText("0,00");
        txtValorPago.setText("0,00");
        cmbFormaPagamento.getSelectionModel().selectFirst();
        atualizarTotais();
        txtBusca.requestFocus();
    }

    @FXML
    private void abrirCadastroProdutos() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pdv/fxml/Produtos.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Cadastro de Produtos");
            stage.setScene(new Scene(root, 900, 600));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            mostrarAlerta("Erro ao abrir cadastro: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void abrirHistoricoVendas() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pdv/fxml/Historico.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Histórico de Vendas");
            stage.setScene(new Scene(root, 800, 500));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            mostrarAlerta("Erro ao abrir histórico: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void atualizarTabela() {
        itensObservable.setAll(vendaAtual.getItens());
        atualizarTotais();
    }

    private void atualizarTotais() {
        BigDecimal desconto = FormatUtil.parseMoeda(txtDesconto.getText());
        vendaAtual.setDesconto(desconto);

        lblSubtotal.setText(FormatUtil.formatarMoeda(vendaAtual.getSubtotal()));
        lblDesconto.setText("- " + FormatUtil.formatarMoeda(desconto));
        lblTotal.setText(FormatUtil.formatarMoeda(vendaAtual.getTotal()));

        calcularTroco();
    }

    private void calcularTroco() {
        BigDecimal valorPago = FormatUtil.parseMoeda(txtValorPago.getText());
        vendaAtual.setValorPago(valorPago);
        lblTroco.setText(FormatUtil.formatarMoeda(vendaAtual.getTroco()));
    }

    private void atualizarDashboard() {
        lblVendasHoje.setText(String.valueOf(vendaDAO.quantidadeVendasHoje()));
        lblTotalDia.setText(FormatUtil.formatarMoeda(vendaDAO.totalVendasHoje()));
    }

    private int parseQuantidade() {
        try {
            return Integer.parseInt(txtQuantidade.getText().trim());
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private void mostrarAlerta(String msg, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
