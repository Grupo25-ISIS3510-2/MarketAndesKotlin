package com.uniandes.marketandes.util

import kotlinx.coroutines.flow.Flow

interface ConnectivityObserver
{
    val isConnected: Flow<Boolean>
    fun observe(): Flow<NetworkStatus>
}
