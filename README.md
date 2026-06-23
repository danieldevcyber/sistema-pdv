# 🛒 Sistema PDV - JavaFX + MVC

Sistema de Ponto de Venda desktop desenvolvido com **JavaFX** e arquitetura **MVC**, banco de dados **PostgreSQL** embutido.

---

## 🏗️ Arquitetura MVC

```
src/main/java/com/pdv/
│
├── model/                  ← MODELO (dados e regras de negócio)
│   ├── Produto.java
│   ├── ItemVenda.java
│   └── Venda.java
│
├── view/                   ← VISÃO (arquivos FXML = telas)
│   └── (arquivos em resources/com/pdv/fxml/)
│
├── controller/             ← CONTROLADOR (lógica de UI)
│   ├── PDVController.java
│   ├── ProdutosController.java
│   └── HistoricoController.java
│[README.md](README.md)
├── dao/                    ← ACESSO A DADOS (JDBC + PostgreSQL)
│   ├── DatabaseConnection.java
│   ├── ProdutoDAO.java
│   └── VendaDAO.java
│
├── util/
│   └── FormatUtil.java     ← Formatação de moeda e datas
│
└── app/
    └── MainApp.java        ← Entry point (extends Application)

src/main/resources/com/pdv/
├── fxml/
│   ├── PDV.fxml            ← Tela principal de vendas
│   ├── Produtos.fxml       ← CRUD de produtos
│   └── Historico.fxml      ← Histórico de vendas
└── css/
    └── style.css           ← Tema escuro profissional
```

---

## ✅ Funcionalidades

### Tela PDV (Principal)
-  Busca de produto por **código** ou **nome**
-  Adiciona itens com quantidade personalizada
-  Remove item com tecla **Delete**
-  Cálculo automático de subtotal, desconto e total
-  Formas de pagamento: Dinheiro, Crédito, Débito, PIX
-  Cálculo de troco automático
-  Dashboard com vendas do dia em tempo real

### Cadastro de Produtos
- CRUD completo (criar, listar, editar, excluir)
- Busca por nome ou código
- Controle de estoque

### Histórico de Vendas
- Listagem das últimas 100 vendas
- Data/hora, forma de pagamento, desconto e total

---

## 🚀 Como Executar

### Pré-requisitos
- Java 21+ (JDK)
- Maven 3.8+
- JavaFX 21 (gerenciado pelo Maven)
- PostgresSQL +13

## 🗄️ Banco de Dados

Este projeto utiliza **PostgreSQL** como banco de dados e acesso via **JDBC + DAO Pattern**.

### Configuração

Crie um banco chamado `pdv` no PostgreSQL e ajuste as credenciais na classe:

```java
DatabaseConnection.java
```

Exemplo de conexão:

```properties
jdbc:postgresql://localhost:5432/pdv
```

### Estrutura inicial

Ao iniciar a aplicação, as tabelas são criadas automaticamente (quando configurado) e alguns produtos de exemplo podem ser inseridos para testes.

**Tecnologias utilizadas no acesso aos dados:**

* PostgreSQL
* JDBC

### Rodando o projeto
```bash
# Clonar / navegar para a pasta
cd pdv-javafx

# Compilar e executar
mvn javafx:run

# Ou gerar JAR executável
mvn clean package
```

### No IntelliJ IDEA
1. **File → Open** → selecionar a pasta `pdv-javafx`
2. Aguardar o Maven indexar as dependências
3. Clicar com botão direito em `MainApp.java` → **Run**

> O banco PostgreSQL (`pdv.db`) é criado automaticamente na raiz do projeto com 15 produtos de exemplo.

---

## 🎨 Design

Tema **dark** profissional com:
- Paleta: fundo `#1a1d23`, accent verde `#00c896`, azul `#0099ff`
- Tabelas com hover e seleção destacados
- Botões com efeito glow no hover
- Layout responsivo (mínimo 1000×600)

---

## 📐 Padrões Aplicados

| Padrão | Onde |
|--------|------|
| **MVC** | Separação FXML (View) / Controller / Model |
| **DAO** | `ProdutoDAO`, `VendaDAO` isolam SQL do Controller |
| **Singleton** | `DatabaseConnection` garante uma só conexão |
| **Transaction** | `VendaDAO.salvar()` usa commit/rollback |
| **Observer** | `ObservableList` atualiza TableView automaticamente |

---

## 🔄 Possíveis Melhorias

- [ ] Relatório PDF com JasperReports
- [ ] Autenticação de operador
- [ ] Gráficos de vendas (JavaFX Charts)
- [ ] Impressão de cupom fiscal
- [ ] Troca de tema claro/escuro
- [ ] Exportar relatório para Excel
