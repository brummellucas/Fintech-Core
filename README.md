# 🏦 Gateway de Pagamentos Simulado (Fintech Core)

![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.0-green.svg)
![Java](https://img.shields.io/badge/Java-17-blue.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

Uma API backend completa que simula o funcionamento de um gateway de pagamentos, implementando operações financeiras seguras, consistentes e transacionais com foco no core financeiro.

## 📋 Índice

- [✨ Funcionalidades](#-funcionalidades)
- [🏗️ Arquitetura](#️-arquitetura)
- [🛠️ Tecnologias](#️-tecnologias)
- [🎯 Regras de Negócio](#-regras-de-negócio)
- [🔐 Segurança](#-segurança)
- [🚀 Como Executar](#-como-executar)
- [📚 Documentação da API](#-documentação-da-api)
- [🧪 Testes e Qualidade](#-testes-e-qualidade)
- [📁 Estrutura do Projeto](#-estrutura-do-projeto)
- [🎨 Diagramas](#-diagramas)
- [🤝 Contribuição](#-contribuição)
- [📄 Licença](#-licença)

## ✨ Funcionalidades

### 🔐 Autenticação & Autorização
- Registro e login de usuários com roles (CLIENT, MERCHANT, ADMIN)
- Autenticação via JWT com expiração configurável
- Controle de acesso baseado em roles (RBAC)

### 💰 Gestão Financeira
- Cadastro automático de conta ao registrar usuário
- Consulta de saldo em tempo real
- Depósitos em conta própria
- Processamento de pagamentos entre usuários

### 💸 Processamento de Transações
- Pagamentos síncronos com validação em tempo real
- Garantia de atomicidade (ACID) em operações financeiras
- Controle de concorrência com locks otimistas/pessimistas
- Histórico completo de transações

### 📊 Monitoramento & Administração
- Dashboard administrativo para visualização de todas as transações
- Extrato personalizado por usuário
- Auditoria completa de operações

## 🏗️ Arquitetura

### 📐 Padrão Arquitetural
```
┌─────────────────┐
│   Controller    │  ← Recebe requisições HTTP
├─────────────────┤
│     Service     │  ← Regras de negócio e lógica
├─────────────────┤
│   Repository    │  ← Acesso a dados (JPA)
├─────────────────┤
│   Entity (JPA)  │  ← Modelo de domínio
└─────────────────┘
```

### 🔄 Fluxo de Pagamento
```mermaid
graph LR
    A[Request POST /payments] --> B[Controller]
    B --> C[PaymentService @Transactional]
    C --> D[Validar regras]
    D --> E[Débito na conta pagadora]
    E --> F[Crédito na conta merchant]
    F --> G[Registrar transação]
    G --> H[Commit automático]
    E -- Rollback --> I[Falha → Nada persiste]
    F -- Rollback --> I
```

## 🛠️ Tecnologias

### **Backend**
- **Java 17+** - Linguagem principal
- **Spring Boot 3.2.0** - Framework principal
- **Spring Security** - Autenticação e autorização
- **Spring Data JPA** - Persistência de dados
- **Hibernate** - ORM

### **Banco de Dados**
- **PostgreSQL 15+** - Banco de dados relacional
- **Flyway** - Migrations (opcional)

### **Segurança**
- **JWT (JSON Web Tokens)** - Autenticação stateless
- **BCrypt** - Hash de senhas
- **Spring Security RBAC** - Controle de acesso

### **Documentação & Testes**
- **Swagger/OpenAPI 3** - Documentação interativa
- **Postman** - Testes de API
- **JUnit 5** - Testes unitários

## 🎯 Regras de Negócio

### **RN01 - Tipos de Usuário**
```java
public enum Role {
    CLIENT,      // Pode pagar, não pode receber
    MERCHANT,    // Pode receber, não pode pagar
    ADMIN       // Acesso administrativo
}
```

### **RN02 - Validações Financeiras**
- ✅ **Saldo suficiente** antes de qualquer débito
- ✅ **Valores positivos** em transações
- ✅ **Impedir saldo negativo** em qualquer cenário
- ✅ **Transações atômicas** - rollback em caso de erro

### **RN03 - Estados da Transação**
```java
public enum TransactionStatus {
    PENDING,    // Em processamento
    APPROVED,   // Concluída com sucesso
    FAILED,     // Falha na operação
    CANCELLED   // Cancelada pelo sistema
}
```

### **RN04 - Regras Específicas**
- **CLIENT**: Pode pagar merchants, pode receber depósitos
- **MERCHANT**: Pode receber pagamentos, não pode enviar
- **ADMIN**: Acesso completo, pode visualizar todas as transações

## 🔐 Segurança

### **Autenticação JWT**
```yaml
app:
  security:
    jwt:
      secret: ${JWT_SECRET}
      expiration: 86400000  # 24 horas
```

### **Proteção de Endpoints**
```java
@PreAuthorize("hasRole('CLIENT')")    // Apenas clientes
@PreAuthorize("hasRole('MERCHANT')")  // Apenas merchants
@PreAuthorize("hasRole('ADMIN')")     // Apenas administradores
@PreAuthorize("hasAnyRole('CLIENT', 'MERCHANT')")  // Ambos
```

### **Criptografia**
- Senhas: BCrypt com salt automático
- Tokens: JWT com assinatura HMAC-SHA512
- Dados sensíveis: Nunca expostos em logs

## 🚀 Como Executar

### **Pré-requisitos**
```bash
# Verifique suas versões
java -version      # Java 17+
postgres --version # PostgreSQL 15+
mvn -version       # Maven 3.8+
```

### **1. Clone o repositório**
```bash
git clone https:https://github.com/brummellucas/Fintech-Core
cd payment-gateway
```

### **2. Configure o banco de dados**
```sql
-- Execute no PostgreSQL
CREATE DATABASE payment_gateway;
CREATE USER payment_user WITH PASSWORD 'secure_password';
GRANT ALL PRIVILEGES ON DATABASE payment_gateway TO payment_user;
```

### **3. Configure as variáveis de ambiente**
```bash
# Linux/Mac
export DB_PASSWORD=secure_password
export JWT_SECRET=your_super_secret_jwt_key_here

# Windows (PowerShell)
$env:DB_PASSWORD="secure_password"
$env:JWT_SECRET="your_super_secret_jwt_key_here"
```

### **4. Execute a aplicação**
```bash
# Opção 1: Via Maven
mvn spring-boot:run

# Opção 2: Build e execução
mvn clean package
java -jar target/payment-gateway-1.0.0.jar

# Opção 3: Docker (se disponível)
docker-compose up --build
```

## 📚 Documentação da API

### **Swagger UI**
Acesse a documentação interativa em:  
🌐 **http://localhost:8080/swagger-ui/index.html**

### **OpenAPI Specification**
📄 **http://localhost:8080/v3/api-docs**

### **Endpoints Principais**

#### 🔐 **Autenticação**
| Método | Endpoint | Descrição | Role |
|--------|----------|-----------|------|
| `POST` | `/api/auth/register` | Registrar novo usuário | Público |
| `POST` | `/api/auth/login` | Login e obtenção de JWT | Público |

#### 💰 **Contas**
| Método | Endpoint | Descrição | Role |
|--------|----------|-----------|------|
| `GET` | `/api/accounts/balance` | Consultar saldo | CLIENT, MERCHANT |
| `POST` | `/api/accounts/deposit` | Realizar depósito | CLIENT, MERCHANT |

#### 💸 **Pagamentos**
| Método | Endpoint | Descrição | Role |
|--------|----------|-----------|------|
| `POST` | `/api/payments` | Realizar pagamento | CLIENT |

#### 📊 **Transações**
| Método | Endpoint | Descrição | Role |
|--------|----------|-----------|------|
| `GET` | `/api/transactions` | Listar transações | CLIENT, MERCHANT |
| `GET` | `/api/admin/transactions` | Todas transações | ADMIN |

### **Exemplos de Requisições**

#### **1. Registrar Usuário**
```bash
POST /api/auth/register
Content-Type: application/json

{
  "name": "João Silva",
  "email": "joao@email.com",
  "password": "senha123",
  "role": "CLIENT"
}
```

#### **2. Login**
```bash
POST /api/auth/login
Content-Type: application/json

{
  "email": "joao@email.com",
  "password": "senha123"
}

# Resposta
{
  "token": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
  "email": "joao@email.com",
  "role": "CLIENT",
  "name": "João Silva"
}
```

#### **3. Realizar Depósito**
```bash
POST /api/accounts/deposit
Authorization: Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "amount": 1000.00,
  "description": "Depósito inicial"
}
```

#### **4. Realizar Pagamento**
```bash
POST /api/payments
Authorization: Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "merchantId": 2,
  "amount": 150.50,
  "description": "Compra de produto X"
}
```


## 📁 Estrutura do Projeto

```
payment-gateway/
├── src/main/java/com/gateway/payment/
│   ├── config/                    # Configurações Spring
│   │   ├── SecurityConfig.java    # Configuração de segurança
│   │   ├── JwtConfig.java         # Propriedades JWT
│   │   └── SwaggerConfig.java     # Documentação OpenAPI
│   │
│   ├── controller/                # Controladores REST
│   │   ├── AuthController.java
│   │   ├── PaymentController.java
│   │   ├── AccountController.java
│   │   └── TransactionController.java
│   │
│   ├── service/                   # Lógica de negócio
│   │   ├── AuthService.java
│   │   ├── PaymentService.java    # ❤️ CORAÇÃO DO SISTEMA
│   │   ├── AccountService.java
│   │   └── TransactionService.java
│   │
│   ├── repository/                # Camada de persistência
│   │   ├── UserRepository.java
│   │   ├── AccountRepository.java
│   │   └── TransactionRepository.java
│   │
│   ├── domain/                    # Domínio do negócio
│   │   ├── entity/
│   │   │   ├── User.java
│   │   │   ├── Account.java
│   │   │   └── Transaction.java
│   │   └── enums/
│   │       ├── Role.java
│   │       └── TransactionStatus.java
│   │
│   ├── dto/                       # Objetos de transferência
│   │   ├── auth/
│   │   │   ├── LoginRequest.java
│   │   │   ├── LoginResponse.java
│   │   │   └── RegisterRequest.java
│   │   ├── payment/
│   │   │   ├── PaymentRequest.java
│   │   │   └── PaymentResponse.java
│   │   ├── account/
│   │   │   ├── BalanceResponse.java
│   │   │   └── DepositRequest.java
│   │   └── transaction/
│   │       └── TransactionResponse.java
│   │
│   ├── security/                  # Segurança e JWT
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── CustomUserDetailsService.java
│   │
│   ├── exception/                 # Tratamento de erros
│   │   ├── BusinessException.java
│   │   ├── InsufficientBalanceException.java
│   │   ├── UnauthorizedException.java
│   │   └── GlobalExceptionHandler.java
│   │
│   └── GatewayApplication.java    # Classe principal
│
├── src/main/resources/
│   ├── application.yml           # Configurações da aplicação
│   ├── schema.sql               # Scripts SQL (opcional)
│   └── data.sql                 # Dados iniciais (opcional)
│
├── src/test/java/               # Testes unitários
├── pom.xml                      # Dependências Maven
├── Dockerfile                   # Containerização
├── docker-compose.yml           # Orquestração
├── README.md                    # Este arquivo
└── .gitignore
```

## 📄 Licença

Este projeto está licenciado sob a Licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

## 👨‍💻 **Desenvolvido por** **Brummel Lucas Silva da Cunha** - Desenvolvedor.

[![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/brummellucas)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/brummellucas/)

---
*Este projeto foi desenvolvido para fins educacionais e de portfólio, simulando um ambiente real de desenvolvimento backend Java.*
