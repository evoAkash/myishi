package com.example.ishi

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class UploadInfo {
    var name: String = ""
    var url: String = ""

    constructor() {}

    constructor(name: String, url: String) {
        this.name = name
        this.url = url
    }
}
class ImgViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
