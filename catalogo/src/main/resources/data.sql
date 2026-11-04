-- 1. Inserción de Categorías Únicas
INSERT INTO categoria (nombre) VALUES ('Aventura');      -- ID 1
INSERT INTO categoria (nombre) VALUES ('Supervivencia'); -- ID 2
INSERT INTO categoria (nombre) VALUES ('Shooter');       -- ID 3
INSERT INTO categoria (nombre) VALUES ('Lucha');         -- ID 4
INSERT INTO categoria (nombre) VALUES ('Acción');        -- ID 5
INSERT INTO categoria (nombre) VALUES ('RPG');           -- ID 6
INSERT INTO categoria (nombre) VALUES ('Carreras');      -- ID 7

-- 2. Inserción de Videojuegos con la columna plataforma
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion, stock, imagen, plataforma) VALUES ('Hogwarts Legacy', 1, 45000, 'Juego de aventura en el mundo mágico.', 15, 'https://cdn.akamai.steamstatic.com/steam/apps/990080/header.jpg', 'PC');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion, stock, imagen, plataforma) VALUES ('Project Zomboid', 2, 12000, 'Supervivencia zombie hardcore y realista.', 25, 'https://cdn.akamai.steamstatic.com/steam/apps/108600/header.jpg', 'PC');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion, stock, imagen, plataforma) VALUES ('DOOM Eternal', 3, 35000, 'Shooter frenético contra demonios.', 30, 'https://cdn.akamai.steamstatic.com/steam/apps/782330/header.jpg', 'PC');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion, stock, imagen, plataforma) VALUES ('Street Fighter 6', 4, 55000, 'Juego de lucha clásico renovado.', 12, 'https://cdn.akamai.steamstatic.com/steam/apps/1364780/header.jpg', 'PlayStation 5');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion, stock, imagen, plataforma) VALUES ('Stellar Blade', 5, 59990, 'Acción y combate vertiginoso.', 8, 'https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/3489700/header.jpg?t=1776466244', 'PlayStation 5');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion, stock, imagen, plataforma) VALUES ('Resident Evil 4 Remake', 2, 39990, 'Supervivencia y terror reimaginado.', 6, 'https://cdn.akamai.steamstatic.com/steam/apps/2050650/header.jpg', 'PlayStation 5');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion, stock, imagen, plataforma) VALUES ('Elden Ring', 6, 49990, 'RPG de acción en un vasto mundo abierto.', 5, 'https://cdn.akamai.steamstatic.com/steam/apps/1245620/header.jpg', 'Xbox Series X|S');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion, stock, imagen, plataforma) VALUES ('Grand Theft Auto V', 5, 20000, 'Acción y mundo abierto aclamado.', 10, 'https://cdn.akamai.steamstatic.com/steam/apps/271590/header.jpg', 'Xbox Series X|S');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion, stock, imagen, plataforma) VALUES ('Mortal Kombat 1', 4, 55000, 'Lucha brutal con universo reiniciado.', 7, 'https://cdn.akamai.steamstatic.com/steam/apps/1971870/header.jpg', 'Xbox Series X|S');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion, stock, imagen, plataforma) VALUES ('The Legend of Zelda: Tears of the Kingdom', 1, 60000, 'Aventura épica con construcción.', 12, 'https://juegosdigitaleschile.com/files/images/productos/1684171975-the-legend-of-zelda-tears-of-the-kingdom-nintendo-switch-0.jpg', 'Nintendo Switch');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion, stock, imagen, plataforma) VALUES ('Super Smash Bros. Ultimate', 4, 50000, 'El mayor crossover de lucha.', 20, 'https://assets.nintendo.com/image/upload/c_fill,w_1200/q_auto:best/f_auto/dpr_2.0/ncom/software/switch/70010000012332/ac4d1fc9824876ce756406f0525d50c57ded4b2a666f6dfe40a6ac5c3563fad9', 'Nintendo Switch');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion, stock, imagen, plataforma) VALUES ('Mario Kart 8 Deluxe', 7, 45000, 'Juego de carreras divertido y familiar.', 18, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQTBaW4XpjBY8HARvbck5AK5cxfpk24bxKBbgiQCW9XvHmoEwIpar2EUQ71&s=10', 'Nintendo Switch');