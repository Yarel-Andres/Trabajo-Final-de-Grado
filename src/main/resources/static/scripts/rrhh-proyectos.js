class RRHHProyectos {
    constructor() {
        this.init()
    }

    // Inicializa las funcionalidades de proyectos
    init() {
        this.detectPage()
        this.setupEventListeners()
    }

    // Detecta la página actual y ejecuta las funciones correspondientes
    detectPage() {
        const path = window.location.pathname

        if (path.includes("/proyectos/crear")) {
            this.initCrearProyecto()
        } else if (path.includes("/proyectos/listar")) {
            this.initListarProyectos()
        } else if (path.includes("/proyectos/horas")) {
            this.initHorasProyecto()
        }
    }

    // Configura event listeners generales
    setupEventListeners() {
        // Event listeners para botones de acción
        const botonesAccion = document.querySelectorAll(".btn-primary, .btn-secondary")
        botonesAccion.forEach((boton) => {
            boton.addEventListener("mouseenter", this.handleButtonHover)
            boton.addEventListener("mouseleave", this.handleButtonLeave)
        })
    }

    // Inicializa la página de crear proyecto
    initCrearProyecto() {
        console.log("Inicializando página de crear proyecto...")
        this.setupFormValidation()
        this.setupCalculadoraCosto()
    }

    // Inicializa la página de listar proyectos
    initListarProyectos() {
        console.log("Inicializando página de listar proyectos...")
        this.setupTableInteractions()
    }

    // Inicializa la página de horas de proyecto
    initHorasProyecto() {
        console.log("Inicializando página de horas de proyecto...")
        this.setupTableInteractions()
    }

    // Configura la validación de formularios
    setupFormValidation() {
        const formularios = document.querySelectorAll("form")
        formularios.forEach((form) => {
            form.addEventListener("submit", this.validateForm)
        })
    }

    // Configura la calculadora de costo
    setupCalculadoraCosto() {
        const tarifaHoraInput = document.getElementById("tarifaHora")
        const horasEstimadasInput = document.getElementById("horasEstimadas")
        const costoTotalInput = document.getElementById("costoTotal")

        if (!tarifaHoraInput || !horasEstimadasInput || !costoTotalInput) return

        const calcularCostoTotal = () => {
            const tarifaHora = Number.parseFloat(tarifaHoraInput.value) || 0
            const horasEstimadas = Number.parseFloat(horasEstimadasInput.value) || 0
            costoTotalInput.value = (tarifaHora * horasEstimadas).toFixed(2)
        }

        tarifaHoraInput.addEventListener("input", calcularCostoTotal)
        horasEstimadasInput.addEventListener("input", calcularCostoTotal)

        // Calcular inicialmente
        calcularCostoTotal()
    }

    // Configura interacciones de tabla
    setupTableInteractions() {
        const filas = document.querySelectorAll(".table tbody tr")
        filas.forEach((fila) => {
            fila.addEventListener("mouseenter", function () {
                this.style.backgroundColor = "rgba(52, 86, 118, 0.05)"
            })
            fila.addEventListener("mouseleave", function () {
                this.style.backgroundColor = ""
            })
        })
    }

    // Maneja el hover de botones
    handleButtonHover(event) {
        const button = event.target
        if (button.classList.contains("btn-primary")) {
            button.style.transform = "translateY(-2px)"
            button.style.boxShadow = "0 4px 8px rgba(0,0,0,0.1)"
        }
    }

    // Maneja cuando se quita el hover de botones
    handleButtonLeave(event) {
        const button = event.target
        if (button.classList.contains("btn-primary")) {
            button.style.transform = ""
            button.style.boxShadow = ""
        }
    }

    // Valida formularios antes del envío
    validateForm(event) {
        const form = event.target
        const requiredFields = form.querySelectorAll("[required]")
        let isValid = true

        requiredFields.forEach((field) => {
            if (!field.value.trim()) {
                isValid = false
                field.classList.add("is-invalid")
            } else {
                field.classList.remove("is-invalid")
            }
        })

        if (!isValid) {
            event.preventDefault()
            alert("Por favor, complete todos los campos requeridos.")
        }
    }
}

// Inicializar cuando el DOM esté listo
document.addEventListener("DOMContentLoaded", () => {
    new RRHHProyectos()
})
