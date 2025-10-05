package com.example.miniprojectmap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.miniprojectmap.databinding.FragmentCalendarBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


data class Event(val date: Long, val name: String)

// Adapter untuk RecyclerView
class EventAdapter(private var events: List<Event>) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val eventNameText: TextView = view.findViewById(R.id.eventNameText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.eventNameText.text = events[position].name
    }

    override fun getItemCount() = events.size

    // Fungsi untuk memperbarui data di adapter
    fun updateEvents(newEvents: List<Event>) {
        events = newEvents
        notifyDataSetChanged()
    }
}

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    // Buat data acara dummy
    private val allEvents = mutableListOf<Event>()
    private lateinit var eventAdapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        createDummyEvents()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView
        eventAdapter = EventAdapter(allEvents)
        binding.eventsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.eventsRecyclerView.adapter = eventAdapter
        updateEventListForToday() // Tampilkan acara hari ini saat pertama kali dibuka

        // Listener untuk kalender
        binding.calendarViewNative.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val selectedDateMillis = selectedCalendar.timeInMillis

            // Filter acara berdasarkan tanggal yang dipilih
            val filteredEvents = allEvents.filter { it.date == selectedDateMillis }
            eventAdapter.updateEvents(filteredEvents)

            // Update teks judul
            val sdf = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
            binding.selectedDateText.text = "Acara pada ${sdf.format(selectedCalendar.time)}"
        }
    }

    private fun createDummyEvents() {
        val today = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
        val tomorrow = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
        val nextWeek = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 7) }

        allEvents.add(Event(today.timeInMillis, "Rapat Panitia Inti"))
        allEvents.add(Event(today.timeInMillis, "Ulang Tahun Ferry Irwandi"))
        allEvents.add(Event(tomorrow.timeInMillis, "Briefing Acara Puncak"))
        allEvents.add(Event(nextWeek.timeInMillis, "Cek Lokasi Venue"))
    }

    private fun updateEventListForToday() {
        val today = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
        val sdf = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
        binding.selectedDateText.text = "Acara pada ${sdf.format(today.time)}"
        val todaysEvents = allEvents.filter { it.date == today.timeInMillis }
        eventAdapter.updateEvents(todaysEvents)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}