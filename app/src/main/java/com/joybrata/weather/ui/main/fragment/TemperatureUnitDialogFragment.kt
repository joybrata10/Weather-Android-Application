package com.joybrata.weather.ui.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.joybrata.weather.databinding.DialogTemperatureUnitBinding
import com.joybrata.weather.util.AppPreferences
import com.joybrata.weather.util.Constants
import kotlinx.coroutines.launch

/**
 * Created by Joybrata Paul on 10/08/2024
 *
 * Lets the user pick Celsius / Fahrenheit. Persists the choice through
 * [AppPreferences] (DataStore) and notifies the host via [onUnitChanged].
 **/
class TemperatureUnitDialogFragment(
    private val onUnitChanged: (String) -> Unit
) : DialogFragment() {

    private var _binding: DialogTemperatureUnitBinding? = null
    private val binding get() = _binding!!

    private val appPreferences by lazy { AppPreferences(requireContext().applicationContext) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogTemperatureUnitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            val unit = appPreferences.getTemperatureUnit()
            if (unit == Constants.UNIT_METRIC) {
                binding.radioButtonCelsius.isChecked = true
            } else {
                binding.radioButtonFahrenheit.isChecked = true
            }
        }

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val unit = when (checkedId) {
                binding.radioButtonCelsius.id -> Constants.UNIT_METRIC
                binding.radioButtonFahrenheit.id -> Constants.UNIT_IMPERIAL
                else -> return@setOnCheckedChangeListener
            }
            persistUnit(unit)
        }
    }

    private fun persistUnit(unit: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            appPreferences.setTemperatureUnit(unit)
            onUnitChanged(unit)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
