package chat.wewe.android.fragment.server_config

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.constraint.ConstraintLayout
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.support.v4.util.ArrayMap
import android.support.v7.widget.SwitchCompat
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import chat.wewe.persistence.encrypt.Cryptor
import chat.wewe.android.BuildConfig
import chat.wewe.android.R
import chat.wewe.android.activity.Success
import chat.wewe.android.api.BaseApiService
import chat.wewe.android.api.MethodCallHelper
import chat.wewe.android.api.UtilsApi
import chat.wewe.android.fragment.sidebar.dialog.EmailDialogFragment
import chat.wewe.android.layouthelper.oauth.OAuthProviderInfo
import chat.wewe.android.log.RCLog
import chat.wewe.android.service.PortSipService
import chat.wewe.android.widget.WaitingView
import chat.wewe.core.models.LoginServiceConfiguration
import chat.wewe.persistence.realm.repositories.RealmLoginServiceConfigurationRepository
import chat.wewe.persistence.realm.repositories.RealmPublicSettingRepository
import com.google.firebase.iid.FirebaseInstanceId
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.security.PrivateKey
import java.security.PublicKey
import java.util.*


/**
 * Login screen.
 */
class LoginFragment : AbstractServerConfigFragment(), LoginContract.View {

    private lateinit var presenter: LoginContract.Presenter
    private lateinit var container: ConstraintLayout
    private lateinit var waitingView: View
    private lateinit var txtUsername: TextView
    private  lateinit var waiting: WaitingView
    private lateinit var txtPasswd: TextView
    private lateinit var textInputUsername: TextInputLayout
    private lateinit var textInputPassword: TextInputLayout
    private  lateinit var switchServer:  SwitchCompat
    private  lateinit var btnEmail:  Button
    private var idmodel: String? = null
    private  var model: String? = null
    private  var version: String? = null
    private var mApiService: BaseApiService? = null
    var SipData: SharedPreferences? = null

    override fun getLayout(): Int {
        return R.layout.fragment_login
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presenter = LoginPresenter(
                RealmLoginServiceConfigurationRepository(hostname),
                RealmPublicSettingRepository(hostname),
                MethodCallHelper(context, hostname)
        )

        idmodel = Settings.Secure.getString(context!!.contentResolver,
                Settings.Secure.ANDROID_ID)
        model = Build.MODEL
        version = BuildConfig.VERSION_NAME

        mApiService = UtilsApi.getAPIService() // meng-init yang ada di package apihelper

        SipData = activity!!.getSharedPreferences("SIP", Context.MODE_PRIVATE)
    }

    override fun onSetupView() {
        container = rootView.findViewById(R.id.container)

         btnEmail = rootView.findViewById(R.id.btn_login_with_email)
        val btnUserRegistration = rootView.findViewById<TextView>(R.id.btn_user_registration)
        val help_pass = rootView.findViewById<TextView>(R.id.help_pass)
        txtUsername = rootView.findViewById(R.id.editor_username)
        txtPasswd = rootView.findViewById(R.id.editor_passwd)
        switchServer = rootView.findViewById(R.id.switchServer)
        waiting = rootView.findViewById(R.id.WaitingView)

        waitingView = rootView.findViewById(R.id.waiting)

        btnEmail.setOnClickListener { _ ->

            Log.d("QQTT", "vvv "+FirebaseInstanceId.getInstance().token)
            loginRequest(FirebaseInstanceId.getInstance().token) }
        btnUserRegistration.setOnClickListener { _ ->
            UserRegistrationDialogFragment.create(hostname, txtUsername.text.toString(), txtPasswd.text.toString())
                    .show(fragmentManager!!, "UserRegistrationDialogFragment")
        }

        help_pass.setOnClickListener { _ ->
            EmailDialogFragment.create()
                    .show(fragmentManager!!, "EmailDialogFragment")
        }
    }

     fun loginRequest(token: String?) {
         Log.d("DEBUG_WEWE", "Start")
         waiting.visibility = View.VISIBLE
        mApiService!!.loginRequest(txtUsername.text.toString(), txtPasswd.text.toString(), idmodel, "GOOGLE", model +" "+ version, token, token)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                        Log.d("DEBUG_WEWE", "Start")
                            try {

                                val jsonRESULTS = JSONObject(response.body()!!.string())
                                if (jsonRESULTS.getString("SUCCESS") == "true") {
                                    var  TOKEN_WE = jsonRESULTS.getString("TOKEN")
                                    getSettings(TOKEN_WE)
                                    pushToken(TOKEN_WE)
                                    SipData!!.edit().putString("TOKEN_WE", TOKEN_WE).commit()

                                } else {
                                    waiting.visibility = View.GONE
                                    when (jsonRESULTS.getInt("ERROR_CODE")) {
                                        1 -> Toast.makeText(activity, "Не верные данные", Toast.LENGTH_SHORT).show()
                                        2 -> Toast.makeText(activity, "Пользователь не найден", Toast.LENGTH_SHORT).show()
                                        3 -> Toast.makeText(activity, "Пользователь неактивен", Toast.LENGTH_SHORT).show()
                                        4 -> Toast.makeText(activity, "Неверный пароль", Toast.LENGTH_SHORT).show()
                                        5 -> Toast.makeText(activity, "Не найден пользователь по токену", Toast.LENGTH_SHORT).show()
                                        7 -> Toast.makeText(activity, "Пользователь не найден", Toast.LENGTH_SHORT).show()
                                    }
                                }

                            } catch (e: JSONException) {
                                e.printStackTrace()
                                waiting.visibility = View.GONE
                            } catch (e: IOException) {
                                e.printStackTrace()
                                waiting.visibility = View.GONE
                            }

                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Toast.makeText(activity, "Пожалуйста, подключитесь к Интернету", Toast.LENGTH_SHORT).show()
                        Log.d("DEBUG_WEWE", "onFailure: ERROR > $t")
                        waiting.visibility = View.GONE
                    }
                })
    }



    override fun showLoader() {
        container.visibility = View.GONE
        waitingView.visibility = View.VISIBLE
    }

    override fun showErrorInUsernameEditText() {
     //   textInputUsername.setErrorEnabled(true);
   //     textInputUsername.setError("Enter a Username")
    }

    override fun showErrorInPasswordEditText() {
      // textInputPassword.setErrorEnabled(true);
     //   textInputPassword.setError("Enter a Password")
    }

    override fun hideLoader() {
        waitingView.visibility = View.GONE
        container.visibility = View.VISIBLE
    }

    override fun showError(message: String) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun showLoginServices(loginServiceList: List<LoginServiceConfiguration>) {
        val viewMap = HashMap<String, View>()
        val supportedMap = HashMap<String, Boolean>()
        for (info in OAuthProviderInfo.LIST) {
            viewMap.put(info.serviceName, rootView.findViewById(info.buttonId))
            supportedMap.put(info.serviceName, false)
        }

        for (authProvider in loginServiceList) {
            for (info in OAuthProviderInfo.LIST) {
                if (supportedMap[info.serviceName] == false && info.serviceName == authProvider.service) {
                    supportedMap.put(info.serviceName, true)
                    viewMap[info.serviceName]?.setOnClickListener { _ ->
                        var fragment: Fragment? = null
                        try {
                            fragment = info.fragmentClass.newInstance()
                        } catch (exception: Exception) {
                            RCLog.w(exception, "failed to build new Fragment")
                        }

                        fragment?.let {
                            val args = Bundle()
                            args.putString("hostname", hostname)
                            fragment.arguments = args
                            showFragmentWithBackStack(fragment)
                        }
                    }
                    viewMap[info.serviceName]?.visibility = View.VISIBLE
                }
            }
        }

        for (info in OAuthProviderInfo.LIST) {
            if (supportedMap[info.serviceName] == false) {
                viewMap[info.serviceName]?.visibility = View.GONE
            }
        }
    }

    private fun getSettings(token: String) {
        mApiService!!.getSettings(" KEY:$token")
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            try {
                                val jsonRESULTS = JSONObject(response.body()!!.string())
                                if (jsonRESULTS.getString("status") == "200") {

                                    val UF_ROCKET_LOGIN = jsonRESULTS.getJSONObject("result").getString("UF_ROCKET_LOGIN")
                                    val UF_ROCKET_PASSWORD = jsonRESULTS.getJSONObject("result").getString("UF_ROCKET_PASSWORD")
                                    val UF_SIP_SERVER = jsonRESULTS.getJSONObject("result").getString("UF_SIP_SERVER")
                                    val UF_SIP_PASSWORD = jsonRESULTS.getJSONObject("result").getString("UF_SIP_PASSWORD")
                                    val UF_SIP_NUMBER = jsonRESULTS.getJSONObject("result").getString("UF_SIP_NUMBER")
                                    val INNER_GROUP = jsonRESULTS.getJSONObject("result").getString("INNER_GROUP")
                                    val UF_ROCKET_SERVER = jsonRESULTS.getJSONObject("result").getString("UF_ROCKET_SERVER")
                                    presenter.login(UF_ROCKET_LOGIN, UF_ROCKET_PASSWORD)

                                    val kp = Cryptor.getKeyPair()
                                    val publicKey: PublicKey = kp.getPublic()
                                    val publicKeyBytes = publicKey.encoded
                                    val publicKeyBytesBase64 = String(Base64.encode(publicKeyBytes, Base64.DEFAULT))

                                    val privateKey: PrivateKey = kp.getPrivate()
                                    val privateKeyBytes = privateKey.encoded
                                    val privateKeyBytesBase64 = String(Base64.encode(privateKeyBytes, Base64.DEFAULT))
                                    SipData!!.edit().putString("UF_SIP_NUMBER", UF_SIP_NUMBER)
                                            .putString("UF_SIP_PASSWORD", UF_SIP_PASSWORD)
                                            .putString("INNER_GROUP", INNER_GROUP)
                                            .putString("CHAT_PUBLIC", publicKeyBytesBase64)
                                            .putString("CHAT_PRIVAT", privateKeyBytesBase64)
                                    .commit()

                                    setPrivatKey(token,publicKeyBytesBase64)

                                    if (switchServer.isChecked()) {
                                        SipData!!.edit().putString("UF_ROCKET_SERVER", UF_ROCKET_SERVER)
                                                .putString("UF_SIP_SERVER", UF_SIP_SERVER)
                                                .commit()
                                    } else {
                                        SipData!!.edit().putString("UF_ROCKET_SERVER", "chat.weltwelle.com")
                                                .putString("UF_SIP_SERVER", "sip.weltwelle.com")
                                                .commit()
                                    }


                                    if (jsonRESULTS.getJSONObject("result").getString("INNER_GROUP").equals("true")) {
                                        activity!!.getSharedPreferences("Sub", Context.MODE_PRIVATE)!!.edit().putBoolean("Sub", true).commit()
                                        activity!!.getSharedPreferences("Sub", Context.MODE_PRIVATE)!!.edit().putBoolean("SubApi", true).commit()
                                    }else{
                                        startActivity(Intent(activity!!.applicationContext, Success::class.java))
                                        activity!!.getSharedPreferences("Sub", Context.MODE_PRIVATE)!!.edit().putBoolean("SubApi", false).commit()
                                    }

                                    if (SipData!!.getString("UF_SIP_NUMBER", null) != null) {
                                        SaveUserInfo()
                                        val onLineIntent = Intent(context, PortSipService::class.java)
                                        onLineIntent.putExtra(PortSipService.EXTRA_PUSHTOKEN, FirebaseInstanceId.getInstance().token)
                                        onLineIntent.action = PortSipService.ACTION_SIP_REGIEST
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            activity!!.startForegroundService(onLineIntent)
                                        } else {
                                            activity!!.startService(onLineIntent)
                                        }
                                    }
                                    waiting.visibility = View.GONE
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                                waiting.visibility = View.GONE
                            } catch (e: IOException) {
                                e.printStackTrace()
                                waiting.visibility = View.GONE
                            }
                        } else {
                            waiting.visibility = View.GONE
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e("debug", "onFailure: ERROR > $t")
                        waiting.visibility = View.GONE
                    }
                })
    }

    override fun showTwoStepAuth() {
        showFragmentWithBackStack(TwoStepAuthFragment.create(
                hostname, txtUsername.text.toString(), txtPasswd.text.toString()
        ))
    }

    override fun onResume() {
        super.onResume()
        presenter.bindView(this)
    }

    override fun onPause() {
        presenter.release()
        super.onPause()
    }
    
    override fun goBack() {
        presenter.goBack(context)
    }

    fun pushToken(token: String) {
        val jsonParams: MutableMap<String, Any> = ArrayMap()
        Log.d("RDEBUG_WEWE", "Start" +token)
        jsonParams["UF_PUSH_TOKEN"] = ""+ FirebaseInstanceId.getInstance().token
        mApiService!!.postPush(" KEY:$token",jsonParams)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                        Log.d("RDEBUG_WEWE", "Start")
                        Log.d("RDEBUG_WEWE", "Start")
                        // if (response.isSuccessful) {
                        try {

                            val jsonRESULTS = JSONObject(response.body()!!.string())
                            Log.d("RDEBUG_WEWE", "$jsonRESULTS")



                        } catch (e: JSONException) {
                            e.printStackTrace()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e("debug", "onFailure: ERROR > $t")
                        Log.d("DEBUG_WEWE", "Start $t")
                    }
                })
    }

    fun setPrivatKey(token: String, prvatKey: String) {
        val jsonParams: MutableMap<String, Any> = ArrayMap()
        jsonParams["UF_PUBLIC_KEY"] = ""+ prvatKey
        mApiService!!.setPublicKey(" KEY:$token",jsonParams)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        try {
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    }
                })
    }


    fun SaveUserInfo() {
        val editor = PreferenceManager.getDefaultSharedPreferences(activity).edit()
        editor.putString(PortSipService.USER_NAME,  SipData!!.getString("UF_SIP_NUMBER", null))
        editor.putString(PortSipService.USER_PWD, SipData!!.getString("UF_SIP_PASSWORD", null))
        editor.putString(PortSipService.SVR_HOST, SipData!!.getString("UF_SIP_SERVER", "sip.weltwelle.com"))
        editor.putString(PortSipService.SVR_PORT, "5061")
        editor.putString(PortSipService.USER_DISPALYNAME, null)
        editor.putString(PortSipService.USER_DOMAIN, null)
        editor.putString(PortSipService.USER_AUTHNAME, null)
        editor.putString(PortSipService.STUN_HOST, null)
        editor.putString(PortSipService.STUN_PORT, "3478")
        editor.commit()
    }

}
