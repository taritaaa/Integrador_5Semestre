// Funciones del formulario de vehículos (formatos, color automático y vista previa de placa)
document.addEventListener('DOMContentLoaded', function () {
    var model = document.getElementById('modelId');
    var serviceType = document.getElementById('serviceType');
    var plate = document.getElementById('licensePlate');
    var plateHelp = document.getElementById('plateHelp');
    var plateColor = document.getElementById('plateColor');
    var plateColorHelp = document.getElementById('plateColorHelp');
    var platePreview = document.getElementById('platePreview');

    if (!model || !serviceType) {
        return;
    }

    function isTrailerOption(option) {
        if (!option) {
            return false;
        }
        if (option.getAttribute('data-trailer') === 'true') {
            return true;
        }
        var example = (option.getAttribute('data-example') || '').toUpperCase();
        return example.indexOf('R-') === 0 || example.indexOf('S-') === 0;
    }

    // Actualiza la vista previa visual de la placa colombiana
    function updatePreview() {
        if (!platePreview) {
            return;
        }
        var option = model.options[model.selectedIndex];
        var example = option ? option.getAttribute('data-example') : null;
        var currentText = plate ? plate.value.trim().toUpperCase() : platePreview.textContent.trim();

        if (plate) {
            if (currentText) {
                platePreview.textContent = currentText;
            } else if (serviceType.value === 'DIPLOMATIC') {
                platePreview.textContent = 'CC-1234';
            } else {
                platePreview.textContent = example || 'ABC-123';
            }
        }

        // Color visual estimado en la vista previa según servicio o selección manual
        var previewColor = 'YELLOW';
        if (plateColor && !plateColor.disabled && plateColor.value) {
            previewColor = plateColor.value;
        } else if (isTrailerOption(option)) {
            previewColor = 'GREEN';
        } else if (serviceType.value === 'PUBLIC') {
            previewColor = 'WHITE';
        } else if (serviceType.value === 'DIPLOMATIC') {
            previewColor = 'BLUE';
        } else if (serviceType.value === 'SPECIAL' || serviceType.value === 'CLASSIC') {
            previewColor = 'BLUE';
        }
        platePreview.setAttribute('data-plate-color', previewColor);
    }

    // Mostrar el formato de placa del modelo seleccionado
    function updatePlate() {
        var option = model.options[model.selectedIndex];
        var example = option ? option.getAttribute('data-example') : null;
        var format = option ? option.getAttribute('data-format') : null;

        if (!example) {
            if (plateHelp) {
                plateHelp.textContent = 'Seleccione un modelo para conocer el formato de placa.';
            }
            if (plate) {
                plate.placeholder = 'Seleccione primero el modelo';
            }
            updatePreview();
            return;
        }

        if (plateHelp) {
            if (serviceType.value === 'DIPLOMATIC') {
                plateHelp.textContent = 'Formato diplomático: CC-1234 o CD-1234.';
            } else {
                plateHelp.textContent = 'Formato de placa: ' + format + '. Puede escribirla con o sin guion.';
            }
        }

        if (plate) {
            plate.placeholder = serviceType.value === 'DIPLOMATIC' ? 'CC-1234' : example;
        }
        updatePreview();
    }

    // Controlar cuándo se selecciona el color manualmente
    function updatePlateColor() {
        if (!plateColor) {
            return;
        }

        var option = model.options[model.selectedIndex];
        var isOfficial = serviceType.value === 'OFFICIAL' && !isTrailerOption(option);

        if (isOfficial) {
            plateColor.disabled = false;
            plateColor.required = true;
            if (plateColorHelp) {
                plateColorHelp.textContent = 'Seleccione el color de placa del vehículo oficial.';
            }
        } else {
            plateColor.value = '';
            plateColor.disabled = true;
            plateColor.required = false;
            if (plateColorHelp) {
                plateColorHelp.textContent = 'El sistema asignará automáticamente el color de placa.';
            }
        }
        updatePreview();
    }

    model.addEventListener('change', function () {
        updatePlate();
        updatePlateColor();
    });

    serviceType.addEventListener('change', function () {
        updatePlate();
        updatePlateColor();
    });

    if (plateColor) {
        plateColor.addEventListener('change', updatePreview);
    }

    if (plate) {
        plate.addEventListener('input', function () {
            plate.value = plate.value.toUpperCase();
            updatePreview();
        });
    }

    // Cargar la información al abrir el formulario
    updatePlate();
    updatePlateColor();
});