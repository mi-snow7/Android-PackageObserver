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

package net.mobileapplab.packageobserver

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import net.mobileapplab.library.PackageObserver
import net.mobileapplab.library.PackageState

class MainActivity : Activity() {

    private lateinit var observer: PackageObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        observer = object : PackageObserver(applicationContext) {
            override fun onPackageStateChanged(packageState: PackageState) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Package state changed: $packageState", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        observer.release()
    }
}
