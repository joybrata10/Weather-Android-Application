package com.joybrata.weather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.joybrata.weather.databinding.DialogTemperatureUnitBinding

class TemperatureUnitDialogFragment(private val onUnitChanged: (String) -> Unit) : DialogFragment() {

    private var _binding: DialogTemperatureUnitBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogTemperatureUnitBinding.inflate(inflater, container, false)
        val view = binding.root

        // Set the current selection
        val sharedPreferences = requireActivity().getSharedPreferences("WeatherPreferences", AppCompatActivity.MODE_PRIVATE)
        val unit = sharedPreferences.getString("temperature_unit", "metric")
        if (unit == "metric") {
            binding.radioButtonCelsius.isChecked = true
        } else {
            binding.radioButtonFahrenheit.isChecked = true
        }

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.radioButtonCelsius.id -> {
                    updateTemperatureUnit("metric")
                    dismiss()
                }
                binding.radioButtonFahrenheit.id -> {
                    updateTemperatureUnit("imperial")
                    dismiss()
                }
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateTemperatureUnit(unit: String) {
        val sharedPreferences = requireActivity().getSharedPreferences("WeatherPreferences", AppCompatActivity.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("temperature_unit", unit)
            apply()
        }

        onUnitChanged(unit)
    }
}