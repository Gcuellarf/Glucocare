<?php
require_once __DIR__ . '/config.php';

header('Content-Type: application/json; charset=utf-8');

$medico_id = $_GET['medico_id'] ?? null;

if (!$medico_id) {
    echo json_encode([
        'ok' => false,
        'message' => 'Falta medico_id.'
    ]);
    exit;
}

try {
    $stmt = $pdo->prepare("
        SELECT id, titulo, descripcion, archivo, tipo, fecha_subida
        FROM contenidos
        WHERE medico_id = ?
        ORDER BY fecha_subida DESC
    ");

    $stmt->execute([$medico_id]);

    echo json_encode([
        'ok' => true,
        'contenidos' => $stmt->fetchAll(PDO::FETCH_ASSOC)
    ]);

} catch (Exception $e) {
    echo json_encode([
        'ok' => false,
        'message' => 'Error al cargar contenidos: ' . $e->getMessage()
    ]);
}