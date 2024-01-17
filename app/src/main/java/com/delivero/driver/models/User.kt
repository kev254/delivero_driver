package com.delivero.driver.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint
import java.io.Serializable

data class User(
    @DocumentId
    var userId:String="",
    var deliveroId:Long=System.nanoTime() and 0xfffff,
    var email:String="",
    var phoneNumber:String="",
    var userName:String="",
    var latitude:Double=0.0,
    var longitude:Double=0.0,
    var location:String="",
    var status:String="",
    var profilePicture:String="",
    var roles:List<String> = listOf(),
    var regNumber:String="",
    var serviceLatitude:Double=0.0,
    var serviceLongitude:Double=0.0,
    var serviceLocation:String="",
    var capacity:String=""
):Serializable
