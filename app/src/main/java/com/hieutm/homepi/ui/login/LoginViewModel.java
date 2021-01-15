package com.hieutm.homepi.ui.login;

import android.annotation.SuppressLint;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hieutm.homepi.R;
import com.hieutm.homepi.auth.AuthenticationService;
import com.hieutm.homepi.models.LoggedInUser;

import java.util.regex.Pattern;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.annotations.NonNull;

public class LoginViewModel extends ViewModel {
    private static final String USERNAME_REGEX_PATTERN = "^[a-zA-Z0-9]{6,}$";
    private static final String PASSWORD_REGEX_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$";

    private final MutableLiveData<LoginFormState> loginFormState;
    private final MutableLiveData<Boolean> isLoading;
    private final AuthenticationService authService;

    public LoginViewModel(AuthenticationService authService) {
        this.loginFormState = new MutableLiveData<>(new LoginFormState(true, false));
        this.isLoading = new MutableLiveData<>(false);
        this.authService = authService;
    }

    public LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    @SuppressLint("CheckResult")
    public Single<LoggedInUser> signIn(String username, String password) {
        return new Single<LoggedInUser>() {
            @Override
            protected void subscribeActual(@NonNull SingleObserver<? super LoggedInUser> observer) {
                isLoading.postValue(true);
                authService
                        .signIn(username, password)
                        .subscribe(observer::onSuccess, observer::onError);
            }
        }.doFinally(() -> isLoading.postValue(false));
    }

    @SuppressLint("CheckResult")
    public Single<LoggedInUser> signUp(String displayName, String username, String password) {
        return new Single<LoggedInUser>() {
            @Override
            protected void subscribeActual(@NonNull SingleObserver<? super LoggedInUser> observer) {
                isLoading.postValue(true);
                authService
                        .signUp(displayName, username, password)
                        .subscribe(observer::onSuccess, observer::onError);
            }
        }.doFinally(() -> isLoading.postValue(false));
    }

    public void toggleSignInSignUp() {
        LoginFormState state = loginFormState.getValue();
        //noinspection ConstantConditions
        loginFormState.postValue(new LoginFormState(!state.isSignIn(), false));
    }

    public void formDataChanged(String displayName, String username, String password, String passwordRetype) {
        @SuppressWarnings("ConstantConditions") boolean isLogin = loginFormState.getValue().isSignIn();
        Integer displayNameError = !isDisplayNameValid(displayName)
                ? R.string.login_activity_invalid_display_name
                : null;
        Integer usernameError = !isUsernameValid(username)
                ? R.string.login_activity_invalid_username
                : null;
        Integer passwordError = !isPasswordValid(password)
                ? R.string.login_activity_invalid_password
                : null;
        Integer passwordRetypeError = !password.equals(passwordRetype)
                ? R.string.login_activity_invalid_password_retype
                : null;
        boolean isValid = (isLogin || (displayNameError == null && passwordRetypeError == null)) && usernameError == null && passwordError == null;
        loginFormState.setValue(new LoginFormState(isLogin, displayNameError, usernameError, passwordError, passwordRetypeError, isValid));
    }

    private boolean isDisplayNameValid(String displayName) {
        if (displayName == null) {
            return false;
        }
        displayName = displayName.trim();
        return !displayName.isEmpty();
    }

    private boolean isUsernameValid(String username) {
        if (username == null) {
            return false;
        }
        return Pattern.matches(USERNAME_REGEX_PATTERN, username);
    }

    private boolean isPasswordValid(String password) {
        if (password == null) {
            return false;
        }
        return Pattern.matches(PASSWORD_REGEX_PATTERN, password);
    }
}