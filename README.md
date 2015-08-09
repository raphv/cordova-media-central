# Media Central Cordova/Phonegap plugin

This plugin is meant to replace the Camera plugin for Android and provide:

- A unified way to access images and videos coming from 3 different pathways:
  - Using the camera activity
  - Using the file picker activity
  - Sharing these files from an external application
  
- More importantly, it works regardless of whether the main PhoneGap activity will be killed by Android:
  - This behaviour may happen when memory is low or when the "Don't keep activities" setting in Developer options is active
  - This means that the workflow is a bit quirky and that you have to take into account the fact that you have to save the state of your application before launching a camera or gallery action

- Apart from these selling points, is has much less features than the original camera plugin

## How to install

    cordova plugin add https://github.com/raphv/cordova-media-central.git

## How to use it

### The 'onimageloaded' callback

The philosophy of this plugin is that all video or image files will be accessed via a single callback, named whose argument is a string composed of the type and URI of the resource

    type;uri
    image;content://url/to/file
    video;file:///url/to/file
    
To set up this callback in your app, use the following syntax:

    document.addEventListener('deviceready', function() {
        window.mediacentral.onimageloaded(function(media_result) {
            var result_parts = media_result.split(";");
        });
    }, false);

### The media actions

These four actions take no callback, all callbacks are handled through the 'onimageloaded' callback.

    window.mediacentral.camera(); //Takes still images from the camera
    window.mediacentral.videocamera(); //Takes videos from the camera
    window.mediacentral.imagegallery(); //Takes images from the gallery
    window.mediacentral.videogallery(); //Takes videos from the gallery

