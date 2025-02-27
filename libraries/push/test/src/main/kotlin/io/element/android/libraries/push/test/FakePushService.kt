/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.push.test

import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.push.api.PushService
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider
import io.element.android.tests.testutils.simulateLongTask

class FakePushService(
    private val testPushBlock: suspend () -> Boolean = { true },
    private val availablePushProviders: List<PushProvider> = emptyList(),
    private val registerWithLambda: suspend (MatrixClient, PushProvider, Distributor) -> Result<Unit> = { _, _, _ ->
        Result.success(Unit)
    },
) : PushService {
    override suspend fun getCurrentPushProvider(): PushProvider? {
        return availablePushProviders.firstOrNull()
    }

    override fun getAvailablePushProviders(): List<PushProvider> {
        return availablePushProviders
    }

    override suspend fun registerWith(
        matrixClient: MatrixClient,
        pushProvider: PushProvider,
        distributor: Distributor,
    ): Result<Unit> = simulateLongTask {
        return registerWithLambda(matrixClient, pushProvider, distributor)
    }

    override suspend fun testPush(): Boolean = simulateLongTask {
        testPushBlock()
    }
}
