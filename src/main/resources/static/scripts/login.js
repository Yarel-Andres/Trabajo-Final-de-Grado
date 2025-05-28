// Clase principal para manejar efectos del login
class LoginEffects {
    constructor() {
        this.init()
    }

    // Inicializa todos los efectos
    init() {
        this.setupInputEffects()
        this.setupButtonAnimation()
        this.createParticles()
    }

    // Configura efectos de los campos de entrada
    setupInputEffects() {
        const inputs = document.querySelectorAll(".input-icon input")

        inputs.forEach((input) => {
            const icon = input.parentElement.querySelector("i")

            // Cambia color del icono al enfocar
            input.addEventListener("focus", () => {
                icon.style.color = "white"
            })

            // Restaura color del icono al desenfocar
            input.addEventListener("blur", () => {
                if (!input.value) {
                    icon.style.color = "rgba(255, 255, 255, 0.6)"
                }
            })
        })
    }

    // Configura animación del botón de login
    setupButtonAnimation() {
        const loginButton = document.querySelector(".btn-login")

        if (loginButton) {
            // Hace girar el icono al pasar el ratón
            loginButton.addEventListener("mouseenter", () => {
                const icon = loginButton.querySelector("i")
                if (icon) {
                    icon.classList.add("fa-spin")
                    setTimeout(() => icon.classList.remove("fa-spin"), 500)
                }
            })
        }
    }

    // Crea todas las partículas de fondo
    createParticles() {
        const container = document.getElementById("particles")
        if (!container) return

        const particleCount = 20
        const fragment = document.createDocumentFragment()

        // Crea 20 partículas
        for (let i = 0; i < particleCount; i++) {
            const particle = this.createParticle()
            fragment.appendChild(particle)
        }

        container.appendChild(fragment)
    }

    // Crea una partícula individual
    createParticle() {
        const particle = document.createElement("div")
        particle.className = "particle"

        // Genera valores aleatorios para la partícula
        const size = Math.random() * 2 + 2
        const posX = Math.random() * 100
        const posY = Math.random() * 100
        const opacity = Math.random() * 0.3 + 0.1

        // Aplica los estilos a la partícula
        Object.assign(particle.style, {
            width: `${size}px`,
            height: `${size}px`,
            left: `${posX}%`,
            top: `${posY}%`,
            opacity: opacity,
        })

        return particle
    }
}

// Inicia los efectos cuando la página carga
document.addEventListener("DOMContentLoaded", () => {
    new LoginEffects()
})
