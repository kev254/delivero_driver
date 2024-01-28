package com.delivero.driver.interfaces

import android.view.View
import com.delivero.driver.models.ServiceType

interface AdapterListener {
    fun onVehicleType(view:View,type: ServiceType){

    }
}