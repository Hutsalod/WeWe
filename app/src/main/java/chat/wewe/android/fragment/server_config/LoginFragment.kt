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
import android.support.v7.widget.SwitchCompat
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import chat.wewe.android.R
import chat.wewe.android.activity.Success
import chat.wewe.android.api.BaseApiService
import chat.wewe.android.api.MethodCallHelper
import chat.wewe.android.api.UtilsApi
import chat.wewe.android.layouthelper.oauth.OAuthProviderInfo
import chat.wewe.android.log.RCLog
import chat.wewe.android.service.PortSipService
import chat.wewe.android.widget.WaitingView
import chat.wewe.core.models.LoginServiceConfiguration
import chat.wewe.persistence.realm.repositories.RealmLoginServiceConfigurationRepository
import chat.wewe.persistence.realm.repositories.RealmPublicSettingRepository
import com.google.firebase.iid.FirebaseInstanceId
import com.jakewharton.rxbinding2.widget.RxTextView
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
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
    public  lateinit var switchServer:  SwitchCompat
    public  lateinit var btnEmail:  Button
    public var idmodel: String? = null
    private  var model: String? = null
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

        mApiService = UtilsApi.getAPIService() // meng-init yang ada di package apihelper

        SipData = activity!!.getSharedPreferences("SIP", Context.MODE_PRIVATE)
    }

    override fun onSetupView() {
        container = rootView.findViewById(R.id.container)

         btnEmail = rootView.findViewById(R.id.btn_login_with_email)
        val btnUserRegistration = rootView.findViewById<TextView>(R.id.btn_user_registration)
        txtUsername = rootView.findViewById(R.id.editor_username)
        txtPasswd = rootView.findViewById(R.id.editor_passwd)
        switchServer = rootView.findViewById(R.id.switchServer)
        waiting = rootView.findViewById(R.id.WaitingView)
       // setUpRxBinders()

        waitingView = rootView.findViewById(R.id.waiting)

      //  btnEmail.setOnClickListener { _ -> presenter.login(txtUsername.text.toString(), txtPasswd.text.toString()) }
        btnEmail.setOnClickListener { _ -> loginRequest(FirebaseInstanceId.getInstance().token) }
        btnUserRegistration.setOnClickListener { _ ->
            UserRegistrationDialogFragment.create(hostname, txtUsername.text.toString(), txtPasswd.text.toString())
                    .show(fragmentManager!!, "UserRegistrationDialogFragment")
        }
    }

     fun loginRequest(token: String?) {
         waiting.visibility = View.VISIBLE
        mApiService!!.loginRequest(txtUsername.text.toString(), txtPasswd.text.toString(), idmodel, "GOOGLE", model, token, token)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            try {

                                val jsonRESULTS = JSONObject(response.body()!!.string())
                                Log.d("REGISTEr", "$jsonRESULTS")
                                if (jsonRESULTS.getString("SUCCESS") == "true") {
                                    var  TOKEN_WE = jsonRESULTS.getString("TOKEN")
                                    getSettings(TOKEN_WE)
                                    val ed: SharedPreferences.Editor = SipData!!.edit()
                                    ed.putString("TOKEN_WE", TOKEN_WE)
                                    ed.commit()
                                 //   postCallvoip(TOKEN_WE)

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

    fun setUpRxBinders() {
        RxTextView.textChanges(txtUsername).subscribe { text ->
            if (!TextUtils.isEmpty(text) && textInputUsername.isErrorEnabled)
                textInputUsername.setErrorEnabled(false)
        }

        RxTextView.textChanges(txtPasswd).subscribe { text ->
            if (!TextUtils.isEmpty(text) && textInputPassword.isErrorEnabled)
                textInputPassword.setErrorEnabled(false)
        }

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
                                    var UF_SIP_SERVER = jsonRESULTS.getJSONObject("result").getString("UF_SIP_SERVER")
                                    var UF_SIP_LOGIN = jsonRESULTS.getJSONObject("result").getString("UF_SIP_LOGIN")
                                    var UF_SIP_PASSWORD = jsonRESULTS.getJSONObject("result").getString("UF_SIP_PASSWORD")
                                    var UF_SIP_NUMBER = jsonRESULTS.getJSONObject("result").getString("UF_SIP_NUMBER")
                                    var UF_ACCESS_OTH_DEVICE = jsonRESULTS.getJSONObject("result").getString("UF_ACCESS_OTH_DEVICE")
                                    var INNER_GROUP = jsonRESULTS.getJSONObject("result").getString("INNER_GROUP")
                                    var UF_ROCKET_SERVER = jsonRESULTS.getJSONObject("result").getString("UF_ROCKET_SERVER")
                                    presenter.login(UF_ROCKET_LOGIN, UF_ROCKET_PASSWORD)
                                    val ed = SipData!!.edit()
                                    ed.putString("UF_SIP_NUMBER", UF_SIP_NUMBER)
                                    ed.putString("UF_SIP_PASSWORD", UF_SIP_PASSWORD)
                                    ed.putString("INNER_GROUP", INNER_GROUP)
                                    ed.commit()
                                    if (switchServer.isChecked()) {
                                        ed.putString("UF_ROCKET_SERVER", UF_ROCKET_SERVER)
                                        ed.putString("UF_SIP_SERVER", UF_SIP_SERVER)
                                        ed.commit()
                                    } else {
                                        ed.putString("UF_ROCKET_SERVER", "chat.weltwelle.com")
                                        ed.putString("UF_SIP_SERVER", "sip.weltwelle.com")
                                        ed.commit()
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
                                            context!!.startService(onLineIntent)
                                        } else {
                                            context!!.startService(onLineIntent)
                                        }
                                    }
                                    waiting.visibility = View.GONE
                                    // getLoginChat();
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
