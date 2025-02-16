# AmigaBlueKick
Amiga 500 Kickstart selector with Bluetooth

It is a common practice to store more than one kickstart on large EPROM that can accomodate more than 512 kB. In that case it is required a switch to select the Kickstart to use. The selction is done by the higher register pins, setting them to 0 or 1. A 27C160 EPROM can host 4x512 kB this means 4 selections so the last 2 pins are needed as selectors. For this purpose, it is common to connect a mechanical switch outside the Amiga drilling the plastic case (impossible for me even to imagine) or some more advanced system uses the reset wire to shuffle between the kickstart at each boot or depending on the time that the user hodls Control-Amiga-Amiga, but even this method requires a wire soldered on the main board.

So the idea is to bring the switch outside the Amiga in a wireless way, via Bluetooth. For this reason I decided to use a ESP32S3 board (but even a ESP32C3 should work perfectly and costs less, simply I had at home a S3 spare). I wired the board to the kickstart with power and two GPIO connected to the A18 and A19 of the Kickstart EPROM and controll it via any Bluetooth (BLE) client, from the computer or the phone.

The code is super simple: it creates a Bluetooth service that accepts ascii chars 0, 1, 2, 3 and based on that it turns HIGH or LOW the GPIOs connected to the EPROM.
The board is on only at the first start of the Amiga and stays on for 30 seconds, after that time it goes in deep sleep turning off everything but the GPIOs.

When the Kickstart is switched the Amiga will be in an unresponsive state because the CPU will not be initialized in a proper way, so a reboot will be needed.

So far I tried it only with an Amiga 500 rev 6A, I have no idea if it works on other machine but I do not see why it should not work.


<p align="center">
  <img src="https://github.com/emanuelelaface/AmigaBlueKick/blob/main/images/wiring.jpg" alt="Schematic" style="width: 50%;">
</p>
<div style="display: flex; justify-content: space-between;">
  <img src="https://github.com/emanuelelaface/AmigaBlueKick/blob/main/images/IMG_9770.JPG" alt="Image1" style="width: 32%;">
  <img src="https://github.com/emanuelelaface/AmigaBlueKick/blob/main/images/IMG_9771.JPG" alt="Image2" style="width: 32%;">
  <img src="https://github.com/emanuelelaface/AmigaBlueKick/blob/main/images/IMG_9772.JPG" alt="Image3" style="width: 32%;">
</div>


WARNING: DON'T CONNECT THE USB OF THE ESP32 WHEN IT IS CONNECTED TO THE AMIGA.
THE POWER WILL ARRIVE DIRECTLY TO THE KICKSTART AND THE MAIN BOARD AND MAY DESTROY THEM.

---

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
