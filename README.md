# UniLivros API

API REST para a plataforma UniLivros - uma rede social acadêmica para promoção da leitura e trocas de livros entre estudantes universitários.

## 📋 Sobre o Projeto

O UniLivros é uma plataforma que conecta estudantes universitários para facilitar a troca de livros acadêmicos. A API fornece todos os endpoints necessários para gerenciar usuários, livros, propostas de troca, agendamentos e confirmações.

## 🚀 Tecnologias Utilizadas

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **Spring Security**
- **PostgreSQL** (ou SQL Server)
- **ModelMapper**
- **JWT** (futuro)
- **ZXing** (QR Code)

## 📁 Estrutura do Projeto

```
src/main/java/com/unilivros/
├── config/                 # Configurações
│   ├── ModelMapperConfig.java
│   └── SecurityConfig.java
├── controller/             # Controllers REST
│   ├── UsuarioController.java
│   ├── LivroController.java
│   ├── PropostaController.java
│   ├── AgendamentoController.java
│   ├── TrocaController.java
│   └── ConquistaController.java
├── dto/                    # Data Transfer Objects
│   ├── UsuarioDTO.java
│   ├── LivroDTO.java
│   ├── PropostaDTO.java
│   ├── AgendamentoDTO.java
│   ├── TrocaDTO.java
│   ├── ConquistaDTO.java
│   └── LivroPropostaDTO.java
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
│   ├── Conquista.java
│   ├── UsuarioLivro.java
│   ├── LivroProposta.java
│   ├── ConquistaUsuario.java
│   └── TrocaUsuario.java
├── repository/             # Repositories JPA
│   ├── UsuarioRepository.java
│   ├── LivroRepository.java
│   ├── PropostaRepository.java
│   ├── AgendamentoRepository.java
│   ├── TrocaRepository.java
│   ├── ConquistaRepository.java
│   └── UsuarioLivroRepository.java
├── service/                # Services com Lógica de Negócio
│   ├── UsuarioService.java
│   ├── LivroService.java
│   ├── PropostaService.java
│   ├── AgendamentoService.java
│   ├── TrocaService.java
│   └── ConquistaService.java
└── UniLivrosApiApplication.java
```

## 🗄️ Modelo de Dados

### Entidades Principais

- **Usuario**: Estudantes universitários
- **Livro**: Livros acadêmicos
- **Proposta**: Propostas de troca entre usuários
- **Agendamento**: Encontros para troca
- **Troca**: Confirmação e avaliação das trocas
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
    url: jdbc:postgresql://localhost:5432/unilivros
    username: postgres
    password: postgres
```

### 2. Executar a Aplicação

```bash
mvn spring-boot:run
```

A API estará disponível em: `http://localhost:8080/api`

## 📚 Endpoints Principais

### Usuários
- `POST /api/usuarios` - Criar usuário
- `GET /api/usuarios/{id}` - Buscar usuário por ID
- `GET /api/usuarios/email/{email}` - Buscar por email
- `GET /api/usuarios/matricula/{matricula}` - Buscar por matrícula
- `PUT /api/usuarios/{id}` - Atualizar usuário
- `DELETE /api/usuarios/{id}` - Deletar usuário

### Livros
- `POST /api/livros` - Criar livro
- `GET /api/livros/{id}` - Buscar livro por ID
- `GET /api/livros/buscar?termo=termo` - Buscar livros
- `GET /api/livros/genero?genero=fantasia` - Buscar por gênero
- `PUT /api/livros/{id}` - Atualizar livro
- `DELETE /api/livros/{id}` - Deletar livro

### Propostas
- `POST /api/propostas` - Criar proposta
- `GET /api/propostas/usuario/{usuarioId}` - Propostas do usuário
- `POST /api/propostas/{id}/aceitar` - Aceitar proposta
- `POST /api/propostas/{id}/rejeitar` - Rejeitar proposta

### Agendamentos
- `POST /api/agendamentos` - Criar agendamento
- `GET /api/agendamentos/usuario/{usuarioId}` - Agendamentos do usuário
- `POST /api/agendamentos/{id}/confirmar` - Confirmar agendamento
- `POST /api/agendamentos/{id}/realizado` - Marcar como realizado

### Trocas
- `POST /api/trocas` - Criar troca
- `POST /api/trocas/{id}/gerar-qr` - Gerar QR Code
- `POST /api/trocas/{id}/confirmar` - Confirmar troca
- `POST /api/trocas/{id}/concluir` - Concluir troca

### Conquistas
- `GET /api/conquistas/disponiveis/{xp}` - Conquistas disponíveis
- `GET /api/conquistas/tipo/{tipo}` - Buscar por tipo

## 🔒 Segurança

- Senhas criptografadas com BCrypt
- Validação de dados com Bean Validation
- CORS configurado para frontend
- Tratamento global de exceções

## 📝 Regras de Negócio

1. **Usuários**: Apenas usuários cadastrados podem propor trocas
2. **Livros**: Um livro só pode estar em uma proposta ativa por vez
3. **Trocas**: Só são concluídas após confirmação mútua
4. **Senhas**: Devem conter letras e números (mínimo 8 caracteres)
5. **XP**: Sistema de pontos por interações e trocas

## 🚀 Próximos Passos

- [ ] Implementar autenticação JWT
- [ ] Adicionar testes unitários
- [ ] Implementar cache Redis
- [ ] Adicionar documentação Swagger
- [ ] Implementar notificações push
- [ ] Adicionar métricas e monitoramento

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## 👥 Contribuição

Contribuições são bem-vindas! Por favor, abra uma issue ou pull request.

---

**UniLivros API** - Conectando estudantes através da leitura 📚
