// Configuración de tipos de eventos
const TIPOS_EVENTO = {
    tarea: {
        clase: "evento-tarea",
        ruta: "/tareas/ver",
        horaDefecto: "T18:00:00",
    },
    proyecto: {
        clase: "evento-proyecto",
        ruta: "/proyectos/ver",
        horaDefecto: "T18:00:00",
    },
    reunion: {
        clase: "evento-reunion",
        ruta: "/reuniones/ver",
        horaDefecto: "",
    },
}

// Clase principal para manejar el calendario
class CalendarioManager {
    constructor() {
        this.calendar = null
        this.eventos = {
            tareas: [],
            proyectos: [],
            reuniones: [],
        }
        this.init()
    }

    // Inicializa el calendario y sus funcionalidades
    init() {
        this.procesarDatos()
        this.inicializarCalendario()
        this.configurarFiltros()
    }

    // Procesa los datos del servidor y los convierte a eventos
    procesarDatos() {
        const data = window.calendarioData

        this.eventos.tareas = this.convertirEventos(data.tareas, "tarea")
        this.eventos.proyectos = this.convertirEventos(data.proyectos, "proyecto")
        this.eventos.reuniones = this.convertirEventos(data.reuniones, "reunion")
    }

    // Convierte tareas a formato de eventos del calendario
    convertirEventos(datos, tipo) {
        const config = TIPOS_EVENTO[tipo]

        return datos.map((item) => {
            // Determinar fecha según tipo
            let fecha =
                tipo === "tarea" ? item.fechaVencimiento : tipo === "proyecto" ? item.fechaFinEstimada : item.fechaHora

            // Agregar hora si no existe
            if (fecha && !fecha.includes("T") && config.horaDefecto) {
                fecha += config.horaDefecto
            }

            return {
                id: `${tipo}-${item.id}`,
                title: tipo === "proyecto" ? item.nombre : item.titulo,
                start: fecha,
                description: item.descripcion,
                backgroundColor: "transparent",
                borderColor: "transparent",
                textColor: "#212529",
                allDay: false,
                className: config.clase,
                tipo: tipo,
                extendedProps: { ...item },
            }
        })
    }

    // Inicializa el calendario con FullCalendar
    inicializarCalendario() {
        const todosEventos = Object.values(this.eventos).flat()

        // Declare the FullCalendar variable before using it
        const FullCalendar = window.FullCalendar

        this.calendar = new FullCalendar.Calendar(document.getElementById("calendario"), {
            initialView: "dayGridMonth",
            headerToolbar: {
                left: "prev,next today",
                center: "title",
                right: "dayGridMonth,timeGridWeek,listWeek",
            },
            locale: "es",
            events: todosEventos,
            eventTimeFormat: {
                hour: "2-digit",
                minute: "2-digit",
                hour12: false,
            },
            eventClick: (info) => {
                const tipo = info.event.extendedProps.tipo
                window.location.href = TIPOS_EVENTO[tipo].ruta
            },
        })

        this.calendar.render()
    }

    // Maneja el click en eventos del calendario
    // Configura los filtros de eventos
    configurarFiltros() {
        ;["mostrarTareas", "mostrarProyectos", "mostrarReuniones"].forEach((id) => {
            const checkbox = document.getElementById(id)
            if (checkbox) {
                checkbox.addEventListener("change", () => this.filtrarEventos())
            }
        })
    }

    // Filtra eventos según los checkboxes seleccionados
    filtrarEventos() {
        const filtros = {
            mostrarTareas: document.getElementById("mostrarTareas").checked,
            mostrarProyectos: document.getElementById("mostrarProyectos").checked,
            mostrarReuniones: document.getElementById("mostrarReuniones").checked,
        }

        const eventosFiltrados = []

        if (filtros.mostrarTareas) eventosFiltrados.push(...this.eventos.tareas)
        if (filtros.mostrarProyectos) eventosFiltrados.push(...this.eventos.proyectos)
        if (filtros.mostrarReuniones) eventosFiltrados.push(...this.eventos.reuniones)

        // Actualizar calendario
        this.calendar.removeAllEvents()
        this.calendar.addEventSource(eventosFiltrados)
    }
}

// Inicializa el calendario cuando la página carga
document.addEventListener("DOMContentLoaded", () => {
    new CalendarioManager()
})
