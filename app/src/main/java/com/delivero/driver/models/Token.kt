package com.delivero.driver.models

import com.google.firebase.firestore.DocumentId
import java.io.Serializable

data class Token(@DocumentId var userId:String="",var token:String=""):Serializable {
}