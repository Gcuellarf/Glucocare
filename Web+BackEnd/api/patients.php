<?php
require_once __DIR__ . '/config.php';

header('Content-Type: application/json; charset=utf-8');

$method = $_SERVER['REQUEST_METHOD'];

/* =====================================================
   POST: agregar paciente desde la app web
===================================================== */
if ($method === 'POST') {
    $data = json_decode(file_get_contents('php://input'), true);

    $medico_id = $data['medico_id'] ?? null;
    $nombre = trim($data['nombre'] ?? '');
    $apellido = trim($data['apellido'] ?? '');
    $sexo = trim($data['sexo'] ?? 'N');
    $tipo_diabetes = trim($data['tipo_diabetes'] ?? '');
    $ultima_glucosa = intval($data['ultima_glucosa'] ?? 0);

    if (!$medico_id || !$nombre) {
        echo json_encode([
            'ok' => false,
            'message' => 'Faltan datos para registrar el paciente.'
        ]);
        exit;
    }

    $usernameBase = strtolower($nombre . $apellido);
    $usernameBase = preg_replace('/[^a-z0-9]/', '', $usernameBase);
    $username = $usernameBase ?: 'paciente';
    $username = $username . rand(100, 999);

    if ($ultima_glucosa > 180) {
        $estado = 'red';
    } elseif ($ultima_glucosa >= 100) {
        $estado = 'yellow';
    } else {
        $estado = 'green';
    }

    try {
        $stmt = $pdo->prepare("
            INSERT INTO usuarios 
            (
                username,
                nombre,
                apellido,
                correo,
                contrasena,
                sexo,
                peso,
                altura,
                fechaNacimiento,
                contactoEmergencia,
                medico_id,
                tipo_diabetes,
                ultima_glucosa,
                ultima_glucosa_tiempo,
                estado_semaforo_web,
                adherencia,
                proxima_cita,
                tiene_novedad
            )
            VALUES 
            (
                ?, ?, ?, '', '1234', ?, 0, 0, '', '',
                ?, ?, ?, 'Registrado desde web', ?, 0, NULL, 0
            )
        ");

        $stmt->execute([
            $username,
            $nombre,
            $apellido,
            $sexo,
            $medico_id,
            $tipo_diabetes,
            $ultima_glucosa,
            $estado
        ]);

        if ($ultima_glucosa > 0) {
            $stmtGlucemia = $pdo->prepare("
                INSERT INTO glucemias 
                (username, valor, momento, fecha, hora, estadoSemaforo)
                VALUES (?, ?, 'REGISTRO WEB', DATE_FORMAT(CURDATE(), '%d/%m/%Y'), TIME_FORMAT(CURTIME(), '%h:%i %p'), ?)
            ");

            $stmtGlucemia->execute([
                $username,
                $ultima_glucosa,
                strtoupper($estado)
            ]);
        }

        echo json_encode([
            'ok' => true,
            'message' => 'Paciente registrado correctamente.'
        ]);
        exit;

    } catch (Exception $e) {
        echo json_encode([
            'ok' => false,
            'message' => 'Error al registrar paciente: ' . $e->getMessage()
        ]);
        exit;
    }
}

/* =====================================================
   GET: listar pacientes del médico con datos móviles
===================================================== */

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
        SELECT 
            u.username,
            u.nombre,
            u.apellido,
            u.correo,
            u.sexo,
            u.peso,
            u.altura,
            u.fechaNacimiento,
            u.contactoEmergencia,
            u.tipo_diabetes,
            u.adherencia,
            u.proxima_cita,

            g.valor AS ultima_glucosa_real,
            g.fecha AS ultima_glucosa_fecha,
            g.hora AS ultima_glucosa_hora,
            g.estadoSemaforo AS ultima_glucosa_estado,

            CASE 
                WHEN n.id IS NULL THEN 0
                ELSE 1
            END AS tiene_novedad_real

        FROM usuarios u

        LEFT JOIN glucemias g 
            ON g.id = (
                SELECT g2.id 
                FROM glucemias g2
                WHERE g2.username = u.username
                ORDER BY g2.id DESC
                LIMIT 1
            )

        LEFT JOIN novedades n
            ON n.id = (
                SELECT n2.id
                FROM novedades n2
                WHERE n2.username = u.username
                ORDER BY n2.id DESC
                LIMIT 1
            )

        WHERE u.medico_id = ?
        ORDER BY u.nombre ASC
    ");

    $stmt->execute([$medico_id]);
    $usuarios = $stmt->fetchAll(PDO::FETCH_ASSOC);

    $patients = [];

    foreach ($usuarios as $u) {
        $username = $u['username'];

        /* ==========================
           Glucemias del paciente
        ========================== */
        $stmtGlucemias = $pdo->prepare("
            SELECT valor, momento, fecha, hora, estadoSemaforo
            FROM glucemias
            WHERE username = ?
            ORDER BY id ASC
        ");
        $stmtGlucemias->execute([$username]);
        $glucemias = $stmtGlucemias->fetchAll(PDO::FETCH_ASSOC);

        $glucoseReadings = [];

        foreach ($glucemias as $g) {
            $estadoRaw = strtoupper($g['estadoSemaforo'] ?? '');

            if ($estadoRaw === 'ALTO' || $estadoRaw === 'RED') {
                $alert = 'red';
            } elseif ($estadoRaw === 'VIGILAR' || $estadoRaw === 'YELLOW') {
                $alert = 'yellow';
            } else {
                $valor = intval($g['valor']);
                if ($valor > 180) {
                    $alert = 'red';
                } elseif ($valor >= 100) {
                    $alert = 'yellow';
                } else {
                    $alert = 'green';
                }
            }

            $glucoseReadings[] = [
                'date' => $g['fecha'],
                'time' => $g['hora'],
                'value' => intval($g['valor']),
                'type' => strtoupper($g['momento'] ?? '') === 'ANTES' ? 'pre' : 'post',
                'alert' => $alert
            ];
        }

        /* ==========================
           Novedades del paciente
        ========================== */
        $stmtNovedades = $pdo->prepare("
            SELECT *
            FROM novedades
            WHERE username = ?
            ORDER BY id DESC
        ");
        $stmtNovedades->execute([$username]);
        $novedadesDB = $stmtNovedades->fetchAll(PDO::FETCH_ASSOC);

        $novedades = [];

        foreach ($novedadesDB as $n) {
            $novedades[] = [
                'date' => $n['fecha'],
                'time' => $n['hora'],
                'symptoms' => [
                    'nauseas' => intval($n['nauseas']) === 1,
                    'vomito' => intval($n['vomito']) === 1,
                    'respira' => intval($n['cuestaRespirar']) === 1,
                    'abdomen' => intval($n['dolorAbdomen']) === 1,
                    'sueno' => intval($n['muchoSueno']) === 1,
                    'debil' => intval($n['debil']) === 1,
                    'cabeza' => intval($n['dolorCabeza']) === 1,
                    'temblando' => intval($n['temblando']) === 1,
                    'palido' => intval($n['palido']) === 1,
                    'sudando' => intval($n['sudando']) === 1,
                    'otro' => $n['otroSintoma'] ?? ''
                ]
            ];
        }

        /* ==========================
           Medicamentos del paciente
        ========================== */
        $stmtMedicamentos = $pdo->prepare("
            SELECT nombre, dosis, horaProgramada, estado
            FROM medicamentos
            WHERE username = ?
            ORDER BY id ASC
        ");
        $stmtMedicamentos->execute([$username]);
        $medicamentosDB = $stmtMedicamentos->fetchAll(PDO::FETCH_ASSOC);

        $medicamentosAgrupados = [];

        foreach ($medicamentosDB as $m) {
            $key = $m['nombre'] . '|' . $m['dosis'];

            if (!isset($medicamentosAgrupados[$key])) {
                $medicamentosAgrupados[$key] = [
                    'name' => $m['nombre'],
                    'dose' => $m['dosis'],
                    'times' => [],
                    'status' => []
                ];
            }

            $estadoMed = strtolower($m['estado'] ?? 'pending');

            if ($estadoMed === 'tomado' || $estadoMed === 'taken') {
                $estadoFront = 'taken';
            } elseif ($estadoMed === 'omitido' || $estadoMed === 'missed') {
                $estadoFront = 'missed';
            } else {
                $estadoFront = 'pending';
            }

            $medicamentosAgrupados[$key]['times'][] = $m['horaProgramada'];
            $medicamentosAgrupados[$key]['status'][] = $estadoFront;
        }

        $medications = array_values($medicamentosAgrupados);

        /* ==========================
           Citas del paciente
        ========================== */
        $stmtCitas = $pdo->prepare("
            SELECT titulo, lugar, hora, fechaIso
            FROM citas
            WHERE username = ?
            ORDER BY fechaIso ASC
        ");
        $stmtCitas->execute([$username]);
        $citasDB = $stmtCitas->fetchAll(PDO::FETCH_ASSOC);

        $appointments = [];

        foreach ($citasDB as $c) {
            $appointments[] = [
                'name' => $c['titulo'] ?: 'Cita médica',
                'date' => $c['fechaIso'],
                'time' => $c['hora'],
                'location' => $c['lugar']
            ];
        }

        /* ==========================
           Última glucosa y estado
        ========================== */
        $ultimaGlucosa = intval($u['ultima_glucosa_real'] ?? 0);

        if ($ultimaGlucosa > 180) {
            $estadoPaciente = 'red';
        } elseif ($ultimaGlucosa >= 100) {
            $estadoPaciente = 'yellow';
        } else {
            $estadoPaciente = 'green';
        }

        $ultimaGlucosaTiempo = 'Sin registro';

        if (!empty($u['ultima_glucosa_fecha']) || !empty($u['ultima_glucosa_hora'])) {
            $ultimaGlucosaTiempo = trim(($u['ultima_glucosa_fecha'] ?? '') . ' ' . ($u['ultima_glucosa_hora'] ?? ''));
        }

        $patients[] = [
            'id' => $username,
            'username' => $username,

            'nombre' => $u['nombre'],
            'apellido' => $u['apellido'],
            'name' => trim(($u['nombre'] ?? '') . ' ' . ($u['apellido'] ?? '')),

            'correo' => $u['correo'],
            'email' => $u['correo'],

            'sexo' => $u['sexo'],
            'sex' => $u['sexo'],

            'peso' => $u['peso'],
            'weight' => $u['peso'],

            'estatura' => $u['altura'],
            'height' => $u['altura'],

            'fecha_nacimiento' => $u['fechaNacimiento'],
            'dob' => $u['fechaNacimiento'],

            'contacto_emergencia' => $u['contactoEmergencia'],
            'emergency' => $u['contactoEmergencia'],

            'tipo_diabetes' => $u['tipo_diabetes'] ?: 'Sin registrar',

            'ultima_glucosa' => $ultimaGlucosa,
            'lastGlucose' => $ultimaGlucosa,

            'ultima_glucosa_tiempo' => $ultimaGlucosaTiempo,
            'lastGlucoseTime' => $ultimaGlucosaTiempo,

            'estado' => $estadoPaciente,
            'alert' => $estadoPaciente,

            'adherencia' => intval($u['adherencia'] ?? 0),
            'adherence' => intval($u['adherencia'] ?? 0),

            'proxima_cita' => $u['proxima_cita'],
            'nextAppt' => $u['proxima_cita'],

            'novedad' => intval($u['tiene_novedad_real']) === 1,
            'hasAlert' => intval($u['tiene_novedad_real']) === 1,

            'glucoseReadings' => $glucoseReadings,
            'novedades' => $novedades,
            'medications' => $medications,
            'appointments' => $appointments
        ];
    }

    echo json_encode([
        'ok' => true,
        'patients' => $patients,
        'pacientes' => $patients
    ]);

} catch (Exception $e) {
    echo json_encode([
        'ok' => false,
        'message' => 'Error al cargar pacientes: ' . $e->getMessage()
    ]);
}