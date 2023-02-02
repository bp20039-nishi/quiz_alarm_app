package com.example.alarm1

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.jvm.JvmName as JvmName1

class SetAlarmActivity : AppCompatActivity(), TimePickerDialog.OnTimeSetListener {
    var pickedAlarmTime: String = "None"


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 画面表示
        setContentView(R.layout.activity_set_alarm)

        // ボタン関係
        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnOK = findViewById<Button>(R.id.btnOK)

        // アラーム追加せずに戻る
        btnBack.setOnClickListener {
            // アクティビティ終了
            finish()
        }

        // アラームを追加（MainActivityに渡す）する
        btnOK.setOnClickListener{

            val newAlarm = this.setNewAlarm()
            val intent = Intent()

            // アラームが正しく設定されていたらここでアラーム情報を一つの文字列に結合
            if( this.IsAlarmCorrect( newAlarm ) ){
                val str =
                    newAlarm["stringTime"] + "," +
                            newAlarm["arrayWeek"] + "," +
                            newAlarm["alarmName"]

                // 作成した文字列を受け渡す準備
                intent.putExtra(MainActivity.EXTRA_MESSAGE, str)
            }
            // MainActivityに渡す
            setResult(Activity.RESULT_OK, intent)
            // アクティビティ終了
            finish()
        }
    }

    // 時間を取得
    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        this.pickedAlarmTime = getString(R.string.stringformat, hourOfDay, minute)
        println("pickTime = $pickedAlarmTime")
    }

    // timePicker起動
    fun showTimePickerDialog(v: View) {
        val newFragment = TimePick()
        newFragment.show(supportFragmentManager, "timePicker")
    }

    // 入力されている情報から、Alarmのmapを作る
    @RequiresApi(Build.VERSION_CODES.O)
    fun setNewAlarm(): MutableMap<String, String> {

        // 例：アラームに必要な要素を定義(今回は初期化)
        val time = ZonedDateTime.now(ZoneId.of("Africa/Abidjan"))
        val dtFormat = DateTimeFormatter.ofPattern("HH:mm")
        val editText = findViewById<EditText>(R.id.alarmName)
        val alarmName: String

        // アラーム名が空かどうか判断
        if(editText.text.toString().isNotEmpty()){
            alarmName = editText.text.toString()
        }else {
            alarmName = ""
        }


        // 以下はしっかり画面からもらっている
        var arrayWeek = ""

        // チェックボックス
        val monday = findViewById<CheckBox>(R.id.Monday)
        val tuesday = findViewById<CheckBox>(R.id.Tuesday)
        val wednesday = findViewById<CheckBox>(R.id.Wednesday)
        val thursday = findViewById<CheckBox>(R.id.Thursday)
        val friday = findViewById<CheckBox>(R.id.Friday)
        val saturday = findViewById<CheckBox>(R.id.Saturday)
        val sunday = findViewById<CheckBox>(R.id.Sunday)

        // チェックボックスの状態を見て、実行する曜日=1 しない曜日を=0 としてを文字列に保存
        // 月曜日
        arrayWeek =
            if (monday.isChecked) arrayWeek.plus('1')
            else arrayWeek.plus('0')
        // 火曜日
        arrayWeek =
            if (tuesday.isChecked) arrayWeek.plus('1')
            else arrayWeek.plus('0')
        // 水曜日
        arrayWeek =
            if (wednesday.isChecked) arrayWeek.plus('1')
            else arrayWeek.plus('0')
        // 木曜日
        arrayWeek =
            if (thursday.isChecked) arrayWeek.plus('1')
            else arrayWeek.plus('0')
        // 金曜日
        arrayWeek =
            if (friday.isChecked) arrayWeek.plus('1')
            else arrayWeek.plus('0')
        // 土曜日
        arrayWeek =
            if (saturday.isChecked) arrayWeek.plus('1')
            else arrayWeek.plus('0')
        // 日曜日
        arrayWeek =
            if (sunday.isChecked) arrayWeek.plus('1')
            else arrayWeek.plus('0')


        // アラーム情報をまとめてmapに格納する準備と一部情報の実行
        val newAlarm = mutableMapOf(
            "stringTime" to "時間（文字列）いれる",
            "arrayWeek" to arrayWeek,
            "alarmName" to alarmName
        )

        // 文字列化した時間を代入
        // newAlarm["stringTime"] = dtFormat.format(time)
        newAlarm["stringTime"] = this.pickedAlarmTime

        // 作成したアラーム情報のmapを返す
        return newAlarm
    }


    // 引数のアラームが、しっかり設定されているか判断
    private fun IsAlarmCorrect( newAlarm: MutableMap<String, String>): Boolean{

        // 曜日設定をしていなかったら、アラームを追加せずにfalseを返す
        if( newAlarm["arrayWeek"] == "0000000" )
            return false
        // アラーム名を設定していなければ。。。
        if( newAlarm["alarmName"] == "")
            return false
        // 時間設定を完了していなければ。。。
        if( newAlarm["alarmTime"] == "None")
            return false

        // しっかり設定されていたらtrueを返す
        return true
    }

}