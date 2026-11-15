package com.thesabihjhatial.cockpit.bluetooth

sealed interface BluetoothStatus {
    data class Connected(val deviceName: String) : BluetoothStatus
    data object Disconnected : BluetoothStatus
}
