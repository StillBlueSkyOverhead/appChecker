document.addEventListener('DOMContentLoaded', function() {

    var tx = document.getElementsByTagName('textarea');

    window.setTimeout(function() {
        for (var i = 0; i < tx.length; i++) {
            tx[i].setAttribute('style', 'height:' + (tx[i].scrollHeight) + 'px;overflow-y:hidden;min-height:4em;');
        }
    }, 600);
});
