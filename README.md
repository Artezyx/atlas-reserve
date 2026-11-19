# Atlas Reserve — Inventory Reservation Engine

**Estado de entrega:** Terminado y funcional según validación externa confirmada por el propietario (18/11/2026). El propietario reportó que todas sus pruebas adicionales aprobaron; los reportes de ejecución no están adjuntos a este repositorio. Esto no equivale a certificación independiente ni a aprobación para producción.

**Stack:** Java 21 · Spring Boot 3 · Spring Security OAuth2 Resource Server · PostgreSQL · Flyway · Kafka · JPA · Docker.

Proyecto original de backend empresarial para pedidos concurrentes; demuestra arquitectura y lógica de dominio. **Instalación completa no verificada en el entorno de preparación; el propietario reportó haberla ejecutado y validado externamente**.

## Caso empresarial

En un comercio B2B, varios compradores reservan un mismo SKU al mismo tiempo. El servicio mantiene `on_hand >= reserved >= 0`, bloquea stock en PostgreSQL y modela transiciones explícitas de reserva.

## API

- `POST /v1/stock` con `{ "sku":"SKU-1", "onHand":100 }`, scope `inventory.admin`.
- `GET /v1/stock/{sku}`, scope `inventory.read`, `inventory.write` o `inventory.admin`.
- `POST /v1/reservations` con `{ "sku":"SKU-1", "orderRef":"ORD-10", "quantity":5 }`, scope `inventory.write`.
- `POST /v1/reservations/{id}/confirm`, scope `inventory.write`.
- `POST /v1/reservations/{id}/cancel`, scope `inventory.write`.

El token OAuth2/JWT debe tener claim `tenant_id`, emisor OIDC verificable y scope autorizado. Cada operación selecciona el tenant del token, no del cuerpo de solicitud.

## Pruebas

```bash
mvn test
# Smoke test de lógica de dominio Java sin Maven:
javac -d /tmp/atlas-classes src/main/java/dev/atlasreserve/domain/InventoryMath.java InventoryMathHarness.java
java -cp /tmp/atlas-classes InventoryMathHarness
```

## Infraestructura

```bash
cp .env.example .env
# Configurar IDP OIDC real y contraseña fuerte
docker compose up --build
```

Al enviar una reserva se registra un evento de outbox en la misma transacción. El dispatcher de Kafka utiliza entrega at-least-once, por lo que los consumidores externos necesitan idempotencia.

## Alcance de la verificación disponible

- Validable localmente: lógica pura de stock con harness de Java 21.
- No verificado en el entorno de preparación: compilación Maven, API Spring, Flyway, OIDC, PostgreSQL, dispatcher Kafka y pruebas integrales concurrentes. El propietario reportó que realizó y aprobó pruebas adicionales fuera de ese entorno.
- Pendiente para producción: expiración de reservas, cancelación por timeout, reintentos en publisher, observabilidad, seguridad de despliegue y pruebas de carga.

Ver [ADR de arquitectura](docs/ARQUITECTURA.md).
