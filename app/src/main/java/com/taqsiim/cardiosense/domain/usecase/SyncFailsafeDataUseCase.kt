package com.taqsiim.cardiosense.domain.usecase

import com.taqsiim.cardiosense.domain.repository.MonitorRepository

class SyncFailsafeDataUseCase(
    private val repository: MonitorRepository
) {
    // Use case for syncing failsafe data
}
