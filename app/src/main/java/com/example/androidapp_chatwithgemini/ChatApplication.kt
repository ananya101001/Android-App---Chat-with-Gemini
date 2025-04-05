import android.app.Application
import com.google.ai.client.generativeai.GenerativeModel

class ChatApplication : Application() {
    lateinit var generativeModel: GenerativeModel

    override fun onCreate() {
        super.onCreate()
        generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = "AIzaSyA1sCjjS7l80ik4Sdg-KZ_DPAWzL4pU6Ck" // Replace with your actual API key
        )
    }
}