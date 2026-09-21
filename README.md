# PIX Donation API

API em Spring Boot 3.3 (Java 21, Maven) para checkout de doação via PIX.

---

## 🚀 Como Executar em MODO TESTE (Profile `dev`)

Em modo desenvolvimento (`dev`), a aplicação usa um banco H2 em memória e a implementação fictícia `MockPixGateway`, gerando QR Code em Base64 (via ZXing) e chave copia-e-cola de teste sem qualquer integração externa real.

Execute o comando abaixo na raiz do projeto:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

> **Aviso de Segurança (Startup Guard):** Se a aplicação for iniciada **sem** o profile `dev` e sem uma implementação concreta de `PixGateway` para produção, o sistema **falhará imediatamente no startup** com uma mensagem de erro explícita (`ProductionGatewayGuard`). Nunca cairá silenciosamente em mock em produção.

---

## 🧪 Fluxo Completo de Teste (Modo Dev)

### 1. Criar uma Doação (POST `/api/donations`)

Submeta um valor em **centavos** (mínimo R$ 15,00 = 1500 centavos, máximo R$ 1.000,00 = 100000 centavos):

```bash
curl -X POST http://localhost:8080/api/donations \
  -H "Content-Type: application/json" \
  -d '{"amountCents": 5000}'
```

**Resposta de Sucesso (201 Created):**
```json
{
  "id": "e4a52c38-1234-4567-89ab-cdef01234567",
  "amountCents": 5000,
  "status": "PENDING",
  "pixCopyPaste": "TESTE-NAO-PAGAR-e4a52c38-1234-4567-89ab-cdef01234567",
  "qrCodeBase64": "iVBORw0KGgoAAAANSUhEUgAAASwAAAEs...",
  "gatewayChargeId": "MOCK-A1B2C3D4",
  "createdAt": "2026-09-20T18:50:00Z"
}
```

---

### 2. Consultar o Status da Doação (GET `/api/donations/{id}`)

```bash
curl http://localhost:8080/api/donations/e4a52c38-1234-4567-89ab-cdef01234567
```

**Resposta (200 OK):**
```json
{
  "id": "e4a52c38-1234-4567-89ab-cdef01234567",
  "amountCents": 5000,
  "status": "PENDING"
}
```

---

### 3. Simular Pagamento (POST `/api/dev/donations/{id}/simulate-paid`)

Este endpoint **só existe** no profile `dev` e altera o status da doação para `PAID`:

```bash
curl -X POST http://localhost:8080/api/dev/donations/e4a52c38-1234-4567-89ab-cdef01234567/simulate-paid
```

**Resposta:** `200 OK`

Consulte novamente o GET `/api/donations/{id}` para confirmar o status atualizado:
```json
{
  "id": "e4a52c38-1234-4567-89ab-cdef01234567",
  "amountCents": 5000,
  "status": "PAID"
}
```

---

## ⏱️ Job de Expiração de Doações PENDENTES

A cada 1 minuto (`@Scheduled`), um job automático busca todas as doações com status `PENDING` criadas há **mais de 30 minutos** e altera seu status para `EXPIRED`.

---

## ⚙️ Variáveis de Ambiente (Produção)

Em produção (sem profile `dev`), forneça as variáveis:
- `DB_URL`
- `DB_USER`
- `DB_PASS`
- `ONIPAY_API_KEY` (ou chave do seu gateway PIX real)