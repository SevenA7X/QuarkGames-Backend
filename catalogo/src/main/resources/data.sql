-- 1. Inserción de Categorías
INSERT INTO categoria (nombre) VALUES ('RPG');
INSERT INTO categoria (nombre) VALUES ('Aventura');
INSERT INTO categoria (nombre) VALUES ('Accion');
INSERT INTO categoria (nombre) VALUES ('Deportes');
INSERT INTO categoria (nombre) VALUES ('Simulacion');
INSERT INTO categoria (nombre) VALUES ('Estrategia');
INSERT INTO categoria (nombre) VALUES ('Lucha');
INSERT INTO categoria (nombre) VALUES ('Terror');
INSERT INTO categoria (nombre) VALUES ('Puzzles');

-- 2. Inserción de Videojuegos
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion, stock, imagen) VALUES ('Elden Ring', 1, 45000, 'Explora las Tierras Intermedias.', 10, 'elden_ring.jpg');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion, stock, imagen) VALUES ('The Legend of Zelda', 2, 55000, 'Una aventura épica de mundo abierto.', 15, 'zelda.jpg');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion, stock, imagen) VALUES ('Hollow Knight', 3, 12000, 'Un viaje épico a través de un reino de insectos en ruinas.', 20, 'hollow_knight.jpg');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion, stock, imagen) VALUES ('FIFA 24', 4, 40000, 'Simulador de fútbol profesional.', 25, 'fifa_24.jpg');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion, stock, imagen) VALUES ('Cyberpunk 2077', 3, 35000, 'Mundo futurista distópico.', 12, 'cyberpunk_2077.jpg');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion, stock, imagen) VALUES ('Minecraft', 5, 25000, 'Construye y explora mundos infinitos de bloques.', 30, 'minecraft.jpg');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion, stock, imagen) VALUES ('Red Dead Redemption 2', 2, 48000, 'Una historia de forajidos en el ocaso del salvaje oeste.', 18, 'red_dead_redemption_2.jpg');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion, stock, imagen) VALUES ('Among Us', 6, 5000, 'Descubre al impostor antes de que elimine a toda la tripulación.', 50, 'among_us.jpg');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion, stock, imagen) VALUES ('Baldur''s Gate 3', 1, 52000, 'Un RPG profundo basado en el universo de Dungeons & Dragons.', 8, 'baldurs_gate_3.jpg');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion, stock, imagen) VALUES ('Stardew Valley', 5, 10000, 'Hereda la vieja granja de tu abuelo y comienza una nueva vida.', 40, 'stardew_valley.jpg');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion, stock, imagen) VALUES ('God of War Ragnarok', 3, 60000, 'Kratos y Atreus emprenden un viaje mítico por los Nueve Reinos.', 15, 'god_of_war_ragnarok.jpg');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion, stock, imagen) VALUES ('Portal 2', 9, 8000, 'Resuelve acertijos espaciales usando portales en Aperture Science.', 25, 'portal_2.jpg');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion, stock, imagen) VALUES ('Street Fighter 6', 7, 42000, 'La evolución definitiva del juego de peleas legendario.', 20, 'street_fighter_6.jpg');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion, stock, imagen) VALUES ('Resident Evil 4', 8, 45000, 'Sobrevive a una pesadilla biológica en una aldea rural europea.', 15, 'resident_evil_4.jpg');
INSERT INTO videojuego (titulo, categoria_id, precio, descripcion, stock, imagen) VALUES ('Age of Empires II', 6, 15000, 'Construye tu imperio y conquista civilizaciones históricas.', 35, 'age_of_empires_ii.jpg');