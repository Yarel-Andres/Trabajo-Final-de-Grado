// Función para mostrar/ocultar participantes con transiciones suaves
function toggleParticipantes(reunionId) {
    var content = document.getElementById("participantes-" + reunionId)

    if (content.style.display === "none" || content.style.display === "") {
        // Mostrar con transición suave
        content.style.display = "block"
        content.style.height = "0px"
        content.style.opacity = "0"
        content.style.overflow = "hidden"
        content.style.transition = "height 0.35s ease, opacity 0.35s ease"

        // Calcular altura real
        var realHeight = content.scrollHeight

        setTimeout(() => {
            content.style.height = realHeight + "px"
            content.style.opacity = "1"
        }, 10)

        // Limpiar estilos después de la transición
        setTimeout(() => {
            content.style.height = "auto"
            content.style.overflow = "visible"
        }, 350)
    } else {
        // Ocultar con transición suave
        var currentHeight = content.offsetHeight
        content.style.height = currentHeight + "px"
        content.style.overflow = "hidden"
        content.style.transition = "height 0.35s ease, opacity 0.35s ease"

        setTimeout(() => {
            content.style.height = "0px"
            content.style.opacity = "0"
        }, 10)

        setTimeout(() => {
            content.style.display = "none"
            content.style.height = "auto"
            content.style.overflow = "visible"
        }, 350)
    }
}

// Función para mostrar mensajes de alerta
function showMessage(message, type) {
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

    var messagesContainer = document.querySelector(".container > div:first-child")
    messagesContainer.appendChild(alertDiv)

    setTimeout(() => {
        alertDiv.classList.remove("show")
        setTimeout(() => {
            alertDiv.remove()
        }, 150)
    }, 5000)
}

// Inicialización para reuniones asignadas
function initReunionesAsignadas() {
    console.log("Página de reuniones asignadas cargada correctamente")

    // Script para finalizar reuniones
    var forms = document.querySelectorAll(".form-eliminar")

    forms.forEach((form) => {
        form.addEventListener("submit", (e) => {
            e.preventDefault()

            var formData = new FormData(form)
            var reunionId = formData.get("reunionId")
            var row = document.getElementById("reunion-" + reunionId)

            fetch(form.action, {
                method: "POST",
                body: formData,
                headers: {
                    "X-Requested-With": "XMLHttpRequest",
                },
            })
                .then((response) => {
                    if (response.ok) {
                        if (row) {
                            row.style.opacity = "0"
                            row.style.transition = "opacity 0.3s ease"

                            setTimeout(() => {
                                row.remove()

                                var tableBody = form.closest("tbody")
                                if (tableBody && tableBody.querySelectorAll("tr").length === 0) {
                                    var cardBody = tableBody.closest(".card-body")
                                    var tableResponsive = tableBody.closest(".table-responsive")

                                    if (cardBody && tableResponsive) {
                                        var noReunionesMsg = document.createElement("div")
                                        noReunionesMsg.className = "alert alert-info d-flex align-items-center"
                                        noReunionesMsg.innerHTML =
                                            '<i class="fas fa-info-circle me-3 fa-lg"></i><div><h5 class="alert-heading mb-1">No hay reuniones programadas</h5><p class="mb-0">Todas las reuniones han sido finalizadas.</p></div>'

                                        cardBody.replaceChild(noReunionesMsg, tableResponsive)
                                    }
                                }

                                showMessage("Reunión finalizada correctamente", "success")
                            }, 300)
                        }
                    } else {
                        showMessage("Error al finalizar la reunión", "danger")
                    }
                })
                .catch((error) => {
                    console.error("Error:", error)
                    showMessage("Error al finalizar la reunión: " + error.message, "danger")
                })
        })
    })
}

// Inicialización para crear reuniones
function initCrearReunion() {
    // Establecer la fecha actual como valor predeterminado
    const fechaReunionInput = document.getElementById("fechaReunion")
    if (fechaReunionInput) {
        const hoy = new Date()
        const año = hoy.getFullYear()
        const mes = String(hoy.getMonth() + 1).padStart(2, "0")
        const dia = String(hoy.getDate()).padStart(2, "0")
        const fechaActual = `${año}-${mes}-${dia}`
        fechaReunionInput.value = fechaActual
    }

    // Validación de participantes
    const form = document.querySelector("form")
    const participanteChecks = document.querySelectorAll(".participante-check")
    const participantesError = document.getElementById("participantesError")

    if (form && participanteChecks.length > 0 && participantesError) {
        form.addEventListener("submit", (event) => {
            // Verificar si al menos un participante está seleccionado
            let participanteSeleccionado = false
            participanteChecks.forEach((check) => {
                if (check.checked) {
                    participanteSeleccionado = true
                }
            })

            if (!participanteSeleccionado) {
                event.preventDefault()
                participantesError.style.display = "block"
                window.scrollTo(0, participantesError.offsetTop - 100)
            } else {
                participantesError.style.display = "none"
            }
        })

        // Ocultar mensaje de error cuando se selecciona un participante
        participanteChecks.forEach((check) => {
            check.addEventListener("change", function () {
                if (this.checked) {
                    participantesError.style.display = "none"
                }
            })
        })
    }
}

// Inicialización para ver reuniones
function initVerReuniones() {
    // Asegurarse de que Bootstrap esté cargado
    if (typeof window.bootstrap === "undefined") {
        console.log("Bootstrap no está cargado, cargando desde CDN...")
        var script = document.createElement("script")
        script.src = "https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"
        script.integrity = "sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p"
        script.crossOrigin = "anonymous"
        document.head.appendChild(script)

        // Esperar a que Bootstrap se cargue
        script.onload = initializeCollapses
    } else {
        initializeCollapses()
    }

    function initializeCollapses() {
        console.log("Inicializando collapses...")
        // Bootstrap 5 los inicializa automáticamente con los atributos data-bs-toggle="collapse"
    }
}

// Inicialización general
document.addEventListener("DOMContentLoaded", () => {
    // Detectar qué página estamos cargando y ejecutar la inicialización correspondiente
    const currentPath = window.location.pathname

    if (currentPath.includes("/reuniones/asignadas")) {
        initReunionesAsignadas()
    } else if (currentPath.includes("/reuniones/crear")) {
        initCrearReunion()
    } else if (currentPath.includes("/reuniones/ver")) {
        initVerReuniones()
    }
})
