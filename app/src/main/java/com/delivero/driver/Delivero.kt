package com.delivero.driver

import android.app.Application
import com.delivero.driver.helpers.MapUtils
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

class Delivero:Application() {
    override fun onCreate() {
        super.onCreate()
        MapUtils.setUpGeoApiContext()
        setUpRemoteConfig()

    }

    private fun setUpRemoteConfig() {
        val config = Firebase.remoteConfig
        val settings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        config.setConfigSettingsAsync(settings)
        config.setDefaultsAsync(R.xml.remote_config_defaults)
        config.fetchAndActivate()
    }




}