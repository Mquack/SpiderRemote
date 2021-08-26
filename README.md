# SpiderRemote
Simple bluetooth remote control app for android, communicates with a arduino with a HC-05 bluetooth module.
The app uses code from a couple of examples found online with some minor changes.
The device must already be paired with the HC-05 before the app can be used since it only checks for paired devices.

The data to be sent is in the buttons TAG but can be changed to a Switch-case statement without a lot of work if wanted.
Arduino sketch for the SpiderBot can be found at:
https://github.com/Mquack/spiderbot_stm32
