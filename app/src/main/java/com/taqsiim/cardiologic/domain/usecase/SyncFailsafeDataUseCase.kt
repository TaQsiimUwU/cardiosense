package com.taqsiim.cardiologic.domain.usecase

import com.taqsiim.cardiologic.domain.repository.MonitorRepository

class SyncFailsafeDataUseCase(
    private val repository: MonitorRepository
) {
    // Use case for syncing failsafe data
}
