# Browser Creation Command Tutorial

-------

## 1) Configure the Browser
There are two possible branches of commands for configuring the browser. You
may have a map browser or an entity browser to configure. Choose one that suits
your needs.

### Map Browser
Run the command `/browser configure map [width:height] [block width:block height] [algorithm]`

Example commands: `/browser configure map 640:640 5:5 FILTER_LITE` This creates a

## 2) Load the Browser
Use the `/browser load [entity | map] [url]` command to load a browser.
Here are some example commands of this:

`/browser load map https://google.com`
This loads a map browser (maps on itemframes) with the Google url.

`/browser load entity https://google.com`
This loads an entity browser (hologram or particle) with the Google url.
