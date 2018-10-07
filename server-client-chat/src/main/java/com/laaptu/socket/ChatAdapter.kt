package com.laaptu.socket

import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.message_received.view.*


class ChatAdapter(private val chatList: ArrayList<ChatMessage>) : RecyclerView.Adapter<ChatViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.message_received, parent, false)
        return ChatViewHolder(view)
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bindView(chatList[position])
    }

    fun addItem(chatMessage: ChatMessage) {
        chatList.add(chatMessage)
        notifyItemInserted(chatList.size - 1)
    }

}

class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bindView(chatMessage: ChatMessage) {
        itemView.msgContainer.background = getDrawableId(chatMessage.backgroundDrawable)
        itemView.imgProfile.background = getDrawableId(chatMessage.profileDrawable)
        itemView.txtChat.text = chatMessage.message
    }

    private fun getDrawableId(drawableId: Int): Drawable? = ContextCompat.getDrawable(itemView.context, drawableId)
}
