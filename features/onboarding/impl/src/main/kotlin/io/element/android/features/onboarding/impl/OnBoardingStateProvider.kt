/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.features.onboarding.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class OnBoardingStateProvider : PreviewParameterProvider<OnBoardingState> {
    override val values: Sequence<OnBoardingState>
        get() = sequenceOf(
            anOnBoardingState(),
            anOnBoardingState(canLoginWithQrCode = true),
            anOnBoardingState(canCreateAccount = true),
            anOnBoardingState(canLoginWithQrCode = true, canCreateAccount = true),
            anOnBoardingState(isDebugBuild = true),
        )
}

fun anOnBoardingState(
    isDebugBuild: Boolean = false,
    productionApplicationName: String = "Parolla",
    canLoginWithQrCode: Boolean = false,
    canCreateAccount: Boolean = false
) = OnBoardingState(
    isDebugBuild = isDebugBuild,
    productionApplicationName = productionApplicationName,
    canLoginWithQrCode = canLoginWithQrCode,
    canCreateAccount = canCreateAccount
)
