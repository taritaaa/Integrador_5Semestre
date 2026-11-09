
document.addEventListener('DOMContentLoaded', function () {

    var mapElement = document.getElementById('repairHistoryMap');

    if (!mapElement || typeof L === 'undefined') {
        return;
    }

    // Crear mapa
    var map = L.map('repairHistoryMap').setView(
        [4.5709, -74.2973], 6
    );

    // Cargar OpenStreetMap
    L.tileLayer(
        'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
        {
            maxZoom: 19,
            attribution: '&copy; OpenStreetMap'
        }
    ).addTo(map);

    var locations = document.querySelectorAll('.location-point');

    var points = [];
    var bounds = [];

    // Leer todas las ubicaciones
    locations.forEach(function (item) {

        var lat = Number(item.dataset.lat);
        var lng = Number(item.dataset.lng);

        if (!Number.isFinite(lat) ||
            !Number.isFinite(lng) ||
            lat < -90 || lat > 90 ||
            lng < -180 || lng > 180) {
            return;
        }

        points.push({
            id: item.dataset.id,
            plate: item.dataset.plate,
            name: item.dataset.name,
            type: item.dataset.type,
            status: item.dataset.status,
            lat: lat,
            lng: lng
        });

    });

    // Agrupar reparaciones que tienen la misma ubicación
    var groups = {};

    points.forEach(function (point) {

        var key = point.lat.toFixed(6) + ',' +
                  point.lng.toFixed(6);

        if (!groups[key]) {
            groups[key] = [];
        }

        groups[key].push(point);

    });

    // Dibujar marcadores
    Object.keys(groups).forEach(function (key) {

        var group = groups[key];

        group.forEach(function (point, index) {

            var lat = point.lat;
            var lng = point.lng;

            var markerLat = lat;
            var markerLng = lng;

            // Si hay varias reparaciones en las mismas
            // coordenadas, separar visualmente los marcadores.
            if (group.length > 1) {

                var angle = (2 * Math.PI * index) / group.length;

                var distance = 0.00012;

                markerLat = lat + Math.cos(angle) * distance;
                markerLng = lng + Math.sin(angle) * distance;

            }

            // Crear marcador
            var marker = L.marker([
                markerLat,
                markerLng
            ]).addTo(map);

            // Contenido del marcador
            var popup = document.createElement('div');

            var title = document.createElement('strong');
            title.textContent = point.name || 'Taller sin nombre';

            popup.appendChild(title);

            var info = document.createElement('p');

            info.textContent =
                'Reparación: ' + point.id +
                ' | Placa: ' + point.plate +
                ' | Tipo: ' + point.type +
                ' | Estado: ' + point.status;

            popup.appendChild(info);

            var coordinates = document.createElement('p');

            coordinates.textContent =
                'Ubicación real: ' +
                point.lat + ', ' + point.lng;

            popup.appendChild(coordinates);

            var link = document.createElement('a');

            link.href = '/repairs/' +
                        encodeURIComponent(point.id);

            link.textContent = 'Ver reparación';

            popup.appendChild(link);

            marker.bindPopup(popup);

            bounds.push([markerLat, markerLng]);

            // Línea hacia la posición real si los
            // marcadores se separaron visualmente.
            if (group.length > 1) {

                L.polyline([
                    [markerLat, markerLng],
                    [lat, lng]
                ], {
                    color: '#666',
                    weight: 1,
                    dashArray: '4,4'
                }).addTo(map);

            }

        });

    });

    // Ajustar mapa para visualizar los marcadores
    if (bounds.length > 0) {

        if (bounds.length === 1) {

            map.setView(bounds[0], 15);

        } else {

            map.fitBounds(bounds, {
                padding: [40, 40],
                maxZoom: 16
            });

        }

    }

});
