package chat.wewe.android.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import chat.wewe.android.R;
import chat.wewe.android.fragment.server_config.LoginFragment;
import chat.wewe.android.fragment.server_config.RetryLoginFragment;
import chat.wewe.android.helper.BackStackHelper;
import chat.wewe.android.service.ConnectivityManager;
import chat.wewe.core.interactors.SessionInteractor;
import chat.wewe.persistence.realm.repositories.RealmSessionRepository;

/**
 * Activity for Login, Sign-up, and Retry connecting...
 */
public class LoginActivity extends AbstractFragmentActivity implements LoginContract.View {
    public static final String KEY_HOSTNAME = "hostname";

    private LoginContract.Presenter presenter;
    private SharedPreferences SipData;

    @Override
    protected int getLayoutContainerForFragment() {
        return R.id.content;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SipData = getSharedPreferences("SIP", Context.MODE_PRIVATE);
        String hostname = null;
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            hostname = intent.getStringExtra(KEY_HOSTNAME);
        }

        presenter = new LoginPresenter(
                hostname,
                new SessionInteractor(new RealmSessionRepository(hostname)),
                ConnectivityManager.getInstance(getApplicationContext())
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.bindView(this);
    }

    @Override
    protected void onDestroy() {
        presenter.release();
        super.onDestroy();
    }

    private void showFragment(Fragment fragment, String hostname) {
        setContentView(R.layout.simple_screen_login);
        injectHostnameArgTo(fragment, hostname);
        super.showFragment(fragment);
    }

    private void injectHostnameArgTo(Fragment fragment, String hostname) {
        Bundle args = fragment.getArguments();
        if (args == null) {
            args = new Bundle();
        }
        args.putString(LoginActivity.KEY_HOSTNAME, hostname);
        fragment.setArguments(args);
    }

    @Override
    protected void onBackPressedNotHandled() {
        moveTaskToBack(true);
    }

    @Override
    public void showLogin(String hostname) {
        showFragment(new LoginFragment(), hostname);
    }

    @Override
    public void showRetryLogin(String hostname) {
        showFragment(new RetryLoginFragment(), hostname);
    }

    @Override
    public void closeView() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

    }

    @Override
    protected boolean onBackPress() {
        if (BackStackHelper.FRAGMENT_TAG.equals("internal")) {
            super.onBackPress();
            BackStackHelper.FRAGMENT_TAG = "login";
        } else if (BackStackHelper.FRAGMENT_TAG.equals("login")) {
            LoginFragment loginFragment = (LoginFragment) getSupportFragmentManager()
                    .findFragmentById(getLayoutContainerForFragment());
            loginFragment.goBack();
        }
        return true;
    }
}
