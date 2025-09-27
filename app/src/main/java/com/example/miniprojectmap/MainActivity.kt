import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.miniprojectmap.databinding.ActivityMainBinding // Impor kelas binding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding // Deklarasi variabel binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater) // Inflate layout
        setContentView(binding.root) // Set content view dari root binding

        // Sekarang Anda bisa mengakses komponen dengan aman
        binding.btnSeeCalendar.setOnClickListener {
            // Tombol diklik!
        }
        binding.cardNearestBirthday.textPersonName.text = "Nama Baru"
    }
}