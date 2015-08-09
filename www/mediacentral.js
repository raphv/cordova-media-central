var exec = require('cordova/exec');

function logWithTimestamp() {
    var args = Array.prototype.slice.call(arguments);
    args.splice(0,0, (new Date()).toISOString());
    console.log.apply(console,args);
}

function logify(f) {
    return function() {
        logWithTimestamp.apply(this, arguments);
        (f || (function(){})).apply(this,arguments);
    };
}

var module_export = {};

[
    'camera',
    'videocamera',
    'imagegallery',
    'videogallery',
    'onimageloaded'
].forEach(function(endpoint) {
    module_export[endpoint] = function(success_cb, error_cb) {
        logWithTimestamp(endpoint + ' called');
        exec(logify(success_cb), logify(error_cb),'MediaCentral',endpoint,[]);
    };
});

module.exports = module_export;