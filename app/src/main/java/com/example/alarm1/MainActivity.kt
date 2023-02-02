package com.example.alarm1

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IntegerRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.io.*
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*


// メイン画面のアクティビティ
class MainActivity : AppCompatActivity() {
    private var alarmList =  mutableListOf<MutableMap<String, String>>()    // アラームのリスト
    private val fileName = "AlarmDataFile.txt"                              // アラーム情報を保存するファイル名
    private lateinit var alarmDataFile: File
    private lateinit var context: Context
    private lateinit var listView: ListView

    companion object {
        const val EXTRA_MESSAGE = "com.example.alarm1.ALARM"
    }

    // 追加したアラームの情報をSetAlarmActivityから受け取る
    private val getResultFromSetAlarmActivity =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if ( it.resultCode == Activity.RESULT_OK ) {
                // resにアラーム情報を受け取る
                val res = it.data?.getStringExtra(EXTRA_MESSAGE)

                // resをカンマ区切りで配列要素に分ける
                val arr = res?.split(",")

                if ( arr != null ) {
                    // arrを解読し、その結果できたalarmをalarmListに追加する
                    this.alarmList.add( this.makeNewAlarm(arr) )

                }

                // 現在のalarmListをファイルに書き込み
                saveFile()

                // MainActivityを再起動
                this.reload()
            }
        }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        System.out.println("開始画面")

        // 画面表示
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.context = applicationContext
        alarmDataFile = File(this.context.filesDir, this.fileName)  // アラーム情報を保存するファイル

        if( !alarmDataFile.exists() ){
            alarmDataFile.createNewFile()
            System.out.println("ファイル作成");
        }


        // button1（仮）
        val btn1 = findViewById<Button>(R.id.reloadbtn)

        btn1.setOnClickListener {

            // クイズアクティビティに遷移
            val intent2 = Intent(this, QuizActivity::class.java)
            startActivity(intent2)

            // reload()
        }

        // button2（仮）
        val btn2 = findViewById<Button>(R.id.addbtn)

        btn2.setOnClickListener {

            /* クイズ画面に遷移
            val intent2 = Intent(this, QuizActivity::class.java)
            startActivity(intent2)
            */

            /* 新規アラーム*/
            val intent = Intent(applicationContext, SetAlarmActivity::class.java)
            val str = "addAlarm"
            intent.putExtra(EXTRA_MESSAGE, str)
            getResultFromSetAlarmActivity.launch(intent)
        }

        // ファイルからalarmListを読み込み
        readFile()

        // ListViewにalarmListをセット
        if( this.alarmList.size != 0) {
            listView = findViewById<ListView>(R.id.alarmList)
            listView.adapter = SimpleAdapter(
                this,
                this.alarmList,
                R.layout.list_item,
                arrayOf("stringTime", "stringWeek", "alarmName"),
                intArrayOf(R.id.stringTime, R.id.stringWeek, R.id.alarmName)
            )
            // 項目を長押ししたときの処理(アラーム削除)
            listView.onItemLongClickListener =
                AdapterView.OnItemLongClickListener setOnItemLongClickListener@{ parent, view, pos, id ->
                    remove(pos)
                    return@setOnItemLongClickListener true
                }
        }


        // アラームリストから直近のアラームを判断し、アラームを設定する
        setAlarm()
    }

    // アラーム削除を実行する関数
    private fun remove(position: Int) {
        this.alarmList.removeAt(position)
        saveFile()
        reload()
    }


    // ホーム画面を表示するたび（クイズ画面からの遷移には未対応）に呼ぶ関数
    // フィールドのAlarmListから直近のアラームがどれか判断し、アラームを設定するする関数
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setAlarm() {
        val nowTime = ZonedDateTime.now(ZoneId.of("Asia/Tokyo"))
        var dtFormat = DateTimeFormatter.ofPattern("HH:mm")
        val nowTimeString = dtFormat.format(nowTime)        // 現在時刻"HH:mm"
        val nowTimeInt = Integer.parseInt( nowTimeString.replace(":", "") )     // intの現在時刻 HHmm

        var hasSetAlarm = false     // １アラーム内で、設定する場合の時間が確定したかどうか
        var timeTmpInt: Int         // for文中で注目しているアラームの時刻
        var newAlarmTime = 1000000  // 現状設定する予定のアラームの時刻
        val afterOneDate = 10000    // 1日後のアラームだとこれを一回、2日後だと2回足す


        // 時間をセットする
        val calendar = Calendar.getInstance()

        // 現在の曜日を受け取る
        val dayOfWeek: Int = calendar.get(Calendar.DAY_OF_WEEK)

        // どのアラームを設定するか、アラームリストから直近のアラームを判断
        for( i in this.alarmList ){
            // 注目しているアラームの設定時刻
            timeTmpInt = Integer.parseInt( i["stringTime"]?.replace(":", "")!! )

            /************ 実行確認用 *************/
            println("現在時刻:$nowTimeInt")
            println("アラーム時刻:$timeTmpInt")
            println("曜日:$dayOfWeek")

            var day2 = dayOfWeek-2
            println("dayOfWeek = "+dayOfWeek)
            println("dayOfWeek-2 = "+day2)
            day2 = (day2+5)%7
            println("dayOfWeek+5%7 = "+day2)

            // 曜日が一致していたら
            if( i["arrayWeek"]?.substring(day2, day2+1).equals("1") ) {
                // 注目アラームの設定時刻が現在時刻より遅いとき
                if( timeTmpInt >= nowTimeInt ) {
                    // 設定済みアラームの時刻より注目中のアラームの時刻が早い場合
                    if( newAlarmTime > timeTmpInt ) {
                        // 設定予定のアラームの時刻を更新
                        newAlarmTime = timeTmpInt
                        // これ以降(同時間、他曜日)で上書きしないことを記録
                        hasSetAlarm = true
                    }
                }

                // 注目アラームの設定時刻が現在時刻より早いとき
                else{
                    // 一週間後として時刻を設定する
                    timeTmpInt += ( afterOneDate*7 )

                    // 設定済みアラームの時刻より注目中のアラームの時刻が早い場合
                    if( newAlarmTime > timeTmpInt ) {
                        // 設定予定のアラームの時刻を更新
                        newAlarmTime = timeTmpInt
                    }
                }
            }

            // 曜日が一致しなかったら or 他の曜日で上書きできるかもしれない時
            if( !hasSetAlarm ) {
                timeTmpInt = Integer.parseInt( i["stringTime"]?.replace(":", "")!! )
                // 直近の曜日を探す
                for( j in dayOfWeek-1..dayOfWeek+4 ) {
                    // 対象の曜日にアラームが有効なとき
                    if( i["arrayWeek"]?.substring( j-(j/7)*7, j-(j/7)*7+1 ).equals("1") ) {
                        // ○日後として時間設定
                        timeTmpInt += ( afterOneDate * (j - dayOfWeek + 2) )

                        // 設定済みアラームの時刻より注目中のアラームの時刻が早い場合
                        if( newAlarmTime > timeTmpInt ) {
                            // 設定予定のアラームの時刻を更新
                            newAlarmTime = timeTmpInt
                            // アラーム時刻が確定したことを記録
                            hasSetAlarm = true
                        }
                        break
                    }
                }
            }
        }


        // 現在の時間、分、秒を取得
        dtFormat = DateTimeFormatter.ofPattern("HH")
        val nowHour = Integer.parseInt( dtFormat.format(nowTime))
        dtFormat = DateTimeFormatter.ofPattern("mm")
        val nowMinute = Integer.parseInt( dtFormat.format(nowTime))
        dtFormat = DateTimeFormatter.ofPattern("ss")
        val nowSecond: Int = Integer.parseInt( dtFormat.format(nowTime) )


        // Calendarを使って現在の時間をミリ秒で取得
        calendar.timeInMillis = System.currentTimeMillis()


        /******* 確かめ用でアラーム時刻+一分後に変更 *******/
        calendar.add(Calendar.MINUTE, 1)

        // 秒数の修正(○分0秒にアラームが鳴るように調整)
        calendar.add(Calendar.SECOND, -nowSecond)

        // 日付設定（何日後かに応じて、時間を追加）
        calendar.add(Calendar.DATE, newAlarmTime/10000)
        /******* 実行確認用 ********/
        println("日後:" + newAlarmTime/10000)

        newAlarmTime -= ( newAlarmTime/10000 * 10000 )

        // 時間設定（何時間後かに応じて、時間を追加）
        calendar.add(Calendar.HOUR, newAlarmTime/100 - nowHour )
        /******* 実行確認用
        val a = newAlarmTime/100 - nowHour
        System.out.println("時間後:" + a )
         ********/
        newAlarmTime -= ( newAlarmTime/100 * 100 )

        // 分設定（何分後かに応じで、時間を追加）
        calendar.add(Calendar.MINUTE, newAlarmTime - nowMinute)
        /******* 実行確認用
        val b = newAlarmTime - nowMinute
        System.out.println("分後後:" + b )
         ********/


        //明示的なBroadCast
        val intent = Intent(
            applicationContext,
            AlarmReceiver::class.java
        )
        val pending = PendingIntent.getBroadcast(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // アラームをセットする
        val am = getSystemService(ALARM_SERVICE) as AlarmManager

        if (am != null) {
            am.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pending)
            // アラームをセットしたことをToastで通知
            Toast.makeText(
                applicationContext,
                "Set Alarm ", Toast.LENGTH_SHORT
            ).show()
        }
    }


    // SetAlarmActivityから受け取った情報を解読し、情報を補充、mapに格納する関数
    private fun makeNewAlarm(arr: List<String>): MutableMap<String, String> {

        // 実行曜日（0010010みたいな配列）の文字列化
        var stTmp = "("
        val arrayWeek = arr[1]
        for (l in 0..6) {
            if (arrayWeek[l] == '1') {
                when (l) {
                    0 -> stTmp += "月 "
                    1 -> stTmp += "火 "
                    2 -> stTmp += "水 "
                    3 -> stTmp += "木 "
                    4 -> stTmp += "金 "
                    5 -> stTmp += "土 "
                    6 -> stTmp += "日 "
                }
            }
        }
        stTmp = stTmp.dropLast(1)
        stTmp += ")"

        // 文字列化したものなど諸々をmapに代入して、返す
        return mutableMapOf(
            "stringTime" to arr[0],
            "arrayWeek" to arrayWeek,
            "stringWeek" to stTmp,
            "alarmName" to arr[2]
        )
    }


    // アクティビティの再起動を行う関数
    private fun reload() {
        val intent = intent
        overridePendingTransition(0, 0)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        finish()
        overridePendingTransition(0, 0)
        startActivity(intent)
    }


    // https://akira-watson.com/android/kotlin/internal-storage.html
    // もらったリストの内容をファイルに保存する関数。ここの保存が上書きになっている。
    private fun saveFile() {


        var str = ""                                                    // ファイルに書き込む１列分のデータを保存する文字列

        // alarmListから要素を一つずつ取り出し、文字列に変換する
        for (i in this.alarmList) {
            str = str.plus( i["stringTime"] + "," )
            str = str.plus( i["arrayWeek"] + "," )
            str = str.plus( i["stringWeek"] + "," )
            str = str.plus( i["alarmName"] + "\n" )
        }
        // ファイル書き込み実行
        try {
            FileWriter(alarmDataFile).use { writer -> writer.write(str) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    // ファイルからリスト要素を読み出し、alarmListを初期化する
    private fun readFile() {


        var strArrayTmp: List<String>?                                  // ファイル一列分の,区切りの情報を保存する配列
        val fileReader = FileReader(alarmDataFile)
        val bufferedReader = BufferedReader(fileReader)

        // 一列ずつ読み込む
        for (it in bufferedReader.readLines()) {
            // ,で区切られた物を、配列として取り出す
            strArrayTmp = it.split(",")
            // mapに格納する
            val newAlarm: MutableMap<String, String> =
                mutableMapOf(
                    "stringTime" to strArrayTmp[0],
                    "arrayWeek" to strArrayTmp[1],
                    "stringWeek" to strArrayTmp[2],
                    "alarmName" to strArrayTmp[3]
                )

            // アラーム一つをalarmListに追加
            this.alarmList.add(newAlarm)
        }
    }
}

