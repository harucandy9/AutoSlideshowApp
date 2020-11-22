package jp.techachademy.autoslideshowapp

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import kotlinx.android.synthetic.main.activity_main.*
import android.provider.MediaStore
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import java.util.*
import android.os.Handler


class MainActivity : AppCompatActivity(),View.OnClickListener {

    private val PERMISSIONS_REQUEST_CODE = 100
    private var cursor :Cursor? = null
    private var sTimer: Timer? = null
    private var sHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                Log.d("test", "許可されている")
                getImageInfo()
                showFirstImage(this.cursor)
            } else {
                Log.d("test", "許可されていない")
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
        }


        buttonNext.setOnClickListener(this)
        buttonBack.setOnClickListener(this)
        buttonSlideShow.setOnClickListener(this)

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getImageInfo()
                    showFirstImage(cursor)
                    Log.d("test","ダイアログの結果許可：最初の画像表示")
                } else {
                    return
                }
        }
    }

    private fun getImageInfo(){
        val resolver = contentResolver
        cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null, //項目
            null, //フィルタ
            null, //フィルタ用パラメータ
            null )//ソート
        Log.d("test","画像データ取得")

    }

    //ボタンクリック時
    override fun onClick(v: View?) {
        Log.d("test","ボタンクリック")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                Log.d("test", "許可されていない")
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
                return
            }
        }

        when(v?.id){
            R.id.buttonNext->showNextImage(this.cursor)
            R.id.buttonBack->showPreviewImage(this.cursor)
            R.id.buttonSlideShow -> slideShow(this.cursor)
        }

    }


    //最初の画面
    private fun showFirstImage(cursor: Cursor?) {
        Log.d("test","最初の画像表示処理")

        if (cursor!!.moveToFirst()) {
            this.imageView.setImageURI(getImageUri(cursor))
        }
    }

    //進むボタン
    private fun showNextImage(cursor: Cursor?){
        Log.d("test","次画像表示処理")

        if (cursor!!.moveToNext()) {
            Log.d("test","次画像がある：次の画像を表示")
            this.imageView.setImageURI(getImageUri(cursor))
        }else{
            Log.d("test","次画像がない：最初の画像を表示")
            cursor.moveToFirst()
            this.imageView.setImageURI(getImageUri(cursor))
        }

    }
    //戻るボタン
    private fun showPreviewImage(cursor: Cursor?){
        Log.d("test","前の画像表示処理")

        if (cursor!!.moveToPrevious()) {
            this.imageView.setImageURI(getImageUri(cursor))
        }else{
            cursor.moveToLast()
            this.imageView.setImageURI(getImageUri(cursor))
        }

    }

    //画像のURI取得
    private fun getImageUri(cursor: Cursor?):Uri{
        Log.d("test","URI取得")

        val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
        val id = cursor.getLong(fieldIndex)
        val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
        return imageUri
    }

    //スライドショー開始停止処理
    private fun slideShow(cursor: Cursor?){
        if(sTimer==null) {
            Log.d("test", "スライドショー処理開始")
            buttonSlideShow.text = "停止"
            buttonNext.isEnabled = false
            buttonBack.isEnabled = false

            sTimer = Timer()
            sTimer!!.schedule(object : TimerTask() {
                override fun run() {
                    sHandler.post { showNextImage(cursor) }
                    Log.d("test", "Timerのrunメソッド")
                }
            }, 2000, 2000) // 最初に始動させるまで 100ミリ秒、ループの間隔を 100ミリ秒 に設定
        }else{
            buttonSlideShow.text = "再生"
            buttonNext.isEnabled = true
            buttonBack.isEnabled = true

            sTimer!!.cancel()
            sTimer = null
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        if(this.cursor!=null){this.cursor!!.close()}
    }
}
