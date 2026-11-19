# ADR-001: Reservas de stock transaccionales

Cliente autenticado OIDC -> API Spring Security -> Service @Transactional -> PostgreSQL con lock pesimista por tenant/SKU.

POST reservar: bloquea fila de stock, comprueba disponibilidad, inserta la reserva y el evento outbox en la misma transacción.

POST confirmar: bloquea la reserva y el stock, consume stock, libera reserva, escribe evento.

POST cancelar: bloquea la reserva y el stock, libera cantidad reservada, escribe evento.

OutboxDispatcher lee filas no publicadas con bloqueo, envía a Kafka y marca `published_at` después de ACK. Publicación **at-least-once**, no exactly-once; consumidores deben deduplicar por `event_id`. La implementación actual envía `payload` sin `event_id`, por lo que antes de integración productiva se debe incluir ese identificador en los headers/event envelope.

## Pendientes importantes

- Pruebas de integración Spring+PostgreSQL+Kafka con Testcontainers.
- Separar transacción de lectura del outbox respecto de la llamada a Kafka para reducir tiempo de locks.
- Caducidad de reservas, compensación de fallos, reintentos controlados y alertas.
- Escalado del dispatcher con `SKIP LOCKED`, particionamiento, trazas y métricas.
- Evaluación de contención real, deadlocks, consumers idempotentes y disaster recovery.
