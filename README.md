
# UniLivros API

API REST para a plataforma UniLivros - uma rede social acadêmica para promoção da leitura e trocas de livros entre estudantes universitários.

## 📋 Sobre o Projeto

O UniLivros é uma plataforma que conecta estudantes universitários para facilitar a troca de livros acadêmicos. A API fornece todos os endpoints necessários para gerenciar usuários, livros, propostas de troca, agendamentos, notificações e sistema de conquistas com gamificação.

## 🚀 Tecnologias Utilizadas

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **Spring Security** com JWT
- **PostgreSQL** / SQL Server
- **Flyway** (Migrations)
- **ModelMapper** 3.2.0
- **JWT (JJWT)** 0.12.3
- **ZXing** 3.5.2 (QR Code)
- **SendGrid** 4.10.2 (Envio de emails)
- **ONNX Runtime** 1.17.0 (IA para análise de nível de leitura)
- **Gson** 2.10.1

## 📁 Estrutura do Projeto

```
src/main/java/com/unilivros/
├── config/                 # Configurações
│   ├── ModelMapperConfig.java
│   └── SecurityConfig.java
├── controller/             # Controllers REST
│   ├── AuthController.java
│   ├── UsuarioController.java
│   ├── LivroController.java
│   ├── PropostaController.java
│   ├── AgendamentoController.java
│   ├── TrocaController.java
│   ├── NotificacaoController.java
│   └── ConquistaController.java
├── dto/                    # Data Transfer Objects
│   ├── AuthResponseDTO.java
│   ├── LoginDTO.java
│   ├── VerificationDTO.java
│   ├── UsuarioDTO.java
│   ├── LivroDTO.java
│   ├── PropostaDTO.java
│   ├── LivroPropostaDTO.java
│   ├── AgendamentoDTO.java
│   ├── TrocaDTO.java
│   ├── AvaliacaoDTO.java
│   ├── NotificacaoDTO.java
│   ├── NotificacaoStatusDTO.java
│   ├── ConquistaDTO.java
│   └── ApiResponseDTO.java
├── exception/              # Tratamento de Exceções
│   ├── ResourceNotFoundException.java
│   ├── BusinessException.java
│   └── GlobalExceptionHandler.java
├── model/                  # Entidades JPA
│   ├── Usuario.java
│   ├── Livro.java
│   ├── Proposta.java
│   ├── Agendamento.java
│   ├── Troca.java
│   ├── Notificacao.java
│   ├── Conquista.java
│   ├── UsuarioLivro.java
│   ├── LivroProposta.java
│   ├── ConquistaUsuario.java
│   └── TrocaUsuario.java
├── repository/             # Repositories JPA
│   ├── UsuarioRepository.java
│   ├── LivroRepository.java
│   ├── PropostaRepository.java
│   ├── LivroPropostaRepository.java
│   ├── AgendamentoRepository.java
│   ├── TrocaRepository.java
│   ├── TrocaUsuarioRepository.java
│   ├── NotificacaoRepository.java
│   ├── ConquistaRepository.java
│   └── UsuarioLivroRepository.java
├── security/               # Segurança e JWT
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   ├── UserDetailsServiceImpl.java
│   └── UserPrincipal.java
├── service/                # Services com Lógica de Negócio
│   ├── UsuarioService.java
│   ├── LivroService.java
│   ├── PropostaService.java
│   ├── AgendamentoService.java
│   ├── TrocaService.java
│   ├── NotificacaoService.java
│   ├── ConquistaService.java
│   ├── EmailService.java
│   └── IAService.java
└── UniLivrosApiApplication.java
```

## 🗄️ Modelo de Dados

### Entidades Principais

- **Usuario**: Estudantes universitários
- **Livro**: Livros acadêmicos
- **Proposta**: Propostas de troca entre usuários
- **Agendamento**: Encontros para troca
- **Troca**: Confirmação e avaliação das trocas
- **Notificacao**: Sistema de notificações
- **Conquista**: Sistema de conquistas e XP

### Relacionamentos

- **UsuarioLivro**: Usuários e seus livros
- **LivroProposta**: Livros em propostas
- **ConquistaUsuario**: Conquistas dos usuários
- **TrocaUsuario**: Participantes das trocas

## 🔧 Configuração

### 1. Banco de Dados

Configure o banco PostgreSQL no `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:${DATABASE_URL}
    username: ${DATABASE_USERNAME:postgres}
    password: ${DATABASE_PASSWORD:postgres}
```

### 2. Variáveis de Ambiente

Crie um arquivo `.env` ou configure as seguintes variáveis:

```properties
DATABASE_URL=postgresql://localhost:5432/unilivros
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres
JWT_SECRET=sua-chave-secreta-muito-segura
JWT_EXPIRATION=86400000
MAIL_PASSWORD=sua-senha-sendgrid
PORT=8088
```

### 3. Executar a Aplicação

```bash
mvn spring-boot:run
```

A API estará disponível em: `http://localhost:8088/api`

## 📚 Endpoints Principais

### Autenticação
- `POST /api/auth/register` - Registrar novo usuário
- `POST /api/auth/login` - Fazer login e receber token JWT
- `POST /api/auth/verify-email/{codigo}` - Verificar email com código
- `POST /api/auth/forgot-password` - Solicitar recuperação de senha
- `POST /api/auth/validate-reset-code` - Validar código de recuperação
- `POST /api/auth/reset-password` - Resetar senha
- `GET /api/auth/me` - Obter dados do usuário autenticado

### Usuários
- `POST /api/usuarios` - Criar usuário
- `GET /api/usuarios` - Listar todos os usuários
- `GET /api/usuarios/{id}` - Buscar usuário por ID
- `GET /api/usuarios/{id}/livros` - Livros do usuário
- `GET /api/usuarios/{id}/avaliacoes` - Avaliações do usuário
- `GET /api/usuarios/{id}/conquistas` - Conquistas do usuário
- `GET /api/usuarios/email/{email}` - Buscar por email
- `GET /api/usuarios/matricula/{matricula}` - Buscar por matrícula
- `GET /api/usuarios/curso/{curso}` - Buscar por curso
- `GET /api/usuarios/semestre/{semestre}` - Buscar por semestre
- `GET /api/usuarios/curso/{curso}/semestre/{semestre}` - Buscar por curso e semestre
- `GET /api/usuarios/xp-minimo/{xpMinimo}` - Buscar por XP mínimo
- `GET /api/usuarios/avaliacao-minima/{avaliacaoMinima}` - Buscar por avaliação mínima
- `GET /api/usuarios/buscar?termo=` - Buscar usuários
- `PUT /api/usuarios/{id}` - Atualizar usuário
- `DELETE /api/usuarios/{id}` - Deletar usuário
- `POST /api/usuarios/{id}/adicionar-xp` - Adicionar XP ao usuário
- `POST /api/usuarios/{id}/atualizar-avaliacao` - Atualizar avaliação
- `POST /api/usuarios/{id}/avatar` - Upload de avatar

### Livros
- `POST /api/livros` - Criar livro
- `GET /api/livros/{id}` - Buscar livro por ID
- `GET /api/livros/isbn/{isbn}` - Buscar por ISBN
- `GET /api/livros/meus-livros` - Livros do usuário autenticado
- `GET /api/livros` - Listar todos os livros
- `GET /api/livros/titulo?titulo=` - Buscar por título
- `GET /api/livros/autor?autor=` - Buscar por autor
- `GET /api/livros/genero?genero=` - Buscar por gênero
- `GET /api/livros/editora?editora=` - Buscar por editora
- `GET /api/livros/ano?ano=` - Buscar por ano
- `GET /api/livros/condicao?condicao=` - Buscar por condição
- `GET /api/livros/buscar?termo=` - Busca geral
- `GET /api/livros/ano-entre?anoInicio=&anoFim=` - Buscar por período
- `GET /api/livros/generos` - Listar gêneros disponíveis
- `GET /api/livros/editoras` - Listar editoras disponíveis
- `GET /api/livros/recentes?limite=` - Livros adicionados recentemente
- `GET /api/livros/{id}/usuarios` - Usuários que possuem o livro
- `GET /api/livros/google/{googleId}/usuarios` - Usuários por Google ID
- `POST /api/livros/analise-ia` - Analisar nível de leitura com IA
- `PUT /api/livros/{id}` - Atualizar livro
- `DELETE /api/livros/{id}` - Deletar livro

### Propostas
- `POST /api/propostas` - Criar proposta
- `GET /api/propostas/{id}` - Buscar proposta por ID
- `GET /api/propostas` - Listar todas as propostas
- `GET /api/propostas/proponente/{proponenteId}` - Propostas enviadas por usuário
- `GET /api/propostas/proposto/{propostoId}` - Propostas recebidas por usuário
- `GET /api/propostas/usuario/{usuarioId}` - Todas propostas do usuário
- `GET /api/propostas/status/{status}` - Buscar por status
- `GET /api/propostas/usuario/{usuarioId}/status/{status}` - Propostas do usuário por status
- `GET /api/propostas/recebidas` - Propostas recebidas (autenticado)
- `GET /api/propostas/enviadas` - Propostas enviadas (autenticado)
- `POST /api/propostas/{id}/aceitar` - Aceitar proposta
- `POST /api/propostas/{id}/rejeitar` - Rejeitar proposta
- `POST /api/propostas/{id}/cancelar` - Cancelar proposta
- `DELETE /api/propostas/{id}` - Deletar proposta

### Agendamentos
- `POST /api/agendamentos` - Criar agendamento
- `GET /api/agendamentos/{id}` - Buscar agendamento por ID
- `GET /api/agendamentos` - Listar todos os agendamentos
- `GET /api/agendamentos/status/{status}` - Buscar por status
- `GET /api/agendamentos/periodo?inicio=&fim=` - Buscar por período
- `GET /api/agendamentos/passados/status/{status}` - Agendamentos passados por status
- `PUT /api/agendamentos/{id}/status` - Atualizar status
- `POST /api/agendamentos/{id}/confirmar` - Confirmar agendamento
- `POST /api/agendamentos/{id}/realizado` - Marcar como realizado
- `POST /api/agendamentos/{id}/cancelar` - Cancelar agendamento
- `DELETE /api/agendamentos/{id}` - Deletar agendamento

### Trocas
- `POST /api/trocas` - Criar troca
- `GET /api/trocas/{id}` - Buscar troca por ID
- `GET /api/trocas/minhas` - Trocas do usuário autenticado
- `GET /api/trocas` - Listar todas as trocas
- `POST /api/trocas/{id}/confirmar` - Confirmar troca
- `POST /api/trocas/{id}/concluir` - Concluir troca
- `POST /api/trocas/{id}/cancelar` - Cancelar troca
- `DELETE /api/trocas/{id}` - Deletar troca

### Notificações
- `GET /api/notificacoes` - Notificações do usuário autenticado
- `GET /api/notificacoes/nao-lidas` - Notificações não lidas
- `GET /api/notificacoes/count` - Contar notificações não lidas
- `GET /api/notificacoes/nao-lidas/status` - Status de notificações não lidas
- `PUT /api/notificacoes/{id}/marcar-lida` - Marcar como lida
- `PUT /api/notificacoes/marcar-como-lidas` - Marcar múltiplas como lidas
- `DELETE /api/notificacoes/{id}` - Deletar notificação

### Conquistas
- `GET /api/conquistas` - Listar todas as conquistas
- `GET /api/conquistas/disponiveis/{xp}` - Conquistas disponíveis por XP
- `GET /api/conquistas/tipo/{tipo}` - Buscar por tipo
- `GET /api/conquistas/usuario/{usuarioId}` - Conquistas do usuário

## 🔒 Segurança

A aplicação implementa múltiplas camadas de segurança para proteger dados e recursos:

### 🔐 Autenticação e Autorização

#### **JWT (JSON Web Token)**
- Tokens assinados com **HMAC SHA-256**
- Expiração configurável (padrão: 24 horas)
- Token retornado no formato `Bearer <token>` após login
- Validação automática em todas as requisições protegidas

**Fluxo de Autenticação:**
1. Usuário faz login via `POST /api/auth/login` com email e senha
2. Sistema valida credenciais contra banco de dados
3. Token JWT é gerado contendo o ID do usuário
4. Cliente envia token no header `Authorization: Bearer <token>` em requisições subsequentes
5. Filtro `JwtAuthenticationFilter` intercepta e valida o token
6. Usuário autenticado é injetado no contexto do Spring Security

#### **Spring Security**
- **Sessões stateless**: Sem armazenamento de sessão no servidor
- **Proteção de endpoints**: 
  - Rotas públicas: `/api/auth/**` e `POST /api/usuarios` (registro)
  - Demais rotas exigem autenticação
- **AuthenticationProvider** customizado com `UserDetailsService`

### 🔑 Criptografia de Senhas

- **BCrypt**: Algoritmo de hash com salt automático
- **Força**: 10 rounds de hashing (padrão BCrypt)
- Senhas nunca são armazenadas em texto puro
- Validação segura durante autenticação

### ✅ Validação de Dados

#### **Bean Validation (Jakarta Validation)**
Validações aplicadas em DTOs e entidades:

```java
@NotBlank(message = "Nome é obrigatório")
@Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
private String nome;

@NotBlank(message = "Email é obrigatório")
@Email(message = "Email deve ser válido")
private String email;

@NotBlank(message = "Senha é obrigatória")
@Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
private String senha;
```

**Validações implementadas:**
- Campos obrigatórios (`@NotBlank`, `@NotNull`)
- Formato de email (`@Email`)
- Tamanho mínimo/máximo (`@Size`)
- Valores numéricos positivos (`@Positive`, `@PositiveOrZero`)

### 🌐 CORS (Cross-Origin Resource Sharing)

Configuração permissiva para desenvolvimento, restritiva para produção:

**Origens permitidas:**
- `https://unilivros.netlify.app` (produção)
- `http://localhost:*` (desenvolvimento)
- `http://localhost:5173` (Vite dev server)

**Métodos HTTP:** GET, POST, PUT, DELETE, OPTIONS  
**Headers:** Todos permitidos  
**Credentials:** Habilitado para envio de cookies/tokens

### 🛡️ Tratamento de Exceções

#### **GlobalExceptionHandler**
Intercepta e padroniza respostas de erro:

| Exceção | Status HTTP | Descrição |
|---------|-------------|-----------|
| `ResourceNotFoundException` | 404 | Recurso não encontrado |
| `BusinessException` | 400 | Regra de negócio violada |
| `MethodArgumentNotValidException` | 400 | Validação de dados falhou |
| `MethodArgumentTypeMismatchException` | 400 | Tipo de parâmetro inválido |
| `Exception` (genérica) | 500 | Erro interno do servidor |

**Estrutura de resposta de erro:**
```json
{
  "status": 400,
  "message": "Email já está em uso",
  "timestamp": "2025-12-09T10:30:00"
}
```

**Resposta de validação:**
```json
{
  "status": 400,
  "message": "Erro de validação",
  "timestamp": "2025-12-09T10:30:00",
  "errors": {
    "nome": "Nome é obrigatório",
    "email": "Email deve ser válido"
  }
}
```

### 🔐 Recuperação de Senha Segura

1. **Solicitação**: Usuário fornece email em `POST /api/auth/forgot-password`
2. **Código único**: Sistema gera código de 6 dígitos válido por tempo limitado
3. **Envio seguro**: Código enviado por email via SendGrid
4. **Validação**: Código verificado em `POST /api/auth/validate-reset-code`
5. **Reset**: Nova senha definida em `POST /api/auth/reset-password`

### 📧 Verificação de Email

- Email de verificação enviado após registro
- Código de verificação único de 6 dígitos
- Conta fica pendente até verificação
- Previne registro com emails falsos

### 🔍 Logs e Auditoria

Níveis de log configurados:
- **INFO**: Operações da aplicação (`com.unilivros`)
- **INFO**: Segurança do Spring (`org.springframework.security`)
- **DEBUG**: Queries SQL do Hibernate
- **DEBUG**: Envio de emails

### 🚫 Proteções Implementadas

✅ **Prevenção de SQL Injection**: Uso de JPA/JPQL com bind parameters  
✅ **Prevenção de XSS**: Validação de entrada e sanitização  
✅ **CSRF desabilitado**: API stateless não requer proteção CSRF  
✅ **Rate limiting**: (Recomendado implementar no gateway/proxy)  
✅ **Exposição de erros controlada**: Mensagens genéricas em produção  

### 📋 Boas Práticas Seguidas

- ✅ Princípio do menor privilégio
- ✅ Separação de responsabilidades (camadas)
- ✅ Fail-safe defaults (negar acesso por padrão)
- ✅ Validação de entrada em múltiplas camadas
- ✅ Não expor informações sensíveis em logs/respostas
- ✅ Uso de bibliotecas atualizadas e auditadas

## 📝 Regras de Negócio

1. **Usuários**: Apenas usuários cadastrados podem propor trocas
2. **Livros**: Um livro só pode estar em uma proposta ativa por vez
3. **Trocas**: Só são concluídas após confirmação mútua
4. **Senhas**: Devem conter letras e números (mínimo 8 caracteres)
5. **XP**: Sistema de pontos por interações e trocas

## 🚀 Próximos Passos

- [x] Implementar autenticação JWT ✅
- [x] Sistema de notificações ✅
- [x] Sistema de conquistas e gamificação ✅
- [x] Integração com IA para análise de nível de leitura ✅
- [x] Sistema de email com SendGrid ✅
- [ ] Adicionar testes unitários
- [ ] Implementar cache Redis
- [ ] Adicionar documentação Swagger/OpenAPI
- [ ] Implementar notificações push
- [ ] Adicionar métricas e monitoramento

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## 👥 Contribuição

Contribuições são bem-vindas! Por favor, abra uma issue ou pull request.

---

**UniLivros API** - Conectando estudantes através da leitura 📚
