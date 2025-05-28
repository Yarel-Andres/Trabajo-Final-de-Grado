// Clase principal para manejar registros de tiempo
class RegistrosTiempo {
    constructor() {
        this.init()
    }

    // Inicializar funcionalidades según la página
    init() {
        const currentPath = window.location.pathname

        if (currentPath.includes("/crear")) {
            this.initCrearPage()
        } else if (
            currentPath.includes("/informes") &&
            !currentPath.includes("/empleado") &&
            !currentPath.includes("/proyecto")
        ) {
            this.initInformesPage()
        }
    }

    // Inicializar página de crear registro
    initCrearPage() {
        this.setupDateDefaults()
        this.setupTimeDefaults()
        this.setupActivityTypeHandlers()
        this.setupFormValidation()
    }

    // Establecer fechas por defecto
    setupDateDefaults() {
        const fechaInicioTemp = document.getElementById("fechaInicioTemp")
        const fechaFinTemp = document.getElementById("fechaFinTemp")

        if (fechaInicioTemp && !fechaInicioTemp.value) {
            const hoy = new Date()
            const año = hoy.getFullYear()
            const mes = String(hoy.getMonth() + 1).padStart(2, "0")
            const dia = String(hoy.getDate()).padStart(2, "0")
            fechaInicioTemp.value = `${año}-${mes}-${dia}`
        }

        if (fechaFinTemp && !fechaFinTemp.value) {
            const hoy = new Date()
            const año = hoy.getFullYear()
            const mes = String(hoy.getMonth() + 1).padStart(2, "0")
            const dia = String(hoy.getDate()).padStart(2, "0")
            fechaFinTemp.value = `${año}-${mes}-${dia}`
        }
    }

    // Establecer horas por defecto
    setupTimeDefaults() {
        const horaInicioTemp = document.getElementById("horaInicioTemp")
        const minutosInicioTemp = document.getElementById("minutosInicioTemp")
        const horaFinTemp = document.getElementById("horaFinTemp")
        const minutosFinTemp = document.getElementById("minutosFinTemp")

        if (horaInicioTemp) horaInicioTemp.value = "9"
        if (minutosInicioTemp) minutosInicioTemp.value = "0"
        if (horaFinTemp) horaFinTemp.value = "17"
        if (minutosFinTemp) minutosFinTemp.value = "0"
    }

    // Configurar manejadores de tipo de actividad
    setupActivityTypeHandlers() {
        const tipoTarea = document.getElementById("tipoTarea")
        const tipoProyecto = document.getElementById("tipoProyecto")
        const tipoReunion = document.getElementById("tipoReunion")

        if (tipoTarea && tipoProyecto && tipoReunion) {
            const updateSections = () => {
                const tareaSection = document.getElementById("tareaSection")
                const proyectoSection = document.getElementById("proyectoSection")
                const reunionSection = document.getElementById("reunionSection")
                const tareaId = document.getElementById("tareaId")
                const proyectoId = document.getElementById("proyectoId")
                const reunionId = document.getElementById("reunionId")

                if (tipoTarea.checked) {
                    tareaSection.classList.remove("d-none")
                    proyectoSection.classList.add("d-none")
                    reunionSection.classList.add("d-none")
                    proyectoId.value = ""
                    reunionId.value = ""
                } else if (tipoProyecto.checked) {
                    tareaSection.classList.add("d-none")
                    proyectoSection.classList.remove("d-none")
                    reunionSection.classList.add("d-none")
                    tareaId.value = ""
                    reunionId.value = ""
                } else if (tipoReunion.checked) {
                    tareaSection.classList.add("d-none")
                    proyectoSection.classList.add("d-none")
                    reunionSection.classList.remove("d-none")
                    tareaId.value = ""
                    proyectoId.value = ""
                }
            }

            tipoTarea.addEventListener("change", updateSections)
            tipoProyecto.addEventListener("change", updateSections)
            tipoReunion.addEventListener("change", updateSections)
            // Inicializar
            updateSections()
        }
    }

    // Configurar validación del formulario
    setupFormValidation() {
        const form = document.querySelector("form")
        if (form) {
            form.addEventListener("submit", (event) => {
                const fechaInicio = document.getElementById("fechaInicioTemp").value
                const horaInicio = document.getElementById("horaInicioTemp").value
                const minutosInicio = document.getElementById("minutosInicioTemp").value
                const fechaFin = document.getElementById("fechaFinTemp").value
                const horaFin = document.getElementById("horaFinTemp").value
                const minutosFin = document.getElementById("minutosFinTemp").value

                const inicio = new Date(`${fechaInicio}T${horaInicio.padStart(2, "0")}:${minutosInicio.padStart(2, "0")}:00`)
                const fin = new Date(`${fechaFin}T${horaFin.padStart(2, "0")}:${minutosFin.padStart(2, "0")}:00`)

                if (fin <= inicio) {
                    event.preventDefault()
                    alert("La hora de fin debe ser posterior a la hora de inicio")
                }
            })
        }
    }

    // Inicializar página de informes
    initInformesPage() {
        const empleadoSelect = document.getElementById("empleadoSelect")
        const proyectoSelect = document.getElementById("proyectoSelect")
        const verEmpleadoBtn = document.getElementById("verEmpleadoBtn")
        const verProyectoBtn = document.getElementById("verProyectoBtn")

        if (verEmpleadoBtn) {
            verEmpleadoBtn.addEventListener("click", () => {
                const empleadoId = empleadoSelect.value
                if (empleadoId) {
                    window.location.href = `/registros-tiempo/informes/empleado/${empleadoId}`
                } else {
                    this.showMessage("Por favor, selecciona un empleado", "warning")
                }
            })
        }

        if (verProyectoBtn) {
            verProyectoBtn.addEventListener("click", () => {
                const proyectoId = proyectoSelect.value
                if (proyectoId) {
                    window.location.href = `/registros-tiempo/informes/proyecto/${proyectoId}`
                } else {
                    this.showMessage("Por favor, selecciona un proyecto", "warning")
                }
            })
        }
    }

    // Mostrar mensaje de alerta
    showMessage(message, type) {
        const alertDiv = document.createElement("div")
        alertDiv.className = `alert alert-${type} alert-dismissible fade show`
        alertDiv.role = "alert"

        const icon =
            type === "success" ? "fa-check-circle" : type === "warning" ? "fa-exclamation-triangle" : "fa-exclamation-circle"

        alertDiv.innerHTML = `
            <div class="d-flex align-items-center">
                <i class="fas ${icon} me-2"></i>
                <span>${message}</span>
            </div>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        `

        const messagesContainer = document.querySelector(".container > div:first-child")
        messagesContainer.appendChild(alertDiv)

        // Auto-eliminar después de 5 segundos
        setTimeout(() => {
            alertDiv.classList.remove("show")
            setTimeout(() => alertDiv.remove(), 150)
        }, 5000)
    }
}

// Inicializar cuando el DOM esté listo
document.addEventListener("DOMContentLoaded", () => {
    new RegistrosTiempo()
})
