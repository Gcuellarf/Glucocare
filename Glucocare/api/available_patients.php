<?php
require_once __DIR__ . '/config.php';

header('Content-Type: application/json; charset=utf-8');

try {
    $stmt = $pdo->prepare("
        SELECT 
            username,
            nombre,
            apellido,
            correo,
            sexo,
            fechaNacimiento
        FROM usuarios
        WHERE medico_id IS NULL
        ORDER BY nombre ASC, apellido ASC
    ");

    $stmt->execute();

    echo json_encode([
        'ok' => true,
        'patients' => $stmt->fetchAll(PDO::FETCH_ASSOC)
    ]);

} catch (Exception $e) {
    echo json_encode([
        'ok' => false,
        'message' => 'Error al cargar pacientes disponibles: ' . $e->getMessage()
    ]);
}