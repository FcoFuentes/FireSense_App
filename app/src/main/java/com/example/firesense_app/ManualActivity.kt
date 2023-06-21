package com.example.firesense_app

import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.viewpager2.widget.ViewPager2
import java.io.File
import java.io.InputStream
import kotlinx.android.synthetic.main.pdf_activity.*

class ManualActivity : AppCompatActivity() {

    lateinit var menu_: Menu
    lateinit var pageViewPager: ViewPager2
    var parcelFileDescriptor: ParcelFileDescriptor? = null
    var pdfAdapter: PDFAdapter? = null

    private val FILE_NAME = "sample_cache.pdf"
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("onCreate", "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pdf_activity)
        supportActionBar!!.title = "Manual de uso"
        pageViewPager = findViewById(R.id.pageViewPager)
        with(pageViewPager) {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
        }
        val pageMarginPx = resources.getDimensionPixelOffset(R.dimen.pageMargin)
        val offsetPx = resources.getDimensionPixelOffset(R.dimen.offset)
        pageViewPager.setPageTransformer { page, position ->
            val viewPager = page.parent.parent as ViewPager2
            val offset = position * -(2 * offsetPx + pageMarginPx)
            if (viewPager.orientation == ViewPager2.ORIENTATION_HORIZONTAL) {
                if (ViewCompat.getLayoutDirection(viewPager) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                    page.translationX = -offset
                } else {
                    page.translationX = offset
                }
            } else {
                page.translationY = offset
            }
        }
        initPdfViewer(getFile())
    }

    fun initPdfViewer(pdfFile: File) {
        try {
            pageViewPager.visibility = View.VISIBLE
            baseProgressBar.visibility = View.GONE

            parcelFileDescriptor =
                    ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfAdapter = PDFAdapter(parcelFileDescriptor!!, this@ManualActivity)
            pageViewPager.adapter = pdfAdapter
        } catch (e: Exception) {
            pdfFile.delete()
        }
    }

    fun getFile(): File {
        val inputStream = assets.open("python_cheat_sheet.pdf")
        return File(filesDir.absolutePath + "python_cheat_sheet.pdf").apply {
            copyInputStreamToFile(inputStream)
        }
    }

    fun File.copyInputStreamToFile(inputStream: InputStream) {
        this.outputStream().use { fileOut -> inputStream.copyTo(fileOut) }
    }

    override fun onDestroy() {
        super.onDestroy()
        parcelFileDescriptor?.close()
        pdfAdapter?.clear()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_manual, menu)
        menu_ = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // util.showToast("onOptionsItemSelected")
        // Handle action bar item clicks here.
        val id = item.itemId
        if (id == R.id.cleanIcon) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
