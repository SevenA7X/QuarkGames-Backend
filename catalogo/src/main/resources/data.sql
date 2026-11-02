-- 1. Inserción de Categorías
INSERT INTO categoria (nombre) VALUES ('RPG');         -- ID 1
INSERT INTO categoria (nombre) VALUES ('Aventura');    -- ID 2
INSERT INTO categoria (nombre) VALUES ('Accion');      -- ID 3
INSERT INTO categoria (nombre) VALUES ('Deportes');    -- ID 4
INSERT INTO categoria (nombre) VALUES ('Simulacion');  -- ID 5
INSERT INTO categoria (nombre) VALUES ('Estrategia');  -- ID 6
INSERT INTO categoria (nombre) VALUES ('Lucha');       -- ID 7
INSERT INTO categoria (nombre) VALUES ('Terror');      -- ID 8
INSERT INTO categoria (nombre) VALUES ('Puzzles');     -- ID 9

-- 2. Inserción de Videojuegos (15 filas referenciando los IDs de categorías)
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion) VALUES ('Elden Ring', 1, 45000, 'Explora las Tierras Intermedias.');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion) VALUES ('The Legend of Zelda', 2, 55000, 'Una aventura épica de mundo abierto.');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion) VALUES ('Hollow Knight', 3, 12000, 'Un viaje épico a través de un reino de insectos en ruinas.');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion) VALUES ('FIFA 24', 4, 40000, 'Simulador de fútbol profesional.');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion) VALUES ('Cyberpunk 2077', 3, 35000, 'Mundo futurista distópico.');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion) VALUES ('Minecraft', 5, 25000, 'Construye y explora mundos infinitos de bloques.');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion) VALUES ('Red Dead Redemption 2', 2, 48000, 'Una historia de forajidos en el ocaso del salvaje oeste.');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion) VALUES ('Among Us', 6, 5000, 'Descubre al impostor antes de que elimine a toda la tripulación.');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion) VALUES ('Baldur''s Gate 3', 1, 52000, 'Un RPG profundo basado en el universo de Dungeons & Dragons.');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion) VALUES ('Stardew Valley', 5, 10000, 'Hereda la vieja granja de tu abuelo y comienza una nueva vida.');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion) VALUES ('God of War Ragnarok', 3, 60000, 'Kratos y Atreus emprenden un viaje mítico por los Nueve Reinos.');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion) VALUES ('Portal 2', 9, 8000, 'Resuelve acertijos espaciales usando portales en Aperture Science.');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion) VALUES ('Street Fighter 6', 7, 42000, 'La evolución definitiva del juego de peleas legendario.');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion) VALUES ('Resident Evil 4', 8, 45000, 'Sobrevive a una pesadilla biológica en una aldea rural europea.');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion) VALUES ('Age of Empires II', 6, 15000, 'Construye tu imperio y conquista civilizaciones históricas.');