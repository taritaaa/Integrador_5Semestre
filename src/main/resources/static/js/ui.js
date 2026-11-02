/*
 * Interfaz común del taller:
 *  - RF-29: notificaciones Toast (éxito, error e información).
 *  - RF-30: modo claro / oscuro, recordado en el navegador.
 * Se carga desde templates/fragments/ui.html. No necesita librerías.
 */
(function () {
    'use strict';
    if (window.__tallerUi) {
        return;
    }
    window.__tallerUi = true;

    var THEME_KEY = 'taller-theme';

    // ---------- Modo claro / oscuro (RF-30) ----------
    function readTheme() {
        try {
            var saved = localStorage.getItem(THEME_KEY);
            if (saved === 'light' || saved === 'dark') {
                return saved;
            }
        } catch (e) { /* sin almacenamiento: se usa la preferencia del sistema */ }
        var prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
        return prefersDark ? 'dark' : 'light';
    }

    function saveTheme(theme) {
        try {
            localStorage.setItem(THEME_KEY, theme);
        } catch (e) { /* se ignora */ }
    }

    function applyTheme(theme) {
        document.documentElement.setAttribute('data-theme', theme);
        // water.css tiene una hoja para cada modo: se cambia light.css <-> dark.css
        var links = document.querySelectorAll('link[href*="water.css"]');
        for (var i = 0; i < links.length; i++) {
            links[i].href = links[i].href.replace(/\/(light|dark)\.css/, '/' + theme + '.css');
        }
        var button = document.getElementById('theme-toggle');
        if (button) {
            button.textContent = theme === 'dark' ? 'Modo claro' : 'Modo oscuro';
            button.setAttribute('aria-label', theme === 'dark' ? 'Cambiar a modo claro' : 'Cambiar a modo oscuro');
        }
    }

    function createThemeButton() {
        var button = document.createElement('button');
        button.id = 'theme-toggle';
        button.type = 'button';
        button.addEventListener('click', function () {
            var next = document.documentElement.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
            saveTheme(next);
            applyTheme(next);
        });
        document.body.appendChild(button);
        applyTheme(document.documentElement.getAttribute('data-theme') || readTheme());
    }

    // ---------- Toast (RF-29) ----------
    function container() {
        var box = document.getElementById('toast-container');
        if (!box) {
            box = document.createElement('div');
            box.id = 'toast-container';
            box.setAttribute('aria-live', 'polite');
            document.body.appendChild(box);
        }
        return box;
    }

    function showToast(type, message) {
        var toast = document.createElement('div');
        toast.className = 'toast toast-' + type;
        toast.setAttribute('role', type === 'error' ? 'alert' : 'status');

        var text = document.createElement('span');
        text.textContent = message; // textContent: nunca se interpreta como HTML

        var close = document.createElement('button');
        close.type = 'button';
        close.className = 'toast-close';
        close.setAttribute('aria-label', 'Cerrar notificación');
        close.textContent = '\u00d7';

        function dismiss() {
            toast.classList.add('toast-hide');
            setTimeout(function () {
                if (toast.parentNode) {
                    toast.parentNode.removeChild(toast);
                }
            }, 300);
        }

        close.addEventListener('click', dismiss);
        toast.appendChild(text);
        toast.appendChild(close);
        container().appendChild(toast);

        // Los errores duran más para que se alcancen a leer
        setTimeout(dismiss, type === 'error' ? 8000 : 5000);
    }

    // Quita el mensaje de texto simple que la página ya mostraba, para no repetirlo
    function removeInlineMessage(message) {
        var paragraphs = document.querySelectorAll('body > p');
        for (var i = 0; i < paragraphs.length; i++) {
            if (paragraphs[i].textContent.trim() === message) {
                paragraphs[i].parentNode.removeChild(paragraphs[i]);
            }
        }
    }

    function showServerMessages() {
        var messages = { success: null, error: null, info: null };

        var data = document.getElementById('toast-data');
        if (data) {
            messages.success = data.getAttribute('data-success');
            messages.error = data.getAttribute('data-error');
            messages.info = data.getAttribute('data-info');
        }

        // Login: Spring Security redirige a /login?error y /login?logout
        if (window.location.pathname === '/login') {
            var query = new URLSearchParams(window.location.search);
            if (query.has('error') && !messages.error) {
                messages.error = 'Correo o contraseña incorrectos, o la cuenta está desactivada.';
            }
            if (query.has('logout') && !messages.success) {
                messages.success = 'Sesión cerrada correctamente.';
            }
        }

        var types = ['success', 'error', 'info'];
        for (var i = 0; i < types.length; i++) {
            var message = messages[types[i]];
            if (message && message.trim() !== '') {
                removeInlineMessage(message.trim());
                showToast(types[i], message.trim());
            }
        }
    }

    // Permite lanzar un toast desde cualquier script: taller.toast('success', 'Listo')
    window.taller = { toast: showToast };

    // El tema se aplica ya (antes de que cargue el resto) para evitar el parpadeo
    applyTheme(readTheme());

    document.addEventListener('DOMContentLoaded', function () {
        createThemeButton();
        showServerMessages();
    });
})();