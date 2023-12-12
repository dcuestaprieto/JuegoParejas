package com.diego.juegoparejas

import android.app.AlertDialog
import android.content.DialogInterface
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
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.get
import com.diego.juegoparejas.databinding.ActivityMainBinding
import com.diego.juegoparejas.databinding.FichasBinding
import java.lang.reflect.Array
import kotlin.system.exitProcess

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
    private lateinit var txtJ1: TextView
    private lateinit var txtJ2: TextView
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var bgSong:MediaPlayer

    //defino que empiece el j1
    private var turnoJ1:Boolean = true
    private lateinit var puntuaciones: ArrayList<Int>

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
        txtJ1 = findViewById(R.id.j1txt)
        txtJ2 = findViewById(R.id.j2txt)

        puntuaciones = arrayListOf(0,0)

        txtJ1.text="J1: ${puntuaciones[0].toString()}"
        txtJ2.text="J2: ${puntuaciones[1].toString()}"


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

    private fun handleClick(image:ImageView){
        image.setImageResource(image.tag as Int)
        // lo pongo a false para que al clickar otra vez sobre la misma imagen no cuente
        image.isEnabled = false
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
                    if (turnoJ1){
                        puntuaciones[0]++
                        txtJ1.text="J1: ${puntuaciones[0].toString()}"
                    }else{
                        puntuaciones[1]++
                        txtJ2.text="J2: ${puntuaciones[1].toString()}"
                    }
                }else{
                    imagenesClickadas[0].setImageResource(R.drawable.oculto)
                    imagenesClickadas[1].setImageResource(R.drawable.oculto)
                    imagenesClickadas[0].isEnabled = true
                    imagenesClickadas[1].isEnabled = true
                    sonido("bad_ending")
                }
                imagenesClickadas.clear()
                turnoJ1 = !turnoJ1
            }
            if (hayVictoria()){
                sonido("good_ending")
                sonido("good_ending")
                sonido("good_ending")
                val mensajeFinal:String
                val puntuacionJ1:Int = txtJ1.text[txtJ1.text.length-1].digitToInt()
                val puntuacionJ2:Int = txtJ2.text[txtJ2.text.length-1].digitToInt()
                mensajeFinal = if(puntuacionJ1>puntuacionJ2){
                    "Ha ganado el J1 con $puntuacionJ1 puntos"
                }else if(puntuacionJ1 == puntuacionJ2){
                    "EMPATE"
                }else{
                    "Ha ganado el J2 con $puntuacionJ2 puntos"
                }
                //después de que suene el sonido de victoria sale una ventana emergente
                val builder = AlertDialog.Builder(this)
                // mostramos el resultado final de los dos jugadores
                builder.setMessage(mensajeFinal)
                    .setPositiveButton("Reiniciar") { dialog, id ->
                        resetear()
                    }
                    .setNegativeButton("Salir") { dialog, id ->
                        exitProcess(0)
                    }
                builder.create().show()
            }
        },300)
    }

    private fun sonido(soundToPlay: String) {
        //obtengo el id del recurso que he pasado por parámetro
        val idResource:Int = resources.getIdentifier(soundToPlay,"raw",packageName)
        //hago sonarlo pasando el id del recurso
        val sound:MediaPlayer = MediaPlayer.create(this,idResource)
        sound.setVolume(0.5F,0.5F)
        sound.start()
        //hago que cuando haya terminado de sonar, pare y libere recursos
        sound.setOnCompletionListener {
            it.stop()
            it.release()
        }
    }

    /**
     * funcion para saber si dos imagenes son la misma instancia,
     * para que no se pueda dar como valido que se haga click sobre la misma imagen
     */
    private fun areTheSameImage(listaImagenes: ArrayList<ImageView>): Boolean{
        return listaImagenes[0].equals(listaImagenes[1])
    }

    private fun hayVictoria():Boolean{
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
        puntuaciones = arrayListOf(0,0)
        txtJ1.text="J1: ${puntuaciones[0].toString()}"
        txtJ2.text="J2: ${puntuaciones[1].toString()}"
    }
}