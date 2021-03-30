package c0d3.vitreen.app.listeners

import android.location.Location

interface OnLocationFetchListner {
    fun onComplete(location: Location?)
    fun onFailed(e: String?)
}