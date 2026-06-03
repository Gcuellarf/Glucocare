<?php
require_once __DIR__ . '/config.php';

header('Content-Type: application/json; charset=utf-8');

try {
    $stmt = $pdo->prepare("
        SELECT 
            c.id,
            c.titulo,
            c.descripcion,
            c.archivo,
            c.tipo,
            c.fecha_subida,
            m.nombre AS medico_nombre,
            m.apellido AS medico_apellido
        FROM contenidos c
        LEFT JOIN medicos m ON c.medico_id = m.id
        ORDER BY c.fecha_subida DESC
    ");

    $stmt->execute();

    $contenidos = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo json_encode([
        'ok' => true,
        'contenidos' => $contenidos
    ]);

} catch (Exception $e) {
    echo json_encode([
        'ok' => false,
        'message' => 'Error al cargar el contenido: ' . $e->getMessage()
    ]);
}

//http://localhost/glucocare/api/contents_mobile.php