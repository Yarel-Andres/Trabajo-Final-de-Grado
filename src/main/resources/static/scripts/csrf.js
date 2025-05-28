// Manejo de tokens CSRF
document.addEventListener("DOMContentLoaded", () => {
    const token = document.querySelector('meta[name="_csrf"]')?.getAttribute("content")
    const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content")

    if (!token || !header) return

    // Agregar token a formularios POST sin CSRF
    document.querySelectorAll("form").forEach((form) => {
        if (form.method.toLowerCase() === "post" && !form.querySelector('input[name="_csrf"]')) {
            const csrfInput = document.createElement("input")
            csrfInput.type = "hidden"
            csrfInput.name = "_csrf"
            csrfInput.value = token
            form.appendChild(csrfInput)
        }
    })

    // Configurar AJAX para incluir token CSRF
    const originalOpen = XMLHttpRequest.prototype.open
    XMLHttpRequest.prototype.open = function () {
        originalOpen.apply(this, arguments)
        this.setRequestHeader(header, token)
    }
})
