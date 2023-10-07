# Browser Creation Command Tutorial

---

## 1) Configure the Browser
There are two possible branches of commands for configuring the browser. You
may have a map browser or an entity browser to configure. Choose one that suits
your needs.

### Map Browser
Run the command `/browser configure map [width:height] [block width:block height] [algorithm]`

Example commands: `/browser configure map 640:640 5:5 FILTER_LITE` This creates a map browser that
has resolution 640 by 640 with a block width 5 by 5. It also uses the Sierra Filter Lite
dithering algorithm for pixels.

For a map display, you also need a board of itemframe maps to interact with. To create one super
easily, run the `/screen` command, which will give you a GUI to create a board of maps.

### Entity Browser
Run the command `/browser configure entity [width:height] [display type] [character]`

Example commands: `/browser configure entity 640:640 hologram BIG_SQUARE` This creates an entity browser that
has resolution 640 by 640 with a type hologram. It also uses a big square as its character for pixels.

---

## 2) Load the Browser
Use the `/browser load [entity | map] [url]` command to load a browser.
Here are some example commands of this:

`/browser load map https://google.com`
This loads a map browser (maps on itemframes) with the Google url.

`/browser load entity https://google.com`
This loads an entity browser (hologram or particle) with the Google url.

---

## 3) Destroy the Browser
Browsers use a lot of resources, and it's important to destroy them when
you aren't using them anymore. To do this, run the `/browser destroy`
command to free up the resources.