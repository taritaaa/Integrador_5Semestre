document.addEventListener('DOMContentLoaded', function () {
    var element = document.getElementById('repairDetailMap');
    if (!element || typeof L === 'undefined') return;
    var lat = Number(element.dataset.lat);
    var lng = Number(element.dataset.lng);
    if (!Number.isFinite(lat) || !Number.isFinite(lng)) return;
    var map = L.map(element).setView([lat, lng], 15);
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19, attribution: '&copy; OpenStreetMap'
    }).addTo(map);
    L.marker([lat, lng]).addTo(map);
});
