/*
 * Interfaz común de Veloxa Service (Neo-Garage):
 *  - RF-29: API de notificaciones Toast (window.taller.toast) alineada a .neo-alert.
 *  - RF-30: Sincronización de tema (dark por defecto) sin inyectar botones duplicados.
 *  - Protección contra doble envío en formularios POST (respeta data-no-guard="true").
 */
(function () {
    'use strict';
    if (window.__tallerUi) {
        return;
    }
    window.__tallerUi = true;

    // ---------- Sincronización de tema (RF-30) ----------
    function syncInitialTheme() {
        var current = document.documentElement.getAttribute('data-theme');
        if (current !== 'light' && current !== 'dark') {
            try {
                var saved = localStorage.getItem('veloxa-theme')
                    || localStorage.getItem('theme')
                    || localStorage.getItem('taller-theme')
                    || 'dark';
                current = (saved === 'light') ? 'light' : 'dark';
            } catch (e) {
                current = 'dark';
            }
            document.documentElement.setAttribute('data-theme', current);
        }
        if (document.body) {
            document.body.classList.toggle('light-theme', current === 'light');
        }
    }

    // ---------- Notificaciones Toast Neo-Garage (RF-29) ----------
    function getToastContainer() {
        var box = document.getElementById('toast-container');
        if (!box) {
            box = document.createElement('div');
            box.id = 'toast-container';
            box.className = 'neo-toast-stack';
            box.setAttribute('aria-live', 'polite');
            box.setAttribute('aria-atomic', 'true');
            document.body.appendChild(box);
        } else if (!box.classList.contains('neo-toast-stack')) {
            box.classList.add('neo-toast-stack');
        }
        return box;
    }

    function showToast(type, message) {
        if (!message || String(message).trim() === '') {
            return;
        }
        var safeType = (type === 'success' || type === 'error' || type === 'warning') ? type : 'info';
        var duration = safeType === 'error' ? 7000 : 5000;

        var titles = {
            success: 'Operación Exitosa',
            error: 'Alerta del Sistema',
            warning: 'Advertencia',
            info: 'Información'
        };

        var alertEl = document.createElement('div');
        alertEl.className = 'neo-alert neo-alert-' + safeType;
        alertEl.setAttribute('role', safeType === 'error' ? 'alert' : 'status');

        var body = document.createElement('div');
        body.className = 'neo-alert-body';

        var title = document.createElement('div');
        title.className = 'neo-alert-title';
        title.textContent = titles[safeType];

        var text = document.createElement('p');
        text.textContent = String(message).trim();

        body.appendChild(title);
        body.appendChild(text);

        var closeBtn = document.createElement('button');
        closeBtn.type = 'button';
        closeBtn.className = 'neo-alert-close';
        closeBtn.setAttribute('aria-label', 'Cerrar notificación');
        closeBtn.textContent = '✕';

        var dismissed = false;
        function dismiss() {
            if (dismissed) return;
            dismissed = true;
            alertEl.style.transition = 'opacity 260ms ease, transform 260ms ease';
            alertEl.style.opacity = '0';
            alertEl.style.transform = 'translateX(20px)';
            setTimeout(function () {
                if (alertEl.parentNode) {
                    alertEl.parentNode.removeChild(alertEl);
                }
            }, 280);
        }

        closeBtn.addEventListener('click', dismiss);
        alertEl.appendChild(body);
        alertEl.appendChild(closeBtn);
        getToastContainer().appendChild(alertEl);

        setTimeout(dismiss, duration);
    }

    // ---------- Prevención de doble envío en formularios POST ----------
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

    // API pública conservada para cualquier script del proyecto
    window.taller = { toast: showToast };

    syncInitialTheme();

    document.addEventListener('DOMContentLoaded', function () {
        syncInitialTheme();
        setupFormSubmitGuard();
    });
})();