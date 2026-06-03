<?php
require_once __DIR__ . '/config.php';
$data = input_json();
$email = trim($data['email'] ?? $data['user'] ?? '');
$password = trim($data['password'] ?? '');

if ($email === '' || $password === '') {
    http_response_code(400);
    echo json_encode(['ok' => false, 'message' => 'Correo y contraseña son obligatorios']);
    exit;
}

$stmt = $pdo->prepare('SELECT id, nombre, apellido, email, especialidad, password_hash FROM medicos WHERE email = ? LIMIT 1');
$stmt->execute([$email]);
$medico = $stmt->fetch();

if (!$medico || !password_verify($password, $medico['password_hash'])) {
    http_response_code(401);
    echo json_encode(['ok' => false, 'message' => 'Credenciales incorrectas']);
    exit;
}

unset($medico['password_hash']);
echo json_encode(['ok' => true, 'medico' => $medico]);
