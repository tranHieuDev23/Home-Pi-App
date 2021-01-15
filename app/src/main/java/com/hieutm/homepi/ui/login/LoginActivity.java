package com.hieutm.homepi.ui.login;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.hieutm.homepi.R;
import com.hieutm.homepi.models.LoggedInUser;
import com.hieutm.homepi.ui.AppViewModelFactory;
import com.hieutm.homepi.ui.home.HomeActivity;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private EditText displayNameEditText;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText passwordRetypeEditText;
    private Button submitButton;
    private TextView switchTextView;
    private ProgressBar loadingProgressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final ViewModelProvider.Factory viewModelFactory = AppViewModelFactory.getInstance(getApplicationContext());
        final ViewModelProvider modelProvider = new ViewModelProvider(this, viewModelFactory);
        loginViewModel = modelProvider.get(LoginViewModel.class);

        displayNameEditText = findViewById(R.id.display_name);
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        passwordRetypeEditText = findViewById(R.id.password_retype);
        submitButton = findViewById(R.id.submit);
        switchTextView = findViewById(R.id.sign_in_sign_up_toggle_text);
        loadingProgressBar = findViewById(R.id.loading);

        loginViewModel.getLoginFormState().observe(this, this::updateView);

        loginViewModel.getIsLoading().observe(this, isLoading -> loadingProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE));

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                String displayName = displayNameEditText.getText().toString();
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String passwordRetype = passwordRetypeEditText.getText().toString();
                loginViewModel.formDataChanged(displayName, username, password, passwordRetype);
            }
        };
        displayNameEditText.addTextChangedListener(afterTextChangedListener);
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordRetypeEditText.addTextChangedListener(afterTextChangedListener);

        submitButton.setOnClickListener(v -> {
            LoginFormState state = loginViewModel.getLoginFormState().getValue();
            String displayName = displayNameEditText.getText().toString();
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            //noinspection ConstantConditions
            if (state.isSignIn()) {
                signIn(username, password);
            } else {
                signUp(displayName, username, password);
            }
        });
    }

    private void updateView(LoginFormState state) {
        if (state == null) {
            return;
        }
        if (state.isSignIn()) {
            displayNameEditText.setVisibility(View.GONE);
            passwordRetypeEditText.setVisibility(View.GONE);
            submitButton.setText(R.string.login_activity_sign_in);
        } else {
            displayNameEditText.setVisibility(View.VISIBLE);
            passwordRetypeEditText.setVisibility(View.VISIBLE);
            submitButton.setText(R.string.login_activity_sign_up);
        }
        updateSwitchTextView(state.isSignIn());
        submitButton.setEnabled(state.isDataValid());
        if (state.getDisplayNameError() != null) {
            displayNameEditText.setError(getString(state.getDisplayNameError()));
        }
        if (state.getUsernameError() != null) {
            usernameEditText.setError(getString(state.getUsernameError()));
        }
        if (state.getPasswordError() != null) {
            passwordEditText.setError(getString(state.getPasswordError()));
        }
        if (state.getPasswordRetypeError() != null) {
            passwordRetypeEditText.setError(getString(state.getPasswordRetypeError()));
        }
    }

    private void updateSwitchTextView(boolean isLogin) {
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                loginViewModel.toggleSignInSignUp();
            }
        };
        SpannableString textViewText;
        if (isLogin) {
            String dontHaveAccount = getString(R.string.login_activity_dont_have_an_account);
            String signUp = getString(R.string.login_activity_sign_up);
            textViewText = new SpannableString(dontHaveAccount + " " + signUp);
            textViewText.setSpan(clickableSpan, dontHaveAccount.length() + 1, textViewText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            String haveAccount = getString(R.string.login_activity_already_have_an_account);
            String signIn = getString(R.string.login_activity_sign_in);
            textViewText = new SpannableString(haveAccount + " " + signIn);
            textViewText.setSpan(clickableSpan, haveAccount.length() + 1, textViewText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        switchTextView.setText(textViewText);
        switchTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @SuppressLint("CheckResult")
    private void signIn(String username, String password) {
        loginViewModel
                .signIn(username, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::updateUiWithUser, throwable -> showLoginFailed());
    }

    @SuppressLint("CheckResult")
    private void signUp(String displayName, String username, String password) {
        loginViewModel
                .signUp(displayName, username, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::updateUiWithUser, throwable -> showLoginFailed());
    }

    private void updateUiWithUser(LoggedInUser user) {
        String welcome = getString(R.string.login_activity_welcome) + ' ' + user.getDisplayName();
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void showLoginFailed() {
        Toast.makeText(getApplicationContext(), R.string.login_activity_submit_failed, Toast.LENGTH_SHORT).show();
    }
}