
window.onload = function () {
    // add eventListener for tizenhwkey
    document.addEventListener('tizenhwkey', function(e) {
        if (e.keyName == "back") {
        	tizen.application.getCurrentApplication().exit();
        }
    });
    
    connect();

};
