<?php
require_once __DIR__ . '/config.php';
$data = input_json();

$nombre = trim($data['nombre'] ?? '');
$apellido = trim($data['apellido'] ?? '');
$email = trim($data['email'] ?? '');
$especialidad = trim($data['especialidad'] ?? 'Endocrinología');
$password = trim($data['password'] ?? '');

if ($nombre === '' || $apellido === '' || $email === '' || $password === '') {
    http_response_code(400);
    echo json_encode(['ok' => false, 'message' => 'Todos los campos obligatorios deben estar completos']);
    exit;
}

if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    http_response_code(400);
    echo json_encode(['ok' => false, 'message' => 'Correo no válido']);
    exit;
}

try {
    $hash = password_hash($password, PASSWORD_DEFAULT);
    $stmt = $pdo->prepare('INSERT INTO medicos (nombre, apellido, email, especialidad, password_hash) VALUES (?, ?, ?, ?, ?)');
    $stmt->execute([$nombre, $apellido, $email, $especialidad, $hash]);
    echo json_encode(['ok' => true, 'message' => 'Registro exitoso']);
} catch (PDOException $e) {
    if ($e->getCode() === '23000') {
        http_response_code(409);
        echo json_encode(['ok' => false, 'message' => 'Ese correo ya está registrado']);
    } else {
        http_response_code(500);
        echo json_encode(['ok' => false, 'message' => 'Error al registrar usuario']);
    }
}
