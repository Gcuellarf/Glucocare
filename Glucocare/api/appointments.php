<?php
require_once __DIR__ . '/config.php';
$stmt = $pdo->query("SELECT c.id, c.titulo, c.fecha, c.hora, c.estado, p.nombre AS paciente_nombre, p.apellido AS paciente_apellido FROM citas c INNER JOIN pacientes p ON p.id = c.paciente_id WHERE c.fecha >= CURDATE() ORDER BY c.fecha ASC, c.hora ASC");
echo json_encode(['ok' => true, 'appointments' => $stmt->fetchAll()]);
