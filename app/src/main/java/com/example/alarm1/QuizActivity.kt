package com.example.alarm1

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity


class QuizActivity : AppCompatActivity() {

    private var rightAnswer: String? = null
    private var rightAnswerCount = 0
    private var quizCount = 1
    private var quizData: MutableList<MutableList<String>> = mutableListOf(mutableListOf("null"))
    private val QUIZ_COUNT = 2
    private val ALL_QUIZ_COUNT = 3


    private lateinit var mediaPlayer: MediaPlayer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_quiz)

        // 音声ファイルの再生用
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound6)
        mediaPlayer.isLooping=true

        mediaPlayer.start();

        // 音楽の再生

        this.quizData = this.setQuizDate()

        showNextQuiz()

    }

    override fun onDestroy() {
        super.onDestroy()

        mediaPlayer.release()
    }

    // クイズデータの設定（Excelから?? ）
    private fun setQuizDate(): MutableList<MutableList<String>> {

        return mutableListOf(
            mutableListOf("1+1=", "2", "1", "3", "4"),
            mutableListOf("1+2=", "3", "1", "2", "4"),
            mutableListOf("8+5=", "13", "15", "12", "14"),
        )
    }

    private fun showNextQuiz(){
        val quiz = quizData[0]

        val questionLabel: TextView = findViewById<TextView?>(R.id.questionLabel)
        val countLabel: TextView = findViewById(R.id.countLabel)
        val answerBtn1: Button = findViewById(R.id.answerBtn1)
        val answerBtn2: Button = findViewById(R.id.answerBtn2)
        val answerBtn3: Button = findViewById(R.id.answerBtn3)
        val answerBtn4: Button = findViewById(R.id.answerBtn4)

        countLabel.text = getString(R.string.count_label, quizCount )

        // 問題文
        questionLabel.text = quiz[0]

        // 正解
        rightAnswer = quiz[1]

        // 問題文をリストから削除
        quiz.removeAt(0)

        // 選択肢をシャッフル
        quiz.shuffle()

        // 選択肢をセット
        answerBtn1.text = quiz[0]
        answerBtn2.text = quiz[1]
        answerBtn3.text = quiz[2]
        answerBtn4.text = quiz[3]

        // 出題した問題をリストから削除
        quizData.removeAt(0)
    }


    fun checkAnswer(view: View) {

        // どの回答ボタンが押されたか
        val answerBtn: Button = findViewById(view.id)
        val btnText = answerBtn.text.toString()

        // ダイアログのタイトルを作成
        val alertTitle: String
        if (btnText == rightAnswer) {
            alertTitle = "正解！"
            rightAnswerCount++
        } else {
            alertTitle = "不正解。。"
        }


        // ダイアログ作成
        AlertDialog.Builder(this)
            .setTitle(alertTitle)
            .setMessage("答え ： $rightAnswer")
            .setPositiveButton("OK") { dialogInterface, i ->
                checkQuizCount()
            }
            .setCancelable(false)
            .show()
    }


    private fun checkQuizCount(){
        if(rightAnswerCount == QUIZ_COUNT || quizCount >= ALL_QUIZ_COUNT){
            stopService(intent)
            mediaPlayer.stop()

            finish()
        } else {
            quizCount++
            showNextQuiz()
        }
    }
}