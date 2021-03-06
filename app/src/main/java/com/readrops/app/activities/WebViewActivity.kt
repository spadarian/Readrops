package com.readrops.app.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.readrops.app.R
import com.readrops.app.databinding.ActivityWebViewBinding
import com.readrops.app.utils.ReadropsKeys.ACTION_BAR_COLOR
import com.readrops.app.utils.ReadropsKeys.WEB_URL

class WebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_web_view)

        setSupportActionBar(binding.activityWebViewToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = ""
        val actionBarColor = intent.getIntExtra(ACTION_BAR_COLOR, ContextCompat.getColor(this, R.color.colorPrimary))
        supportActionBar?.setBackgroundDrawable(ColorDrawable(actionBarColor))
        setWebViewSettings()

        binding.activityWebViewSwipe.setOnRefreshListener { binding.webView.reload() }
        binding.activityWebViewProgress.indeterminateDrawable.setColorFilter(actionBarColor, PorterDuff.Mode.SRC_IN)
        binding.activityWebViewProgress.max = 100

        val url: String = intent.getStringExtra(WEB_URL)
        binding.webView.loadUrl(url)
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun setWebViewSettings() {
        val settings: WebSettings = binding.webView.settings

        settings.javaScriptEnabled = true
        settings.setSupportZoom(true)

        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                binding.webView.loadUrl(request?.url.toString())
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                binding.activityWebViewSwipe.isRefreshing = false
                binding.activityWebViewProgress.progress = 0
                binding.activityWebViewProgress.visibility = View.VISIBLE

                super.onPageStarted(view, url, favicon)
            }
        }

        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                setTitle(title)
                supportActionBar?.subtitle = Uri.parse(view?.url).host

                super.onReceivedTitle(view, title)
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                binding.activityWebViewProgress.progress = newProgress
                if (newProgress == 100)
                    binding.activityWebViewProgress.visibility = View.GONE

                super.onProgressChanged(view, newProgress)
            }
        }
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack())
            binding.webView.goBack()
        else
            super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                if (binding.webView.canGoBack())
                    binding.webView.goBack()
                else
                    finish()
                return true
            }
            R.id.web_view_refresh -> {
                binding.webView.reload()
            }
            R.id.web_view_share -> {
                shareLink()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun shareLink() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, binding.webView.url.toString())
        startActivity(Intent.createChooser(intent, getString(R.string.share_url)))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.webview_menu, menu)
        return true
    }
}
