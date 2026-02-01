package com.example.cardiosense.domain.usecase

import com.example.cardiosense.domain.repository.MonitorRepository

class SyncFailsafeDataUseCase(
    private val repository: MonitorRepository
) {
    // Use case for syncing failsafe data
}
