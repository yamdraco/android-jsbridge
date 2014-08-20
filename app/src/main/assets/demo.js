var jsFunc = function(id) {
  window.Android.bridgeResponse("log", "I am a log message from javascript");
  setTimeout(function() {
    window.Android.bridgeResponse(id, "I am an aysnc response from javascript!")
  }, 2000)
}