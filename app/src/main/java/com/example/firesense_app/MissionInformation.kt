package com.example.firesense_app

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.firesenseapp.Mission
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.time.format.DateTimeFormatter

class MissionInformation(var mission: Mission) : Fragment() {

    private lateinit var txtMissionName: TextView
    private lateinit var txtFireName: TextView
    private lateinit var txtAltitude: TextView
    private lateinit var txtDateMission: TextView
    private lateinit var txtTimeMission: TextView
    private lateinit var mapView: MapView
    private lateinit var map: GoogleMap
    lateinit var mMapView: MapView
    private var googleMap: GoogleMap? = null

    @SuppressLint("MissingPermission")
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mission_information, container, false)
        mMapView = view.findViewById(R.id.mapView)
        mMapView.onCreate(savedInstanceState)
        mMapView.onResume()

        try {
            MapsInitializer.initialize(requireActivity().applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mMapView.getMapAsync { mMap ->
            googleMap = mMap

            // For showing a move to my location button
            //            googleMap!!.setMyLocationEnabled(true)

            // For dropping a marker at a point on the Map
            val mark = LatLng(mission.lat, mission.lon)
            googleMap!!.addMarker(
                    MarkerOptions().position(mark).title(mission.name).snippet("Marker Description")
            )

            // For zooming automatically to the location of the marker
            val cameraPosition = CameraPosition.Builder().target(mark).zoom(12f).build()
            googleMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }

        txtMissionName = view.findViewById(R.id.mission_name)
        txtFireName = view.findViewById(R.id.locate_name)
        // txtAltitude     = view.findViewById(R.id.mission_altitude)
        txtDateMission = view.findViewById(R.id.date_mission)
        txtTimeMission = view.findViewById(R.id.time_mission)

        // mapView = view.findViewById(R.id.mapView)
        //        mapView.onCreate(savedInstanceState)

        txtMissionName.text = mission.name
        // txtAltitude.text = mission.alt.toString()
        txtFireName.text = mission.fireName.toString()

        val formatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd") // HH:mm:ss
        val formatterHour = DateTimeFormatter.ofPattern("HH:mm")

        txtDateMission.text = mission.date.format(formatterDate)
        txtTimeMission.text = mission.date.format(formatterHour)
        return view
    }

    override fun onResume() {
        super.onResume()
        mMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mMapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView.onLowMemory()
    }
}
