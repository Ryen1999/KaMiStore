-- t_product.total_stock is redundant for card-secret products.
-- Available stock is counted from unsold rows in t_kami_item.
ALTER TABLE t_product DROP COLUMN total_stock;
