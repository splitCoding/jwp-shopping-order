DROP TABLE IF EXISTS order_item;
DROP TABLE IF EXISTS shopping_order;
DROP TABLE IF EXISTS cart_item;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS member;

CREATE TABLE product
(
    id        BIGINT PRIMARY KEY AUTO_INCREMENT,
    name      VARCHAR(255) NOT NULL,
    price     INT          NOT NULL,
    image_url VARCHAR(255) NOT NULL
);

CREATE TABLE member
(
    id       BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    email    VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE cart_item
(
    id         BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    member_id  BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity   INT    NOT NULL,
    FOREIGN KEY (member_id) REFERENCES member (id),
    FOREIGN KEY (product_id) REFERENCES product (id)
);

CREATE TABLE shopping_order
(
    id        BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    FOREIGN KEY (member_id) REFERENCES member (id)
);

CREATE TABLE order_item
(
    id         BIGINT  NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_id   BIGINT  NOT NULL,
    product_id BIGINT  NOT NULL,
    quantity   INTEGER NOT NULL,
    FOREIGN KEY (order_id) REFERENCES shopping_order (id),
    FOREIGN KEY (product_id) REFERENCES product (id)
);
