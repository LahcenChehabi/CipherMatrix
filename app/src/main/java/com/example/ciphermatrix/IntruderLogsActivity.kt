package com.example.ciphermatrix

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class IntruderLogsActivity : AppCompatActivity() {
    private lateinit var imageFiles: MutableList<File>
    private lateinit var folder: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intruder_logs)

        val gridView = findViewById<GridView>(R.id.gv_intruder_logs)
        folder = File(filesDir, ".CipherLogs")
        refreshImageList()

        gridView.adapter = object : BaseAdapter() {
            override fun getCount() = imageFiles.size
            override fun getItem(position: Int) = imageFiles[position]
            override fun getItemId(position: Int) = position.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val file = imageFiles[position]

                val card = FrameLayout(this@IntruderLogsActivity)
                card.layoutParams = AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 450)
                card.setBackgroundColor(Color.parseColor("#121215"))

                val imageView = ImageView(this@IntruderLogsActivity)
                imageView.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                Glide.with(this@IntruderLogsActivity).load(file).into(imageView)
                card.addView(imageView)


                val timeBar = TextView(this@IntruderLogsActivity)
                val date = Date(file.lastModified())
                timeBar.text = "LOG: " + SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date)
                timeBar.setTextColor(Color.parseColor("#FF1744"))
                timeBar.textSize = 10f
                timeBar.gravity = Gravity.CENTER
                timeBar.setBackgroundColor(Color.parseColor("#CC000000"))
                timeBar.typeface = Typeface.MONOSPACE

                val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 70)
                params.gravity = Gravity.BOTTOM
                timeBar.layoutParams = params
                card.addView(timeBar)

                card.setOnLongClickListener {
                    if (file.delete()) {
                        Toast.makeText(this@IntruderLogsActivity, "EVIDENCE PURGED", Toast.LENGTH_SHORT).show()
                        refreshImageList()
                        notifyDataSetChanged()
                    }
                    true
                }
                return card
            }
        }
    }

    private fun refreshImageList() {
        imageFiles = if (folder.exists()) folder.listFiles()?.toMutableList() ?: mutableListOf() else mutableListOf()
    }
}