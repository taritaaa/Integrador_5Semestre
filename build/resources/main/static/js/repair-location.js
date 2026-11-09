document.addEventListener('DOMContentLoaded', function () {
    var mapElement = document.getElementById('repairLocationMap');
    var latitude = document.getElementById('workshopLatitude');
    var longitude = document.getElementById('workshopLongitude');
    var currentButton = document.getElementById('currentLocation');
    var message = document.getElementById('locationMessage');
    if (!mapElement || !latitude || !longitude || typeof L === 'undefined') return;

    var map = L.map(mapElement).setView([4.5709, -74.2973], 6);
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19, attribution: '&copy; OpenStreetMap'
    }).addTo(map);
    var marker = null;

    function showPoint(lat, lng, moveMap) {
        if (!Number.isFinite(lat) || !Number.isFinite(lng) ||
            lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            if (message) message.textContent = 'La latitud debe estar entre -90 y 90, y la longitud entre -180 y 180.';
            return;
        }
        latitude.value = lat.toFixed(6);
        longitude.value = lng.toFixed(6);
        if (marker) marker.setLatLng([lat, lng]);
        else marker = L.marker([lat, lng], { draggable: true }).addTo(map);
        marker.off('dragend');
        marker.on('dragend', function () {
            var position = marker.getLatLng();
            showPoint(position.lat, position.lng, false);
        });
        if (moveMap) map.setView([lat, lng], 15);
        if (message) message.textContent = 'Ubicación seleccionada. Guarde el formulario para registrarla.';
    }

    map.on('click', function (event) {
        showPoint(event.latlng.lat, event.latlng.lng, false);
    });

    function updateFromInputs() {
        if (latitude.value.trim() === '' || longitude.value.trim() === '') return;
        showPoint(Number(latitude.value), Number(longitude.value), true);
    }
    latitude.addEventListener('change', updateFromInputs);
    longitude.addEventListener('change', updateFromInputs);

    if (currentButton) currentButton.addEventListener('click', function () {
        if (!navigator.geolocation) {
            if (message) message.textContent = 'Este navegador no permite acceder a la ubicación actual.';
            return;
        }
        if (message) message.textContent = 'Solicitando permiso de ubicación...';
        navigator.geolocation.getCurrentPosition(function (position) {
            showPoint(position.coords.latitude, position.coords.longitude, true);
        }, function () {
            if (message) message.textContent = 'No se pudo obtener la ubicación. Puede marcarla en el mapa.';
        }, { enableHighAccuracy: true, timeout: 12000 });
    });
    if (latitude.value.trim() && longitude.value.trim()) updateFromInputs();
});
