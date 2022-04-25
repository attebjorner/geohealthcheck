package gosha.kalosha.entity

import java.util.concurrent.atomic.AtomicBoolean

data class AppStatus(
    val namespace: String,
    val isOk: AtomicBoolean = AtomicBoolean(true)
)
