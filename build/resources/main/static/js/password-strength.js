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

    var confirmInput = document.getElementById('confirmPassword');
    var matchHint = document.getElementById('pw-match-hint');

    var rules = [
        { text: 'Entre 8 y 72 caracteres', ok: function (p) { return p.length >= 8 && p.length <= 72; } },
        { text: 'Una mayúscula', ok: function (p) { return p !== p.toLowerCase(); } },
        { text: 'Una minúscula', ok: function (p) { return p !== p.toUpperCase(); } },
        { text: 'Un número', ok: function (p) { return /\d/.test(p); } },
        { text: 'Un símbolo (ej: !@#$%&*)', ok: function (p) { return /[^\p{L}\p{N}\s]/u.test(p); } }
    ];

    function renderStrength() {
        var p = input.value;
        box.textContent = '';
        box.className = 'pw-strength-box';

        var passed = 0;
        var list = document.createElement('ul');
        list.className = 'pw-rules-list';

        rules.forEach(function (r) {
            var ok = r.ok(p);
            if (ok) {
                passed++;
            }
            var li = document.createElement('li');
            if (ok) {
                li.className = 'is-ok';
            }
            li.textContent = (ok ? '\u2714 ' : '\u2718 ') + r.text;
            list.appendChild(li);
        });

        // Barra animada proporcional a las reglas cumplidas
        var track = document.createElement('div');
        track.className = 'pw-meter-track';
        var fill = document.createElement('div');
        fill.className = 'pw-meter-fill';
        var pct = p.length === 0 ? 0 : Math.round((passed / rules.length) * 100);
        fill.style.width = pct + '%';
        if (passed === rules.length) {
            fill.classList.add('level-high');
        } else if (passed >= 3) {
            fill.classList.add('level-mid');
        }
        track.appendChild(fill);

        var label = document.createElement('strong');
        label.className = 'pw-status-label';
        if (p.length === 0) {
            label.textContent = 'Requisitos de seguridad';
        } else if (passed === rules.length) {
            label.textContent = 'Contraseña de ALTA seguridad';
            label.classList.add('is-high');
        } else {
            label.textContent = 'Contraseña de BAJA seguridad (' + passed + '/' + rules.length + ')';
            label.classList.add('is-low');
        }

        box.appendChild(track);
        box.appendChild(label);
        box.appendChild(list);
        renderMatch();
    }

    function renderMatch() {
        if (!confirmInput || !matchHint) {
            return;
        }
        var p1 = input.value;
        var p2 = confirmInput.value;
        matchHint.className = 'pw-match-hint';
        if (!p2) {
            matchHint.textContent = '';
            return;
        }
        if (p1 === p2) {
            matchHint.textContent = '\u2714 Las contraseñas coinciden';
            matchHint.classList.add('is-match');
        } else {
            matchHint.textContent = '\u2718 Las contraseñas no coinciden';
            matchHint.classList.add('is-mismatch');
        }
    }

    input.addEventListener('input', renderStrength);
    if (confirmInput) {
        confirmInput.addEventListener('input', renderMatch);
    }
    renderStrength();
})();