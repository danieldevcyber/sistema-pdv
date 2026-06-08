package com.pdv.controller;

import com.pdv.dao.VendaDAO;
import com.pdv.model.Venda;
import com.pdv.util.FormatUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class HistoricoController implements Initializable {

    @FXML private TableView<Venda> tabelaVendas;
    @FXML private TableColumn<Venda, Integer> colId;
    @FXML private TableColumn<Venda, String> colDataHora;
    @FXML private TableColumn<Venda, String> colForma;
    @FXML private TableColumn<Venda, String> colDesconto;
    @FXML private TableColumn<Venda, String> colTotal;

    private final VendaDAO vendaDAO = new VendaDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTabela();
        carregarVendas();
    }

    private void configurarTabela() {
        colId.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(data.getValue().getId()).asObject());
        colDataHora.setCellValueFactory(data ->
                new SimpleStringProperty(FormatUtil.formatarDataHora(data.getValue().getDataHora())));
        colForma.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getFormaPagamento().toString()));
        colDesconto.setCellValueFactory(data ->
                new SimpleStringProperty(FormatUtil.formatarMoeda(data.getValue().getDesconto())));
        colTotal.setCellValueFactory(data ->
                new SimpleStringProperty(FormatUtil.formatarMoeda(data.getValue().getTotal())));
    }

    private void carregarVendas() {
        tabelaVendas.setItems(FXCollections.observableArrayList(vendaDAO.listarUltimas(100)));
    }
}
