/*
 * Copyright (C) 2018 Mobile Application Lab
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.mobileapplab.library

import java.io.Serializable

/**
 * Package information about the state of package.
 *
 * @param packageName Name of package.
 * @param state State of changed package.
 */
data class PackageState(val packageName: String, val state: State) : Serializable {
    enum class State {
        INSTALLED,
        UPDATING,
        UPDATED,
        REMOVED,
        FULLY_REMOVED,
        UPDATE_REMOVED,
        CHANGE_ENABLED_SETTINGS;
    }
}