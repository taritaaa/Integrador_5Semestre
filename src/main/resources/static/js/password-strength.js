/*
 * Indicador de seguridad de la contraseña.
 * Se usa en auth/change-password.html y auth/reset-password.html.
 * Necesita un <input id="newPassword"> y un <div id="pw-strength">.
 */
(function () {
    'use strict';
    var input = document.getElementById('newPassword');
    var box = document.getElementById('pw-strength');
    if (!input || !box) {
        return;
    }

    var rules = [
        { text: 'Entre 8 y 72 caracteres', ok: function (p) { return p.length >= 8 && p.length <= 72; } },
        { text: 'Una mayúscula', ok: function (p) { return p !== p.toLowerCase(); } },
        { text: 'Una minúscula', ok: function (p) { return p !== p.toUpperCase(); } },
        { text: 'Un número', ok: function (p) { return /\d/.test(p); } },
        { text: 'Un símbolo (ej: !@#$%&*)', ok: function (p) { return /[^\p{L}\p{N}\s]/u.test(p); } }
    ];

    function render() {
        var p = input.value;
        box.textContent = '';
        var passed = 0;
        var list = document.createElement('ul');
        rules.forEach(function (r) {
            var ok = r.ok(p);
            if (ok) {
                passed++;
            }
            var li = document.createElement('li');
            li.textContent = (ok ? '\u2714 ' : '\u2718 ') + r.text;
            list.appendChild(li);
        });
        var label = document.createElement('strong');
        if (p.length === 0) {
            label.textContent = '';
        } else if (passed === rules.length) {
            label.textContent = 'Contraseña de ALTA seguridad';
        } else {
            label.textContent = 'Contraseña de BAJA seguridad';
        }
        box.appendChild(label);
        box.appendChild(list);
    }

    input.addEventListener('input', render);
    render();
})();
