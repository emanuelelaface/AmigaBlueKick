/*
Amiga 500 Bluetooth Kickstart selector

WARNING: DON'T CONNECT THE USB OF THE ESP32 WHEN IT IS CONNECTED TO THE AMIGA.
THE POWER WILL ARRIVE DIRECTLY TO THE KICKSTART AND THE MAIN BOARD AND MAY DESTROY THEM.

---

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

#include <Arduino.h>
#include <Preferences.h>
#include "BLEDevice.h"
#include "BLEServer.h"
#include "BLEUtils.h"
#include "BLE2902.h"

#define A18_GPIO 6
#define A19_GPIO 7

#define INACTIVITY_TIMEOUT 30000

BLEServer* pServer = nullptr;
BLECharacteristic* pCharacteristic = nullptr;
bool deviceConnected = false;

Preferences preferences;

static unsigned long lastActivityTime = 0;

void setKickstart(char c) {
  if (c == '0') {
    digitalWrite(A18_GPIO, LOW);
    digitalWrite(A19_GPIO, LOW);
    preferences.putString("kick_value", String(c));
  } else if (c == '1') {
    digitalWrite(A18_GPIO, HIGH);
    digitalWrite(A19_GPIO, LOW);
    preferences.putString("kick_value", String(c));
  } else if (c == '2') {
    digitalWrite(A18_GPIO, LOW);
    digitalWrite(A19_GPIO, HIGH);
    preferences.putString("kick_value", String(c));
  } else if (c == '3') {
    digitalWrite(A18_GPIO, HIGH);
    digitalWrite(A19_GPIO, HIGH);
    preferences.putString("kick_value", String(c));
  } else {
    digitalWrite(A18_GPIO, LOW);
    digitalWrite(A19_GPIO, LOW);
    preferences.putString("kick_value", "0");
  }
}

class MyServerCallbacks : public BLEServerCallbacks {
  void onConnect(BLEServer* pServer) {
    deviceConnected = true;
    lastActivityTime = millis();
  }

  void onDisconnect(BLEServer* pServer) {
    deviceConnected = false;
    lastActivityTime = millis();
  }
};

class MyCharacteristicCallbacks : public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic* pCharacteristic) {
    String value = pCharacteristic->getValue().c_str();
    if (value.length() > 0) {
      char c = value[0];
      setKickstart(c);
      lastActivityTime = millis();
    }
  }
};

void setup() {
  Serial.begin(115200);

  pinMode(A18_GPIO, OUTPUT);
  pinMode(A19_GPIO, OUTPUT);

  preferences.begin("kickstart", false); 
  String savedValue = preferences.getString("kick_value", "0"); 
  setKickstart(savedValue[0]);

  BLEDevice::init("AmigaKickstartControl");
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

  BLEService *pService = pServer->createService("A500");

  pCharacteristic = pService->createCharacteristic(
                      "1234",
                      BLECharacteristic::PROPERTY_READ |
                      BLECharacteristic::PROPERTY_WRITE
                    );

  pCharacteristic->setCallbacks(new MyCharacteristicCallbacks());
  pCharacteristic->setValue(savedValue.c_str());
  pService->start();

  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID("A500");
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x06);
  pAdvertising->setMinPreferred(0x12);
  BLEDevice::startAdvertising();
  
  lastActivityTime = millis();
}

void loop() {
    if (!deviceConnected && (millis() - lastActivityTime >= INACTIVITY_TIMEOUT)) {
      gpio_hold_en( (gpio_num_t)A18_GPIO );
      gpio_hold_en( (gpio_num_t)A19_GPIO );
      esp_deep_sleep_start();
      gpio_hold_dis( (gpio_num_t)A18_GPIO );
      gpio_hold_dis( (gpio_num_t)A19_GPIO );
    }
}
