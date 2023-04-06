package com.kakovets.criminalintent_pro

import android.content.Context
import android.icu.text.DateFormat as IcuDateFormat
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "CrimeListFragment"

// TODO: Deny creating a new crime if the title is empty

class CrimeListFragment: Fragment() {

    interface Callbacks {
        fun onCrimeSelected(crimeID: UUID)
    }

    private var callbacks: Callbacks? = null
    private lateinit var crimeRecyclerView: RecyclerView
    private lateinit var buttonNewCrime: Button
    private var adapter: CrimeAdapter? = CrimeAdapter()
    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProvider (this)[CrimeListViewModel::class.java]
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        setHasOptionsMenu(true)
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        @Suppress("DEPRECATION")
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list, menu)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("0","0"))
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_crime -> {
                addNewCrime()
                true
            }
            R.id.drop_database -> {
                crimeListViewModel.dropDatabase()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)
        buttonNewCrime = view.findViewById(R.id.button_new_crime)
        crimeRecyclerView = view.findViewById(R.id.recyclerView)
        crimeRecyclerView.layoutManager = LinearLayoutManager(context)
        crimeRecyclerView.adapter = adapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buttonNewCrime.setOnClickListener {
            addNewCrime()
        }
        crimeListViewModel.crimeListLiveData.observe(
            viewLifecycleOwner,
            Observer { crimes ->
                crimes?.let {
                    Log.i(TAG, "Got crimes ${crimes.size}")
                    updateUI(crimes)
                }
            }
        )
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    private fun  updateUI(crimes: List<Crime>) {
        adapter = CrimeAdapter().apply {
            submitList(crimes)
        }
        crimeRecyclerView.adapter = adapter
        if (crimeRecyclerView.adapter?.itemCount == 0) {
            buttonNewCrime.visibility = View.VISIBLE
        } else {
            buttonNewCrime.visibility = View.GONE
        }
    }

    private fun addNewCrime() {
        val crime = Crime()
        crimeListViewModel.addCrime(crime)
        callbacks?.onCrimeSelected(crime.id)
    }

    private inner class CrimeHolder(view: View): ViewHolder(view), OnClickListener {

        private lateinit var crime: Crime
        private val titleTextView: TextView = itemView.findViewById(R.id.crime_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.crime_date)
        private val crimeSolvedImageView: ImageView = itemView.findViewById(R.id.imageView_crime_solved)
        private val listItemCrime: ConstraintLayout = itemView.findViewById(R.id.linearLayout)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(crime: Crime) {
            this.crime = crime
            titleTextView.text = this.crime.title
            dateTextView.text = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                val df = IcuDateFormat.getDateInstance(IcuDateFormat.LONG, Locale.getDefault())
                df.format(crime.date)
            } else {
                this.crime.date.toString()
            }
//            For some reason in the case of fast scrolling all imageViews become invisible.
//            Maybe because of reusing holders.
//            if (!crime.isSolved) crimeSolvedImageView.isVisible = false
            crimeSolvedImageView.visibility = if (crime.isSolved) {
                View.VISIBLE
            } else {
                View.GONE
            }
            val solved = if (crime.isSolved) {
                "is solved"
            } else {
                "is not solved"
            }
            val dateFormat = SimpleDateFormat("EEEE d MMMM yyyy, H:m", Locale.getDefault())
            val formattedDate = dateFormat.format(crime.date)
            listItemCrime.contentDescription = getString(R.string.crime_item_description, crime.title, formattedDate, solved)
        }

        override fun onClick(v: View?) {
            callbacks?.onCrimeSelected(crime.id)
        }
    }

    private inner class CrimeAdapter: ListAdapter<Crime, CrimeHolder>(CrimeCallback) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            val view = layoutInflater.inflate(R.layout.list_item_crime, parent, false)
            return CrimeHolder(view)
        }

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = getItem(position)
            holder.bind(crime)
        }
    }

    object CrimeCallback: DiffUtil.ItemCallback<Crime>() {

        override fun areItemsTheSame(oldItem: Crime, newItem: Crime) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Crime, newItem: Crime) = oldItem == newItem
    }

    companion object {
        fun newInstance(): CrimeListFragment {
            return CrimeListFragment()
        }
    }
}