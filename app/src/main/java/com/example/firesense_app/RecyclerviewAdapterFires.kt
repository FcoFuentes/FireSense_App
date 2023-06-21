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
import de.hdodenhof.circleimageview.CircleImageView
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class RecyclerviewAdapterFires(var context: Context, fireDataList: ArrayList<Fire>) :
        RecyclerView.Adapter<RecyclerviewAdapterFires.RecyclerviewHolder>() {
    var fireDataList: ArrayList<Fire>
    var filteredFireDataList: ArrayList<Fire>
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerviewHolder {
        val view: View =
                LayoutInflater.from(context).inflate(R.layout.recyclerview_row_item, parent, false)
        return RecyclerviewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerviewHolder, position: Int) {
        holder.fireName.text = filteredFireDataList[position].name
        holder.fireLocation.text = "Localidad: " + filteredFireDataList[position].locationPlace

        val localDateTime = filteredFireDataList[position].date
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
        holder.fireDate.text = "Fecha: " + formatter.format(localDateTime)
        holder.fireMissions.text = "Misiones: " + filteredFireDataList[position].missions.size
        holder.fireImage.setImageResource(filteredFireDataList[position].imageUrl)
        holder.fireDesc.text = "Descripción: " + filteredFireDataList[position].description

        //  ItemAnimation.animateFadeIn(holder.itemView, position)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, FireActivity::class.java)
            intent.putExtra("firesJetson", fireDataList)
            intent.putExtra("fire", filteredFireDataList[position])
            // intent.putExtra("userDesc", filteredFireDataList[position].locationPlace)
            context.startActivity(intent)
        }
        holder.fireImage.setOnClickListener(
                View.OnClickListener {
                    Toast.makeText(context, "User Name Clicked", Toast.LENGTH_SHORT).show()
                }
        )
    }

    override fun getItemCount(): Int {
        return filteredFireDataList.size
    }

    class RecyclerviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var fireImage: CircleImageView
        var fireName: TextView
        var fireLocation: TextView
        var fireDate: TextView
        var fireMissions: TextView
        var fireDesc: TextView

        init {
            fireImage = itemView.findViewById(R.id.fireImage)
            fireName = itemView.findViewById(R.id.fireName)
            fireLocation = itemView.findViewById(R.id.fireLocate)
            fireDate = itemView.findViewById(R.id.fireDate)
            fireMissions = itemView.findViewById(R.id.fireMissions)
            fireDesc = itemView.findViewById(R.id.fireDes)
        }
    }

    val filter: Filter
        get() =
                object : Filter() {
                    override fun performFiltering(charSequence: CharSequence): FilterResults {
                        val Key = charSequence.toString()
                        filteredFireDataList =
                                if (Key.isEmpty()) {
                                    fireDataList
                                } else {
                                    val lstFiltered: MutableList<Fire> = ArrayList<Fire>()
                                    for (row in fireDataList) {
                                        if (row.name
                                                        .lowercase(Locale.getDefault())
                                                        .contains(
                                                                Key.lowercase(Locale.getDefault())
                                                        )
                                        ) {
                                            lstFiltered.add(row)
                                        }
                                    }
                                    lstFiltered as ArrayList<Fire>
                                }
                        val filterResults = FilterResults()
                        filterResults.values = filteredFireDataList
                        return filterResults
                    }

                    override fun publishResults(
                            charSequence: CharSequence,
                            filterResults: FilterResults
                    ) {
                        filteredFireDataList = filterResults.values as ArrayList<Fire>
                        notifyDataSetChanged()
                    }
                }

    init {
        this.fireDataList = fireDataList
        filteredFireDataList = fireDataList
    }
}
