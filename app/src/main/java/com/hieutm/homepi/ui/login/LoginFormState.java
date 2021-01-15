package com.hieutm.homepi.ui.login;

import androidx.annotation.Nullable;

/**
 * Data validation state of the login form.
 */
class LoginFormState {
    private final boolean isLogin;
    @Nullable
    private final Integer displayNameError;
    @Nullable
    private final Integer usernameError;
    @Nullable
    private final Integer passwordError;
    @Nullable
    private final Integer passwordRetypeError;
    private final boolean isDataValid;

    LoginFormState(boolean isLogin, @Nullable Integer displayNameError, @Nullable Integer usernameError, @Nullable Integer passwordError, @Nullable Integer passwordRetypeError, boolean isDataValid) {
        this.isLogin = isLogin;
        this.displayNameError = displayNameError;
        this.usernameError = usernameError;
        this.passwordError = passwordError;
        this.passwordRetypeError = passwordRetypeError;
        this.isDataValid = isDataValid;
    }

    LoginFormState(boolean isLogin, boolean isDataValid) {
        this.isLogin = isLogin;
        this.displayNameError = null;
        this.usernameError = null;
        this.passwordError = null;
        this.passwordRetypeError = null;
        this.isDataValid = isDataValid;
    }

    public boolean isSignIn() {
        return isLogin;
    }

    @Nullable
    public Integer getDisplayNameError() {
        return displayNameError;
    }

    @Nullable
    public Integer getUsernameError() {
        return usernameError;
    }

    @Nullable
    public Integer getPasswordError() {
        return passwordError;
    }

    @Nullable
    public Integer getPasswordRetypeError() {
        return passwordRetypeError;
    }

    public boolean isDataValid() {
        return isDataValid;
    }
}