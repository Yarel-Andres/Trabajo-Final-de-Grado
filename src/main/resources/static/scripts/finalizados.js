// Gestión de la página de elementos finalizados
class FinalizadosManager {
    constructor() {
        this.init()
    }

    // Inicializa la funcionalidad de la página
    init() {
        this.initTooltips()
        this.initCollapses()
    }

    // Inicializa todos los tooltips de Bootstrap
    initTooltips() {
        const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
        const bootstrap = window.bootstrap // Declare the bootstrap variable
        tooltipTriggerList.map((tooltipTriggerEl) => new bootstrap.Tooltip(tooltipTriggerEl))
    }

    // Inicializa todos los elementos collapse
    initCollapses() {
        const collapseElementList = [].slice.call(document.querySelectorAll(".collapse"))
        const bootstrap = window.bootstrap // Declare the bootstrap variable
        collapseElementList.map((collapseEl) => new bootstrap.Collapse(collapseEl, { toggle: false }))
    }
}

// Inicializar cuando el DOM esté listo
document.addEventListener("DOMContentLoaded", () => new FinalizadosManager())
