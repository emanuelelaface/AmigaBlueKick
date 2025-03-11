package com.example.amigabluekick

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import java.util.*

class BLEManager(private val context: Context) {

    var listener: ((String?)->Unit)? = null

    // Nome del dispositivo BLE da cercare (uguale al tuo "sensorName" in iOS)
    private val deviceName = "AmigaKickstartControl"

    // UUID del servizio e della caratteristica (ricavati rispettivamente da "A500" e "1234")
    private val serviceUUID: UUID = UUID.fromString("0000A500-0000-1000-8000-00805f9b34fb")
    private val characteristicUUID: UUID = UUID.fromString("00001234-0000-1000-8000-00805f9b34fb")

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var scanning = false
    private var bluetoothGatt: BluetoothGatt? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    @SuppressLint("MissingPermission")
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Dispositivo connesso, avvio discoverServices()")
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                listener?.invoke(null)
                Log.d(TAG, "Dispositivo disconnesso")
                bluetoothGatt = null
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Servizi scoperti con successo")
                val service = gatt.getService(serviceUUID)
                if (service != null) {
                    val characteristic = service.getCharacteristic(characteristicUUID)
                    if (characteristic != null) {
                        bluetoothGatt?.readCharacteristic(characteristic)
                        writeCharacteristic = characteristic
                        Log.d(TAG, "Caratteristica individuata: $characteristicUUID")
                    } else {
                        Log.e(TAG, "Caratteristica non trovata!")
                    }
                } else {
                    Log.e(TAG, "Servizio non trovato!")
                }
            } else {
                Log.e(TAG, "onServicesDiscovered fallito con status $status")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            if (characteristic.uuid == characteristicUUID) {
                val data = characteristic.value
                val stringValue = data?.let { String(it) }
                Log.d(TAG, "Valore aggiornato dalla caratteristica: $stringValue")
                listener?.invoke(stringValue)
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            bluetoothGatt?.readCharacteristic(characteristic)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (characteristic.uuid == characteristicUUID) {
                val data = characteristic.value
                val stringValue = data?.let { String(it) }
                Log.d(TAG, "Valore aggiornato dalla caratteristica: $stringValue")
                listener?.invoke(stringValue)
            }
        }
    }

    init {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            Log.e(TAG, "Bluetooth disabilitato o non disponibile")
            return
        }

        if (!scanning) {
            scanning = true
            Log.d(TAG, "Inizio scansione per $deviceName")

            bluetoothLeScanner?.startScan(null, scanSettings, leScanCallback)
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (scanning) {
            scanning = false
            bluetoothLeScanner?.stopScan(leScanCallback)
            Log.d(TAG, "Scansione fermata")
        }
    }

    private val leScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device ?: return
            val name = result.scanRecord?.deviceName?: "Sconosciuto"
            if (name == deviceName) {
                Log.d(TAG, "Dispositivo trovato: $name, fermo la scansione e mi connetto")
                stopScan()
                connectToDevice(device)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    @SuppressLint("MissingPermission")
    fun send(message: String) {
        if (bluetoothGatt == null || writeCharacteristic == null) {
            Log.e(TAG, "Gatt o caratteristica non inizializzati, impossibile inviare dati")
            return
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Permessi di connessione BLE non concessi!")
            return
        }

        writeCharacteristic?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        writeCharacteristic?.value = message.toByteArray()
        val success = bluetoothGatt?.writeCharacteristic(writeCharacteristic)
        Log.d(TAG, "Tentativo di invio dati (success=$success)")
    }

    companion object {
        private const val TAG = "BLEManager"
    }
}
