import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch

class UserPreferencesViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    var selectedFaculties = mutableListOf<String>()
    var selectedInterests = mutableListOf<String>()

    fun saveFaculties(userId: String, onSuccess: () -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId)

        userRef.set(mapOf("faculties" to selectedFaculties), SetOptions.merge())
            .addOnSuccessListener {
                Log.d("Firestore", "Facultades guardadas exitosamente.")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error guardando facultades", e)
            }
    }


    fun saveInterests(userId: String, onSuccess: () -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId)

        userRef.set(mapOf("interests" to selectedInterests), SetOptions.merge())
            .addOnSuccessListener {
                Log.d("Firestore", "Intereses guardados exitosamente.")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error guardando intereses", e)
            }
    }
}
