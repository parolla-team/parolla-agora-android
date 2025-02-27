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

package io.element.android.features.login.impl.accountprovider

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.appconfig.AuthenticationConfig

open class AccountProviderProvider : PreviewParameterProvider<AccountProvider> {
    override val values: Sequence<AccountProvider>
        get() = sequenceOf(
            anAccountProvider(),
            anAccountProvider().copy(subtitle = null),
            anAccountProvider().copy(subtitle = null, title = "no.sliding.sync", supportSlidingSync = false),
            anAccountProvider().copy(subtitle = null, title = "invalid", isValid = false, supportSlidingSync = false),
            anAccountProvider().copy(subtitle = null, title = "Other", isPublic = false, isMatrixOrg = false),
            // Add other state here
        )
}

fun anAccountProvider() = AccountProvider(
    url = AuthenticationConfig.PAROLLA_ORG_URL,
    subtitle = "",
    isPublic = true,
    isMatrixOrg = true,
    isValid = true,
    supportSlidingSync = true,
)
