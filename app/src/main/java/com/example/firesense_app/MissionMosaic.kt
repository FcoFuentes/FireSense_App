package com.example.firesense_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firesenseapp.Communication
import com.example.firesenseapp.Mission
import com.example.firesenseapp.Utils
import com.squareup.picasso.Picasso
import java.io.File

class MissionMosaic(
        var mission: Mission,
        var images: ArrayList<String>,
        var fire: String,
        var utils: Utils,
        var comm: Communication,
        var connect: Boolean
) : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var imageGalleryAdapter: ImageGalleryAdapter
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_mission_mosaic, container, false)
        val layoutManager = GridLayoutManager(view.context, 2)
        recyclerView = view.findViewById(R.id.rv_images)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = layoutManager
        imageGalleryAdapter =
                ImageGalleryAdapter(
                        view.context,
                        MissionPhoto.getSunsetPhotos(
                                images,
                                "MOSAICO",
                                mission.name,
                                fire,
                                utils,
                                comm,
                                connect
                        )
                )
        // return inflater.inflate(R.layout.fragment_mission_mosaic, container, false)
        return view
    }

    override fun onStart() {
        super.onStart()
        recyclerView.adapter = imageGalleryAdapter
    }

    private inner class ImageGalleryAdapter(
            val context: Context,
            val missionPhotos: Array<MissionPhoto>
    ) : RecyclerView.Adapter<ImageGalleryAdapter.MyViewHolder>() {
        override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
        ): ImageGalleryAdapter.MyViewHolder {
            val context = parent.context
            val inflater = LayoutInflater.from(context)
            val photoView = inflater.inflate(R.layout.item_image, parent, false)
            return MyViewHolder(photoView)
        }

        override fun onBindViewHolder(holder: ImageGalleryAdapter.MyViewHolder, position: Int) {
            val sunsetPhoto = missionPhotos[position]
            val imageView = holder.photoImageView
            if (connect) {
                Picasso.get()
                        .load(sunsetPhoto.url)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.error)
                        .fit()
                        .into(imageView)
            } else {
                Log.i("onBindViewHolder", sunsetPhoto.url)
                var file = File(sunsetPhoto.url)
                if (file.exists()) Log.i("EXISTE", sunsetPhoto.url)
                else Log.i("NO EXISTE", sunsetPhoto.url)

                // if(!sunsetPhoto?.url.toString().contains(".txt")){
                Picasso.get()
                        .load("file://" + sunsetPhoto.url)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.error)
                        .fit()
                        .into(imageView)
                // }
                // Picasso.get().load("file://"+sunsetPhoto?.url).config(Bitmap.Config.RGB_565).fit().centerCrop().into(imageView);
            }
        }

        override fun getItemCount(): Int {
            return missionPhotos.size
        }

        inner class MyViewHolder(itemView: View) :
                RecyclerView.ViewHolder(itemView), View.OnClickListener {
            var photoImageView: ImageView = itemView.findViewById(R.id.iv_photo)
            init {
                itemView.setOnClickListener(this)
            }
            override fun onClick(view: View) {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    //  if(utils.isImported(mission.name,mission.fireName)) {
                    val sunsetPhoto = missionPhotos[position]
                    val intent = Intent(context, MissionPhotoActivity::class.java)
                    intent.putExtra(MissionPhotoActivity.EXTRA_SUNSET_PHOTO, sunsetPhoto)
                    intent.putExtra("missionName", mission.name)
                    intent.putExtra("fireName", mission.fireName)
                    intent.putExtra("connect", connect)
                    startActivity(intent)
                }
            }
        }
    }
}
