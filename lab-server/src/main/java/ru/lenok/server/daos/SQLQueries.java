package ru.lenok.server.daos;

public enum SQLQueries {
    //USERS
    CREATE_SEQUENCE_USER("CREATE SEQUENCE IF NOT EXISTS user_seq START 1"),
    CREATE_TABLE_USER("""
                   CREATE TABLE IF NOT EXISTS users (
                   id BIGINT DEFAULT nextval('user_seq') PRIMARY KEY,
                   name VARCHAR(256) NOT NULL UNIQUE,
                   pw_hash VARCHAR(256) NOT NULL )
            """),
    CREATE_INDEX_USER("CREATE INDEX IF NOT EXISTS idx_user_name ON users (name)"),
    CREATE_USER("""
                INSERT INTO users (
                    name,
                    pw_hash
                ) VALUES (?, ?)
                RETURNING id
            """),
    CREATE_USER_WITH_ID("""
                INSERT INTO users (
                    name,
                    pw_hash,
                    id
                ) VALUES (?, ?, ?)
                RETURNING id
            """),
    DROP_ALL_USERS("""
              DROP INDEX IF EXISTS idx_user_name;
              DROP TABLE IF EXISTS users;
              DROP SEQUENCE IF EXISTS user_seq;
            """),
    GET_USER_BY_NAME("SELECT id, name, pw_hash FROM users WHERE name = ?"),

    //LABWORK
    DROP_ALL_LABWORK("""
                  DROP INDEX IF EXISTS idx_labwork_name;
                  DROP INDEX IF EXISTS idx_labwork_unique_key;
                  DROP TABLE IF EXISTS lab_work;
                  DROP SEQUENCE IF EXISTS lab_work_seq;
                  DROP TYPE IF EXISTS DIFFICULTY;
            """),
    CREATE_SEQUENCE_LABWORK("CREATE SEQUENCE IF NOT EXISTS lab_work_seq START 1"),
    CREATE_TABLE_LABWORK("""
        CREATE TABLE IF NOT EXISTS lab_work (
            id BIGINT DEFAULT nextval('lab_work_seq') PRIMARY KEY,
            key VARCHAR(256) NOT NULL,
            name VARCHAR(256) NOT NULL,
            coord_x DOUBLE PRECISION NOT NULL,
            coord_y REAL NOT NULL,
            creation_date TIMESTAMP NOT NULL,
            minimal_point DOUBLE PRECISION NOT NULL,
            description VARCHAR(2863) NOT NULL,
            difficulty VARCHAR(256) NOT NULL,
            discipline_name VARCHAR(256),
            discipline_practice_hours BIGINT NOT NULL,
            owner_id BIGINT REFERENCES users(id)
        )
        """),
    CREATE_NAME_INDEX_LABWORK("CREATE INDEX IF NOT EXISTS idx_labwork_name ON lab_work (name)"),
    CREATE_KEY_INDEX_LABWORK("CREATE UNIQUE INDEX IF NOT EXISTS idx_labwork_unique_key ON lab_work (key)"),
    UPDATE_LAB_WORK("""
            UPDATE lab_work
            SET
                key = ?,
                name = ?,
                coord_x = ?,
                coord_y = ?,
                creation_date = ?,
                minimal_point = ?,
                description = ?,
                difficulty = ?,
                discipline_name = ?,
                discipline_practice_hours = ?,
                owner_id = ?
            WHERE id = ?
            """),
    CREATE_LAB_WORK("""
                INSERT INTO lab_work (
                    key,
                    name,
                    coord_x,
                    coord_y,
                    creation_date,
                    minimal_point,
                    description,
                    difficulty,
                    discipline_name,
                    discipline_practice_hours,
                    owner_id
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id
            """),
    CREATE_LAB_WORK_WITH_ID("""
                INSERT INTO lab_work (
                    key,
                    name,
                    coord_x,
                    coord_y,
                    creation_date,
                    minimal_point,
                    description,
                    difficulty,
                    discipline_name,
                    discipline_practice_hours,
                    owner_id,
                    id
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id
            """),
    SELECT_ALL("""
            SELECT *
            FROM lab_work
            ORDER BY name
            """),
    DELETE_FOR_USER_LABWORK("DELETE FROM lab_work WHERE owner_id = ?"),
    DELETE_BY_KEYS_LABWORK("DELETE FROM lab_work WHERE key = ANY (?)"),
    DELETE_LABWORK("DELETE FROM lab_work WHERE key = ?"),

    //PRODUCT
    DROP_ALL_PRODUCT("""
        DROP INDEX IF EXISTS idx_product_name;
        DROP TABLE IF EXISTS product;
        DROP SEQUENCE IF EXISTS product_seq;
        """),
    CREATE_SEQUENCE_PRODUCT("""
        CREATE SEQUENCE IF NOT EXISTS product_seq START 1;
        """),
    CREATE_TABLE_PRODUCT("""
        CREATE TABLE IF NOT EXISTS product (
            id BIGINT DEFAULT nextval('product_seq') PRIMARY KEY,
            name VARCHAR(256) NOT NULL,
            owner_id BIGINT NOT NULL
        );
        """),
    CREATE_INDEX_PRODUCT("""
        CREATE INDEX IF NOT EXISTS idx_product_name ON product (name);
        """),
    CREATE_PRODUCT("""
                INSERT INTO product (
                    name,
                    owner_id
                ) VALUES (?, ?)
                RETURNING id
            """),
    SELECT_PRODUCTS_BY_OWNER("""
            SELECT * FROM product WHERE owner_id = ?
            """),
    SELECT_PRODUCTS_BY_ID("""
            SELECT * FROM product WHERE id = ?
            """),
    UPDATE_PRODUCT("""
            UPDATE product
            SET owner_id = ?,
            name = ?
            WHERE id = ?
            """),

    //OFFER
    DROP_ALL_OFFER("""
        DROP TABLE IF EXISTS offer;
        DROP SEQUENCE IF EXISTS offer_seq;
        """),
    CREATE_SEQUENCE_OFFER("""
        CREATE SEQUENCE IF NOT EXISTS offer_seq START 1;
        """),
    CREATE_TABLE_OFFER("""
            CREATE TABLE IF NOT EXISTS offer (
                id BIGINT DEFAULT nextval('offer_seq') PRIMARY KEY,
                labWork_id BIGINT NOT NULL,
                product_id BIGINT NOT NULL,
                status VARCHAR(256) NOT NULL,
                CONSTRAINT fk_labwork FOREIGN KEY (labWork_id) REFERENCES lab_work(id),
                CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES product(id)
            )
            """),

    CREATE_OFFER("""
                INSERT INTO offer (
                    labWork_id,
                    product_id,
                    status
                ) VALUES (?, ?, ?)
                RETURNING id
            """),
    UPDATE_OFFER("""
                UPDATE offer
                SET
                    labWork_id = ?,
                    product_id = ?,
                    status = ?
                WHERE id = ?        
            """),
    SELECT_OFFERS_BY_LAB_WORK_OWNER("""
            SELECT o.id, lw.id, lw.name, p.id, p.name, u_pr.id, u_pr.name, o.status 
            FROM offer o
            JOIN lab_work lw ON o.labWork_id=lw.id
            JOIN product p ON o.product_id=p.id
            JOIN users u_lw ON u_lw.id=lw.owner_id
            JOIN users u_pr ON u_pr.id=p.owner_id
            WHERE u_lw.id = ? AND o.status LIKE 'OPEN'
            """),
    SELECT_OFFERS_BY_PRODUCT_OWNER("""
            SELECT o.id, lw.id, lw.name, p.id, p.name, u_lw.id, u_lw.name, o.status 
            FROM offer o
            JOIN lab_work lw ON o.labWork_id=lw.id
            JOIN product p ON o.product_id=p.id
            JOIN users u_lw ON u_lw.id=lw.owner_id
            JOIN users u_pr ON u_pr.id=p.owner_id
            WHERE u_pr.id = ? AND o.status LIKE 'OPEN'
            """),
    SELECT_OFFER_BY_ID("""
            SELECT o.id, lw.id, lw.name, p.id, p.name, u_lw.id, u_lw.name, o.status 
            FROM offer o
            JOIN lab_work lw ON o.labWork_id=lw.id
            JOIN product p ON o.product_id=p.id
            JOIN users u_lw ON u_lw.id=lw.owner_id
            JOIN users u_pr ON u_pr.id=p.owner_id
            WHERE o.id = ?
            """),
    SELECT_OFFERS_BY_PRODUCT_ID("""
            SELECT * FROM offer
            WHERE product_id = ?
            """),
    SELECT_OFFERS_BY_LAB_WORK_ID("""
            SELECT * FROM offer
            WHERE labWork_id = ?
            """);

    private final String text;

    SQLQueries(String text) {
        this.text = text;
    }

    public String t() {
        return text;
    }
}
