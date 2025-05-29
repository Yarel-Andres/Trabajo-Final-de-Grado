class RRHHPresupuestos {
    constructor() {
        this.init()
    }

    // Inicializa las funcionalidades de presupuestos
    init() {
        this.detectPage()
    }

    // Detecta la página actual y ejecuta las funciones correspondientes
    detectPage() {
        const path = window.location.pathname

        if (path.includes("/presupuestos/crear")) {
            this.initCrearPresupuesto()
        } else if (path.includes("/presupuestos/") && path.includes("/editar")) {
            this.initEditarPresupuesto()
        }
    }

    // Inicializa la página de crear presupuesto
    initCrearPresupuesto() {
        console.log("Inicializando página de crear presupuesto...")
        this.setupCalculadoraCosto()
    }

    // Inicializa la página de editar presupuesto
    initEditarPresupuesto() {
        console.log("Inicializando página de editar presupuesto...")
        this.setupCalculadoraCostoEditar()
    }

    // Configura la calculadora de costo para crear presupuesto
    setupCalculadoraCosto() {
        const tarifaInput = document.getElementById("tarifaHora")
        const costoTotalInput = document.getElementById("costoTotal")

        if (!tarifaInput || !costoTotalInput) {
            console.error("No se encontraron los elementos necesarios para la calculadora")
            return
        }

        // Obtener horas estimadas del servidor
        const horasEstimadas = this.getHorasEstimadas()
        console.log(`Configurando calculadora con ${horasEstimadas} horas estimadas`)

        const calcularCostoTotal = () => {
            const tarifa = Number.parseFloat(tarifaInput.value) || 0
            const costoTotal = tarifa * horasEstimadas
            costoTotalInput.value = costoTotal.toFixed(2)
            console.log(`Calculado: ${tarifa} × ${horasEstimadas} = ${costoTotal.toFixed(2)}`)
        }

        // Calcular inicialmente
        calcularCostoTotal()

        // Recalcular cuando cambie la tarifa
        tarifaInput.addEventListener("input", calcularCostoTotal)

        console.log("Calculadora de costos configurada correctamente")
    }

    // Configura la calculadora de costo para editar presupuesto
    setupCalculadoraCostoEditar() {
        const tarifaHoraInput = document.getElementById("tarifaHora")
        const horasEstimadasInput = document.getElementById("horasEstimadas")
        const costoTotalInput = document.getElementById("costoTotal")

        if (!tarifaHoraInput || !horasEstimadasInput || !costoTotalInput) return

        const calcularCostoTotal = () => {
            const tarifaHora = Number.parseFloat(tarifaHoraInput.value) || 0
            const horasEstimadas = Number.parseFloat(horasEstimadasInput.value) || 0
            const costoTotal = tarifaHora * horasEstimadas
            costoTotalInput.value = costoTotal.toFixed(2)
        }

        // Event listeners
        tarifaHoraInput.addEventListener("input", calcularCostoTotal)
        horasEstimadasInput.addEventListener("input", calcularCostoTotal)

        // Calcular al cargar la página
        calcularCostoTotal()
    }

    // Obtiene las horas estimadas del elemento en la página
    getHorasEstimadas() {
        // Buscar el elemento que contiene las horas estimadas por su texto
        const horasElements = document.querySelectorAll("p.form-control-static")
        for (const element of horasElements) {
            const text = element.textContent.trim()
            const horasValue = Number.parseFloat(text)
            if (!Number.isNaN(horasValue) && horasValue > 0) {
                console.log(`Horas estimadas encontradas: ${horasValue}`)
                return horasValue
            }
        }

        // Buscar por el label "Horas Estimadas"
        const labels = document.querySelectorAll("label")
        for (const label of labels) {
            if (label.textContent.includes("Horas Estimadas")) {
                const nextElement = label.parentElement.querySelector("p")
                if (nextElement) {
                    const horasValue = Number.parseFloat(nextElement.textContent.trim())
                    if (!Number.isNaN(horasValue)) {
                        console.log(`Horas estimadas encontradas (método 2): ${horasValue}`)
                        return horasValue
                    }
                }
            }
        }

        // Buscar en el HTML por el valor de Thymeleaf
        const thymeleafElements = document.querySelectorAll('[th\\:text*="totalHoras"], [th\\:text*="horas"]')
        for (const element of thymeleafElements) {
            const horasValue = Number.parseFloat(element.textContent.trim())
            if (!Number.isNaN(horasValue)) {
                console.log(`Horas estimadas encontradas (método 3): ${horasValue}`)
                return horasValue
            }
        }

        // Buscar en scripts inline como fallback
        const scripts = document.querySelectorAll("script")
        for (const script of scripts) {
            const content = script.textContent
            const match = content.match(/horasEstimadas\s*=\s*(\d+(?:\.\d+)?)/)
            if (match) {
                const horasValue = Number.parseFloat(match[1])
                console.log(`Horas estimadas encontradas (método 4): ${horasValue}`)
                return horasValue
            }
        }

        console.warn("No se pudieron encontrar las horas estimadas")
        return 0
    }
}

// Inicializar cuando el DOM esté listo
document.addEventListener("DOMContentLoaded", () => {
    new RRHHPresupuestos()
})
