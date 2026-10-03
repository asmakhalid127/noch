package com.noch.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DBInitializer {

    public static final int REVIEW_EDIT_WINDOW_MINUTES = 5;

    // Base path for product images inside the project resources folder
    private static final String IMG = "src/main/resources/images/";

    public static void initialize() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("PRAGMA foreign_keys = OFF;");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS categories (
                    category_id   TEXT PRIMARY KEY,
                    category_name TEXT NOT NULL,
                    category_type TEXT,
                    description   TEXT
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS products (
                    product_id   TEXT PRIMARY KEY,
                    product_name TEXT NOT NULL,
                    description  TEXT,
                    price        DECIMAL(10,2) NOT NULL,
                    category_id  TEXT,
                    color        TEXT,
                    gender       TEXT,
                    activity     TEXT DEFAULT 'Active',
                    image        TEXT
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    user_id   TEXT PRIMARY KEY,
                    email     TEXT UNIQUE NOT NULL,
                    password  TEXT NOT NULL,
                    name      TEXT NOT NULL,
                    role      TEXT NOT NULL,
                    verified  INTEGER DEFAULT 0
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS orders (
                    order_id     TEXT PRIMARY KEY,
                    user_id      TEXT NOT NULL,
                    order_date   TEXT DEFAULT (datetime('now')),
                    status       TEXT DEFAULT 'Pending',
                    total_amount DECIMAL(10,2) DEFAULT 0
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS order_items (
                    item_id    TEXT PRIMARY KEY,
                    order_id   TEXT NOT NULL,
                    product_id TEXT NOT NULL,
                    quantity   INTEGER NOT NULL,
                    unit_price DECIMAL(10,2) NOT NULL,
                    subtotal   DECIMAL(10,2) NOT NULL
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS delivery (
                    delivery_id      TEXT PRIMARY KEY,
                    order_id         TEXT NOT NULL,
                    delivery_address TEXT,
                    delivery_status  TEXT DEFAULT 'Pending',
                    delivery_time    TEXT
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS reviews (
                    review_id      TEXT PRIMARY KEY,
                    product_id     TEXT NOT NULL,
                    user_id        TEXT NOT NULL,
                    customer_name  TEXT NOT NULL,
                    rating         INTEGER NOT NULL,
                    comment        TEXT NOT NULL,
                    helpful        INTEGER DEFAULT 0,
                    unhelpful      INTEGER DEFAULT 0,
                    flagged        INTEGER DEFAULT 0,
                    image_path     TEXT,
                    editable_until TEXT,
                    created_at     TEXT DEFAULT (datetime('now'))
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS review_votes (
                    user_id   TEXT NOT NULL,
                    review_id TEXT NOT NULL,
                    vote_type TEXT NOT NULL,
                    PRIMARY KEY (user_id, review_id)
                );
            """);

            // ── SEED: categories ──────────────────────────────────────
            stmt.execute("""
                INSERT OR IGNORE INTO categories VALUES
                ('cat-1','Tops',     'Unisex','T-shirts and shirts'),
                ('cat-2','Bottoms',  'Unisex','Trousers and jeans'),
                ('cat-3','Outerwear','Unisex','Jackets and coats'),
                ('cat-4','Dresses',  'Women', 'Evening and casual dresses');
            """);

            // ── SEED: clothing products with images ───────────────────
            stmt.execute("INSERT OR IGNORE INTO products VALUES " +
                "('1','NOCH SATIN EVENING DRESS','Elegant emerald satin evening dress with a sophisticated silhouette. Deep V-back, floor-length cut, premium satin fabric.',142.00,'cat-4','Emerald','Women','Active','" + IMG + "product1.jpg')," +
                "('2','NOCH LINEN SHIRT','Breathable pure linen shirt with a loose, relaxed silhouette. Perfect for all seasons.',59.99,'cat-1','Beige','Unisex','Active','" + IMG + "product2.jpg')," +
                "('3','NOCH SLIM TROUSERS','Tailored slim-fit trousers in stretch cotton. Smart enough for the office, easy enough for every day.',69.99,'cat-2','Black','Unisex','Active','" + IMG + "product3.jpg')," +
                "('4','NOCH WIDE LEG JEANS','Wide-leg denim in a clean indigo wash. High waist, straight cut, timeless.',74.99,'cat-2','Indigo','Women','Active','" + IMG + "product4.jpg')," +
                "('5','NOCH OVERSHIRT JACKET','A structured overshirt jacket in brushed cotton twill. Wear open or buttoned as a light layer.',89.99,'cat-3','Olive','Unisex','Active','" + IMG + "product5.jpg')," +
                "('6','NOCH WOOL COAT','A minimalist single-breasted wool blend coat. Longline cut, clean finish, cold weather essential.',149.99,'cat-3','Camel','Unisex','Active','" + IMG + "product6.jpg');");

            // ── SEED: real users ──────────────────────────────────────
            stmt.execute("""
                INSERT OR IGNORE INTO users VALUES
                ('user-1',  'user@email.com', 'user123',  'USER',  'CUSTOMER', 1),
                ('admin-1', 'admin@noch.com', 'admin123', 'ADMIN', 'ADMIN',    1);
            """);

            // ── SEED: demo users ──────────────────────────────────────
            stmt.execute("""
                INSERT OR IGNORE INTO users VALUES
                ('demo-u1','sarah@demo.com', 'x','Sarah M', 'CUSTOMER',1),
                ('demo-u2','james@demo.com', 'x','James T', 'CUSTOMER',1),
                ('demo-u3','priya@demo.com', 'x','Priya K', 'CUSTOMER',1),
                ('demo-u4','oliver@demo.com','x','Oliver R','CUSTOMER',1),
                ('demo-u5','emma@demo.com',  'x','Emma L',  'CUSTOMER',1);
            """);

            // ── SEED: demo reviews ────────────────────────────────────
            stmt.execute("""
                INSERT OR IGNORE INTO reviews
                    (review_id,product_id,user_id,customer_name,rating,comment,helpful,unhelpful,flagged,editable_until,created_at)
                VALUES
                ('demo-r1','1','demo-u1','Sarah M',5,'This dress is absolutely stunning. The emerald satin catches the light beautifully and the V-back is so elegant. I wore it to a black tie event and received so many compliments.',18,1,0,NULL,'2025-11-10 09:15:00'),
                ('demo-r2','1','demo-u3','Priya K',5,'Perfect fit and gorgeous colour. The satin quality is exceptional and the silhouette is incredibly flattering. Worth every penny for a special occasion.',14,0,0,NULL,'2025-12-01 11:05:00'),
                ('demo-r3','2','demo-u4','Oliver R',5,'The linen shirt is beautiful. Incredibly breathable and the oversized fit looks effortlessly stylish. Perfect for summer.',20,0,0,NULL,'2025-10-05 08:30:00'),
                ('demo-r4','2','demo-u5','Emma L',4,'Great quality linen and lovely drape. Washed it twice and it only got softer. Would love more colour options.',9,1,0,NULL,'2025-10-20 16:45:00'),
                ('demo-r5','3','demo-u1','Sarah M',5,'These trousers are incredible. The stretch fabric means they are smart but comfortable all day. Wore them to work and to dinner.',16,0,0,NULL,'2025-09-12 10:00:00'),
                ('demo-r6','3','demo-u2','James T',4,'Excellent fit and quality. The slim cut is modern without being too tight. The waistband is comfortable even after a full day.',8,0,0,NULL,'2025-09-28 13:30:00'),
                ('demo-r7','4','demo-u3','Priya K',5,'These jeans are absolutely stunning. The wide leg silhouette is so flattering and the denim quality is exceptional. Already on my second pair.',22,1,0,NULL,'2025-08-15 09:00:00'),
                ('demo-r8','4','demo-u5','Emma L',4,'Love the high waist and wide leg cut. True to size and the indigo wash is a beautiful deep blue. Very happy with this purchase.',13,0,0,NULL,'2025-08-03 12:00:00'),
                ('demo-r9','5','demo-u4','Oliver R',5,'The overshirt jacket is so versatile. I wear it open over a tee or buttoned as a shirt. The brushed cotton feels premium and it layers perfectly.',17,0,0,NULL,'2025-07-22 17:15:00'),
                ('demo-r10','5','demo-u2','James T',4,'Really great jacket. The olive colour is rich and goes with everything. Well constructed and feels like it will last for years.',11,0,0,NULL,'2025-07-15 10:00:00'),
                ('demo-r11','6','demo-u2','James T',5,'This coat is worth every penny. The wool blend is heavyweight and warm, the cut is clean and classic. I get compliments every time I wear it.',25,0,0,NULL,'2025-07-10 15:00:00'),
                ('demo-r12','6','demo-u1','Sarah M',5,'Absolutely stunning coat. The camel colour is rich and the longline cut is so elegant. Fits perfectly and feels incredibly luxurious.',19,1,0,NULL,'2025-06-30 10:30:00');
            """);

            stmt.execute("PRAGMA foreign_keys = ON;");
            System.out.println("Database initialised successfully.");

        } catch (SQLException e) {
            System.err.println("DBInitializer error: " + e.getMessage());
        }
    }
}