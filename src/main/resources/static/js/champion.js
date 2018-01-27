$.fn.extend({
    animateCss: function (animationName, callback) {
        var animationEnd =
            'webkitAnimationEnd mozAnimationEnd MSAnimationEnd oanimationend animationend';
        this.addClass('animated ' + animationName).one(animationEnd, function () {
            $(this).removeClass('animated ' + animationName);
            if (callback) {
                callback();
            }
        });
        return this;
    },
});

var ws = null;
var audio = new Audio('music/4899.mp3');

function setConnected(connected) {
}

function connect() {
    ws = new SockJS('/echo');
    ws.onopen = function () {
        setConnected(true);
        log('Info: WebSocket connection opened.');
    };
    ws.onmessage = function (event) {
        log('Received: ' + event.data);
        parseData(event.data);
    };
    ws.onclose = function () {
        setConnected(false);
        log('Info: WebSocket connection closed.');
    };
}

function disconnect() {
    if (ws != null) {
        ws.close();
        ws = null;
    }
    setConnected(false);
}

function echo() {
    if (ws != null) {
        var message = document.getElementById('message').value;
        log('Sent: ' + message);
        ws.send(message);
    } else {
        alert('WebSocket connection not established, please connect.');
    }
}

function reset() {
    $('#ch-1').html('');
    $('#ch-2').html('');
    $('#ch-3').html('');

}

function parseData(data) {
    if (data.toLowerCase() == 'reset') {
        reset();
        return;
    }
    if (data.indexOf(';') > 0) {
        var championsArr = data.split(';');
        var champions = [];
        for (var i = 0; i < championsArr.length - 1; i++) {
            var championDataArr = championsArr[i].split(',');
            champions.push({'round': championDataArr[0], 'winner': championDataArr[1]})
        }
        _.sortBy(champions, 'round', 'asc');
        _.each(champions, function (obj) {
            showChampion(obj.round, obj.winner);
        })
        return;
    }

    var championDataArr = data.split(',');
    if (championDataArr.length > 1)
        playMusic(championDataArr[0], championDataArr[1]);

}

function playMusic(round, winner) {
    var target = '#ch-' + round;
    $(target).html('');

    if (!audio) {
        audio = new Audio('music/4899.mp3');
        log('init audio');
    }
    audio.play();
    setTimeout(function () {
        showChampion(round, winner)
    }, 4000);
}

function log(message) {
    var console = document.getElementById('console');
    var p = document.createElement('p');
    p.style.wordWrap = 'break-word';
    p.appendChild(document.createTextNode(message));
    console.appendChild(p);
    while (console.childNodes.length > 25) {
        console.removeChild(console.firstChild);
    }
    console.scrollTop = console.scrollHeight;
}

function showChampion(round, winner) {
    var target = '#ch-' + round;
    $(target).html(winner);
    $(target).animateCss('zoomIn');
}

$(document).ready(function () {
    connect();
});