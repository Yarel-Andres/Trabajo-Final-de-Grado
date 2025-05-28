// Clase para manejar la funcionalidad del layout
class LayoutManager {
    constructor() {
        this.init()
    }

    // Inicializar todas las funcionalidades
    init() {
        this.initTooltips()
        this.initSidebar()
        this.initDropdowns()
        this.initActiveLinks()
        this.initPageTransition()
        this.initMobileCards()
    }

    // Inicializar tooltips de Bootstrap
    initTooltips() {
        const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]')
        const bootstrap = window.bootstrap // Declare the bootstrap variable
        tooltipTriggerList.forEach((el) => new bootstrap.Tooltip(el))
    }

    // Configurar sidebar para móviles
    initSidebar() {
        const toggle = document.querySelector(".navbar-toggler")
        const sidebar = document.querySelector(".sidebar")
        const overlay = document.querySelector(".sidebar-overlay")
        const close = document.querySelector(".sidebar-close")

        if (toggle && sidebar && overlay) {
            toggle.addEventListener("click", () => this.toggleSidebar(sidebar, overlay))
            overlay.addEventListener("click", () => this.closeSidebar(sidebar, overlay))
            close?.addEventListener("click", () => this.closeSidebar(sidebar, overlay))
        }
    }

    // Abrir/cerrar sidebar
    toggleSidebar(sidebar, overlay) {
        sidebar.classList.toggle("show")
        overlay.classList.toggle("show")
        document.body.classList.toggle("sidebar-open")
    }

    // Cerrar sidebar
    closeSidebar(sidebar, overlay) {
        sidebar.classList.remove("show")
        overlay.classList.remove("show")
        document.body.classList.remove("sidebar-open")
    }

    // Configurar dropdowns del sidebar
    initDropdowns() {
        const links = document.querySelectorAll('.sidebar-link[data-bs-toggle="collapse"]')
        links.forEach((link) => link.addEventListener("click", (e) => this.handleDropdown(e, link)))
    }

    // Manejar dropdown del sidebar
    handleDropdown(e, link) {
        const submenuId = link.getAttribute("href")

        // Cerrar otros submenús
        document.querySelectorAll(".sidebar-submenu.show").forEach((menu) => {
            if (menu.id !== submenuId.substring(1)) {
                menu.classList.remove("show")
                const parentLink = document.querySelector(`[href="#${menu.id}"]`)
                parentLink?.classList.add("collapsed")
                parentLink?.setAttribute("aria-expanded", "false")
            }
        })

        // Toggle icono
        link.querySelector(".sidebar-dropdown-icon")?.classList.toggle("rotate")
    }

    // Activar enlace actual en sidebar
    initActiveLinks() {
        const currentPath = window.location.pathname
        document.querySelectorAll(".sidebar-menu a").forEach((link) => {
            if (link.getAttribute("href") === currentPath) {
                link.classList.add("active")
                this.expandParentMenu(link)
            }
        })
    }

    // Expandir menú padre si es submenú
    expandParentMenu(link) {
        const parentLi = link.closest(".sidebar-submenu")
        if (parentLi) {
            const parentId = parentLi.id
            const parentLink = document.querySelector(`[href="#${parentId}"]`)
            if (parentLink) {
                parentLink.classList.remove("collapsed")
                parentLink.setAttribute("aria-expanded", "true")
                document.querySelector(`#${parentId}`).classList.add("show")
                parentLink.querySelector(".sidebar-dropdown-icon")?.classList.add("rotate")
            }
        }
    }

    // Añadir animación de transición de página
    initPageTransition() {
        document.querySelector("main")?.classList.add("page-transition")
    }

    // Mejorar comportamiento de tarjetas en móviles
    initMobileCards() {
        if (window.innerWidth < 768) {
            document.querySelectorAll(".card").forEach((card) => {
                card.addEventListener("click", (e) => this.handleMobileCardClick(e, card))
            })
        }
    }

    // Manejar click en tarjetas móviles
    handleMobileCardClick(e, card) {
        if (!e.target.closest("button") && !e.target.closest("a")) {
            card.querySelector("a")?.click()
        }
    }
}

// Inicializar cuando el DOM esté listo
document.addEventListener("DOMContentLoaded", () => new LayoutManager())
