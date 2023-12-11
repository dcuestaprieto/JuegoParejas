package com.diego.juegoparejas

import android.content.Intent
import android.content.res.Configuration
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.get
import com.diego.juegoparejas.databinding.ActivityMainBinding
import com.diego.juegoparejas.databinding.FichasBinding

class MainActivity : AppCompatActivity() {
    /*
     * TODO cancion cuando ganas
     * registrar actividad en el manifest
     */

    private lateinit var binding: ActivityMainBinding
    private lateinit var bindingFichas: FichasBinding
    //cada vez que se inicie la app, la variable que refleja la cantidad de clicks que se han hecho vuelve a 0
    private val CANTIDAD_COLUMNAS:Int = 4
    private var imageList = listOf(
        R.drawable.luffy,
        R.drawable.brook,
        R.drawable.nami,
        R.drawable.robin,
        R.drawable.usopp,
        R.drawable.zoro,
        R.drawable.luffy,
        R.drawable.brook,
        R.drawable.nami,
        R.drawable.robin,
        R.drawable.usopp,
        R.drawable.zoro
    ).shuffled()


    //Separo la lista de imagenes en listas que contengan la misma cantidad de fotos que de columnas haya
    //cada sublista representa una fila en la interfaz
    private var listOfLists: List<List<Int>> = imageList.chunked(CANTIDAD_COLUMNAS)

    private lateinit var contenedorImagenes: LinearLayout
    private lateinit var imagenesClickadas: ArrayList<ImageView>
    private lateinit var btnReset:Button
    private lateinit var btnMusica: ImageButton
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var bgSong:MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //al empezar la aplicacion pongo la música a sonar
        bgSong = MediaPlayer.create(this,R.raw.main_song)
        bgSong.isLooping = true
        bgSong.setVolume(0.1F,0.1F)

        binding = ActivityMainBinding.inflate(layoutInflater)
        bindingFichas = FichasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        contenedorImagenes = findViewById(R.id.imageContainer)

        imagenesClickadas = ArrayList()

        btnReset = findViewById(R.id.btnReset)
        btnReset.setOnClickListener{resetear()}
        btnMusica = findViewById(R.id.btnSonido)
        btnMusica.setOnClickListener{gestionarMusicaFondo()}

        gestionarMusicaFondo()

        //Recorro el contenedor de LinearLayouts
        for (i in 0 until contenedorImagenes.childCount){
            //obtengo el hijo i del contenedor de LinearLayouts
            val linearLayout:LinearLayout = contenedorImagenes[i] as LinearLayout
            //recorro los hijos del linearlayout (las fichas)
            for (j in 0 until linearLayout.childCount){
                val currentImage:ImageView = (linearLayout[j] as ImageView)
                //le añado como tag la foto que se le va a poner cuando reciba un click
                currentImage.tag=listOfLists[i][j]
                //añado evento on click
                currentImage.setOnClickListener {handleClick(currentImage)}
            }
        }

    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        gestionarMusicaFondo()
    }

    private fun gestionarMusicaFondo() {
        if (!bgSong.isPlaying){
            btnMusica.setImageResource(R.drawable.sound_on)
            bgSong.start()
        }else{
            btnMusica.setImageResource(R.drawable.sound_off)
            bgSong.pause()
        }
    }

    fun handleClick(image:ImageView){
        image.setImageResource(image.tag as Int)
        handler.postDelayed({
            imagenesClickadas.add(image)
            if (imagenesClickadas.size == 2){
                //compruebo que son match pero que no haya sido la misma imagen
                if ((imagenesClickadas[0].tag.equals(imagenesClickadas[1].tag) && !areTheSameImage(imagenesClickadas))){
                    imagenesClickadas[0].isEnabled = false
                    imagenesClickadas[1].isEnabled = false
                    imagenesClickadas[0].tag="encontrada"
                    imagenesClickadas[1].tag="encontrada"
                    sonido("good_ending")
                }else{
                    imagenesClickadas[0].setImageResource(R.drawable.oculto)
                    imagenesClickadas[1].setImageResource(R.drawable.oculto)
                    sonido("bad_ending")
                }
                imagenesClickadas.clear()
            }
            if (hayVictoria()){
                Toast.makeText(this, "HAS GANADO", Toast.LENGTH_SHORT).show()
            }
        },300)
    }

    private fun sonido(soundToPlay: String) {
        val resId:Int = resources.getIdentifier(soundToPlay,"raw",packageName)
        val sound:MediaPlayer = MediaPlayer.create(this,resId)
        sound.setVolume(0.5F,0.5F)
        sound.start()
        sound.setOnCompletionListener {
            it.stop()
            it.release()
        }
    }

    fun areTheSameImage(listaImagenes: ArrayList<ImageView>): Boolean{
        return listaImagenes[0].equals(listaImagenes[1])
    }

    fun hayVictoria():Boolean{
        var victoria:Boolean = true
        for (i in 0 until contenedorImagenes.childCount){
            val linearLayout:LinearLayout = contenedorImagenes[i] as LinearLayout
            for (j in 0 until linearLayout.childCount){
                val currentImage:ImageView = (linearLayout[j] as ImageView)
                //si alguna imagen no tiene como tag encontrada es que
                // no se ha terminado el juego
                if(!currentImage.tag.equals("encontrada")){
                    victoria = false
                }
            }
        }
        return victoria
    }

    private fun resetear(){
        //randomizo la lista de nuevo
        imageList = imageList.shuffled()
        //vuelvo a generar la lista de listas con la nueva lista randomizada
        listOfLists = imageList.chunked(CANTIDAD_COLUMNAS)
        imagenesClickadas.clear()
        for (i in 0 until contenedorImagenes.childCount){
            val linearLayout:LinearLayout = contenedorImagenes[i] as LinearLayout
            for (j in 0 until linearLayout.childCount){
                val currentImage:ImageView = (linearLayout[j] as ImageView)
                currentImage.tag=listOfLists[i][j]
                currentImage.setImageResource(R.drawable.oculto)
                currentImage.isEnabled = true
            }
        }
    }
}