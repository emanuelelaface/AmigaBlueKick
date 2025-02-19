import Foundation
import CoreBluetooth

class BLEManager: NSObject, ObservableObject, CBCentralManagerDelegate, CBPeripheralDelegate {

    let sensorName = "AmigaKickstartControl"
    let serviceName = "A500"
    let characteristicName = "1234"
  
    var myCentral: CBCentralManager!
    

    @Published var isSwitchedOn = false
    @Published var status = ""
    var sensorValue: UInt8 = 0
    
    private var peripheral: CBPeripheral!
    private var writeCharacteristic: CBCharacteristic?

    override init() {
        super.init()

        myCentral = CBCentralManager(delegate: self, queue: nil)
        myCentral.delegate = self
    }

    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        if central.state == .poweredOn {
            isSwitchedOn = true
        }
        else {
            isSwitchedOn = false
        }
    }
    
    func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
        var peripheralName: String!
        if let name = advertisementData[CBAdvertisementDataLocalNameKey] as? String {
            peripheralName = name
        }
        else {
            peripheralName = "Unknown"
        }
        if peripheralName == sensorName {
            self.stopScanning()
            self.myCentral.connect(peripheral, options: nil)
            self.peripheral = peripheral
        }
    }
    
    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        peripheral.delegate = self
        peripheral.discoverServices(nil)
    }
    func centralManager(_ central: CBCentralManager, didDisconnectPeripheral: CBPeripheral, error: Error?) {
        self.startScanning()
    }
    
    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        if let services = peripheral.services {
            for service in services {
                if service.uuid == CBUUID(string: serviceName) {
                    peripheral.discoverCharacteristics(nil, for: service)
                }
            }
        }
    }
    
    func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
        if let charac = service.characteristics {
            for characteristic in charac {
                if characteristic.uuid == CBUUID(string: characteristicName) {
                    self.writeCharacteristic = characteristic
                    self.peripheral.readValue(for: characteristic)
                    if let data = characteristic.value {
                        self.sensorValue = data[0]
                        self.status = String(self.sensorValue)
                    }
                }
            }
        }
    }
    func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
        if characteristic.uuid == CBUUID(string: characteristicName) {
            self.peripheral.readValue(for: characteristic)
            if let data = characteristic.value {
                self.sensorValue = data[0]
                self.status = String(self.sensorValue)
            }
        }
    }
    func send(message: String) {
        guard let peripheral = self.peripheral, let characteristic = self.writeCharacteristic else {
            print("Peripheral o caratteristica non disponibili")
            return
        }
        // Converte il messaggio in Data
        if let data = message.data(using: .utf8) {
            // Scrive i dati sul peripheral
            peripheral.writeValue(data, for: characteristic, type: .withResponse)
        }
    }
    func startScanning() {
        self.status = "Searching Amiga Blue Kick"
        myCentral.scanForPeripherals(withServices: nil, options: nil)
    }
    func stopScanning() {
        myCentral.stopScan()
    }

}
