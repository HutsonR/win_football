package jiharp.asembleya.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import jiharp.asembleya.app.databinding.ActivityGameBinding
import kotlin.random.Random


class GameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGameBinding
    private val TAG = "Debug Tag"
    private var startGameFlag: Boolean = false
    private val buttons = mutableListOf<View>() // Список чисел
    private var totalTime = 15000 // Время на поиск в миллисекундах
    private var timer: CountDownTimer? = null
    private var findNumber: Int = 1
    private var recordCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    // Инициализация
    private fun init() {
        val maxLevelTV = binding.gameMaxLevel
        val levelLayout = binding.gameLevelWrapper
        val findLayout = binding.gameFindWrapper
        val progressBar = binding.progressBar
        val btnStart = binding.startButton

        maxLevelTV.text = SharedPreferencesHelper.getRecord(this).toString() // Восстановление рекорда
        levelLayout.visibility = View.GONE
        findLayout.visibility = View.GONE
        progressBar.visibility = View.GONE

        buttons.addAll(listOf(binding.button1, binding.button2, binding.button3, binding.button4, binding.button5, binding.button6, binding.button7, binding.button8, binding.button9, binding.button9, binding.button10, binding.button11, binding.button12, binding.button13, binding.button14, binding.button15, binding.button16))

        btnStart.setOnClickListener {
            startGameFlag = true
            startGame()
        }
    }

    private fun startGame() {
        if (startGameFlag) {
            val levelLayout = binding.gameLevelWrapper
            val findLayout = binding.gameFindWrapper
            val levelTV = binding.gameLevel
            val findTV = binding.gameFind
            val btnStart = binding.startButton
            val progressBar = binding.progressBar

            btnStart.visibility = View.GONE
            levelLayout.visibility = View.VISIBLE
            findLayout.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE

            findNumber = Random.nextInt(1, 17)
            findTV.text = findNumber.toString()

            buttonsShuffle() // Первоначальное перемешивание кнопок

            timerStart(totalTime, progressBar)

            for (button in buttons) {
                button.setOnClickListener {
                    if (startGameFlag) {
                        val buttonText = (button as AppCompatButton).text.toString().toInt()

                        if (buttonText == findNumber) {
                            totalTime -= 1000
                            recordCount++
                            findNumber = Random.nextInt(1, 17)
                            findTV.text = findNumber.toString()
                            levelTV.text = recordCount.toString()
                            buttonsShuffle()
                            timer?.cancel() // Остановить таймер
                            progressBar.progress = 0 // Сбросить прогресс бар
                            timerStart(totalTime, progressBar) // Запускаем таймер с новым временем

                        } else {
                            showCustomToast("Wrong number! You've lost", Toast.LENGTH_SHORT)
                            timer?.cancel() // Остановить таймер
                            progressBar.progress = 0 // Сбросить прогресс бар
                            stopGame()
                        }
                    }
                }
            }
        }
    }

    private fun stopGame() {
        startGameFlag = false
        val recordTV = binding.gameMaxLevel
        val levelLayout = binding.gameLevelWrapper
        val findLayout = binding.gameFindWrapper
        val progressBar = binding.progressBar

        if (SharedPreferencesHelper.getRecord(this) < recordCount) {
            SharedPreferencesHelper.setRecord(this, recordCount)
            recordTV.text = SharedPreferencesHelper.getRecord(this).toString()
        }
        recordCount = 0
        binding.gameLevel.text = recordCount.toString()
        totalTime = 15000 // Восстанавливаем время

        progressBar.progress = 0
        timer?.cancel() // Остановить таймер

        progressBar.visibility = View.GONE
        levelLayout.visibility = View.GONE
        findLayout.visibility = View.GONE

        binding.startButton.visibility = View.VISIBLE
    }

    private fun timerStart(totalTime: Int, progressBar: ProgressBar) {
        timer = object : CountDownTimer(totalTime.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val progress = ((millisUntilFinished.toFloat() / totalTime) * 100).toInt()
                progressBar.progress = progress
            }

            override fun onFinish() {
                progressBar.progress = 0
                showCustomToast("Time's up! You've lost", Toast.LENGTH_SHORT)
                stopGame()
            }
        }.start()
    }

    private fun buttonsShuffle() {
        buttons.clear()
        val numbers = (1..16).toMutableList()
        numbers.shuffle()
        for (i in 0 until 16) {
            val buttonId = resources.getIdentifier("button${i + 1}", "id", packageName)
            val button = findViewById<AppCompatButton>(buttonId)
            button.text = numbers[i].toString()
            buttons.add(button)
        }
    }

    private fun showCustomToast(message: String, duration: Int) {
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.custom_toast, findViewById(R.id.custom_toast_layout))

        val text = layout.findViewById<TextView>(R.id.customToastText)
        text.text = message

        val toast = Toast(applicationContext)
        toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 40)
        toast.duration = duration
        toast.view = layout
        toast.show()
    }


}