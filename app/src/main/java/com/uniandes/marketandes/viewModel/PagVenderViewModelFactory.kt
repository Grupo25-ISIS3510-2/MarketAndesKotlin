import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.uniandes.marketandes.repository.ProductRepository
import com.uniandes.marketandes.util.NetworkConnectivityObserver
import com.uniandes.marketandes.viewmodel.PagVenderViewModel

class PagVenderViewModelFactory(
    private val productRepository: ProductRepository,
    private val connectivityObserver: NetworkConnectivityObserver
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PagVenderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PagVenderViewModel(productRepository, connectivityObserver) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
