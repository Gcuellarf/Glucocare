<?php
require_once __DIR__ . '/config.php';

header('Content-Type: application/json; charset=utf-8');

$medico_id = $_POST['medico_id'] ?? null;
$titulo = trim($_POST['titulo'] ?? '');
$descripcion = trim($_POST['descripcion'] ?? '');

if (!$medico_id || !$titulo || !isset($_FILES['archivo'])) {
    echo json_encode([
        'ok' => false,
        'message' => 'Faltan datos para subir el contenido.'
    ]);
    exit;
}

if ($_FILES['archivo']['error'] !== UPLOAD_ERR_OK) {
    echo json_encode([
        'ok' => false,
        'message' => 'Error al recibir el archivo.'
    ]);
    exit;
}

$uploadDir = __DIR__ . '/../uploads/';

if (!is_dir($uploadDir)) {
    mkdir($uploadDir, 0777, true);
}

$nombreOriginal = $_FILES['archivo']['name'];
$tmp = $_FILES['archivo']['tmp_name'];
$tipo = $_FILES['archivo']['type'];

$extension = pathinfo($nombreOriginal, PATHINFO_EXTENSION);
$nombreSeguro = preg_replace('/[^a-zA-Z0-9_\.-]/', '_', pathinfo($nombreOriginal, PATHINFO_FILENAME));
$nombreFinal = time() . '_' . $nombreSeguro . '.' . $extension;

$rutaFinal = $uploadDir . $nombreFinal;
$rutaBD = 'uploads/' . $nombreFinal;

if (!move_uploaded_file($tmp, $rutaFinal)) {
    echo json_encode([
        'ok' => false,
        'message' => 'No se pudo guardar el archivo en la carpeta uploads.'
    ]);
    exit;
}

try {
    $stmt = $pdo->prepare("
        INSERT INTO contenidos (medico_id, titulo, descripcion, archivo, tipo)
        VALUES (?, ?, ?, ?, ?)
    ");

    $stmt->execute([
        $medico_id,
        $titulo,
        $descripcion,
        $rutaBD,
        $tipo
    ]);

    echo json_encode([
        'ok' => true,
        'message' => 'Contenido subido correctamente.'
    ]);

} catch (Exception $e) {
    echo json_encode([
        'ok' => false,
        'message' => 'Error al guardar en la base de datos: ' . $e->getMessage()
    ]);
}