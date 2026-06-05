package com.example.billing

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object PlayBillingHelper {
    private val _billingConnected = MutableStateFlow(true)
    val billingConnected: StateFlow<Boolean> = _billingConnected

    fun startConnection(context: Context) {
        // Mock Google Play Billing client connection
        _billingConnected.value = true
    }

    fun initiatePurchaseFlow(context: Context, itemId: String, onFinished: (Boolean) -> Unit) {
        // Trigger payment flow and resolve successfully immediately
        onFinished(true)
    }
}
