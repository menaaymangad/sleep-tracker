package com.example.android.trackmysleepquality.sleeptracker




import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView


import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.convertDurationToFormatted
import com.example.android.trackmysleepquality.convertNumericQualityToString
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.databinding.ListItemSleepNightBinding

class SleepNightAdapter: ListAdapter<SleepNight, SleepNightAdapter.ViewHolder>(SleepNightDiffCallback()){

     //Override onBindViewHolder() and have it update the contents of the
     //// ViewHolder to reflect the item at the given position.
     override fun onBindViewHolder(holder: ViewHolder, position: Int) {
         val item = getItem(position)

         holder.bind(item)
     }



     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
         return ViewHolder.from(parent)
     }

     //  Inside the ViewHolder, use findViewById() to create properties for sleepLength,
     // quality, and qualityImage.
     class ViewHolder private constructor(private val binding: ListItemSleepNightBinding) : RecyclerView.ViewHolder(binding.root){
         val res: Resources = itemView.context.resources
         fun bind(
             item: SleepNight

         ) {

             binding.sleepLength.text =
                 convertDurationToFormatted(item.startTimeMilli, item.endTimeMilli, res)
             binding.qualityString.text = convertNumericQualityToString(item.sleepQuality, res)

             binding.qualityImage.setImageResource(
                 when (item.sleepQuality) {
                     0 -> R.drawable.ic_sleep_0
                     1 -> R.drawable.ic_sleep_1
                     2 -> R.drawable.ic_sleep_2
                     3 -> R.drawable.ic_sleep_3
                     4 -> R.drawable.ic_sleep_4
                     5 -> R.drawable.ic_sleep_5
                     else -> R.drawable.ic_sleep_active
                 }
             )
         }

         companion object {
             fun from(parent: ViewGroup): ViewHolder {
                 val layoutInflater = LayoutInflater.from(parent.context)
                 val binding=ListItemSleepNightBinding.inflate(layoutInflater,parent,false)

                 return ViewHolder(binding)
             }
         }
     }
     class SleepNightDiffCallback :
         DiffUtil.ItemCallback<SleepNight>() {
         override fun areItemsTheSame(oldItem: SleepNight, newItem: SleepNight): Boolean {
             return oldItem.nightId==newItem.nightId
         }

         override fun areContentsTheSame(oldItem: SleepNight, newItem: SleepNight): Boolean {
             return oldItem==newItem
         }
     }

 }