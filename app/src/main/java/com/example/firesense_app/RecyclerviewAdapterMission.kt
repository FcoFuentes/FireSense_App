package com.example.firesense_app

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.firesenseapp.Fire
import com.example.firesenseapp.Mission
import de.hdodenhof.circleimageview.CircleImageView
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

// class RecyclerviewAdapterMission(var context: Context, missionDataList: ArrayList<Mission>) :
class RecyclerviewAdapterMission(var context: Context, var fire: Fire, var fires: ArrayList<Fire>) :
        RecyclerView.Adapter<RecyclerviewAdapterMission.RecyclerviewHolder>() {
    var missionDataList: ArrayList<Mission>
    var filteredMissionDataList: ArrayList<Mission>
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerviewHolder {
        val view: View =
                LayoutInflater.from(context)
                        .inflate(R.layout.recyclerview_row_item_mission, parent, false)
        return RecyclerviewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerviewHolder, position: Int) {
        holder.missionName.text = "Nombre: " + filteredMissionDataList[position].name
        holder.missionDesc.text = "Descripción: " + filteredMissionDataList[position].description
        // holder.missionAlt.setText("Altura: "+filteredMissionDataList[position].alt)
        val localDateTime = filteredMissionDataList[position].date
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
        holder.missionDate.text = "Fecha: " + formatter.format(localDateTime)
        holder.missionImage.setImageResource(filteredMissionDataList[position].imageUrl)
        //  ItemAnimation.animateFadeIn(holder.itemView, position)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ProgressActivity::class.java)
            intent.putExtra("activityTo", "mission")
            intent.putExtra("mission", filteredMissionDataList[position])
            intent.putExtra("fire", fire)
            intent.putExtra("firesJetson", fires)
            intent.putExtra("activityFrom", "FIRE")
            context.startActivity(intent)
        }
        holder.missionImage.setOnClickListener(
                View.OnClickListener {
                    Toast.makeText(context, "User Name Clicked", Toast.LENGTH_SHORT).show()
                }
        )
    }

    override fun getItemCount(): Int {
        return filteredMissionDataList.size
    }

    class RecyclerviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var missionImage: CircleImageView
        var missionName: TextView
        var missionDesc: TextView

        // var missionAlt: TextView
        var missionDate: TextView

        init {
            missionImage = itemView.findViewById(R.id.missionImage)
            missionName = itemView.findViewById(R.id.missionName)
            missionDesc = itemView.findViewById(R.id.missionDesc)
            missionDate = itemView.findViewById(R.id.missionDate)
            // missionAlt  = itemView.findViewById(R.id.missionAlt)
        }
    }

    val filter: Filter
        get() =
                object : Filter() {
                    override fun performFiltering(charSequence: CharSequence): FilterResults {
                        val Key = charSequence.toString()
                        filteredMissionDataList =
                                if (Key.isEmpty()) {
                                    missionDataList
                                } else {
                                    val lstFiltered: MutableList<Mission> = ArrayList<Mission>()
                                    for (row in missionDataList) {
                                        if (row.name
                                                        .lowercase(Locale.getDefault())
                                                        .contains(
                                                                Key.lowercase(Locale.getDefault())
                                                        )
                                        ) {
                                            lstFiltered.add(row)
                                        }
                                    }
                                    lstFiltered as ArrayList<Mission>
                                }
                        val filterResults = FilterResults()
                        filterResults.values = filteredMissionDataList
                        return filterResults
                    }
                    override fun publishResults(
                            charSequence: CharSequence,
                            filterResults: FilterResults
                    ) {
                        filteredMissionDataList = filterResults.values as ArrayList<Mission>
                        notifyDataSetChanged()
                    }
                }

    init {
        this.missionDataList = fire.missions
        filteredMissionDataList = missionDataList
    }
}
