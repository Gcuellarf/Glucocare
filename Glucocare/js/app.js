/* ==========================================
   GLUCOCARE — MAIN APP JAVASCRIPT
   Medical Portal
   ========================================== */

// ==========================================
// MOCK DATA
// ==========================================

let PATIENTS = [];
let currentMedico = null;

function getCurrentMedico() {
  try {
    return JSON.parse(localStorage.getItem('glucocare_medico'));
  } catch (e) {
    return null;
  }
}

function applyDoctorProfile() {
  currentMedico = getCurrentMedico();

  if (!currentMedico || !currentMedico.id) {
    window.location.href = 'login.html';
    return;
  }

  const nombre = `${currentMedico.nombre || ''} ${currentMedico.apellido || ''}`.trim() || 'Médico';
  const especialidad = currentMedico.especialidad || 'Médico';
  const iniciales = initials(nombre);

  const nameEl = document.getElementById('doctor-name');
  const specialtyEl = document.getElementById('doctor-specialty');
  const avatarEl = document.getElementById('doctor-avatar');

  if (nameEl) nameEl.textContent = nombre;
  if (specialtyEl) specialtyEl.textContent = especialidad;
  if (avatarEl) avatarEl.textContent = iniciales;
}

function normalizePatient(row) {
  const nombreCompleto = `${row.nombre || ''} ${row.apellido || ''}`.trim() || row.name || 'Paciente sin nombre';
  const lastGlucose = Number(row.ultima_glucosa || row.lastGlucose || 0);
  const alert = row.estado || row.alert || row.status || row.estado_semaforo_web || classifyGlucose(lastGlucose);

  return {
    id: String(row.username || row.codigo || row.id || ''),
    dbId: row.id,
    name: nombreCompleto,
    age: Number(row.edad || row.age || 0),
    sex: row.sexo || row.sex || row.gender || 'N',
    weight: Number(row.peso || row.weight || 70),
    height: Number(row.estatura || row.altura || row.height || 165),
    dob: row.fecha_nacimiento || row.fechaNacimiento || row.dob || row.birthDate || '—',
    email: row.email || row.correo || '—',
    emergency: row.contacto_emergencia || row.contactoEmergencia || row.emergency || 'Sin contacto / —',
    avatarColor: row.avatar_color || '#00b7eb',
    alert,
    lastGlucose,
    lastGlucoseTime: row.ultima_glucosa_tiempo || row.lastGlucoseTime || 'Sin registro',
    adherence: Number(row.adherencia || row.adherence || 0),
    nextAppt: row.proxima_cita || row.nextAppt || row.nextAppointment || '',
    novedad: Number(row.novedad || row.tiene_novedad || row.hasAlert || 0) === 1 || row.novedad === true || row.hasAlert === true,
    appointments: row.appointments || [],
    medications: row.medications || [],
    glucoseReadings: row.glucoseReadings || [],
    novedades: row.novedades || []
  };
}

function classifyGlucose(value) {
  value = Number(value || 0);
  if (value > 180) return 'red';
  if (value >= 100) return 'yellow';
  return 'green';
}

async function loadPatientsFromServer() {
  if (!currentMedico || !currentMedico.id) return;

  try {
    const response = await fetch(`api/patients.php?medico_id=${encodeURIComponent(currentMedico.id)}`);
    const data = await response.json();

    if (!data.ok) {
      console.error(data.message || 'No se pudieron cargar pacientes');
      PATIENTS = [];
      return;
    }

    PATIENTS = (data.patients || data.pacientes || []).map(normalizePatient);
    const banner = document.getElementById('urgent-alert-banner');

    if (banner) {
      banner.style.display = PATIENTS.length > 0 ? 'flex' : 'none';
    }

  } catch (error) {
    console.error('Error cargando pacientes:', error);
    PATIENTS = [];
  }
}

async function addPatient() {
  if (!currentMedico || !currentMedico.id) {
    alert('No se encontró el médico en sesión.');
    return;
  }

  try {
    const response = await fetch('api/available_patients.php');
    const data = await response.json();

    if (!data.ok) {
      alert(data.message || 'No se pudieron cargar los pacientes disponibles.');
      return;
    }

    const pacientes = data.patients || [];

    if (pacientes.length === 0) {
      alert('No hay pacientes disponibles para asignar. Primero el paciente debe registrarse desde la app móvil.');
      return;
    }

    let mensaje = 'Pacientes disponibles:\n\n';

    pacientes.forEach((p, index) => {
      const nombreCompleto = `${p.nombre || ''} ${p.apellido || ''}`.trim();
      mensaje += `${index + 1}. ${nombreCompleto} — ${p.username}\n`;
    });

    mensaje += '\nEscribe el número del paciente que deseas asignar:';

    const seleccion = prompt(mensaje);

    if (!seleccion) return;

    const index = Number(seleccion) - 1;

    if (isNaN(index) || index < 0 || index >= pacientes.length) {
      alert('Selección inválida.');
      return;
    }

    const pacienteSeleccionado = pacientes[index];

    const confirmacion = confirm(
      `¿Deseas asignar a ${pacienteSeleccionado.nombre} ${pacienteSeleccionado.apellido} a este médico?`
    );

    if (!confirmacion) return;

    const assignResponse = await fetch('api/assign_patient.php', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        medico_id: currentMedico.id,
        username: pacienteSeleccionado.username
      })
    });

    const assignData = await assignResponse.json();

    alert(assignData.message || 'Proceso finalizado.');

    if (assignData.ok) {
      await loadPatientsFromServer();
      renderDashboard();
      renderPatientsTable();
      navigate('patients');
    }

  } catch (error) {
    console.error(error);
    alert('No se pudo conectar con el servidor. Revisa XAMPP, Apache y MySQL.');
  }
}


const SYMPTOM_LABELS = {
  nauseas: "Náuseas", vomito: "Vómito", respira: "Dificultad respiratoria",
  abdomen: "Dolor abdominal", sueno: "Somnolencia excesiva", debil: "Debilidad",
  cabeza: "Cefalea intensa", temblando: "Temblores", palido: "Palidez",
  sudando: "Sudoración", otro: "Otro"
};

// ==========================================
// STATE
// ==========================================

let currentPatient = null;
let currentTab = "glucose";
let chartInstance = null;

// ==========================================
// NAVIGATION
// ==========================================

async function navigate(page) {
  document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
  document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));

  const pg = document.getElementById('page-' + page);
  if (pg) pg.classList.add('active');

  const nav = document.querySelector(`[data-nav="${page}"]`);
  if (nav) nav.classList.add('active');

  if (page === 'dashboard') {
    await loadPatientsFromServer();
    renderDashboard();
  }

  if (page === 'patients') {
    await loadPatientsFromServer();
    renderPatientsTable();
  }

  if (page === 'alerts') {
    await loadPatientsFromServer();
    renderAlertsPage();
  }

  if (page === 'content') {
    loadContents();
  }
}

// ==========================================
// DASHBOARD
// ==========================================

function renderDashboard() {
  const alertPts = PATIENTS.filter(p => p.alert === 'red').length;
  const watchPts = PATIENTS.filter(p => p.alert === 'yellow').length;
  const stablePts = PATIENTS.filter(p => p.alert === 'green').length;
  const totalAdh = PATIENTS.length ? Math.round(PATIENTS.reduce((a, p) => a + p.adherence, 0) / PATIENTS.length) : 0;

  document.getElementById('stat-alert').textContent = alertPts;
  document.getElementById('stat-watch').textContent = watchPts;
  document.getElementById('stat-stable').textContent = stablePts;
  document.getElementById('stat-adherence').textContent = totalAdh + '%';

  renderRecentActivity();
  renderMiniPatientList();
  renderAdherenceChart();
  renderGlucoseDistChart();
}

function renderRecentActivity() {
  const list = document.getElementById('recent-activity');
  if (!list) return;

  if (PATIENTS.length === 0) {
    list.innerHTML = `
      <div class="empty-state">
        <div style="font-size:1rem;font-weight:600;color:var(--gray-500)">Sin actividad reciente</div>
        <div class="empty-state-text">Agrega pacientes para empezar a ver registros en este panel.</div>
      </div>`;
    return;
  }

  const activities = PATIENTS.slice(0, 5).map(p => ({
    patient: p.name,
    action: p.lastGlucose ? `Glucosa registrada (${p.lastGlucose} mg/dL)` : 'Paciente registrado',
    time: p.lastGlucoseTime || 'Reciente',
    icon: p.alert === 'red' ? 'alert' : 'drop',
    color: p.alert === 'red' ? 'red' : p.alert === 'yellow' ? 'yellow' : 'green'
  }));

  list.innerHTML = activities.map(a => `
    <div class="activity-item">
      <div class="activity-icon activity-${a.color}">${getActivityIcon(a.icon)}</div>
      <div class="activity-info">
        <div class="activity-patient">${a.patient}</div>
        <div class="activity-action">${a.action}</div>
      </div>
      <div class="activity-time">${a.time}</div>
    </div>
  `).join('');
}

function getActivityIcon(type) {
  const icons = {
    alert: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>`,
    warning: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>`,
    pill: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.5 20H4a2 2 0 0 1-2-2V5c0-1.1.9-2 2-2h3.93a2 2 0 0 1 1.66.9l.82 1.2a2 2 0 0 0 1.66.9H20a2 2 0 0 1 2 2v3.5"/><path d="m15 18 5 5"/><path d="m20 18-5 5"/></svg>`,
    drop: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2.69l5.66 5.66a8 8 0 1 1-11.31 0z"/></svg>`,
    calendar: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>`
  };
  return icons[type] || icons.drop;
}

function renderMiniPatientList() {
  const list = document.getElementById('mini-patient-list');
  if (!list) return;

  const sorted = [...PATIENTS].sort((a, b) => {
    const order = { red: 0, yellow: 1, green: 2 };
    return order[a.alert] - order[b.alert];
  });

  if (sorted.length === 0) {
    list.innerHTML = `<div class="empty-state"><div class="empty-state-text">Este médico aún no tiene pacientes.</div></div>`;
    return;
  }

  list.innerHTML = sorted.map(p => `
    <div class="mini-patient-item" onclick="openPatient('${p.id}')">
      <div class="patient-avatar" style="background:${p.avatarColor}">${initials(p.name)}</div>
      <div class="mini-patient-info">
        <div class="mini-patient-name">${p.name}</div>
        <div class="mini-patient-meta">${p.lastGlucose} mg/dL · ${p.lastGlucoseTime}</div>
      </div>
      <span class="badge badge-${p.alert}">
        <span class="badge-dot"></span>
        ${p.alert === 'red' ? 'Alerta' : p.alert === 'yellow' ? 'Vigilar' : 'Estable'}
      </span>
    </div>
  `).join('');
}

function renderAdherenceChart() {
  const canvas = document.getElementById('chart-adherence');
  if (!canvas) return;
  const ctx = canvas.getContext('2d');

  const labels = PATIENTS.map(p => (p.name.split(' ')[0] || '') + ' ' + (p.name.split(' ')[1] || ''));
  const data = PATIENTS.map(p => p.adherence);

  if (PATIENTS.length === 0) {
    if (window.adherenceChart) window.adherenceChart.destroy();
    return;
  }
  const colors = data.map(v => v >= 80 ? '#2ecc71' : v >= 60 ? '#f1c40f' : '#e74c3c');

  if (window.adherenceChart) window.adherenceChart.destroy();
  window.adherenceChart = new Chart(ctx, {
    type: 'bar',
    data: {
      labels,
      datasets: [{
        data,
        backgroundColor: colors,
        borderRadius: 6,
        borderSkipped: false,
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: { legend: { display: false } },
      scales: {
        y: {
          beginAtZero: true, max: 100,
          ticks: { callback: v => v + '%', font: { size: 11 }, color: '#8d9aaa' },
          grid: { color: '#eef1f4' }
        },
        x: {
          ticks: { font: { size: 10 }, color: '#8d9aaa', maxRotation: 30 },
          grid: { display: false }
        }
      }
    }
  });
}

function renderGlucoseDistChart() {
  const canvas = document.getElementById('chart-glucose-dist');
  if (!canvas) return;
  const ctx = canvas.getContext('2d');

  const allReadings = PATIENTS.flatMap(p => p.glucoseReadings || []);
  const red = allReadings.filter(r => r.alert === 'red').length;
  const yellow = allReadings.filter(r => r.alert === 'yellow').length;
  const green = allReadings.filter(r => r.alert === 'green').length;

  if (window.distChart) window.distChart.destroy();
  window.distChart = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: ['Rojo (>180)', 'Amarillo (100-180)', 'Verde (70-100)'],
      datasets: [{
        data: [red, yellow, green],
        backgroundColor: ['#e74c3c', '#f1c40f', '#2ecc71'],
        borderWidth: 0,
        hoverOffset: 6
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      cutout: '72%',
      plugins: {
        legend: {
          position: 'bottom',
          labels: { font: { size: 11 }, color: '#627180', padding: 12, usePointStyle: true }
        }
      }
    }
  });
}

// ==========================================
// PATIENTS TABLE
// ==========================================

function renderPatientsTable(filter = '') {
  const tbody = document.getElementById('patients-tbody');
  if (!tbody) return;

  const countLabel = document.getElementById('patients-count-label');
  if (countLabel) countLabel.textContent = `${PATIENTS.length} paciente${PATIENTS.length === 1 ? '' : 's'} activo${PATIENTS.length === 1 ? '' : 's'}`;

  let pts = PATIENTS;
  if (filter === 'alert') pts = pts.filter(p => p.novedad);
  if (filter === 'red') pts = pts.filter(p => p.alert === 'red');

  if (pts.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="7">
          <div class="empty-state">
            <div style="font-size:1rem;font-weight:600;color:var(--gray-500)">No hay pacientes registrados</div>
            <div class="empty-state-text">Este perfil médico está nuevo. Usa el botón + Agregar paciente para crear el primero.</div>
          </div>
        </td>
      </tr>`;
    return;
  }

  tbody.innerHTML = pts.map(p => {
    const alertClass = p.novedad ? 'patient-row-alert' : '';
    const badgeClass = `badge-${p.alert}`;
    const badgeText = p.alert === 'red' ? 'Alerta' : p.alert === 'yellow' ? 'Vigilar' : 'Estable';
    const adh = p.adherence;
    const adhColor = adh >= 80 ? '#2ecc71' : adh >= 60 ? '#f1c40f' : '#e74c3c';

    return `
      <tr class="${alertClass}" onclick="openPatient('${p.id}')">
        <td>
          <div class="patient-info-cell">
            <div class="patient-avatar" style="background:${p.avatarColor}">${initials(p.name)}</div>
            <div>
              <div class="patient-name">${p.name}${p.novedad ? '<span class="badge badge-alert" style="margin-left:8px;font-size:0.62rem">⚠ Novedad</span>' : ''}</div>
              <div class="patient-id">${p.id}</div>
            </div>
          </div>
        </td>
        <td>${p.age} años · ${p.sex}</td>
        <td>
          <div style="display:flex;align-items:center;gap:8px">
            <strong style="font-size:1rem;color:var(--gray-800)">${p.lastGlucose}</strong>
            <span style="font-size:0.72rem;color:var(--gray-400)">mg/dL · ${p.lastGlucoseTime}</span>
          </div>
        </td>
        <td>
          <div style="display:flex;align-items:center;gap:8px">
            <div style="flex:1;height:6px;background:var(--gray-100);border-radius:3px;min-width:60px">
              <div style="width:${adh}%;height:100%;background:${adhColor};border-radius:3px"></div>
            </div>
            <span style="font-size:0.78rem;font-weight:700;color:${adhColor}">${adh}%</span>
          </div>
        </td>
        <td><span class="badge ${badgeClass}"><span class="badge-dot"></span>${badgeText}</span></td>
        <td>${formatDate(p.nextAppt)}</td>
        <td>
          <button class="btn btn-outline btn-sm" onclick="event.stopPropagation();openPatient('${p.id}')">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
            Ver detalle
          </button>
        </td>
      </tr>
    `;
  }).join('');
}

// ==========================================
// PATIENT DETAIL
// ==========================================

function openPatient(id) {
  currentPatient = PATIENTS.find(p => p.id === id);
  if (!currentPatient) return;

  document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
  document.getElementById('page-patient-detail').classList.add('active');
  renderPatientDetail();
}

function renderPatientDetail() {
  const p = currentPatient;

  // Header
  document.getElementById('detail-name').textContent = p.name;
  document.getElementById('detail-meta').textContent = `${p.id} · ${p.age} años · ${p.sex === 'F' ? 'Femenino' : 'Masculino'}`;
  document.getElementById('detail-avatar').textContent = initials(p.name);
  document.getElementById('detail-avatar').parentElement.style.background = 'rgba(255,255,255,0.15)';

  const alertEl = document.getElementById('detail-alert-badge');
  const badges = { red: 'badge-red', yellow: 'badge-yellow', green: 'badge-green' };
  const labels = { red: '⚠ Alerta', yellow: '◉ Vigilar', green: '✓ Estable' };
  alertEl.className = `badge ${badges[p.alert]}`;
  alertEl.textContent = labels[p.alert];

  openTab('glucose');
}

function openTab(tab) {
  currentTab = tab;
  document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
  document.querySelectorAll('.tab-pane').forEach(t => t.classList.remove('active'));
  const tabEl = document.querySelector(`[data-tab="${tab}"]`);
  if (tabEl) tabEl.classList.add('active');
  const pane = document.getElementById(`tab-${tab}`);
  if (pane) pane.classList.add('active');

  if (tab === 'glucose') renderGlucoseTab();
  if (tab === 'medications') renderMedicationsTab();
  if (tab === 'novedades') renderNovedadesTab();
  if (tab === 'appointments') renderAppointmentsTab();
  if (tab === 'info') renderInfoTab();
}

// ==========================================
// GLUCOSE TAB
// ==========================================

function renderGlucoseTab() {
  const p = currentPatient;
  if (!p) return;

  renderGlucoseTable();
  renderGlucoseLineChart();
}

function renderGlucoseTable() {
  const p = currentPatient;
  const tbody = document.getElementById('glucose-tbody');
  if (!tbody) return;

  if (!p.glucoseReadings || p.glucoseReadings.length === 0) {
    tbody.innerHTML = `<tr><td colspan="5">Sin registros de glucometría para este paciente.</td></tr>`;
    return;
  }

  tbody.innerHTML = [...p.glucoseReadings].reverse().map(r => `
    <tr>
      <td>${r.date}</td>
      <td>${r.time}</td>
      <td>
        <strong style="font-size:1.05rem">${r.value}</strong> <span style="color:var(--gray-400);font-size:0.78rem">mg/dL</span>
      </td>
      <td><span class="badge ${r.type === 'pre' ? 'badge-teal' : 'badge-gray'}">${r.type === 'pre' ? 'Antes de comer' : 'Después de comer'}</span></td>
      <td>
        <span class="badge badge-${r.alert}">
          <span class="badge-dot"></span>
          ${r.alert === 'red' ? 'Alto' : r.alert === 'yellow' ? 'Borderline' : 'Normal'}
        </span>
      </td>
    </tr>
  `).join('');
}

function renderGlucoseLineChart() {
  const p = currentPatient;
  const canvas = document.getElementById('chart-glucose-line');
  if (!canvas) return;
  const ctx = canvas.getContext('2d');

  if (window.glucoseLineChart) window.glucoseLineChart.destroy();

  if (!p.glucoseReadings || p.glucoseReadings.length === 0) return;

  const labels = p.glucoseReadings.map(r => `${r.date.slice(5)} ${r.time}`);
  const values = p.glucoseReadings.map(r => r.value);
  const colors = p.glucoseReadings.map(r =>
    r.alert === 'red' ? '#e74c3c' : r.alert === 'yellow' ? '#f1c40f' : '#2ecc71'
  );

  window.glucoseLineChart = new Chart(ctx, {
    type: 'line',
    data: {
      labels,
      datasets: [
        {
          label: 'Glucosa (mg/dL)',
          data: values,
          borderColor: '#178080',
          backgroundColor: 'rgba(23,128,128,0.08)',
          borderWidth: 2.5,
          pointBackgroundColor: colors,
          pointBorderColor: '#ffffff',
          pointBorderWidth: 2,
          pointRadius: 7,
          pointHoverRadius: 9,
          fill: true,
          tension: 0.35,
        },
        {
          label: 'Límite alto (180)',
          data: Array(values.length).fill(180),
          borderColor: 'rgba(231,76,60,0.3)',
          borderDash: [6, 4],
          borderWidth: 1.5,
          pointRadius: 0,
          fill: false,
        },
        {
          label: 'Límite bajo (70)',
          data: Array(values.length).fill(70),
          borderColor: 'rgba(46,204,113,0.3)',
          borderDash: [6, 4],
          borderWidth: 1.5,
          pointRadius: 0,
          fill: false,
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      interaction: { mode: 'index', intersect: false },
      plugins: {
        legend: {
          labels: { font: { size: 11 }, color: '#627180', usePointStyle: true, padding: 16 }
        },
        tooltip: {
          callbacks: {
            label: (ctx) => ` ${ctx.dataset.label}: ${ctx.parsed.y} mg/dL`
          }
        }
      },
      scales: {
        y: {
          min: 40,
          ticks: { callback: v => v + ' mg/dL', font: { size: 11 }, color: '#8d9aaa' },
          grid: { color: '#eef1f4' }
        },
        x: {
          ticks: { font: { size: 10 }, color: '#8d9aaa', maxRotation: 40 },
          grid: { display: false }
        }
      }
    }
  });
}

// ==========================================
// MEDICATIONS TAB
// ==========================================

function renderMedicationsTab() {
  const p = currentPatient;
  const container = document.getElementById('medications-list');
  if (!container) return;

  const today = new Date().toLocaleDateString('es-CO', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' });

  if (!p.medications || p.medications.length === 0) {
    document.getElementById('med-summary').innerHTML = '';
    container.innerHTML = `<div class="empty-state"><div class="empty-state-text">No hay medicamentos registrados.</div></div>`;
    return;
  }

  const taken = p.medications.flatMap(m => m.status).filter(s => s === 'taken').length;
  const total = p.medications.flatMap(m => m.status).filter(s => s !== 'pending').length + p.medications.flatMap(m => m.status).filter(s => s === 'pending').length;
  const missed = p.medications.flatMap(m => m.status).filter(s => s === 'missed').length;

  document.getElementById('med-summary').innerHTML = `
    <div style="display:flex;gap:16px;margin-bottom:20px;flex-wrap:wrap">
      <div style="background:var(--teal-50);border:1px solid var(--teal-200);border-radius:10px;padding:12px 20px;text-align:center">
        <div style="font-size:1.6rem;font-weight:800;color:var(--teal-600)">${taken}</div>
        <div style="font-size:0.72rem;color:var(--teal-400);text-transform:uppercase;font-weight:600">Tomados</div>
      </div>
      <div style="background:#fff5f5;border:1px solid #fecaca;border-radius:10px;padding:12px 20px;text-align:center">
        <div style="font-size:1.6rem;font-weight:800;color:#b91c1c">${missed}</div>
        <div style="font-size:0.72rem;color:#b91c1c;text-transform:uppercase;font-weight:600">Omitidos</div>
      </div>
      <div style="background:var(--gray-50);border:1px solid var(--gray-200);border-radius:10px;padding:12px 20px;text-align:center">
        <div style="font-size:1.6rem;font-weight:800;color:var(--gray-700)">${Math.round((taken / total) * 100) || 0}%</div>
        <div style="font-size:0.72rem;color:var(--gray-400);text-transform:uppercase;font-weight:600">Adherencia hoy</div>
      </div>
    </div>
    <div style="font-size:0.75rem;color:var(--gray-400);margin-bottom:4px">${today}</div>
  `;

  container.innerHTML = p.medications.map(m => `
    <div class="med-item">
      <div class="med-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.5 20H4a2 2 0 0 1-2-2V5c0-1.1.9-2 2-2h3.93a2 2 0 0 1 1.66.9l.82 1.2a2 2 0 0 0 1.66.9H20a2 2 0 0 1 2 2v3.5"/><circle cx="17" cy="17" r="5"/><path d="M17 14v6M14 17h6"/></svg>
      </div>
      <div class="med-info">
        <div class="med-name">${m.name}</div>
        <div class="med-dose">${m.dose}</div>
        <div class="med-times">
          ${m.times.map((t, i) => `<span class="time-chip ${m.status[i]}">${t} — ${m.status[i] === 'taken' ? '✓ Tomado' : m.status[i] === 'missed' ? '✗ Omitido' : '⏳ Pendiente'}</span>`).join('')}
        </div>
      </div>
    </div>
  `).join('');
}

// ==========================================
// NOVEDADES TAB
// ==========================================

function renderNovedadesTab() {
  const p = currentPatient;
  const container = document.getElementById('novedades-list');
  if (!container) return;

  if (!p.novedades || p.novedades.length === 0) {
    container.innerHTML = `
      <div class="empty-state">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
        <div style="font-size:1rem;font-weight:600;color:var(--gray-500)">Sin novedades</div>
        <div class="empty-state-text">Este paciente no ha reportado ninguna novedad de salud.</div>
      </div>`;
    return;
  }

  container.innerHTML = p.novedades.map(n => {
    const positiveSymptoms = Object.entries(n.symptoms).filter(([k, v]) => v === true || (k === 'otro' && v));
    const isCritical = positiveSymptoms.length >= 4;

    return `
      <div class="novedad-item ${isCritical ? 'critical' : ''}" style="flex-direction:column;align-items:flex-start">
        <div style="display:flex;align-items:center;gap:12px;width:100%;margin-bottom:12px">
          <div style="font-size:1.3rem">${isCritical ? '🚨' : '⚠️'}</div>
          <div>
            <div class="novedad-symptom">${isCritical ? 'Reporte crítico' : 'Reporte de síntomas'}</div>
            <div class="novedad-time">${n.date} · ${n.time}</div>
          </div>
          ${isCritical ? '<span class="badge badge-red" style="margin-left:auto">Crítico</span>' : '<span class="badge badge-yellow" style="margin-left:auto">Moderado</span>'}
        </div>
        <div class="symptom-grid">
          ${Object.entries(n.symptoms).map(([key, val]) => {
            if (!SYMPTOM_LABELS[key]) return '';
            if (key === 'otro' && !val) return '';
            const label = key === 'otro' ? `Otro: ${val}` : SYMPTOM_LABELS[key];
            const isPositive = val === true || (key === 'otro' && val);
            return `<span class="symptom-tag ${isPositive ? 'positive' : 'negative'}">${isPositive ? '✓' : '✗'} ${label}</span>`;
          }).join('')}
        </div>
      </div>
    `;
  }).join('');
}

// ==========================================
// APPOINTMENTS TAB
// ==========================================

function renderAppointmentsTab() {
  const p = currentPatient;
  const container = document.getElementById('appointments-list');
  if (!container) return;

  if (!p.appointments || p.appointments.length === 0) {
    container.innerHTML = `<div class="empty-state"><div class="empty-state-text">No hay citas programadas.</div></div>`;
    return;
  }

  container.innerHTML = p.appointments.map(a => {
    const d = new Date(a.date);
    const day = d.getDate();
    const month = d.toLocaleString('es', { month: 'short' }).toUpperCase();
    return `
      <div class="appointment-item">
        <div class="appt-date-block">
          <div class="appt-day">${day}</div>
          <div class="appt-month">${month}</div>
        </div>
        <div style="flex:1">
          <div class="appt-title">${a.name}</div>
          <div class="appt-meta">${a.date} · ${a.time}</div>
        </div>
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:16px;height:16px;color:var(--gray-300)"><polyline points="9 18 15 12 9 6"/></svg>
      </div>
    `;
  }).join('');

  renderMiniCalendar();
}

function renderMiniCalendar() {
  const p = currentPatient;
  const cal = document.getElementById('mini-calendar');
  if (!cal) return;

  const now = new Date(2026, 5, 1); // June 2026
  const year = now.getFullYear();
  const month = now.getMonth();
  const firstDay = new Date(year, month, 1).getDay();
  const daysInMonth = new Date(year, month + 1, 0).getDate();

  const apptDates = new Set(p.appointments.map(a => new Date(a.date).getDate()));

  const headers = ['D','L','M','M','J','V','S'].map(d =>
    `<div class="cal-header">${d}</div>`
  ).join('');

  let days = Array(firstDay === 0 ? 6 : firstDay - 1).fill('<div class="cal-day inactive"></div>');

  for (let i = 1; i <= daysInMonth; i++) {
    const isToday = i === 1;
    const hasEvent = apptDates.has(i);
    days.push(`<div class="cal-day${isToday ? ' today' : ''}${hasEvent ? ' has-event' : ''}">${i}</div>`);
  }

  cal.innerHTML = headers + days.join('');
}

// ==========================================
// INFO TAB
// ==========================================

function renderInfoTab() {
  const p = currentPatient;
  const container = document.getElementById('patient-info-detail');
  if (!container) return;

  const bmi = p.weight && p.height ? (p.weight / Math.pow(p.height / 100, 2)).toFixed(1) : '—';

  container.innerHTML = `
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:20px">
      <div class="card">
        <div class="card-header"><div class="card-title">Datos personales</div></div>
        <div class="card-body">
          <div class="data-grid">
            <div class="data-pill"><div class="data-pill-label">Nombre</div><div class="data-pill-value" style="font-size:0.9rem">${p.name}</div></div>
            <div class="data-pill"><div class="data-pill-label">ID</div><div class="data-pill-value">${p.id}</div></div>
            <div class="data-pill"><div class="data-pill-label">Fecha nac.</div><div class="data-pill-value" style="font-size:0.9rem">${p.dob}</div></div>
            <div class="data-pill"><div class="data-pill-label">Edad</div><div class="data-pill-value">${p.age} años</div></div>
            <div class="data-pill"><div class="data-pill-label">Sexo</div><div class="data-pill-value">${p.sex === 'F' ? 'Femenino' : 'Masculino'}</div></div>
            <div class="data-pill"><div class="data-pill-label">Correo</div><div class="data-pill-value" style="font-size:0.8rem">${p.email}</div></div>
          </div>
        </div>
      </div>
      <div class="card">
        <div class="card-header"><div class="card-title">Datos clínicos</div></div>
        <div class="card-body">
          <div class="data-grid">
            <div class="data-pill"><div class="data-pill-label">Peso</div><div class="data-pill-value">${p.weight} kg</div></div>
            <div class="data-pill"><div class="data-pill-label">Talla</div><div class="data-pill-value">${p.height} cm</div></div>
            <div class="data-pill"><div class="data-pill-label">IMC</div><div class="data-pill-value">${bmi}</div></div>
          </div>
        </div>
      </div>
    </div>
    <div class="card" style="margin-top:20px">
      <div class="card-header"><div class="card-title">Contacto de emergencia</div></div>
      <div class="card-body">
        <div style="display:flex;align-items:center;gap:12px">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:20px;height:20px;color:var(--coral)"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 13a19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 3.6 2h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"/></svg>
          <div>
            <div style="font-weight:600;color:var(--gray-800)">${p.emergency.split('/')[0].trim()}</div>
            <div style="font-size:0.82rem;color:var(--gray-400)">${p.emergency.split('/')[1]?.trim()}</div>
          </div>
        </div>
      </div>
    </div>
  `;
}

// ==========================================
// ALERTS PAGE
// ==========================================

function renderAlertsPage() {
  const container = document.getElementById('alerts-list');
  if (!container) return;

  const alertPts = PATIENTS.filter(p => p.novedad || p.alert === 'red');

  if (alertPts.length === 0) {
    container.innerHTML = `<div class="empty-state"><div class="empty-state-text">No hay alertas para este médico.</div></div>`;
    return;
  }

  container.innerHTML = alertPts.map(p => {
    const latestNov = p.novedades[0];
    const positiveCount = latestNov ? Object.values(latestNov.symptoms).filter(v => v === true).length : 0;

    return `
      <div class="card" style="margin-bottom:16px;cursor:pointer" onclick="openPatient('${p.id}')">
        <div class="card-body" style="padding:20px 24px">
          <div style="display:flex;align-items:flex-start;gap:16px">
            <div class="patient-avatar" style="background:${p.avatarColor};width:44px;height:44px;font-size:0.9rem;flex-shrink:0">${initials(p.name)}</div>
            <div style="flex:1">
              <div style="display:flex;align-items:center;gap:10px;flex-wrap:wrap;margin-bottom:4px">
                <div style="font-weight:700;color:var(--gray-800)">${p.name}</div>
                <span class="badge badge-${p.alert}"><span class="badge-dot"></span>${p.alert === 'red' ? 'Alerta' : 'Vigilar'}</span>
              </div>
              <div style="font-size:0.8rem;color:var(--gray-400);margin-bottom:12px">${p.id}</div>
              <div style="display:flex;gap:16px;flex-wrap:wrap">
                <div style="background:var(--gray-50);border-radius:8px;padding:8px 14px">
                  <div style="font-size:0.68rem;color:var(--gray-400);text-transform:uppercase;font-weight:600">Última glucosa</div>
                  <div style="font-size:1.1rem;font-weight:800;color:${p.alert === 'red' ? '#b91c1c' : '#b45309'}">${p.lastGlucose} mg/dL</div>
                  <div style="font-size:0.7rem;color:var(--gray-400)">${p.lastGlucoseTime}</div>
                </div>
                ${latestNov ? `
                <div style="background:#fff5f5;border:1px solid #fecaca;border-radius:8px;padding:8px 14px">
                  <div style="font-size:0.68rem;color:#b91c1c;text-transform:uppercase;font-weight:600">Síntomas reportados</div>
                  <div style="font-size:1.1rem;font-weight:800;color:#b91c1c">${positiveCount} síntomas</div>
                  <div style="font-size:0.7rem;color:var(--gray-400)">${latestNov.date} ${latestNov.time}</div>
                </div>
                ` : ''}
                <div style="background:var(--gray-50);border-radius:8px;padding:8px 14px">
                  <div style="font-size:0.68rem;color:var(--gray-400);text-transform:uppercase;font-weight:600">Adherencia</div>
                  <div style="font-size:1.1rem;font-weight:800;color:${p.adherence < 60 ? '#b91c1c' : '#b45309'}">${p.adherence}%</div>
                </div>
              </div>
            </div>
            <button class="btn btn-outline btn-sm" style="flex-shrink:0">Ver paciente →</button>
          </div>
        </div>
      </div>
    `;
  }).join('');
}

// ==========================================
// SEARCH
// ==========================================

function handleSearch(query) {
  if (!query.trim()) return;
  query = query.toLowerCase();
  const matches = PATIENTS.filter(p =>
    p.name.toLowerCase().includes(query) || p.id.toLowerCase().includes(query)
  );
  if (matches.length === 1) {
    openPatient(matches[0].id);
  } else {
    navigate('patients');
    setTimeout(() => {
      const rows = document.querySelectorAll('#patients-tbody tr');
      rows.forEach(r => {
        const name = r.querySelector('.patient-name')?.textContent.toLowerCase() || '';
        r.style.display = name.includes(query) ? '' : 'none';
      });
    }, 100);
  }
}

// ==========================================
// HELPERS
// ==========================================

function initials(name) {
  return name.split(' ').slice(0, 2).map(n => n[0]).join('').toUpperCase();
}

function formatDate(dateStr) {
  if (!dateStr) return '—';
  const d = new Date(dateStr);
  return d.toLocaleDateString('es-CO', { day: '2-digit', month: 'short', year: 'numeric' });
}

// ==========================================
// INIT
// ==========================================

document.addEventListener('DOMContentLoaded', async () => {
  applyDoctorProfile();
  await loadPatientsFromServer();
  navigate('dashboard');
  initContentForm();

  // Sidebar navigation
  document.querySelectorAll('[data-nav]').forEach(item => {
    item.addEventListener('click', () => navigate(item.dataset.nav));
  });

  // Tab navigation (patient detail)
  document.querySelectorAll('[data-tab]').forEach(tab => {
    tab.addEventListener('click', () => openTab(tab.dataset.tab));
  });

  // Search
  const searchInput = document.querySelector('.search-bar input');
  if (searchInput) {
    searchInput.addEventListener('keypress', (e) => {
      if (e.key === 'Enter') handleSearch(e.target.value);
    });
  }

  // Table filters
  document.querySelectorAll('[data-filter]').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('[data-filter]').forEach(b => b.classList.remove('btn-primary'));
      document.querySelectorAll('[data-filter]').forEach(b => b.classList.add('btn-outline'));
      btn.classList.remove('btn-outline');
      btn.classList.add('btn-primary');
      renderPatientsTable(btn.dataset.filter);
    });
  });

  // Add patient
  document.getElementById('btn-add-patient')?.addEventListener('click', addPatient);

  // Back button
  document.getElementById('btn-back')?.addEventListener('click', () => navigate('patients'));
});

// ==========================================
// CONTENT
// ==========================================

function initContentForm() {
  const contentForm = document.getElementById('content-form');
  if (!contentForm) return;

  contentForm.addEventListener('submit', async function(e) {
    e.preventDefault();

    const medico = JSON.parse(localStorage.getItem('glucocare_medico'));

    if (!medico || !medico.id) {
      alert('No se encontró el médico en sesión.');
      return;
    }

    const titulo = document.getElementById('content-title').value.trim();
    const descripcion = document.getElementById('content-description').value.trim();
    const archivo = document.getElementById('content-file').files[0];

    if (!titulo || !archivo) {
      alert('Debes ingresar un título y seleccionar un archivo.');
      return;
    }

    const formData = new FormData();
    formData.append('medico_id', medico.id);
    formData.append('titulo', titulo);
    formData.append('descripcion', descripcion);
    formData.append('archivo', archivo);

    try {
      const response = await fetch('api/upload_content.php', {
        method: 'POST',
        body: formData
      });

      const data = await response.json();

      alert(data.message || 'Proceso finalizado.');

      if (data.ok) {
        contentForm.reset();
        await loadContents();
      }

    } catch (error) {
      console.error('Error subiendo contenido:', error);
      alert('No se pudo subir el contenido. Revisa XAMPP, PHP y la base de datos.');
    }
  });
}

async function loadContents() {
  const container = document.getElementById('contents-list');
  if (!container) return;

  const medico = JSON.parse(localStorage.getItem('glucocare_medico'));

  if (!medico || !medico.id) {
    container.innerHTML = '<p>No se encontró el médico en sesión.</p>';
    return;
  }

  container.innerHTML = '<p>Cargando contenido...</p>';

  try {
    const response = await fetch(`api/contents.php?medico_id=${encodeURIComponent(medico.id)}`);
    const data = await response.json();

    if (!data.ok) {
      container.innerHTML = `<p>${data.message || 'No se pudo cargar el contenido.'}</p>`;
      return;
    }

    const contenidos = data.contenidos || [];

    if (contenidos.length === 0) {
      container.innerHTML = `
        <div class="empty-state">
          <div style="font-size:1rem;font-weight:600;color:var(--gray-500)">
            No hay contenido publicado
          </div>
          <div class="empty-state-text">
            Sube el primer material educativo para tus pacientes.
          </div>
        </div>
      `;
      return;
    }

    container.innerHTML = contenidos.map(item => `
      <div class="content-item" style="border:1px solid var(--gray-100);border-radius:12px;padding:16px;margin-bottom:12px">
        <div style="display:flex;justify-content:space-between;gap:16px;align-items:flex-start">
          <div>
            <h4 style="margin:0 0 6px;color:var(--gray-800)">
              ${item.titulo}
            </h4>

            <p style="margin:0 0 10px;color:var(--gray-500);font-size:0.85rem">
              ${item.descripcion || 'Sin descripción'}
            </p>

            <div style="font-size:0.75rem;color:var(--gray-400);margin-bottom:10px">
              ${item.tipo || 'Archivo'} · ${item.fecha_subida || ''}
            </div>

            <a href="${item.archivo}" target="_blank" class="btn btn-outline btn-sm">
              Ver archivo
            </a>
          </div>
        </div>
      </div>
    `).join('');

  } catch (error) {
    console.error('Error cargando contenidos:', error);
    container.innerHTML = '<p>No se pudo conectar con el servidor para cargar los contenidos.</p>';
  }
}

async function loadContents() {
  const medico = JSON.parse(localStorage.getItem('glucocare_medico'));

  const response = await fetch(`api/contents.php?medico_id=${medico.id}`);
  const data = await response.json();

  const container = document.getElementById('contents-list');

  if (!container) return;

  container.innerHTML = '';

  if (!data.ok || data.contenidos.length === 0) {
    container.innerHTML = '<p>No hay contenido publicado.</p>';
    return;
  }

  data.contenidos.forEach(item => {
    container.innerHTML += `
      <div class="content-item">
        <h4>${item.titulo}</h4>
        <p>${item.descripcion || ''}</p>
        <a href="${item.archivo}" target="_blank">Ver archivo</a>
      </div>
    `;
  });
}

loadContents();