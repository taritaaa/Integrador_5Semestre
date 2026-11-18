
// Funciones del formulario de vehículos

document.addEventListener('DOMContentLoaded', function () {

    var model = document.getElementById('modelId');
    var serviceType = document.getElementById('serviceType');
    var plate = document.getElementById('licensePlate');
    var plateHelp = document.getElementById('plateHelp');
    var plateColor = document.getElementById('plateColor');
    var plateColorHelp = document.getElementById('plateColorHelp');

    if (!model || !serviceType) {
        return;
    }

    // Mostrar el formato de placa del modelo seleccionado
    function updatePlate() {

        var option = model.options[model.selectedIndex];

        var example = option
            ? option.getAttribute('data-example')
            : null;

        var format = option
            ? option.getAttribute('data-format')
            : null;

        if (!example) {

            if (plateHelp) {
                plateHelp.textContent =
                    'Seleccione un modelo para conocer el formato de placa.';
            }

            if (plate) {
                plate.placeholder = 'Seleccione primero el modelo';
            }

            return;
        }

        if (plateHelp) {

            if (serviceType.value === 'DIPLOMATIC') {
                plateHelp.textContent =
                    'Formato diplomático: CC-1234 o CD-1234.';
            } else {
                plateHelp.textContent =
                    'Formato de placa: ' + format +
                    '. Puede escribirla con o sin guion.';
            }
        }

        if (plate) {

            if (serviceType.value === 'DIPLOMATIC') {
                plate.placeholder = 'CC-1234';
            } else {
                plate.placeholder = example;
            }
        }
    }

    // Controlar cuándo se selecciona el color manualmente
    function updatePlateColor() {

        if (!plateColor) {
            return;
        }

        var option = model.options[model.selectedIndex];

        var isTrailer = option &&
            option.getAttribute('data-trailer') === 'true';

        var isOfficial =
            serviceType.value === 'OFFICIAL' && !isTrailer;

        if (isOfficial) {

            plateColor.disabled = false;
            plateColor.required = true;

            if (plateColorHelp) {
                plateColorHelp.textContent =
                    'Seleccione el color de placa del vehículo oficial.';
            }

        } else {

            plateColor.value = '';
            plateColor.disabled = true;
            plateColor.required = false;

            if (plateColorHelp) {
                plateColorHelp.textContent =
                    'El sistema asignará automáticamente el color de placa.';
            }
        }
    }

    // Cuando cambia el modelo
    model.addEventListener('change', function () {

        updatePlate();
        updatePlateColor();

    });

    // Cuando cambia el tipo de servicio
    serviceType.addEventListener('change', function () {

        updatePlate();
        updatePlateColor();

    });

    // Convertir la placa a mayúsculas
    if (plate) {

        plate.addEventListener('input', function () {

            plate.value = plate.value.toUpperCase();

        });

    }
    // Cargar la información al abrir el formulario
    updatePlate();
    updatePlateColor();

});
