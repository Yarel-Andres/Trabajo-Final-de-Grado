// Clase principal para manejar funcionalidades de proyectos
class ProyectosManager {
    constructor() {
        this.init()
    }

    // Inicializar funcionalidades según la página actual
    init() {
        if (document.getElementById("fechaInicio")) {
            this.initCrearProyecto()
        }
        if (document.querySelector(".proyecto-card")) {
            this.initVerProyectos()
        }
    }

    // Funcionalidades para crear proyecto
    initCrearProyecto() {
        this.setDefaultDates()
        this.setupDateValidation()
    }

    // Establecer fechas por defecto
    setDefaultDates() {
        const fechaInicioInput = document.getElementById("fechaInicio")
        const fechaFinInput = document.getElementById("fechaFinEstimada")

        // Establecer fecha actual como fecha de inicio si no tiene valor
        if (fechaInicioInput && !fechaInicioInput.value) {
            fechaInicioInput.value = this.formatDate(new Date())
        }

        // Establecer fecha de fin estimada (3 meses después) si no tiene valor
        if (fechaFinInput && !fechaFinInput.value) {
            const fechaFin = new Date()
            fechaFin.setMonth(fechaFin.getMonth() + 3)
            fechaFinInput.value = this.formatDate(fechaFin)
        }

        // Actualizar fecha fin cuando cambie fecha inicio
        if (fechaInicioInput) {
            fechaInicioInput.addEventListener("change", () => this.updateFechaFin())
        }
    }

    // Formatear fecha a YYYY-MM-DD
    formatDate(date) {
        const año = date.getFullYear()
        const mes = String(date.getMonth() + 1).padStart(2, "0")
        const dia = String(date.getDate()).padStart(2, "0")
        return `${año}-${mes}-${dia}`
    }

    // Actualizar fecha fin para que sea al menos igual a fecha inicio
    updateFechaFin() {
        const fechaInicio = document.getElementById("fechaInicio")
        const fechaFin = document.getElementById("fechaFinEstimada")

        if (fechaInicio && fechaFin && fechaInicio.value) {
            const fechaInicioDate = new Date(fechaInicio.value)
            const fechaFinDate = new Date(fechaFin.value)

            // Si fecha fin es anterior a fecha inicio, ajustarla
            if (!fechaFin.value || fechaFinDate < fechaInicioDate) {
                const nuevaFechaFin = new Date(fechaInicioDate)
                nuevaFechaFin.setMonth(nuevaFechaFin.getMonth() + 3)
                fechaFin.value = this.formatDate(nuevaFechaFin)
            }
        }
    }

    // Configurar validación de fechas
    setupDateValidation() {
        const fechaFin = document.getElementById("fechaFinEstimada")
        const form = document.querySelector("form")

        // Validar cuando cambie la fecha de fin
        if (fechaFin) {
            fechaFin.addEventListener("change", () => this.validateDates())
        }

        // Validar antes de enviar el formulario
        if (form) {
            form.addEventListener("submit", (e) => {
                if (!this.validateDates()) {
                    e.preventDefault()
                    alert("La fecha de fin estimada debe ser posterior a la fecha de inicio.")
                }
            })
        }
    }

    // Validar que fecha fin sea posterior a fecha inicio
    validateDates() {
        const fechaInicio = document.getElementById("fechaInicio")
        const fechaFin = document.getElementById("fechaFinEstimada")

        if (fechaInicio && fechaFin && fechaInicio.value && fechaFin.value) {
            const fechaInicioDate = new Date(fechaInicio.value)
            const fechaFinDate = new Date(fechaFin.value)

            if (fechaFinDate < fechaInicioDate) {
                fechaFin.classList.add("is-invalid")
                return false
            } else {
                fechaFin.classList.remove("is-invalid")
                return true
            }
        }
        return true
    }

    // Funcionalidades para ver proyectos
    initVerProyectos() {
        this.setupProjectCards()
    }

    // Configurar tarjetas de proyectos
    setupProjectCards() {
        const cards = document.querySelectorAll(".proyecto-card")
        cards.forEach((card) => {
            card.addEventListener("mouseenter", () => {
                card.style.transform = "translateY(-2px)"
            })
            card.addEventListener("mouseleave", () => {
                card.style.transform = "translateY(0)"
            })
        })
    }
}

// Inicializar cuando el DOM esté listo
document.addEventListener("DOMContentLoaded", () => {
    new ProyectosManager()
})
