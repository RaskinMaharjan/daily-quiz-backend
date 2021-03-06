package com.machpay.api.user.auth;

import com.machpay.api.common.enums.ContactType;
import com.machpay.api.security.CurrentUser;
import com.machpay.api.security.UserPrincipal;
import com.machpay.api.user.auth.dto.AccessTokenRequest;
import com.machpay.api.user.auth.dto.AccessTokenResponse;
import com.machpay.api.user.auth.dto.AuthResponse;
import com.machpay.api.user.auth.dto.CurrentUserResponse;
import com.machpay.api.user.auth.dto.SignInRequest;
import com.machpay.api.user.auth.dto.SignUpRequest;
import com.machpay.api.user.password.ForgotPasswordRequest;
import com.machpay.api.user.password.PasswordService;
import com.machpay.api.user.password.ResetPasswordRequest;
import com.machpay.api.user.verification.ContactVerificationService;
import com.machpay.api.user.verification.VerificationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private ContactVerificationService contactVerificationService;

    @Autowired
    private MobileAppGoogleAuthService mobileAppGoogleAuthService;

    @PostMapping("/signin")
    public AuthResponse signIn(@Valid @RequestBody SignInRequest signInRequest) {
        return authService.signIn(signInRequest);
    }

    @PostMapping("/users")
    public AuthResponse signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        return authService.signUp(signUpRequest);
    }

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('MEMBER','ADMIN')")
    public CurrentUserResponse getCurrentUser(@CurrentUser UserPrincipal userPrincipal) {
        return authService.getCurrentUser(userPrincipal);
    }

    @GetMapping("/google/{idToken}")
    public AuthResponse mobileAppSignInWithGoogle(@PathVariable("idToken") String idToken) {
        return mobileAppGoogleAuthService.signIn(idToken);
    }

    @GetMapping("/verify/{type}/{token}")
    public void verify(@CurrentUser UserPrincipal userPrincipal,
                       @PathVariable("type") ContactType contactType, @PathVariable("token") String token) {
        contactVerificationService.verifyToken(userPrincipal.getId(), token, contactType);
    }

    @GetMapping("/resend-verification/{type}")
    @PreAuthorize("hasRole('MEMBER')")
    public void resendVerification(@CurrentUser UserPrincipal userPrincipal,
                                   @PathVariable("type") ContactType contactType) {
        contactVerificationService.createDeviceVerification(userPrincipal.getId(), contactType);
    }

    @GetMapping("/signout")
    @PreAuthorize("hasRole('READ')")
    public void signOut(HttpServletRequest request) {
       authService.signOut(request);
    }

    @PostMapping("/forgot-password")
    public void forgotPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        passwordService.sendResetPasswordMail(forgotPasswordRequest.getEmail());
    }

    @PutMapping("/reset-password/{recoveryToken}")
    public VerificationResponse resetPassword(@PathVariable String recoveryToken,
                                              @RequestBody @Valid ResetPasswordRequest resetPasswordRequest) {
        return passwordService.resetPassword(recoveryToken, resetPasswordRequest.getNewPassword());
    }

    @PutMapping("/reset-password")
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    public VerificationResponse resetLoggedInUserPassword(@CurrentUser UserPrincipal userPrincipal,
                                                          @RequestBody @Valid ResetPasswordRequest resetPasswordRequest) {
        return passwordService.resetPassword(userPrincipal, resetPasswordRequest.getNewPassword(),
                resetPasswordRequest.getOldPassword());
    }

    @PostMapping("/token")
    public AccessTokenResponse refreshAccessToken(@Valid @RequestBody AccessTokenRequest accessTokenRequest) {
        return authService.refreshAccessToken(accessTokenRequest);
    }
}