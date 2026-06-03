-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 02-06-2026 a las 18:12:38
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `glucocareplus`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `citas`
--

CREATE TABLE `citas` (
  `id` int(11) NOT NULL,
  `username` varchar(50) DEFAULT NULL,
  `titulo` varchar(100) DEFAULT NULL,
  `medico` varchar(100) DEFAULT NULL,
  `lugar` varchar(100) DEFAULT NULL,
  `hora` varchar(20) DEFAULT NULL,
  `fechaIso` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `glucemias`
--

CREATE TABLE `glucemias` (
  `id` int(11) NOT NULL,
  `username` varchar(50) DEFAULT NULL,
  `valor` int(11) DEFAULT NULL,
  `momento` varchar(30) DEFAULT NULL,
  `fecha` varchar(20) DEFAULT NULL,
  `hora` varchar(20) DEFAULT NULL,
  `estadoSemaforo` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `glucemias`
--

INSERT INTO `glucemias` (`id`, `username`, `valor`, `momento`, `fecha`, `hora`, `estadoSemaforo`) VALUES
(1, 'luis', 105, 'ANTES', '01/06/2026', '11:55 p.m.', 'NORMAL'),
(2, 'luis', 110, 'ANTES', '01/06/2026', '11:56 p.m.', 'NORMAL');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `medicamentos`
--

CREATE TABLE `medicamentos` (
  `id` int(11) NOT NULL,
  `username` varchar(50) DEFAULT NULL,
  `nombre` varchar(100) DEFAULT NULL,
  `dosis` varchar(50) DEFAULT NULL,
  `horaProgramada` varchar(20) DEFAULT NULL,
  `estado` varchar(20) DEFAULT NULL,
  `horaTomada` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `medicos`
--

CREATE TABLE `medicos` (
  `id` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `apellido` varchar(100) NOT NULL,
  `email` varchar(150) NOT NULL,
  `especialidad` varchar(120) DEFAULT 'Endocrinologia',
  `password_hash` varchar(255) NOT NULL,
  `fecha_registro` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `medicos`
--

INSERT INTO `medicos` (`id`, `nombre`, `apellido`, `email`, `especialidad`, `password_hash`, `fecha_registro`) VALUES
(1, 'Paula', 'Sarzosa', 'paula@correo.com', 'Endocrinologia', '$2y$10$9w3zeWZogt9TNK3gbKTIU..d0Pk9H02OeK8h6DWtPBwOkuxK/ntey', '2026-06-02 14:29:23');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `novedades`
--

CREATE TABLE `novedades` (
  `id` int(11) NOT NULL,
  `username` varchar(50) DEFAULT NULL,
  `fecha` varchar(20) DEFAULT NULL,
  `hora` varchar(20) DEFAULT NULL,
  `nauseas` tinyint(1) DEFAULT NULL,
  `vomito` tinyint(1) DEFAULT NULL,
  `cuestaRespirar` tinyint(1) DEFAULT NULL,
  `dolorAbdomen` tinyint(1) DEFAULT NULL,
  `muchoSueno` tinyint(1) DEFAULT NULL,
  `debil` tinyint(1) DEFAULT NULL,
  `dolorCabeza` tinyint(1) DEFAULT NULL,
  `temblando` tinyint(1) DEFAULT NULL,
  `palido` tinyint(1) DEFAULT NULL,
  `sudando` tinyint(1) DEFAULT NULL,
  `otroSintoma` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `novedades`
--

INSERT INTO `novedades` (`id`, `username`, `fecha`, `hora`, `nauseas`, `vomito`, `cuestaRespirar`, `dolorAbdomen`, `muchoSueno`, `debil`, `dolorCabeza`, `temblando`, `palido`, `sudando`, `otroSintoma`) VALUES
(1, 'luis', '02/06/2026', '09:33 a.m.', 0, 0, 0, 1, 0, 0, 1, 0, 1, 0, '');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios`
--

CREATE TABLE `usuarios` (
  `username` varchar(50) NOT NULL,
  `nombre` varchar(100) DEFAULT NULL,
  `apellido` varchar(100) DEFAULT NULL,
  `correo` varchar(100) DEFAULT NULL,
  `contrasena` varchar(255) DEFAULT NULL,
  `sexo` varchar(20) DEFAULT NULL,
  `peso` double DEFAULT NULL,
  `altura` int(11) DEFAULT NULL,
  `fechaNacimiento` varchar(20) DEFAULT NULL,
  `contactoEmergencia` varchar(20) DEFAULT NULL,
  `medico_id` int(11) DEFAULT NULL,
  `tipo_diabetes` varchar(80) DEFAULT '',
  `ultima_glucosa` int(11) DEFAULT 0,
  `ultima_glucosa_tiempo` varchar(80) DEFAULT 'Sin registro',
  `estado_semaforo_web` varchar(20) DEFAULT 'green',
  `adherencia` int(11) DEFAULT 0,
  `proxima_cita` date DEFAULT NULL,
  `tiene_novedad` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `usuarios`
--

INSERT INTO `usuarios` (`username`, `nombre`, `apellido`, `correo`, `contrasena`, `sexo`, `peso`, `altura`, `fechaNacimiento`, `contactoEmergencia`, `medico_id`, `tipo_diabetes`, `ultima_glucosa`, `ultima_glucosa_tiempo`, `estado_semaforo_web`, `adherencia`, `proxima_cita`, `tiene_novedad`) VALUES
('luis', 'Luis', 'Gomez', 'luis@correo.com', '1234', 'Masculino', 70, 170, '2000-01-01', '3001234567', NULL, '', 0, 'Sin registro', 'green', 0, NULL, 0);

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `citas`
--
ALTER TABLE `citas`
  ADD PRIMARY KEY (`id`),
  ADD KEY `username` (`username`);

--
-- Indices de la tabla `glucemias`
--
ALTER TABLE `glucemias`
  ADD PRIMARY KEY (`id`),
  ADD KEY `username` (`username`);

--
-- Indices de la tabla `medicamentos`
--
ALTER TABLE `medicamentos`
  ADD PRIMARY KEY (`id`),
  ADD KEY `username` (`username`);

--
-- Indices de la tabla `medicos`
--
ALTER TABLE `medicos`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Indices de la tabla `novedades`
--
ALTER TABLE `novedades`
  ADD PRIMARY KEY (`id`),
  ADD KEY `username` (`username`);

--
-- Indices de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  ADD PRIMARY KEY (`username`),
  ADD KEY `fk_usuarios_medico` (`medico_id`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `citas`
--
ALTER TABLE `citas`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `glucemias`
--
ALTER TABLE `glucemias`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT de la tabla `medicamentos`
--
ALTER TABLE `medicamentos`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `medicos`
--
ALTER TABLE `medicos`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `novedades`
--
ALTER TABLE `novedades`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `citas`
--
ALTER TABLE `citas`
  ADD CONSTRAINT `citas_ibfk_1` FOREIGN KEY (`username`) REFERENCES `usuarios` (`username`);

--
-- Filtros para la tabla `glucemias`
--
ALTER TABLE `glucemias`
  ADD CONSTRAINT `glucemias_ibfk_1` FOREIGN KEY (`username`) REFERENCES `usuarios` (`username`);

--
-- Filtros para la tabla `medicamentos`
--
ALTER TABLE `medicamentos`
  ADD CONSTRAINT `medicamentos_ibfk_1` FOREIGN KEY (`username`) REFERENCES `usuarios` (`username`);

--
-- Filtros para la tabla `novedades`
--
ALTER TABLE `novedades`
  ADD CONSTRAINT `novedades_ibfk_1` FOREIGN KEY (`username`) REFERENCES `usuarios` (`username`);

--
-- Filtros para la tabla `usuarios`
--
ALTER TABLE `usuarios`
  ADD CONSTRAINT `fk_usuarios_medico` FOREIGN KEY (`medico_id`) REFERENCES `medicos` (`id`) ON DELETE SET NULL;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
