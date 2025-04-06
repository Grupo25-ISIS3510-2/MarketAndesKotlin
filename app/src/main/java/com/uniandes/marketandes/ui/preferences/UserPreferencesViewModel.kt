import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch

class UserPreferencesViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    var selectedFaculties by mutableStateOf<List<String>>(emptyList())
    var selectedInterests by mutableStateOf<List<String>>(emptyList())


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

    fun loadFaculties(uid: String, onResult: (List<String>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val faculties = document.get("facultades") as? List<String> ?: emptyList()
                onResult(faculties)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    fun loadInterests(uid: String, onResult: (List<String>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val interests = document.get("intereses") as? List<String> ?: emptyList()
                onResult(interests)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }


}
