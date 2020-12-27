package com.hieutm.homepi.ui.login;

import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hieutm.homepi.R;
import com.hieutm.homepi.auth.AuthenticationService;
import com.hieutm.homepi.data.Result;
import com.hieutm.homepi.data.model.LoggedInUser;

public class LoginViewModel extends ViewModel {
    private final MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private final AuthenticationService authService;

    public LoginViewModel(AuthenticationService authService) {
        this.authService = authService;
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void login(String username, String password) {
        authService.logIn(username, password, new Result.ResultHandler<LoggedInUser>() {
            @Override
            public void onSuccess(Result.Success<LoggedInUser> result) {
                loginResult.setValue(new LoginResult(new LoggedInUserView(result.getData().getDisplayName())));
            }

            @Override
            public void onError(Result.Error error) {
                loginResult.setValue(new LoginResult(R.string.login_activity_login_failed));
            }
        });
    }

    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.login_activity_invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.login_activity_invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return !username.trim().isEmpty();
        }
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }
}