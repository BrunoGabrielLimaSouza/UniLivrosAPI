# 📋 Mapeamento de Implementação JWT - UniLivros API

## 🎯 Objetivo
Implementar autenticação JWT para proteger endpoints da API e permitir acesso seguro aos recursos.

---

## 📁 ARQUIVOS QUE SERÃO CRIADOS

### 1. **`src/main/java/com/unilivros/security/JwtTokenProvider.java`**
**Responsabilidade:** Gerar e validar tokens JWT
- Métodos: `generateToken()`, `validateToken()`, `getUserIdFromToken()`
- Usa configuração do `application.yml` (secret, expiration)
- **Impacto:** Nenhum - classe nova, não afeta código existente

### 2. **`src/main/java/com/unilivros/security/JwtAuthenticationFilter.java`**
**Responsabilidade:** Filtrar requisições e validar tokens JWT
- Intercepta requisições HTTP
- Extrai token do header `Authorization: Bearer <token>`
- Valida token e injeta usuário no contexto Spring Security
- **Impacto:** Nenhum - apenas processa requisições, não quebra funcionalidades existentes

### 3. **`src/main/java/com/unilivros/security/UserDetailsServiceImpl.java`**
**Responsabilidade:** Carregar usuário do banco para autenticação
- Implementa `UserDetailsService` do Spring Security
- Busca usuário por email no banco
- Converte `Usuario` para `UserDetails` (Spring Security)
- **Impacto:** Nenhum - usa `UsuarioRepository` existente, não modifica

### 4. **`src/main/java/com/unilivros/controller/AuthController.java`**
**Responsabilidade:** Endpoints de autenticação (login/registro)
- `POST /api/auth/register` - Criar novo usuário (público)
- `POST /api/auth/login` - Autenticar e retornar token (público)
- `GET /api/auth/me` - Obter usuário atual autenticado (protegido)
- **Impacto:** Adiciona novos endpoints, não remove existentes

### 5. **`src/main/java/com/unilivros/dto/LoginDTO.java`**
**Responsabilidade:** DTO para receber credenciais de login
- Campos: `email`, `senha`
- Validações: `@NotBlank`, `@Email`
- **Impacto:** Nenhum - classe nova

### 6. **`src/main/java/com/unilivros/dto/AuthResponseDTO.java`**
**Responsabilidade:** DTO para resposta de autenticação
- Campos: `token`, `type` ("Bearer"), `usuario` (UsuarioDTO)
- **Impacto:** Nenhum - classe nova

---

## 🔧 ARQUIVOS QUE SERÃO MODIFICADOS

### 1. **`src/main/java/com/unilivros/config/SecurityConfig.java`**
**Mudanças:**
- ✅ **ANTES:** `anyRequest().permitAll()` - Todos endpoints públicos
- ✅ **DEPOIS:** Configuração granular de segurança:
  - `/api/auth/**` - Público (login, registro)
  - `/api/usuarios` (POST) - Público (registro alternativo)
  - Todos outros endpoints - Requerem autenticação

**Impacto na execução:**
- ⚠️ **BREAKING CHANGE:** Endpoints protegidos exigirão token JWT
- Endpoints antigos sem token retornarão `401 Unauthorized`
- Frontend precisará incluir token no header: `Authorization: Bearer <token>`

**Código de exemplo do que será adicionado:**
```java
// Adicionar filtro JWT antes do filtro padrão
.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
// Configurar autorização
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/auth/**", "/usuarios").permitAll()
    .anyRequest().authenticated()
)
```

### 2. **`src/main/java/com/unilivros/service/UsuarioService.java`**
**Mudanças:**
- Adicionar método: `authenticateUser(String email, String senha)`
- Método retorna `Usuario` autenticado ou lança exceção
- Método `buscarPorEmail()` já existe e pode ser reutilizado

**Impacto na execução:**
- ✅ **NÃO QUEBRA:** Métodos existentes permanecem intactos
- Apenas adiciona novo método para autenticação

### 3. **`src/main/java/com/unilivros/dto/UsuarioDTO.java`**
**Mudanças:**
- ⚠️ **OPCIONAL:** Adicionar método para não retornar senha em respostas
- Ou criar `UsuarioResponseDTO` separado (melhor prática)

**Impacto na execução:**
- ✅ **SEGRANÇA:** Senha não será exposta em respostas JSON
- Endpoints existentes continuam funcionando

---

## 🔄 FLUXO DE AUTENTICAÇÃO (Como Funcionará)

### **Antes (Atual):**
```
Cliente → POST /api/usuarios → Cria usuário → Retorna UsuarioDTO
Cliente → POST /api/propostas → Cria proposta (SEM AUTENTICAÇÃO)
```

### **Depois (Com JWT):**
```
1. REGISTRO/LOGIN:
   Cliente → POST /api/auth/register (público)
   OU
   Cliente → POST /api/auth/login (público)
   ↓
   API → Retorna AuthResponseDTO { token, usuario }
   ↓
   Cliente → Armazena token

2. REQUISIÇÕES PROTEGIDAS:
   Cliente → GET /api/propostas (com header: Authorization: Bearer <token>)
   ↓
   JwtAuthenticationFilter → Valida token
   ↓
   SecurityConfig → Permite acesso se token válido
   ↓
   Controller → Processa requisição normalmente
```

---

## ⚠️ IMPACTO NA EXECUÇÃO DO PROJETO

### **1. QUEBRAS DE COMPATIBILIDADE:**

#### ✅ **Endpoints que NÃO quebram:**
- `POST /api/auth/register` - Novo endpoint (público)
- `POST /api/auth/login` - Novo endpoint (público)
- `POST /api/usuarios` - Continua público (registro alternativo)
- `GET /api/auth/me` - Novo endpoint (requer autenticação)

#### ⚠️ **Endpoints que PASSAM A REQUERER AUTENTICAÇÃO:**
- `GET /api/usuarios/{id}` - **Antes:** Público | **Depois:** Requer token
- `GET /api/usuarios` - **Antes:** Público | **Depois:** Requer token
- `PUT /api/usuarios/{id}` - **Antes:** Público | **Depois:** Requer token
- `DELETE /api/usuarios/{id}` - **Antes:** Público | **Depois:** Requer token
- **Todos endpoints de:** Livros, Propostas, Agendamentos, Trocas, Conquistas
  - **Antes:** Públicos | **Depois:** Requerem token

#### 🔒 **Comportamento quando token ausente/inválido:**
```
Status: 401 Unauthorized
Response: {
  "status": 401,
  "message": "Token JWT inválido ou ausente",
  "timestamp": "2024-..."
}
```

---

### **2. MUDANÇAS NO FRONTEND (Se houver):**

#### **Antes:**
```javascript
// Requisição sem autenticação
fetch('http://localhost:8088/api/propostas', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(data)
})
```

#### **Depois:**
```javascript
// 1. Fazer login primeiro
const loginResponse = await fetch('http://localhost:8088/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email: 'user@email.com', senha: 'senha123' })
})
const { token } = await loginResponse.json()

// 2. Armazenar token (localStorage/sessionStorage)
localStorage.setItem('token', token)

// 3. Usar token em requisições subsequentes
fetch('http://localhost:8088/api/propostas', {
  method: 'POST',
  headers: { 
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`  // ← ADICIONAR ESTE HEADER
  },
  body: JSON.stringify(data)
})
```

---

### **3. TESTES E VALIDAÇÃO:**

#### **Como testar manualmente (Postman/cURL):**

**1. Registrar/Login:**
```bash
# Login
curl -X POST http://localhost:8088/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"usuario@email.com","senha":"senha123"}'

# Resposta esperada:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "usuario": { "id": 1, "nome": "...", ... }
}
```

**2. Usar token em requisição protegida:**
```bash
# Criar proposta (requer token)
curl -X POST http://localhost:8088/api/propostas \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{"proponenteId":1,"propostoId":2,...}'
```

**3. Testar sem token (deve retornar 401):**
```bash
curl -X GET http://localhost:8088/api/propostas
# Resposta: 401 Unauthorized
```

---

### **4. CONFIGURAÇÕES NECESSÁRIAS:**

#### **`application.yml` (já configurado):**
```yaml
spring:
  security:
    jwt:
      secret: unilivros-secret-key-2024-very-secure  # ✅ Já existe
      expiration: 86400000  # 24 horas ✅ Já existe
```

#### **Dependências (já presentes no pom.xml):**
- ✅ `jjwt-api` - Já existe
- ✅ `jjwt-impl` - Já existe
- ✅ `jjwt-jackson` - Já existe
- ✅ `spring-boot-starter-security` - Já existe

**✅ Nenhuma nova dependência necessária!**

---

## 📊 RESUMO DE IMPACTO

| Aspecto | Status | Descrição |
|---------|--------|-----------|
| **Compatibilidade** | ⚠️ **Breaking Change** | Endpoints protegidos exigem token |
| **Novos Endpoints** | ✅ **Adiciona** | `/api/auth/*` - Não remove nada |
| **Dependências** | ✅ **OK** | Todas já presentes |
| **Configuração** | ✅ **OK** | `application.yml` já configurado |
| **Código Existente** | ✅ **Preservado** | Apenas adiciona, não modifica lógica |
| **Banco de Dados** | ✅ **Sem mudanças** | Usa `Usuario` existente |
| **Frontend** | ⚠️ **Atualização necessária** | Precisa incluir token no header |

---

## 🚀 ESTRATÉGIA DE IMPLEMENTAÇÃO

### **Fase 1: Implementar Classes Base** ✅
1. Criar `JwtTokenProvider` (gerar/validar tokens)
2. Criar `UserDetailsServiceImpl` (carregar usuário)
3. Criar DTOs (`LoginDTO`, `AuthResponseDTO`)

### **Fase 2: Implementar Filtro** ✅
4. Criar `JwtAuthenticationFilter` (processar requisições)
5. Atualizar `SecurityConfig` (aplicar filtro e regras)

### **Fase 3: Implementar Endpoints** ✅
6. Criar `AuthController` (login/registro)
7. Atualizar `UsuarioService` (método authenticate)

### **Fase 4: Testes** ✅
8. Testar fluxo completo de autenticação
9. Validar que endpoints protegidos funcionam
10. Validar que endpoints públicos continuam acessíveis

---

## ✅ CHECKLIST ANTES DE IMPLEMENTAR

- [x] Analisar estrutura atual do projeto
- [x] Identificar todos os endpoints
- [x] Mapear dependências necessárias (todas presentes ✅)
- [x] Mapear mudanças em arquivos existentes
- [x] Mapear novos arquivos a serem criados
- [x] Identificar breaking changes
- [x] Documentar impacto no frontend
- [ ] **Implementar código** ⏳

---

## 🎯 PRÓXIMOS PASSOS

1. ✅ **Mapeamento completo** - CONCLUÍDO
2. ⏳ **Implementar código** - PRÓXIMO
3. ⏳ **Testes manuais** - Após implementação
4. ⏳ **Atualizar README** - Documentar novos endpoints
5. ⏳ **Documentar para frontend** - Como usar autenticação

---

**Status:** ✅ Pronto para implementação
**Risco:** ⚠️ Médio (breaking change para endpoints protegidos, mas adiciona segurança)
**Tempo estimado:** 1-2 horas de implementação + testes


