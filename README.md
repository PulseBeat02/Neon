# Neon

-----
## What is Neon?
Neon is a Bukkit-based plugin written in Java that serves to provide a 
user-friendly way to browser the internet. Besides rendering websites, it's
able to support playback from videos, files, etc. on the internet. Since Neon
uses Selenium, the plugin is able to load web pages at blazing fast speeds.

## Inspiration
Neon was heavily inspired by my EzMediaCore library, which was a plugin that
was able to play videos from the internet. It took advantage of playback using
VLC Media Player, FFmpeg, and YoutubeDL, but the code base became too complex
and I wanted to divert to a project that is more general. That's when I had the
idea of websites.

## How do I use the plugin?
The plugin is currently in development, so there aren't any automated builds. 
However, you are welcome to compile the plugin and test out the features
yourself. Here are some of the requirements for compiling locally.

- Java 17
- Minecraft Version 1.20.2
- BuildTools (1.20.2)

Steps for Building Locally
1) Run [BuildTools](https://www.spigotmc.org/wiki/buildtools/) to generate the
   1.20.2 server jar.
2) Clone the repository by using this Git link: https://github.com/MinecraftMediaLibrary/EzMediaCore.git.
3) Run `gradle build` to compile the plugin JAR.
4) Find the mods and plugins in the build folder.