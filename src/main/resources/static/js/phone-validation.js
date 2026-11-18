document.addEventListener('DOMContentLoaded', function () {
    var country = document.querySelector('.phone-country');
    var local = document.querySelector('.phone-local');
    var full = document.getElementById('phone') || document.getElementById('ownerPhone');
    if (!country || !local || !full) return;

    function rules() {
        switch (country.value) {
            case '+57': return { pattern: /^3[0-9]{9}$/, example: '3001234567' };
            case '+52': return { pattern: /^[0-9]{10}$/, example: '5512345678' };
            case '+1': return { pattern: /^[2-9][0-9]{2}[2-9][0-9]{6}$/, example: '2025550123' };
            case '+34': return { pattern: /^[67][0-9]{8}$/, example: '612345678' };
            case '+56': return { pattern: /^9[0-9]{8}$/, example: '912345678' };
            case '+51': return { pattern: /^9[0-9]{8}$/, example: '912345678' };
        }
        return null;
    }

    var existing = full.value.replace(/[\s-]/g, '');
    var codes = ['+57', '+52', '+1', '+34', '+56', '+51'];
    for (var i = 0; i < codes.length; i++) {
        if (existing.startsWith(codes[i])) {
            country.value = codes[i];
            local.value = existing.slice(codes[i].length);
            break;
        }
    }
    // Los datos antiguos sin prefijo se muestran en Colombia para corregirlos al editar.
    if (!local.value && existing && !existing.startsWith('+')) local.value = existing;

    function sync() {
        local.value = local.value.replace(/\D/g, '');
        var rule = rules();
        local.placeholder = rule.example;
        var valid = rule.pattern.test(local.value);
        local.setCustomValidity(!local.value || valid ? '' : 'El numero no coincide con el formato de ' + country.options[country.selectedIndex].text);
        full.value = country.value + local.value;
    }
    country.addEventListener('change', sync);
    local.addEventListener('input', sync);
    sync();
});
