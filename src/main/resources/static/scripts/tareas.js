document.addEventListener("DOMContentLoaded", () => {
    console.log("Script de tareas cargado")

    // Configurar funcionalidades según la página actual
    setupTareasAsignadas()
    setupTareasVer()
    setupAsignarTarea()
})

// Para tareas asignadas
function setupTareasAsignadas() {
    // Solo ejecutar en la página de tareas asignadas
    if (!document.querySelector(".form-eliminar")) {
        return
    }

    console.log("Configurando tareas asignadas")

    // Capturar todos los formularios de eliminación
    var forms = document.querySelectorAll(".form-eliminar")
    console.log("Formularios encontrados:", forms.length)

    forms.forEach((form) => {
        form.addEventListener("submit", (e) => {
            e.preventDefault() // Prevenir envío normal del formulario

            var formData = new FormData(form)
            var tareaId = formData.get("tareaId")
            var row = document.getElementById("tarea-" + tareaId)
            console.log("Finalizando tarea:", tareaId)

            // Enviar solicitud mediante fetch API
            fetch(form.action, {
                method: "POST",
                body: formData,
                headers: {
                    "X-Requested-With": "XMLHttpRequest",
                },
            })
                .then((response) => {
                    if (response.ok) {
                        console.log("Respuesta exitosa")
                        // Eliminar la fila con animación
                        if (row) {
                            animateRowRemoval(row, form)
                        }
                    } else {
                        console.log("Error en la respuesta")
                        showMessage("Error al finalizar la tarea", "danger")
                    }
                })
                .catch((error) => {
                    console.error("Error:", error)
                    showMessage("Error al finalizar la tarea: " + error.message, "danger")
                })
        })
    })
}

// Animar eliminación de fila
function animateRowRemoval(row, form) {
    row.style.opacity = "0"
    row.style.transform = "translateY(-20px)"
    row.style.transition = "opacity 0.3s ease, transform 0.3s ease"

    setTimeout(() => {
        row.remove()

        // Verificar si quedan filas en la tabla
        checkEmptyTable(form)

        // Mostrar mensaje de éxito
        showMessage("Tarea finalizada correctamente", "success")
    }, 300)
}

// Verificar si la tabla está vacía después de eliminar
function checkEmptyTable(form) {
    var tableBody = form.closest("tbody")
    if (tableBody && tableBody.querySelectorAll("tr").length === 0) {
        var cardBody = tableBody.closest(".card-body")
        var tableResponsive = tableBody.closest(".table-responsive")

        if (cardBody && tableResponsive) {
            var noTareasMsg = document.createElement("div")
            noTareasMsg.className = "alert alert-info d-flex align-items-center"
            noTareasMsg.innerHTML =
                '<i class="fas fa-info-circle me-3 fa-lg"></i>' +
                "<div>" +
                '<h5 class="alert-heading mb-1">No hay tareas en este proyecto</h5>' +
                '<p class="mb-0">Todas las tareas han sido finalizadas.</p>' +
                "</div>"

            cardBody.replaceChild(noTareasMsg, tableResponsive)
        }
    }
}


// Para ver tareas
function setupTareasVer() {
    // Solo ejecutar en la página de ver tareas
    if (!document.querySelector('[data-bs-toggle="tooltip"]')) {
        return
    }

    console.log("Configurando tooltips para ver tareas")

    // Inicializar tooltips para descripciones largas
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
    var tooltipList = tooltipTriggerList.map((tooltipTriggerEl) => new window.bootstrap.Tooltip(tooltipTriggerEl))

    console.log("Tooltips inicializados:", tooltipList.length)
}


// Para asignar tareas
function setupAsignarTarea() {
    // Solo ejecutar en la página de asignar tarea
    var fechaInput = document.getElementById("fechaVencimientoDate")
    var horaSelect = document.getElementById("horaVencimiento")

    if (!fechaInput || !horaSelect) {
        return
    }

    console.log("Configurando formulario de asignar tarea")

    // Establecer fecha actual como valor predeterminado
    setDefaultDate(fechaInput)

    // Establecer hora predeterminada (09:00)
    setDefaultTime(horaSelect)
}

// Establecer fecha actual por defecto
function setDefaultDate(fechaInput) {
    var hoy = new Date()
    var año = hoy.getFullYear()
    var mes = String(hoy.getMonth() + 1).padStart(2, "0")
    var dia = String(hoy.getDate()).padStart(2, "0")
    var fechaActual = año + "-" + mes + "-" + dia

    fechaInput.value = fechaActual
    console.log("Fecha predeterminada establecida:", fechaActual)
}

// Establecer hora predeterminada (09:00)
function setDefaultTime(horaSelect) {
    horaSelect.value = "9" // Selecciona la opción "09"
    console.log("Hora predeterminada establecida: 09:00")
}



// General

// Mostrar mensajes de alerta
function showMessage(message, type) {
    // Crear elemento de alerta
    var alertDiv = document.createElement("div")
    alertDiv.className = "alert alert-" + type + " alert-dismissible fade show"
    alertDiv.role = "alert"

    var icon = type === "success" ? "fa-check-circle" : "fa-exclamation-circle"
    alertDiv.innerHTML =
        '<div class="d-flex align-items-center">' +
        '<i class="fas ' +
        icon +
        ' me-2"></i>' +
        "<span>" +
        message +
        "</span>" +
        "</div>" +
        '<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>'

    // Insertar alerta al principio del contenedor
    var messagesContainer = document.querySelector(".container > div:first-child")
    if (messagesContainer) {
        messagesContainer.appendChild(alertDiv)
    }

    // Eliminar automáticamente después de 5 segundos
    setTimeout(() => {
        alertDiv.classList.remove("show")
        setTimeout(() => {
            if (alertDiv.parentNode) {
                alertDiv.remove()
            }
        }, 150)
    }, 5000)
}
