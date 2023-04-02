package com.kakovets.criminalintent_pro

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.widget.DatePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import java.util.*

private const val ARG_DATE = "date"

class DatePickerFragment: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        @Suppress("DEPRECATION")
        val date: Date = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable(ARG_DATE, Date::class.java) ?: Date()
        } else {
            arguments?.getSerializable(ARG_DATE) as Date
        }

        val calendar = Calendar.getInstance()
        calendar.time = date
        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)

        val dateListener = DatePickerDialog.OnDateSetListener {
                _: DatePicker, year: Int, month: Int, day: Int ->

            val calendar2 = Calendar.getInstance()
            calendar2.time = date
            calendar2.set(Calendar.YEAR, year)
            calendar2.set(Calendar.MONTH, month)
            calendar2.set(Calendar.DAY_OF_MONTH, day)
            val resultDate: Date = calendar2.time

            // In the book authors use setTargetFragment(), but now it is deprecated,
            // so there is an implementation with relevant setFragmentResult()
            setFragmentResult("requestKey", bundleOf("bundleKey" to resultDate))
        }

        return DatePickerDialog(
            requireContext(),
            dateListener,
            initialYear,
            initialMonth,
            initialDay
        )
    }

    companion object {
        fun newInstance(date: Date): DatePickerFragment {
            val args = Bundle().apply {
                putSerializable(ARG_DATE, date)
            }
            return DatePickerFragment().apply {
                arguments = args
            }
        }
    }
}