-- Create order_items table if it doesn't exist
CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    product_id INT NOT NULL,
    product_quantity INT NOT NULL,
    product_price DECIMAL(10,2) NOT NULL,
    order_id BIGINT NOT NULL,
    CONSTRAINT fk_order
    FOREIGN KEY(order_id)
    REFERENCES orders(id)
    ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );