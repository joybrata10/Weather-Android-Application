package com.joybrata.weather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.joybrata.weather.databinding.BottomSheetCityInputBinding

class CityInputBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCityInputBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCityInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.submitButton.setOnClickListener {
            val cityName = binding.cityInput.text.toString()
            if (cityName.isNotEmpty()) {
                (activity as MainActivity).fetchWeatherData(cityName)
                dismiss()
            } else {
                binding.cityInput.error = "Please enter a Valid Location"
                Toast.makeText(context, "Please enter a Valid Location", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
