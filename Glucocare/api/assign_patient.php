<?php
require_once __DIR__ . '/config.php';

header('Content-Type: application/json; charset=utf-8');

$data = json_decode(file_get_contents('php://input'), true);

$medico_id = $data['medico_id'] ?? null;
$username = $data['username'] ?? null;

if (!$medico_id || !$username) {
    echo json_encode([
        'ok' => false,
        'message' => 'Faltan datos para asignar el paciente.'
    ]);
    exit;
}

try {
    $stmt = $pdo->prepare("
        UPDATE usuarios
        SET medico_id = ?
        WHERE username = ?
    ");

    $stmt->execute([
        $medico_id,
        $username
    ]);

    if ($stmt->rowCount() === 0) {
        echo json_encode([
            'ok' => false,
            'message' => 'No se encontró el paciente o ya estaba asignado.'
        ]);
        exit;
    }

    echo json_encode([
        'ok' => true,
        'message' => 'Paciente asignado correctamente.'
    ]);

} catch (Exception $e) {
    echo json_encode([
        'ok' => false,
        'message' => 'Error al asignar paciente: ' . $e->getMessage()
    ]);
}