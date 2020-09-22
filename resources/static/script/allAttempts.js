document.addEventListener('DOMContentLoaded', function() {

    var socket = new WebSocket(`ws://${window.location.host}/`);

    socket.addEventListener('message', function (e) {

        var update = JSON.parse(e.data);
        var tr = document.getElementById(`row${update.id}`);
        var tds = tr.getElementsByTagName('td');

        tds[2].innerHTML = `<mark>${update.status}</mark>`;
        tds[3].innerHTML = `<mark>${update.score}</mark>`;
    });

});