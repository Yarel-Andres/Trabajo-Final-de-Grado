class Dashboard {
    constructor() {
        this.init()
    }


    //Inicializa el dashboard
    init() {
        this.showCurrentDate()
        this.animateCards()
    }


    // Muestra la fecha actual
    showCurrentDate() {
        const dateElement = document.getElementById("current-date")
        if (dateElement) {
            const today = new Date()
            dateElement.textContent = today.toLocaleDateString("es-ES", {
                weekday: "long",
                year: "numeric",
                month: "long",
                day: "numeric",
            })
        }
    }


    // Anima las tarjetas
    animateCards() {
        const cards = document.querySelectorAll(".dashboard-card")
        cards.forEach((card, index) => {
            setTimeout(() => card.classList.add("fade-in"), 100 * index)
        })
    }
}

// Inicializar
document.addEventListener("DOMContentLoaded", () => new Dashboard())
