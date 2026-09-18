package com.joybrata.weather.ui.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.joybrata.weather.R
import com.joybrata.weather.databinding.BottomSheetCityInputBinding
import com.joybrata.weather.util.showToast

/**
 * Created by Joybrata Paul on 10/08/2024
 *
 * Collects a city name and reports it back via [onCitySubmitted] instead of
 * casting to a concrete Activity (decoupled from MainActivity).
 **/
class CityInputBottomSheetFragment(
    private val onCitySubmitted: (String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCityInputBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCityInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.submitButton.setOnClickListener {
            val cityName = binding.cityInput.text?.toString()?.trim().orEmpty()
            if (cityName.isNotEmpty()) {
                onCitySubmitted(cityName)
                dismiss()
            } else {
                val error = getString(R.string.error_invalid_location)
                binding.cityInput.error = error
                requireContext().showToast(error)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
