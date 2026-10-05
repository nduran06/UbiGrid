/*
 * Map + "request a vehicle" interaction, modeled on the Wild Rydes ride
 * request flow: click the map to set a pickup point, request a ride, and
 * watch the assigned vehicle drive there.
 *
 * Unlike Wild Rydes' straight-line flight to the pickup point, the path here
 * is the real route UbiGrid's backend computes: POST /api/routes assigns the
 * nearest seeded vehicle and returns VehicleRouteService's actual waypoints,
 * distance, duration and traffic level. The map draws that route and animates
 * the vehicle along it, not through empty space.
 */
(function requestScope() {
  var sessionEmail = UbiGrid.auth.requireSession('/signin.html');
  document.getElementById('sessionEmail').textContent = sessionEmail;

  // Mythical creatures the visitor can pick to represent the assigned vehicle.
  // The first entry is the default. Emoji have no winged horse, so Pegaso uses
  // the horse.
  var CREATURES = [
    { id: 'unicornio', icon: '🦄', name: 'Unicornio' },
    { id: 'pegaso', icon: '🐎', name: 'Pegaso' },
    { id: 'dragon', icon: '🐉', name: 'Dragón' },
    { id: 'grifo', icon: '🦅', name: 'Grifo' },
    { id: 'basilisco', icon: '🦎', name: 'Basilisco' }
  ];
  var CREATURE_KEY = 'ubigrid_creature';

  var TRAFFIC_COLORS = {
    LIGHT: '#2e9e5b',
    MODERATE: '#f0a020',
    HEAVY: '#d93a3a'
  };
  var SENSOR_POLL_MS = 5000;

  var TRAFFIC_LABELS = {
    LIGHT: 'tráfico ligero',
    MODERATE: 'tráfico moderado',
    HEAVY: 'tráfico pesado'
  };

  var BOGOTA_CENTER = [4.7110, -74.0721];

  var map = L.map('map').setView(BOGOTA_CENTER, 13);
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap contributors',
    maxZoom: 18
  }).addTo(map);

  var pickupMarker = null;
  var vehicleMarker = null;
  var routeLayer = null;
  var sensorMarkers = {};
  var selectedPoint = null;

  var requestBtn = document.getElementById('requestBtn');
  var updatesList = document.getElementById('updates');

  var creatureSelect = document.getElementById('creatureSelect');
  CREATURES.forEach(function addOption(creature) {
    var option = document.createElement('option');
    option.value = creature.id;
    option.textContent = creature.icon + ' ' + creature.name;
    creatureSelect.appendChild(option);
  });
  try {
    creatureSelect.value = localStorage.getItem(CREATURE_KEY) || CREATURES[0].id;
  } catch (e) { /* storage unavailable: keep the default */ }
  if (!creatureSelect.value) {
    creatureSelect.value = CREATURES[0].id;
  }
  creatureSelect.addEventListener('change', function onCreatureChange() {
    try {
      localStorage.setItem(CREATURE_KEY, creatureSelect.value);
    } catch (e) { /* not persisted */ }
  });

  function selectedCreatureIcon() {
    return CREATURES.filter(function byId(c) { return c.id === creatureSelect.value; })[0].icon;
  }

  map.on('click', onMapClick);
  requestBtn.addEventListener('click', onRequestClick);

  document.getElementById('accountToggle').addEventListener('click', function toggle() {
    document.getElementById('accountDropdown').classList.toggle('open');
  });
  document.getElementById('signOutLink').addEventListener('click', function signOut(event) {
    event.preventDefault();
    UbiGrid.auth.signOut();
    window.location.href = '/index.html';
  });

  // Live traffic sensors: polled from the backend feed and drawn as circles
  // colored by congestion, so the data the router prices paths with is visible.
  function refreshSensors() {
    fetch('/api/traffic/sensors')
      .then(function parse(response) { return response.json(); })
      .then(function render(sensors) {
        sensors.forEach(function draw(sensor) {
          var color = TRAFFIC_COLORS[sensor.trafficLevel];
          var tooltip = '<strong>' + sensor.name + '</strong><br>'
            + sensor.averageSpeedKmh.toFixed(0) + ' km/h · ' + sensor.vehicleCount + ' vehículos<br>'
            + 'Congestión ' + Math.round(sensor.congestionLevel * 100) + '%'
            + (sensor.incident ? '<br>⚠️ Incidente reportado' : '');
          var marker = sensorMarkers[sensor.sensorId];
          if (!marker) {
            marker = L.circleMarker([sensor.lat, sensor.lng], { weight: 2, fillOpacity: 0.55 })
              .bindTooltip(tooltip)
              .addTo(map);
            sensorMarkers[sensor.sensorId] = marker;
          }
          marker.setStyle({
            color: sensor.incident ? '#7a1010' : color,
            fillColor: color,
            radius: 7 + sensor.congestionLevel * 8
          });
          marker.setTooltipContent(tooltip);
        });
        var time = new Date().toLocaleTimeString();
        document.getElementById('trafficFeed').innerHTML = '📡 Feed de tráfico en vivo · ' + sensors.length
          + ' sensores · ' + time
          + '<br>Tráfico:'
          + '<span class="legend-dot" style="background:' + TRAFFIC_COLORS.LIGHT + '"></span>ligero'
          + '<span class="legend-dot" style="background:' + TRAFFIC_COLORS.MODERATE + '"></span>moderado'
          + '<span class="legend-dot" style="background:' + TRAFFIC_COLORS.HEAVY + '"></span>pesado';
      })
      .catch(function ignore() {
        document.getElementById('trafficFeed').textContent = '📡 Feed de tráfico no disponible';
      });
  }
  refreshSensors();
  setInterval(refreshSensors, SENSOR_POLL_MS);

  function onMapClick(event) {
    if (requestBtn.disabled && requestBtn.textContent === 'Calculando ruta...') {
      return;
    }
    selectedPoint = event.latlng;

    if (pickupMarker) {
      map.removeLayer(pickupMarker);
    }
    pickupMarker = L.marker(selectedPoint).addTo(map);

    requestBtn.disabled = false;
    requestBtn.textContent = 'Solicitar vehículo';
  }

  function onRequestClick() {
    if (!selectedPoint) {
      return;
    }
    requestBtn.disabled = true;
    requestBtn.textContent = 'Calculando ruta...';
    requestRoute(selectedPoint, onRouteAssigned, onRouteError);
  }

  function requestRoute(pickupLocation, onSuccess, onError) {
    fetch('/api/routes', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        destinationLatitude: pickupLocation.lat,
        destinationLongitude: pickupLocation.lng,
        routePreference: 'FASTEST'
      })
    })
      .then(function handleResponse(response) {
        if (!response.ok) {
          return response.json().then(function readError(body) {
            throw new Error(body.error || 'No se pudo calcular la ruta.');
          });
        }
        return response.json();
      })
      .then(onSuccess)
      .catch(onError);
  }

  function onRouteAssigned(route) {
    var icon = selectedCreatureIcon();
    var km = (route.totalDistanceMeters / 1000).toFixed(1);
    var minutes = Math.max(1, Math.round(route.totalDurationSeconds / 60));
    var trafficLabel = TRAFFIC_LABELS[route.trafficSummary] || 'tráfico desconocido';

    displayUpdate('📡 Consultando ' + route.activeSensors + ' sensores de tráfico y evaluando '
      + route.evaluations.length + ' rutas posibles...');
    route.evaluations.forEach(function describe(evaluation) {
      var evalMinutes = Math.max(1, Math.round(evaluation.durationSeconds / 60));
      displayUpdate((evaluation.chosen ? '✅ ' : '▫️ ') + evaluation.label + ': '
        + (evaluation.distanceMeters / 1000).toFixed(1) + ' km, ~' + evalMinutes + ' min, '
        + (TRAFFIC_LABELS[evaluation.trafficLevel] || '') + (evaluation.chosen ? ' (elegida)' : ''));
    });
    displayUpdate(
      icon + ' ' + route.vehicleId + ' asignado. Ruta óptima: ' + km + ' km, ~' + minutes + ' min, ' + trafficLabel + '.'
    );

    drawRoute(route.path, route.segmentTraffic);
    animateAlongRoute(route.path, icon, function onArrival() {
      displayUpdate(icon + ' ' + route.vehicleId + ' ha llegado al punto de recogida.');
      resetForNextRequest();
    });
  }

  function onRouteError(error) {
    displayUpdate('No se pudo solicitar el vehículo: ' + error.message);
    requestBtn.disabled = false;
    requestBtn.textContent = 'Reintentar';
  }

  // One polyline per segment, colored by the traffic level the backend priced it with.
  function drawRoute(path, segmentTraffic) {
    if (routeLayer) {
      map.removeLayer(routeLayer);
    }
    routeLayer = L.featureGroup();
    for (var i = 0; i < path.length - 1; i++) {
      var color = TRAFFIC_COLORS[segmentTraffic[i]] || '#1560bd';
      L.polyline([[path[i].lat, path[i].lng], [path[i + 1].lat, path[i + 1].lng]],
        { color: color, weight: 5, opacity: 0.85 }).addTo(routeLayer);
    }
    routeLayer.addTo(map);
    map.fitBounds(routeLayer.getBounds(), { padding: [40, 40] });
  }

  function animateAlongRoute(path, icon, onDone) {
    var totalDurationMs = 6000;
    var distances = [];
    var totalDistance = 0;

    for (var i = 0; i < path.length - 1; i++) {
      var d = haversine(path[i], path[i + 1]);
      distances.push(d);
      totalDistance += d;
    }

    var markerIcon = L.divIcon({
      className: 'vehicle-marker-icon',
      html: icon,
      iconSize: [28, 28]
    });
    vehicleMarker = L.marker([path[0].lat, path[0].lng], { icon: markerIcon }).addTo(map);

    if (totalDistance === 0) {
      onDone();
      return;
    }

    var segmentIndex = 0;
    var segmentStart = null;
    var elapsedBeforeSegment = 0;

    function segmentDurationMs(index) {
      return (distances[index] / totalDistance) * totalDurationMs;
    }

    function step(timestamp) {
      if (!segmentStart) {
        segmentStart = timestamp;
      }
      var segmentElapsed = timestamp - segmentStart;
      var duration = segmentDurationMs(segmentIndex);
      var progress = duration > 0 ? Math.min(segmentElapsed / duration, 1) : 1;

      var from = path[segmentIndex];
      var to = path[segmentIndex + 1];
      var lat = from.lat + (to.lat - from.lat) * progress;
      var lng = from.lng + (to.lng - from.lng) * progress;
      vehicleMarker.setLatLng([lat, lng]);

      if (progress >= 1) {
        segmentIndex += 1;
        segmentStart = null;
        if (segmentIndex >= path.length - 1) {
          onDone();
          return;
        }
      }
      requestAnimationFrame(step);
    }

    requestAnimationFrame(step);
  }

  function haversine(a, b) {
    var R = 6371000;
    var toRad = function toRadians(deg) { return (deg * Math.PI) / 180; };
    var dLat = toRad(b.lat - a.lat);
    var dLng = toRad(b.lng - a.lng);
    var lat1 = toRad(a.lat);
    var lat2 = toRad(b.lat);
    var h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
      + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
    return R * 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
  }

  function resetForNextRequest() {
    if (pickupMarker) {
      map.removeLayer(pickupMarker);
      pickupMarker = null;
    }
    if (vehicleMarker) {
      map.removeLayer(vehicleMarker);
      vehicleMarker = null;
    }
    if (routeLayer) {
      map.removeLayer(routeLayer);
      routeLayer = null;
    }
    selectedPoint = null;
    requestBtn.disabled = true;
    requestBtn.textContent = 'Fijar punto de recogida';
  }

  function displayUpdate(text) {
    var item = document.createElement('li');
    item.textContent = text;
    updatesList.appendChild(item);
    updatesList.scrollTop = updatesList.scrollHeight;
  }
}());
