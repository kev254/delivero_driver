package com.delivero.driver.helpers

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.animation.LinearInterpolator
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.delivero.driver.BuildConfig
import com.delivero.driver.R
import com.delivero.driver.interfaces.CompleteListener
import com.google.android.gms.maps.model.LatLng
import com.google.maps.GeoApiContext
import com.google.maps.GeocodingApi
import org.json.JSONObject
import kotlin.math.abs
import kotlin.math.atan

object MapUtils {
    lateinit var geoApiContext: GeoApiContext

    fun setUpGeoApiContext() {
        geoApiContext = GeoApiContext.Builder()
            .apiKey(BuildConfig.GOOGLE_API_KEY)
            .build()
    }


    fun reverseGeocode(listener: CompleteListener, from: LatLng) {
        val result = GeocodingApi.reverseGeocode(
            geoApiContext,
            com.google.maps.model.LatLng(from.latitude, from.longitude)
        ).await()
        listener.onComplete(result[0].formattedAddress)

    }

    fun getOriginMarkerBitmap(context: Context): Bitmap {
        return BitmapFactory.decodeResource(context.resources, R.drawable.pickup)
    }

    fun getDestinationMarkerBitmap(context: Context): Bitmap {
        return BitmapFactory.decodeResource(context.resources, R.drawable.destination)
    }

    fun getMyMarkerBitmap(context: Context): Bitmap {
        return BitmapFactory.decodeResource(context.resources, R.drawable.my_location)
    }

    fun getCarBitmap(context: Context): Bitmap {
        return BitmapFactory.decodeResource(context.resources, R.drawable.cart)
        // return Bitmap.createScaledBitmap(bitmap, 50, 100, false)
    }


    fun polylineAnimator(): ValueAnimator {
        val valueAnimator = ValueAnimator.ofInt(0, 100)
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.duration = 4000
        return valueAnimator
    }

    fun getRotation(start: LatLng, end: LatLng): Float {
        val latDifference: Double = abs(start.latitude - end.latitude)
        val lngDifference: Double = abs(start.longitude - end.longitude)
        var rotation = -1F
        when {
            start.latitude < end.latitude && start.longitude < end.longitude -> {
                rotation = Math.toDegrees(atan(lngDifference / latDifference)).toFloat()
            }

            start.latitude >= end.latitude && start.longitude < end.longitude -> {
                rotation = (90 - Math.toDegrees(atan(lngDifference / latDifference)) + 90).toFloat()
            }

            start.latitude >= end.latitude && start.longitude >= end.longitude -> {
                rotation = (Math.toDegrees(atan(lngDifference / latDifference)) + 180).toFloat()
            }

            start.latitude < end.latitude && start.longitude >= end.longitude -> {
                rotation =
                    (90 - Math.toDegrees(atan(lngDifference / latDifference)) + 270).toFloat()
            }
        }
        return rotation
    }

    fun carAnimator(): ValueAnimator {
        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.duration = 3000
        valueAnimator.interpolator = LinearInterpolator()
        return valueAnimator
    }

    fun getRoutes(pickupLatLng: LatLng, destLatLng: LatLng, completeCallback: CompleteListener) {
        val apiKey = BuildConfig.GOOGLE_API_KEY

        val origin = "${pickupLatLng.latitude},${pickupLatLng.longitude}"
        val destination = "${destLatLng.latitude},${destLatLng.longitude}"
        val mode = "driving"
        val output = "json"
        val url =
            "https://maps.googleapis.com/maps/api/directions/$output?origin=$origin&destination=$destination&sensor=false&mode=$mode&key=$apiKey"
        AndroidNetworking.post(url)
            .setPriority(Priority.IMMEDIATE)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.e("Routes Response", response.toString())
                    val routes = response.getJSONArray("routes")
                    val distArray = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0)
                        .getJSONObject("distance")
                    val distance = distArray.getString("text")
                    Log.e("distArray", response.toString())
                    Log.e("distance", distance)

                    val polyString = routes.getJSONObject(0).getJSONObject("overview_polyline")
                        .getString("points")
                    completeCallback.onPoints(decodePoly(polyString), distance)
                }

                override fun onError(anError: ANError?) {

                }
            })
    }

    private fun decodePoly(encoded: String): List<LatLng> {
        val poly: MutableList<LatLng> = ArrayList()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val p = LatLng(
                lat.toDouble() / 1E5,
                lng.toDouble() / 1E5
            )
            poly.add(p)
        }
        return poly
    }

}