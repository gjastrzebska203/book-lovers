-- 1. Dodawanie Autorów
INSERT INTO authors (id, first_name, last_name, bio) VALUES 
(1, 'Andrzej', 'Sapkowski', 'Polski pisarz fantasy, twórca Wiedźmina.'),
(2, 'J.K.', 'Rowling', 'Autorka serii o Harrym Potterze.'),
(3, 'J.R.R.', 'Tolkien', 'Ojciec gatunku high fantasy.');

-- 2. Dodawanie Książek (Opisy skrócone dla czytelności)
INSERT INTO books (id, title, isbn, description, author_id, cover_image) VALUES 
(1, 'Ostatnie życzenie', '978-83-7578-063-5', 'Zbiór opowiadań o Wiedźminie Geralcie.', 1, NULL),
(2, 'Miecz przeznaczenia', '978-83-7578-064-2', 'Drugi tom opowiadań o Wiedźminie.', 1, NULL),
(3, 'Krew elfów', '978-83-7578-065-9', 'Pierwsza powieść z sagi o Wiedźminie.', 1, NULL),
(4, 'Harry Potter i Kamień Filozoficzny', '978-83-8008-211-3', 'Początek przygód młodego czarodzieja.', 2, NULL),
(5, 'Harry Potter i Komnata Tajemnic', '978-83-8008-212-0', 'Drugi rok nauki w Hogwarcie.', 2, NULL),
(6, 'Hobbit, czyli tam i z powrotem', '978-83-244-0400-9', 'Wyprawa Bilbo Bagginsa.', 3, NULL),
(7, 'Władca Pierścieni: Drużyna Pierścienia', '978-83-244-0401-6', 'Początek wielkiej wojny o pierścień.', 3, NULL);

INSERT INTO users (id, email, enabled, password, role, username) VALUES 
(1, 'admin@test.pl', TRUE, '$2a$10$phHEaJ1eMVhnBnxEW7dBbuREznWsPihxxMzb/3hR4iHorTIUdalJy', 'ROLE_ADMIN', 'admin'),
(2, 'user@test.pl', TRUE, '$2a$10$539KKiPbjOHaotKik/JQAuL4w2GVhIB.R9w5BL3xFb3x4QdYRuNeS', 'ROLE_USER', 'user');

ALTER TABLE authors ALTER COLUMN id RESTART WITH 4;
ALTER TABLE books ALTER COLUMN id RESTART WITH 8;
ALTER TABLE users ALTER COLUMN id RESTART WITH 3;