package com.kakovets.criminalintent_pro

import android.Manifest.permission.READ_CONTACTS
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import java.util.*

private const val TAG = "CrimeFragment"
private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val DATE_FORMAT = "EEE, MMM, dd"

class CrimeFragment: Fragment(){

    private lateinit var crime: Crime
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var chooseSuspectButton: Button
    private lateinit var sendReportButton: Button
    private lateinit var callButton: Button
    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProvider(this)[CrimeDetailViewModel::class.java]
    }

    private val launcherSend = registerForActivityResult(ActivityResultContracts.PickContact()) { result ->
        if (result != null) {
            val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
            val cursor = requireActivity().contentResolver.query(result, queryFields, null, null, null)
            cursor?.use {
                if (it.count != 0) {
                    it.moveToFirst()
                    val suspect = it.getString(0)
                    crime.suspect = suspect
                    crimeDetailViewModel.saveCrime(crime)
                    chooseSuspectButton.text = suspect
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeID: UUID = UUID.fromString(arguments?.getString(ARG_CRIME_ID))
        Log.d(TAG, crimeID.toString())
        crimeDetailViewModel.loadCrime(crimeID)

        // In the book authors use setTargetFragment(), but now it is deprecated,
        // so there is an implementation with relevant setFragmentResult()
        setFragmentResultListener("requestKey") { _, bundle ->
            val receivedDate = bundle.getSerializable("bundleKey") as Date
            crime.date = receivedDate
            updateUI()
        }
        setFragmentResultListener("request") { _, bundle ->
            val receivedDate = bundle.getSerializable("bundle") as Date
            crime.date.time = receivedDate.time
            updateUI()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)
        titleField = view.findViewById(R.id.editText_crime_title)
        dateButton = view.findViewById(R.id.button_crime)
        timeButton = view.findViewById(R.id.button_time)
        solvedCheckBox = view.findViewById(R.id.checkBox_crime)
        chooseSuspectButton = view.findViewById(R.id.button_choose_suspect)
        sendReportButton = view.findViewById(R.id.button_send_report)
        callButton = view.findViewById(R.id.button_call)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeDetailViewModel.crimeLivedata.observe(
            viewLifecycleOwner,
            Observer { crime ->
                crime?.let {
                    this.crime = crime
                    updateUI()
                }
            }
        )
    }

    override fun onStart() {
        super.onStart()

        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(sequence: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) {
                crime.title = sequence.toString()
            }

            override fun afterTextChanged(sequence: Editable?) {

            }
        }
        titleField.addTextChangedListener(titleWatcher)

        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }

        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(crime.date).apply {
                show(this@CrimeFragment.parentFragmentManager, DIALOG_DATE)
            }
        }

        timeButton.setOnClickListener {
            TimePickerFragment.newInstance(crime.date).apply {
                show(this@CrimeFragment.parentFragmentManager, DIALOG_TIME)
            }
        }

        chooseSuspectButton.setOnClickListener {
            launcherSend.launch()
        }

        sendReportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))
            }.also { intent ->
                val chooserIntent = Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }

        callButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireActivity(), READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(READ_CONTACTS), 0)
            } else {
                val number = getNumber()
                val numUri: Uri = Uri.parse("tel:$number")
                val intent = Intent(Intent.ACTION_DIAL, numUri)
                startActivity(intent)
            }
        }
    }

    private fun getNumber(): String {
        val phoneContract = ContactsContract.CommonDataKinds.Phone.NUMBER
        val queryFields = arrayOf(phoneContract)
        val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY} = ?"
        val selectionArgs = arrayOf(crime.suspect)
        val cursor = requireActivity().contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            queryFields,
            selection,
            selectionArgs,
            null)
        var number = ""
        cursor?.use {
            if (it.count != 0) {
                it.moveToFirst()
                val numIndex = it.getColumnIndex(phoneContract)
                number = it.getString(numIndex)
            }
        }
        return number
    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    private fun updateUI() {
        titleField.setText(crime.title)
        dateButton.text = crime.date.toString()
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }
        if (crime.suspect.isNotBlank()) {
            chooseSuspectButton.text = crime.suspect
        }
    }

    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }
        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
        val suspect = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }
        return getString(R.string.crime_report, crime.title, dateString, solvedString, suspect)
    }

    companion object {
        fun newInstance(id: UUID): CrimeFragment {
            val args = Bundle().apply {
                putString(ARG_CRIME_ID, id.toString())
            }
            return CrimeFragment().apply {
                arguments = args
            }
        }
    }
}