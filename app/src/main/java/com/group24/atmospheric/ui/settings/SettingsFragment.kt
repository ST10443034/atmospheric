package com.group24.atmospheric.ui.settings

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.group24.atmospheric.notifications.NotificationHelper
import com.group24.atmospheric.util.ImageCodec
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.group24.atmospheric.R
import com.group24.atmospheric.databinding.FragmentSettingsBinding
import com.group24.atmospheric.ui.ViewModelFactory
import com.group24.atmospheric.ui.login.LoginActivity
import kotlinx.coroutines.launch

/**
 * Settings: DataStore-backed preferences (theme, units, language, notifications, logout).
 */
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = ViewModelFactory(requireContext())
        viewModel = ViewModelProvider(this, factory)[SettingsViewModel::class.java]

        binding.optThemeLight.setOnClickListener { viewModel.updateTheme("LIGHT") }
        binding.optThemeDark.setOnClickListener { viewModel.updateTheme("DARK") }
        binding.optThemeSystem.setOnClickListener { viewModel.updateTheme("SYSTEM") }

        binding.optUnitsMetric.setOnClickListener { viewModel.updateUnits("METRIC") }
        binding.optUnitsImperial.setOnClickListener { viewModel.updateUnits("IMPERIAL") }

        binding.rowLangEnglish.setOnClickListener { selectLanguage("ENGLISH", "en") }
        binding.rowLangZulu.setOnClickListener { selectLanguage("ISIZULU", "zu") }
        binding.rowLangAfrikaans.setOnClickListener { selectLanguage("AFRIKAANS", "af") }

        binding.switchNotifications.setOnCheckedChangeListener { switchView, isChecked ->
            if (!switchView.isPressed) return@setOnCheckedChangeListener
            if (isChecked && !NotificationHelper.canPost(requireContext())) {
                // Android 13+: ask for the runtime permission first; the result decides the switch.
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                viewModel.toggleNotifications(isChecked)
            }
        }

        setupProfile()

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            binding.root.context?.let {
                startActivity(Intent(it, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
            requireActivity().finish()
        }

        observeState()
    }

    /** Photo chosen by the user but not yet saved (Base64 JPEG), shown immediately in the avatar. */
    private var pendingPhotoBase64: String? = null

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                viewModel.toggleNotifications(true)
            } else {
                Log.w(TAG, "POST_NOTIFICATIONS denied")
                binding.switchNotifications.isChecked = false
                Toast.makeText(requireContext(), R.string.notif_permission_denied, Toast.LENGTH_LONG).show()
            }
        }

    private val pickPhoto =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri == null) return@registerForActivityResult
            val bitmap = ImageCodec.decodeSampled({ requireContext().contentResolver.openInputStream(uri) })
            if (bitmap == null) {
                Toast.makeText(requireContext(), R.string.profile_photo_failed, Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            pendingPhotoBase64 = ImageCodec.toBase64Jpeg(bitmap)
            binding.imgProfile.setImageBitmap(ImageCodec.fromBase64(pendingPhotoBase64!!))
        }

    /** Profile card: name + photo stored in Cloud Firestore (NoSQL), photo as a compressed Base64 blob. */
    private fun setupProfile() {
        binding.btnChangePhoto.setOnClickListener { pickPhoto.launch("image/*") }
        binding.btnSaveProfile.setOnClickListener {
            val photo = pendingPhotoBase64 ?: viewModel.profile.value?.photoBase64.orEmpty()
            viewModel.saveProfile(binding.etDisplayName.text.toString(), photo)
            Toast.makeText(requireContext(), R.string.profile_saved, Toast.LENGTH_SHORT).show()
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.profile.collect { profile ->
                if (profile == null) return@collect
                if (binding.etDisplayName.text.isNullOrEmpty()) binding.etDisplayName.setText(profile.displayName)
                if (pendingPhotoBase64 == null) {
                    ImageCodec.fromBase64(profile.photoBase64)?.let { binding.imgProfile.setImageBitmap(it) }
                }
            }
        }
    }

    private fun observeState() {
        var theme = "DARK"
        var units = "METRIC"
        var language = "ENGLISH"

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.themeMode.collect {
                theme = it
                setSegmentSelected(binding.optThemeLight, it == "LIGHT")
                setSegmentSelected(binding.optThemeDark, it == "DARK")
                setSegmentSelected(binding.optThemeSystem, it == "SYSTEM")
                updateDiagnostics(theme, units, language)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.units.collect {
                units = it
                setSegmentSelected(binding.optUnitsMetric, it == "METRIC")
                setSegmentSelected(binding.optUnitsImperial, it == "IMPERIAL")
                updateDiagnostics(theme, units, language)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.language.collect {
                language = it
                binding.tagLangEnglish.visibility = if (it == "ENGLISH") View.VISIBLE else View.GONE
                binding.tagLangZulu.visibility = if (it == "ISIZULU") View.VISIBLE else View.GONE
                binding.tagLangAfrikaans.visibility = if (it == "AFRIKAANS") View.VISIBLE else View.GONE
                setLangRowSelected(binding.rowLangEnglish, it == "ENGLISH")
                setLangRowSelected(binding.rowLangZulu, it == "ISIZULU")
                setLangRowSelected(binding.rowLangAfrikaans, it == "AFRIKAANS")
                updateDiagnostics(theme, units, language)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.notificationsEnabled.collect { binding.switchNotifications.isChecked = it }
        }
    }

    private fun setSegmentSelected(option: TextView, isSelected: Boolean) {
        option.setBackgroundResource(if (isSelected) R.drawable.bg_segment_selected else 0)
        option.setTextColor(requireContext().getColor(if (isSelected) R.color.ink else R.color.ink_muted))
    }

    private fun setLangRowSelected(row: View, isSelected: Boolean) {
        row.setBackgroundResource(if (isSelected) R.drawable.bg_card_selected_language else R.drawable.bg_card)
    }

    /** Switches the in-app locale only (per-app locales), not the device language. */
    private fun selectLanguage(languageKey: String, localeTag: String) {
        viewModel.updateLanguage(languageKey)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(localeTag))
    }

    private fun updateDiagnostics(theme: String, units: String, language: String) {
        binding.tvDiagnostics.text = "DataStore · theme=$theme units=$units lang=$language"
    }

    private companion object {
        const val TAG = "SettingsFragment"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
