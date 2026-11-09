document.addEventListener('DOMContentLoaded', function () {
    var container = document.getElementById('items');
    var addButton = document.getElementById('addItem');
    var template = document.getElementById('itemTemplate');
    var vehicle = document.getElementById('vehicleId');
    if (!container || !addButton || !template) return;

    function filterParts() {
        if (!vehicle) return;
        var selected = vehicle.options[vehicle.selectedIndex];
        var modelId = selected ? selected.getAttribute('data-model') : '';
        container.querySelectorAll('select[name="partIds"]').forEach(function (select) {
            Array.from(select.options).forEach(function (option) {
                var partModel = option.getAttribute('data-model');
                option.hidden = !!partModel && partModel !== modelId;
                option.disabled = !!partModel && partModel !== modelId;
            });
            if (select.selectedOptions.length && select.selectedOptions[0].disabled) select.value = '';
        });
    }

    addButton.addEventListener('click', function () {
        container.appendChild(template.content.cloneNode(true));
        filterParts();
    });
    container.addEventListener('click', function (event) {
        if (event.target.classList.contains('remove-item')) {
            event.target.closest('.repair-item').remove();
        }
    });
    if (vehicle) vehicle.addEventListener('change', filterParts);
    filterParts();
});
