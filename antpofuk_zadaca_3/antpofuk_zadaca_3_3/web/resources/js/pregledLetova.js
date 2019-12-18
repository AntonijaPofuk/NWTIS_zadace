var wsocket;
function connect() {
    var aplikacija = "/" + document.location.pathname.split("/")[1];
    var wsUri = "ws://" + document.location.host + aplikacija + "/infoPutnik";
    wsocket = new WebSocket(wsUri);
    wsocket.onmessage = onMessage;
}
function onMessage(evt) {
    var putnikID = evt.data;
    var odabraniPutnik = document.getElementById("forma:odabraniPutnik").selectedOptions[0].value;
    if (odabraniPutnik === putnikID) {
        document.getElementById("forma:preuzmiLetoveBtn").click();
    }   
}
window.addEventListener("load", connect, false);
