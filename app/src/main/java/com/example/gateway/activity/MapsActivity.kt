package com.example.gateway.activity


import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.example.gateway.R
import com.example.gateway.databinding.ActivityMapsBinding
import com.example.gateway.utils.DirectionsJSONParser
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class MapsActivity : FragmentActivity(), OnMapReadyCallback {

    lateinit var mMap: GoogleMap
    private val originalLatLong = LatLng(22.2403032,70.9007178)
    //    var markerPoints: ArrayList = ArrayList()
    private val markerPoints = ArrayList<LatLng>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        markerPoints.add(originalLatLong)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera

        mMap.addMarker(MarkerOptions().position(originalLatLong).title("Source Place"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(originalLatLong, 16F))
        mMap.setOnMapClickListener { latLng ->
            if (markerPoints.size > 1) {
                markerPoints.clear()
                mMap.clear()
            }

            // Adding new item to the ArrayList
            markerPoints.add(latLng)

            // Creating MarkerOptions
            val options = MarkerOptions()

            // Setting the position of the marker
            options.position(latLng)
            if (markerPoints.size == 1) {
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            } else if (markerPoints.size == 2) {
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            }

            // Add new marker to the Google Map Android API V2
            mMap.addMarker(options)

            // Checks, whether start and end locations are captured
            if (markerPoints.size >= 2) {
                val origin = markerPoints[0]
                val dest = markerPoints[1]

                // Getting URL to the Google Directions API
                val url: String = getDirectionsUrl(origin, dest)
                val downloadTask = DownloadTask()

                // Start downloading json data from Google Directions API
                downloadTask.execute(url)
            }
        }
    }

    inner class DownloadTask : AsyncTask<String?, String?, String?>() {
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            val parserTask = ParserTask()
            parserTask.execute(result)
        }

        override fun doInBackground(vararg url: String?): String {
            var data = ""
            try {
                data = downloadUrl(url[0]!!)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return data
        }

        @Throws(IOException::class)
        fun downloadUrl(strUrl: String): String {
            var data = ""
            var iStream: InputStream? = null
            var urlConnection: HttpURLConnection? = null
            try {
                val url = URL(strUrl)
                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.connect()
                iStream = urlConnection.inputStream
                val br = BufferedReader(InputStreamReader(iStream))
                val sb = StringBuffer()
                var line: String? = ""
                while (br.readLine().also({ line = it }) != null) {
                    sb.append(line)
                }
                data = sb.toString()
                br.close()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            } finally {
                iStream?.close()
                urlConnection?.disconnect()
            }
            return data
        }
    }

    inner class ParserTask : AsyncTask<String?, Int?, List<List<HashMap<String, String>>>?>() {

        override fun onPostExecute(result: List<List<HashMap<String, String>>>?) {
            var points: ArrayList<LatLng>
            var lineOptions: PolylineOptions? = null
            val markerOptions = MarkerOptions()
            for (i in result!!.indices) {
                points = ArrayList()
                lineOptions = PolylineOptions()
                val path: List<HashMap<String, String>> = result[i]
                for (j in path.indices) {
                    val point: HashMap<String, String> = path[j]
                    val lat: Double = point["lat"]!!.toDouble()
                    val lng: Double = point["lng"]!!.toDouble()
                    val position = LatLng(lat, lng)
                    points.add(position)
                }
                lineOptions.addAll(points)
                lineOptions.width(12f)
                lineOptions.color(Color.RED)
                lineOptions.geodesic(true)
            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions !=null) {
                mMap.addPolyline(lineOptions)
            }
        }

        override fun doInBackground(vararg jsonData: String?): List<List<HashMap<String, String>>>? {
            val jObject: JSONObject
            var routes: List<List<HashMap<String, String>>>? = null
            try {
                jObject = JSONObject(jsonData[0])
                val parser = DirectionsJSONParser()
                routes = parser.parse(jObject)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return routes
        }
    }

    private fun getDirectionsUrl(origin: LatLng, dest: LatLng): String {

        // Origin of route
        val str_origin = "origin=" + origin.latitude + "," + origin.longitude

        // Destination of route
        val str_dest = "destination=" + dest.latitude + "," + dest.longitude

        // Sensor enabled
        val sensor = "sensor=false"
        val mode = "mode=driving"
        // Enable direction API and generate new key
        val key = "key=" + "{YOUR_MAP_KEY}"

        // Building the parameters to the web service
        // Building the parameters to the web service
        val parameters = "$str_origin&$str_dest&$sensor&$mode&$key"
//
//        // Building the parameters to the web service
//        val parameters = "$str_origin&$str_dest&$sensor&$mode"

        // Output format
        val output = "json"

        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/$output?$parameters"
    }

}