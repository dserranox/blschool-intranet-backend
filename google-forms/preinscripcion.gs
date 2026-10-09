/**
 * BLSchool - Envío de preinscripciones desde la planilla de respuestas del Google Form
 * al backend (POST /blschool-backend/api/public/preinscripciones).
 *
 * Instalación (ver PREINSCRIPCION_GOOGLE_FORM.md):
 *   1. Planilla de respuestas -> Extensiones -> Apps Script -> pegar este archivo.
 *   2. Configuración del proyecto -> Propiedades del script -> agregar API_KEY = <la clave del servidor>.
 *   3. Ajustar ANIO_PREINSCRIPCION.
 *   4. Ejecutar una vez "instalarTrigger" (pide permisos).
 *   5. (Opcional) Ejecutar "enviarPendientes" para mandar las respuestas que ya existían.
 *
 * Cada fila enviada queda marcada en la columna "Estado BLSchool" (OK / ERROR).
 * Reenviar una fila es seguro: el backend actualiza en lugar de duplicar.
 */

// ===================== Configuración =====================

const URL_ENDPOINT = 'https://britishlanguageschoolsf.com.ar/blschool-backend/api/public/preinscripciones';
const COLUMNA_ESTADO = 'Estado BLSchool';

/**
 * Campo del backend -> comienzo del encabezado de la columna en la planilla
 * (se compara sin mayúsculas, acentos ni signos ¿?:). Si cambian las preguntas del
 * formulario, alcanza con mantener el comienzo del texto.
 */
const CAMPOS = {
  marcaTemporal: 'marca temporal',
  emailFormulario: 'direccion de correo electronico',
  apellido: 'apellido',
  nombre: 'nombre',
  dni: 'dni',
  fechaNacimiento: 'fecha de nacimiento',
  telefonoMadre: 'telefono madre',
  telefonoPadre: 'telefono padre',
  telefonoAlumno: 'telefono alumno',
  domicilio: 'domicilio',
  email: 'correo electronico',
  escuelaCursoTurno: 'escuela',
  tieneProblemaSalud: 'el alumno tiene',
  problemaSalud: 'cual',
  anio: 'preinscripci',
};

// ===================== Triggers =====================

/** Se ejecuta automáticamente con cada respuesta nueva del formulario. */
function onFormSubmit(e) {
  const sheet = e.range.getSheet();
  enviarFila_(sheet, e.range.getRow());
}

/** Ejecutar UNA vez a mano para instalar el trigger de envío automático. */
function instalarTrigger() {
  const ss = SpreadsheetApp.getActiveSpreadsheet();
  const yaExiste = ScriptApp.getProjectTriggers()
    .some(t => t.getHandlerFunction() === 'onFormSubmit');
  if (!yaExiste) {
    ScriptApp.newTrigger('onFormSubmit').forSpreadsheet(ss).onFormSubmit().create();
  }
  Logger.log(yaExiste ? 'El trigger ya estaba instalado' : 'Trigger instalado');
}

/**
 * Envía todas las filas que todavía no se enviaron bien (columna de estado vacía o con ERROR).
 * Sirve para la carga inicial y para reintentar errores. Si hay muchas filas y se corta
 * por el límite de tiempo de Apps Script, volver a ejecutarla: sigue desde donde quedó.
 */
function enviarPendientes() {
  const sheet = hojaRespuestas_();
  const colEstado = columnaEstado_(sheet);
  const ultimaFila = sheet.getLastRow();
  const inicio = Date.now();
  let enviadas = 0;

  for (let fila = 2; fila <= ultimaFila; fila++) {
    const estado = String(sheet.getRange(fila, colEstado).getValue() || '');
    if (estado.startsWith('OK')) continue;
    enviarFila_(sheet, fila);
    enviadas++;
    if (Date.now() - inicio > 5 * 60 * 1000) {
      Logger.log('Se cortó por tiempo después de ' + enviadas + ' filas. Volver a ejecutar enviarPendientes.');
      return;
    }
  }
  Logger.log('Filas enviadas: ' + enviadas);
}

// ===================== Envío =====================

function enviarFila_(sheet, fila) {
  const lock = LockService.getScriptLock();
  lock.waitLock(30000);
  try {
    const colEstado = columnaEstado_(sheet);
    const encabezados = sheet.getRange(1, 1, 1, sheet.getLastColumn()).getValues()[0];
    const valores = sheet.getRange(fila, 1, 1, encabezados.length).getValues()[0];

    const payload = {};
    Object.keys(CAMPOS).forEach(campo => {
      const col = buscarColumna_(encabezados, CAMPOS[campo]);
      if (col >= 0) payload[campo] = formatear_(campo, valores[col]);
    });

    const apiKey = PropertiesService.getScriptProperties().getProperty('API_KEY');
    if (!apiKey) throw new Error('Falta la propiedad del script API_KEY');

    const resp = UrlFetchApp.fetch(URL_ENDPOINT, {
      method: 'post',
      contentType: 'application/json',
      headers: { 'X-Api-Key': apiKey },
      payload: JSON.stringify(payload),
      muteHttpExceptions: true,
    });

    const ahora = Utilities.formatDate(new Date(), Session.getScriptTimeZone(), 'dd/MM/yyyy HH:mm');
    const codigo = resp.getResponseCode();
    let estado;
    if (codigo === 200) {
      const r = JSON.parse(resp.getContentText());
      estado = 'OK ' + ahora + ' - ' + (r.alumnoNuevo ? 'alumno nuevo' : 'alumno existente') +
        ' (id ' + r.alumnoId + ')' + (r.preinscripcionNueva ? '' : ' - actualizada');
    } else {
      estado = 'ERROR ' + codigo + ' ' + ahora + ': ' + resp.getContentText().substring(0, 200);
    }
    sheet.getRange(fila, colEstado).setValue(estado);
  } catch (err) {
    sheet.getRange(fila, columnaEstado_(sheet)).setValue('ERROR ' + err.message);
  } finally {
    lock.releaseLock();
  }
}

// ===================== Utilidades =====================

function hojaRespuestas_() {
  // La hoja de respuestas del formulario suele ser la primera
  return SpreadsheetApp.getActiveSpreadsheet().getSheets()[0];
}

function normalizar_(texto) {
  return String(texto || '')
    .normalize('NFD').replace(/[̀-ͯ]/g, '')
    .toLowerCase()
    .replace(/[¿?:"]/g, '')
    .replace(/\s+/g, ' ')
    .trim();
}

function buscarColumna_(encabezados, prefijo) {
  // "nombre" no debe tomar la columna vieja "Nombre y apellido completo"
  return encabezados.findIndex(h => {
    const n = normalizar_(h);
    return n.startsWith(prefijo) && !(prefijo === 'nombre' && n.startsWith('nombre y apellido'));
  });
}

function columnaEstado_(sheet) {
  const encabezados = sheet.getRange(1, 1, 1, sheet.getLastColumn()).getValues()[0];
  const idx = encabezados.indexOf(COLUMNA_ESTADO);
  if (idx >= 0) return idx + 1;
  const nueva = encabezados.length + 1;
  sheet.getRange(1, nueva).setValue(COLUMNA_ESTADO);
  return nueva;
}

function formatear_(campo, valor) {
  if (valor === null || valor === undefined || valor === '') return '';
  const tz = Session.getScriptTimeZone();
  if (valor instanceof Date) {
    return campo === 'marcaTemporal'
      ? Utilities.formatDate(valor, tz, "yyyy-MM-dd'T'HH:mm:ss")
      : Utilities.formatDate(valor, tz, 'yyyy-MM-dd');
  }
  return String(valor).trim();
}
