CREATE TABLE stocks (
  id UUID PRIMARY KEY,
  tenant_id VARCHAR(120) NOT NULL,
  sku VARCHAR(255) NOT NULL,
  on_hand BIGINT NOT NULL CHECK (on_hand>=0),
  reserved BIGINT NOT NULL CHECK (reserved>=0),
  version BIGINT NOT NULL,
  CONSTRAINT chk_reserved_stock CHECK(reserved<=on_hand),
  CONSTRAINT uq_stock UNIQUE(tenant_id,sku)
);
CREATE TABLE reservations (
  id UUID PRIMARY KEY,
  tenant_id VARCHAR(120) NOT NULL,
  order_ref VARCHAR(255) NOT NULL,
  sku VARCHAR(255) NOT NULL,
  quantity BIGINT NOT NULL CHECK(quantity>0),
  status VARCHAR(32) NOT NULL CHECK(status IN ('RESERVED','CONFIRMED','CANCELLED')),
  version BIGINT NOT NULL,
  CONSTRAINT uq_order_ref UNIQUE(tenant_id,order_ref),
  CONSTRAINT fk_res_stock FOREIGN KEY(tenant_id,sku) REFERENCES stocks(tenant_id,sku)
);
CREATE TABLE outbox_events (
  id UUID PRIMARY KEY,
  tenant_id VARCHAR(120) NOT NULL,
  event_type VARCHAR(255) NOT NULL,
  payload TEXT NOT NULL,
  published_at TIMESTAMPTZ
);
CREATE INDEX idx_outbox_unpublished ON outbox_events(published_at) WHERE published_at IS NULL;
