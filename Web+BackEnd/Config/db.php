<?php

$host = "127.0.0.1";
$port = 3307;
$user = "root";
$password = "";
$database = "glucocareplus";

$conn = new mysqli($host, $user, $password, $database, $port);

if ($conn->connect_error) {
    die("Error de conexión: " . $conn->connect_error);
}

$conn->set_charset("utf8mb4");

?>