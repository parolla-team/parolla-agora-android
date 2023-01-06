/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.x.initializer

import android.content.Context
import androidx.startup.Initializer
import coil.Coil
import coil.ImageLoader
import coil.ImageLoaderFactory
import io.element.android.x.architecture.bindings
import io.element.android.x.di.AppBindings

class CoilInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        Coil.setImageLoader(ElementImageLoaderFactory(context))
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}

private class ElementImageLoaderFactory(
    private val context: Context
) : ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader
            .Builder(context)
            .components {
                val appBindings = context.bindings<AppBindings>()
                val matrixUi = appBindings.matrixUi()
                val matrixClientProvider = {
                    appBindings
                        .sessionComponentsOwner().activeSessionComponent?.matrixClient()
                }
                matrixUi.registerCoilComponents(this, matrixClientProvider)
            }
            .build()
    }
}
