package c0d3.vitreen.app.listeners

import android.location.Location

interface OnLocationFetchListener {

    fun onComplete(location: Location?) { }

}