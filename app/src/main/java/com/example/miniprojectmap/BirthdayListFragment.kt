package com.example.miniprojectmap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.miniprojectmap.databinding.FragmentBirthdayListBinding

// Data class untuk setiap orang
data class Person(val name: String, val birthDate: String)

// Adapter untuk RecyclerView
class BirthdayListAdapter(private val people: List<Person>) : RecyclerView.Adapter<BirthdayListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.personNameText)
        val dateTextView: TextView = view.findViewById(R.id.birthDateText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_birthday, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val person = people[position]
        holder.nameTextView.text = person.name
        holder.dateTextView.text = person.birthDate
    }

    override fun getItemCount() = people.size
}

// Class Fragment Utama
class BirthdayListFragment : Fragment() {

    private var _binding: FragmentBirthdayListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBirthdayListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Buat data dummy
        val birthdayList = listOf(
            Person("Ferry Irwandi", "24 Juni 2026"),
            Person("Sukimin", "30 Juni 2025"),
            Person("Devin Nathanael", "9 Mei 2026"),
            Person("John Doe", "2 September 2025")
        )

        // Setup RecyclerView
        binding.birthdayRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.birthdayRecyclerView.adapter = BirthdayListAdapter(birthdayList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}