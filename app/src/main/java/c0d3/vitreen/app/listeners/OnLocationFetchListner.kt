package c0d3.vitreen.app.listeners

import android.location.Location

interface OnLocationFetchListner {
    fun OnComplete(location: Location?)
    fun OnFailed(e: String?)
}