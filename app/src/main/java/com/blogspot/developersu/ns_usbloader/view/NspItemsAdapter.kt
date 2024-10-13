package com.blogspot.developersu.ns_usbloader.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.blogspot.developersu.ns_usbloader.R
import com.blogspot.developersu.ns_usbloader.view.NspItemsAdapter.NspViewHolder
import java.util.Collections
import java.util.Locale

class NspItemsAdapter(private val mDataset: ArrayList<NSPElement>) :
    RecyclerView.Adapter<NspViewHolder>() {
    class NspViewHolder internal constructor(itemTV: View) : RecyclerView.ViewHolder(itemTV) {
        private var nspCTV: CheckedTextView = itemTV.findViewById(R.id.checkedCTV)
        private var sizeTV: TextView =
            itemTV.findViewById(R.id.sizeTV)
        private var statusTV: TextView =
            itemTV.findViewById(R.id.statusTV)
        private var nspElement: NSPElement? = null

        init {
            nspCTV.setOnClickListener { e: View? ->
                nspCTV.toggle()
                nspElement!!.isSelected = nspCTV.isChecked
            }
        }

        private fun getCuteSize(byteSize: Long): String {
            return if (byteSize / 1024.0 / 1024.0 / 1024.0 > 1) String.format(
                Locale.getDefault(),
                "%.2f",
                byteSize / 1024.0 / 1024.0 / 1024.0
            ) + " GB"
            else if (byteSize / 1024.0 / 1024.0 > 1) String.format(
                Locale.getDefault(),
                "%.2f",
                byteSize / 1024.0 / 1024.0
            ) + " MB"
            else String.format(
                Locale.getDefault(),
                "%.2f",
                byteSize / 1024.0
            ) + " kB"
        }

        var data: NSPElement?
            get() = nspElement
            set(element) {
                nspElement = element
                nspCTV.text = element!!.filename
                sizeTV.text = getCuteSize(element.size)
                nspCTV.isChecked = element.isSelected
                statusTV.text = element.status
            }
    }

    fun move(fromPosition: Int, toPostition: Int) {
        if (fromPosition < toPostition) {
            var i = fromPosition
            while (i < toPostition) Collections.swap(mDataset, i, ++i)
        } else {
            var i = fromPosition
            while (i > toPostition) Collections.swap(mDataset, i, --i)
        }
        notifyItemMoved(fromPosition, toPostition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NspViewHolder {
        val tv = LayoutInflater.from(parent.context)
            .inflate(R.layout.nsp_item, parent, false)
        return NspViewHolder(tv)
    }

    override fun onBindViewHolder(holder: NspViewHolder, position: Int) {
        holder.data = mDataset[position]
    }

    override fun getItemCount(): Int {
        return mDataset.size
    }
}