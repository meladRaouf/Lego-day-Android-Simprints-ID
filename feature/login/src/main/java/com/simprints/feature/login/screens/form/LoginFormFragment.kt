package com.simprints.feature.login.screens.form

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.feature.login.LoginError
import com.simprints.feature.login.LoginResult
import com.simprints.feature.login.R
import com.simprints.feature.login.databinding.FragmentLoginFormBinding
import com.simprints.feature.login.tools.play.GooglePlayServicesAvailabilityChecker
import com.simprints.feature.login.tools.play.GooglePlayServicesAvailabilityChecker.Companion.GOOGLE_PLAY_SERVICES_UPDATE_REQUEST_CODE
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.navigation.finishWithResult
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
internal class LoginFormFragment : Fragment(R.layout.fragment_login_form) {

    private val args by navArgs<LoginFormFragmentArgs>()
    private val binding by viewBinding(FragmentLoginFormBinding::bind)
    private val viewModel by viewModels<LoginFormViewModel>()

    @Inject
    lateinit var playServicesChecker: GooglePlayServicesAvailabilityChecker

    // TODO handle missing google play

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            finishWithError(LoginError.LoginNotCompleted)
        }

        initUi()
        observeUiState()
        viewModel.init()
        playServicesChecker.check(requireActivity()) { finishWithError(it) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == GOOGLE_PLAY_SERVICES_UPDATE_REQUEST_CODE) {
            // Check again to make sure that the user did the need actions.
            playServicesChecker.check(requireActivity()) { finishWithError(it) }
        } else super.onActivityResult(requestCode, resultCode, data)
    }

    private fun initUi() {
        binding.loginUserId.setText(args.loginParams.userId)
        binding.loginButtonScanQr.setOnClickListener {
            Simber.tag(LoggingConstants.CrashReportTag.LOGIN.name).i("Scan QR button clicked")
            // TODO open QR scanner screen for result
        }
        binding.loginButtonSignIn.setOnClickListener {
            Simber.tag(LoggingConstants.CrashReportTag.LOGIN.name).i("Login button clicked")
            binding.loginProgress.isVisible = true
            viewModel.signInClicked()
        }
    }

    private fun observeUiState() {
        viewModel.signInResult.observe(viewLifecycleOwner) { handleSignInResult(it) }
    }

    private fun handleSignInResult(result: Boolean?) {
        binding.loginProgress.isVisible = false
        // TODO map result
    }

    private fun finishWithError(error: LoginError) {
        findNavController().finishWithResult(this, LoginResult(false, error))
    }

}
