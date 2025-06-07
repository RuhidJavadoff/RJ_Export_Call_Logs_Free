package com.ruhidjavadoff.rjexportcalllogsfree

import android.content.Context // Context üçün import
import android.graphics.PorterDuff
// import android.graphics.PorterDuffColorFilter // Birbaşa istifadə olunmur, setColorFilter özü idarə edir
import android.provider.CallLog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CallLogAdapter(private var callLogList: List<CallLogItem>) :
    RecyclerView.Adapter<CallLogAdapter.CallLogViewHolder>() {

    inner class CallLogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val phoneNumberTextView: TextView = itemView.findViewById(R.id.textViewPhoneNumber)
        val timestampTextView: TextView = itemView.findViewById(R.id.textViewTimestamp)
        val callTypeTextView: TextView = itemView.findViewById(R.id.textViewCallTypeText)
        val callTypeIconImageView: ImageView = itemView.findViewById(R.id.imageViewCallTypeIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallLogViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_call_log, parent, false)
        return CallLogViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CallLogViewHolder, position: Int) {
        val currentItem = callLogList[position]
        val context = holder.itemView.context // Context-i buradan alırıq

        holder.phoneNumberTextView.text = currentItem.phoneNumber ?: context.getString(R.string.text_unknown_number) // String resursu istifadə edildi
        holder.timestampTextView.text = formatTimestamp(currentItem.timestamp)
        holder.callTypeTextView.text = formatCallTypePublic(context, currentItem.callType) // Context ötürülür

        var iconResId = R.drawable.ic_call_unknown_default
        var iconTintColor = ContextCompat.getColor(context, R.color.call_log_icon_default_tint)

        when (currentItem.callType) {
            CallLog.Calls.INCOMING_TYPE -> {
                iconResId = R.drawable.ic_call_received_arrow
                iconTintColor = ContextCompat.getColor(context, R.color.call_log_icon_green)
            }
            CallLog.Calls.OUTGOING_TYPE -> {
                iconResId = R.drawable.ic_call_made_arrow
                iconTintColor = ContextCompat.getColor(context, R.color.call_log_icon_green)
            }
            CallLog.Calls.MISSED_TYPE -> {
                iconResId = R.drawable.ic_call_missed_arrow
                iconTintColor = ContextCompat.getColor(context, R.color.call_log_icon_red)
            }
            CallLog.Calls.REJECTED_TYPE -> {
                iconResId = R.drawable.ic_call_rejected_arrow
                iconTintColor = ContextCompat.getColor(context, R.color.call_log_icon_red)
            }
            CallLog.Calls.BLOCKED_TYPE -> {
                iconResId = R.drawable.ic_call_blocked_arrow
                iconTintColor = ContextCompat.getColor(context, R.color.call_log_icon_red)
            }
            CallLog.Calls.VOICEMAIL_TYPE -> {
                iconResId = R.drawable.ic_voicemail_indicator
            }
        }

        holder.callTypeIconImageView.setImageResource(iconResId)
        holder.callTypeIconImageView.setColorFilter(iconTintColor, PorterDuff.Mode.SRC_IN)
    }

    override fun getItemCount(): Int {
        return callLogList.size
    }

    fun updateData(newList: List<CallLogItem>) {
        callLogList = newList
        notifyDataSetChanged()
    }

    private fun formatTimestamp(timestamp: Long): String {
        val date = Date(timestamp)
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) // Bu format lokallaşdırılmış ay adlarını istifadə edəcək
        return sdf.format(date)
    }

    // Zəng növü üçün mətn təsvirini qaytarır, indi Context qəbul edir
    fun formatCallTypePublic(context: Context, callType: Int): String {
        return when (callType) {
            CallLog.Calls.INCOMING_TYPE -> context.getString(R.string.call_type_answered)
            CallLog.Calls.MISSED_TYPE -> context.getString(R.string.call_type_missed)
            CallLog.Calls.OUTGOING_TYPE -> context.getString(R.string.call_type_outgoing)
            CallLog.Calls.VOICEMAIL_TYPE -> context.getString(R.string.call_type_voicemail)
            CallLog.Calls.REJECTED_TYPE -> context.getString(R.string.call_type_rejected)
            CallLog.Calls.BLOCKED_TYPE -> context.getString(R.string.call_type_blocked)
            else -> context.getString(R.string.call_type_unknown)
        }
    }
}