# AmigaBlueKick

Amiga 500 Kickstart Selector with Bluetooth

It is common practice to store multiple Kickstart ROMs on large EPROMs that can accommodate more than 512 KB. In such cases, a switch is required to select the Kickstart to use. The selection is made using the higher address pins, setting them to 0 or 1. A 27C160 EPROM can hold four 512 KB Kickstart ROMs, meaning four selections are possible, requiring the last two address pins as selectors.

Traditionally, a mechanical switch is mounted outside the Amiga by drilling a hole in the plastic case (something I can't even imagine doing). More advanced methods use the reset wire to cycle through Kickstarts at each boot or based on how long the user holds Control-Amiga-Amiga, but even this method requires a wire soldered to the motherboard.

The idea is to make the switch wireless via Bluetooth. To achieve this, I used an ESP32S3 board. It is probably possible to use something else but it has to support the GPIO hold state after the Deep Sleep in order to keep the kickstart not floating. The board is wired to the Kickstart EPROM with power and two GPIOs connected to A18 and A19, allowing control via any Bluetooth (BLE) client, such as a computer or smartphone.

The code is very simple: it creates a Bluetooth service that accepts ASCII characters 0, 1, 2, and 3, and based on the input, it sets the GPIOs connected to the EPROM either HIGH or LOW.

The board is powered on only at the Amiga's first startup and remains active for 30 seconds. After that, it enters deep sleep, turning off everything except the GPIOs.

When the Kickstart is switched, the Amiga will become unresponsive because the CPU will not be properly initialized. A reboot is required.

There if the GPIO 3 is connected to the keyboard reset of Amiga, the ESP32 will be available also when the Amiga has a reset, also the ESP will reset the Amiga for each Kickstart Switch making the change smoother, otherwise the user has to reset manually.

So far, I have tested this only on an Amiga 500 rev 6A and on Amiga 600. I have no idea if it works on other machines, but I see no reason why it shouldn't.


<p align="center">
  <img src="https://github.com/emanuelelaface/AmigaBlueKick/blob/main/images/wiring.jpg" alt="Schematic" style="width: 50%;">
</p>
<div style="display: flex; justify-content: space-between;">
  <img src="https://github.com/emanuelelaface/AmigaBlueKick/blob/main/images/IMG_9770.JPG" alt="Image1" style="width: 32%;">
  <img src="https://github.com/emanuelelaface/AmigaBlueKick/blob/main/images/IMG_9771.JPG" alt="Image2" style="width: 32%;">
  <img src="https://github.com/emanuelelaface/AmigaBlueKick/blob/main/images/IMG_9772.JPG" alt="Image3" style="width: 32%;">
</div>

---

## Installation From the Binary File

You can load the binary file **AmigaBlueKick.bin**, which is located in the BIN folder.
The tool to upload the binary is the `esptool`. This is available as web page [esptool](https://espressif.github.io/esptool-js/). The web page should be compatible with Chrome browser or similar, probably not with Firefox, but on some operating system (like Mac OS) there can be a problem of binding the port to the web page.

1. Disconnect the adapter from the AMIGA.
2. Press and hold the **BOOT** button before connecting the board to the USB cable on the computer. Then, connect the board, wait a second, and release the button.
3. Go on the website, click on Connect, select the port for your adapter, change the Flash Address into 0x0000 and upload the firmware.

---

## Mobile App companion

<p align="center">
  <img src="https://github.com/emanuelelaface/AmigaBlueKick/blob/main/images/iOS1.png" alt="iOS Image1" style="width: 25%;">
  <img src="https://github.com/emanuelelaface/AmigaBlueKick/blob/main/images/iOS2.png" alt="iOS Image2" style="width: 25%;">
</p>

### iOS
To switch Kickstart, any Bluetooth explorer is fine, but I created an iOS app to make it easier to use.
The app is not available on the usual App Store because Apple has absurd rules that prevent me from using the name "Amiga" for no reason, and they refuse to discuss it.
Luckily, thanks to the European Union directive, we now have alternative stores where we can distribute our apps!
This app is now available on [AltStore](https://altstore.io/). After following their instructions to install the alternative store, you can add my repository using the URL `app.scumm.it/index.json`. Once the repository is added to AltStore, the app will be available for installation.

### Android
For Android, user @ibaldachini wrote and built the APK version, which can be found in the corresponding [folder](https://github.com/emanuelelaface/AmigaBlueKick/tree/main/android/AmigaBlueKick/app/release). There is currently no official distribution on Google Play due to the complications involved in meeting Google's beta testing and other requirements.

---

WARNING: DON'T CONNECT THE USB OF THE ESP32 WHEN IT IS CONNECTED TO THE AMIGA.
THE POWER WILL ARRIVE DIRECTLY TO THE KICKSTART AND THE MAIN BOARD AND MAY DESTROY THEM.

---

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
