package com.kakovets.criminalintent_pro

import android.app.Dialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.os.Build
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import java.util.*

private const val ARG_TIME = "ARG_TIME"

class TimePickerFragment: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        @Suppress("DEPRECATION")
        val date: Date = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable(ARG_TIME, Date::class.java) ?: Date()
        } else {
            arguments?.getSerializable(ARG_TIME) as Date
        }
        val calendar = Calendar.getInstance()
        calendar.time = date
        val initialHours = calendar.get(Calendar.HOUR_OF_DAY)
        val initialMinutes = calendar.get(Calendar.MINUTE)

        val timeListener = OnTimeSetListener { _, hours, minutes ->
            val calendar2 = Calendar.getInstance()
            calendar2.time = date
            calendar2.set(Calendar.HOUR_OF_DAY, hours)
            calendar2.set(Calendar.MINUTE, minutes)
            val time: Date = calendar2.time

            setFragmentResult("request", bundleOf("bundle" to time))
        }

        return TimePickerDialog(
            context,
            timeListener,
            initialHours,
            initialMinutes,
            true
        )
    }

    companion object {
        fun newInstance(time: Date): TimePickerFragment {
            val args = Bundle().apply {
                putSerializable(ARG_TIME, time)
            }
            return TimePickerFragment().apply {
                arguments = args
            }
        }
    }
}















