/*
 * Interfaz común del taller:
 *  - RF-29: notificaciones Toast (éxito, error e información).
 *  - RF-30: modo claro / oscuro, recordado en el navegador.
 *  - Menú responsivo, enlace activo y protección contra doble envío en formularios.
 *
 * El modo se guarda en <html data-theme="light|dark">; los colores salen de theme.css.
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
        var button = document.getElementById('theme-toggle');
        if (button) {
            var labelText = theme === 'dark' ? 'Modo claro' : 'Modo oscuro';
            button.setAttribute('aria-label', 'Cambiar a ' + labelText.toLowerCase());
            button.setAttribute('title', 'Cambiar a ' + labelText.toLowerCase());
            var labelSpan = button.querySelector('.theme-label');
            if (labelSpan) {
                labelSpan.textContent = theme === 'dark' ? 'Claro' : 'Oscuro';
            } else {
                button.textContent = labelText;
            }
        }
    }

    // Si la página tiene barra (fragments/nav.html) ya trae su botón; si no, se crea uno flotante
    function setupThemeButton() {
        var button = document.getElementById('theme-toggle');
        if (!button) {
            button = document.createElement('button');
            button.id = 'theme-toggle';
            button.type = 'button';
            button.className = 'theme-toggle floating';
            document.body.appendChild(button);
        }
        button.addEventListener('click', function () {
            var next = document.documentElement.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
            saveTheme(next);
            applyTheme(next);
        });
        applyTheme(document.documentElement.getAttribute('data-theme') || readTheme());
    }

    // ---------- Menú móvil ----------
    function setupMobileMenu() {
        var toggle = document.getElementById('nav-toggle');
        var menu = document.getElementById('main-menu');
        if (!toggle || !menu) {
            return;
        }
        toggle.addEventListener('click', function () {
            var isOpen = menu.classList.toggle('is-open');
            toggle.setAttribute('aria-expanded', isOpen ? 'true' : 'false');
        });
        document.addEventListener('click', function (ev) {
            if (menu.classList.contains('is-open') && !menu.contains(ev.target) && !toggle.contains(ev.target)) {
                menu.classList.remove('is-open');
                toggle.setAttribute('aria-expanded', 'false');
            }
        });
    }

    // ---------- Enlace activo del menú ----------
    function markActiveLink() {
        var links = document.querySelectorAll('.menu a');
        var path = window.location.pathname;
        var best = null;
        for (var i = 0; i < links.length; i++) {
            var href = links[i].getAttribute('href');
            if (!href) {
                continue;
            }
            var linkPath = href.split('?')[0];
            var matches = linkPath === '/' ? path === '/' : (path === linkPath || path.indexOf(linkPath + '/') === 0);
            if (matches && (best === null || linkPath.length > best.path.length)) {
                best = { link: links[i], path: linkPath };
            }
        }
        if (best) {
            best.link.setAttribute('aria-current', 'page');
        }
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
        var duration = type === 'error' ? 8000 : 5000;
        var toast = document.createElement('div');
        toast.className = 'toast toast-' + type;
        toast.setAttribute('role', type === 'error' ? 'alert' : 'status');
        toast.style.setProperty('--toast-duration', duration + 'ms');

        var badge = document.createElement('span');
        badge.className = 'toast-dot';
        badge.setAttribute('aria-hidden', 'true');

        var text = document.createElement('span');
        text.className = 'toast-message';
        text.textContent = message; // textContent: nunca se interpreta como HTML

        var close = document.createElement('button');
        close.type = 'button';
        close.className = 'toast-close';
        close.setAttribute('aria-label', 'Cerrar notificación');
        close.textContent = '×';

        var progress = document.createElement('span');
        progress.className = 'toast-progress';

        var dismissed = false;
        function dismiss() {
            if (dismissed) {
                return;
            }
            dismissed = true;
            toast.classList.add('toast-hide');
            setTimeout(function () {
                if (toast.parentNode) {
                    toast.parentNode.removeChild(toast);
                }
            }, 260);
        }

        close.addEventListener('click', dismiss);
        toast.appendChild(badge);
        toast.appendChild(text);
        toast.appendChild(close);
        toast.appendChild(progress);
        container().appendChild(toast);

        setTimeout(dismiss, duration);
    }

    function removeInlineMessage(message) {
        var paragraphs = document.querySelectorAll('body p');
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

    // ---------- Prevención de doble envío en formularios ----------
    function setupFormSubmitGuard() {
        document.addEventListener('submit', function (ev) {
            var form = ev.target;
            if (!form || form.method.toLowerCase() !== 'post' || form.dataset.noGuard === 'true') {
                return;
            }
            var submitBtn = form.querySelector('button[type="submit"]:not([disabled]), input[type="submit"]:not([disabled])');
            if (submitBtn && !form.classList.contains('logout-form')) {
                submitBtn.classList.add('is-loading');
                setTimeout(function () {
                    submitBtn.disabled = true;
                }, 0);
            }
        });
    }

    // Permite lanzar un toast desde cualquier script: taller.toast('success', 'Listo')
    window.taller = { toast: showToast };

    applyTheme(document.documentElement.getAttribute('data-theme') || readTheme());

    document.addEventListener('DOMContentLoaded', function () {
        setupThemeButton();
        setupMobileMenu();
        markActiveLink();
        showServerMessages();
        setupFormSubmitGuard();
    });
})();