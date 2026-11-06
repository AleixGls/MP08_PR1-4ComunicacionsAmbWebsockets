package com.client



import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import java.net.URI
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake



class MainActivity : AppCompatActivity() {

    // Atributs
    lateinit var wsclient : MyWebSocketClient



    // Clase main de la gui(?
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        // Trobar el textView del estat de la connexio i posarli l'estat
        findViewById<TextView>(R.id.txtEstatConexio).text = "Connectant..."

        // Utilitzar la funcio declarada mes abaix per conectar el client al servidor
        connectaWS()

        // Trobar el boto d'enviar i el camp d'imput del missatge i guardarlo
        val btnEnviar = findViewById<Button>(R.id.buttonEnviar)
        val input = findViewById<TextInputEditText>(R.id.inputMissatge)

        // Detectar cuan es pulsa el boto d'enviar
        btnEnviar.setOnClickListener {

            // Agafar el text d'input i pasarlo a string
            val texto = input.text.toString()

            // Utilitzar la funcio definida mes abaix per enviar el missatge al servidor
            enviaMissatge(texto)

            // Borrar el camp d'input per poder enviar el seguent missatge
            input.text?.clear()
        }
    }



    // Clase per la connexio amb el servidor
    inner class MyWebSocketClient(serverUri: URI) : WebSocketClient(serverUri) {

        // Metode per cuan el client es desconecta del servidor
        override fun onOpen(handshakedata: ServerHandshake?) {

            // Missatges de debug per consola
            println("Connectat al servidor")
            println("Codi d'estat: ${handshakedata?.httpStatus}")
            println("Descripció: ${handshakedata?.httpStatusMessage}")

            // Actualizar la GUI en un altre fil perque no deixa fer-ho en el mateix fil
            runOnUiThread {

                // Trobar el textView del estat de la connexio i posarli l'estat
                val tv = findViewById<TextView>(R.id.txtEstatConexio)
                tv.text = "Connectat"
            }
        }

        // Metode per cuan el client rep un missatge del servidor
        override fun onMessage(message: String?) {

            // Missatges de debug per consola
            println("📥 Rebut: $message")

            // Actualizar la GUI en un altre fil perque no deixa fer-ho en el mateix fil
            runOnUiThread {

                // Pasar el string del missatge rebut a un objecte text view
                val tv = TextView(this@MainActivity)
                tv.text = message
                tv.textSize = 18f

                // Trobar el layout per possar el text view per pantalla
                val layout = findViewById<LinearLayout>(R.id.layoutMissatges)
                layout.addView(tv)

                // Trobar el scroll i fer que baixi automaticament
                val scroll = findViewById<ScrollView>(R.id.scrollMissatges)
                scroll.post {
                    scroll.fullScroll(View.FOCUS_DOWN)
                }
            }
        }

        // Metode per cuan el client es desconecta del servidor
        override fun onClose(code: Int, reason: String?, remote: Boolean) {

            // Missatges de debug per consola
            println("Connexió tancada")
            println("Codi: $code, Raó: $reason, Remote: $remote")

            // Actualizar la GUI en un altre fil perque no deixa fer-ho en el mateix fil
            runOnUiThread {

                // Trobar el textView del estat de la connexio i posarli l'estat
                val tv = findViewById<TextView>(R.id.txtEstatConexio)
                tv.text = "Desconnectat"
            }
        }

        // Metode per cuan hi ha un error de connexio
        override fun onError(ex: Exception?) {

            // Missatges de debug per consola
            println("Error: ${ex?.message}")
            ex?.printStackTrace()

            // Actualizar la GUI en un altre fil perque no deixa fer-ho en el mateix fil
            runOnUiThread {

                // Trobar el textView del estat de la connexio i posarli l'estat
                val tv = findViewById<TextView>(R.id.txtEstatConexio)
                tv.text = "Error de connexio"
            }
        }
    }



    // Funcio per connectar al servidor
    fun connectaWS() {

        // Definir la url del servidor
        // HE TENIDO QUE HACER UN TUNEL EN WINDOWS PORQUE 10.0.2.2:8080 NO CONECTABA
        // COMANDO QUE HE USADO EN LA POWERSHELL DE ANDROID STUDIO:
        // & "C:\Users\aleix\AppData\Local\Android\Sdk\platform-tools\adb.exe" reverse tcp:8080 tcp:8080
        //
        // SI HAY MAS DE 2 EMULADORES ABIERTOS HAY QUE USAR:
        // & "C:\Users\aleix\AppData\Local\Android\Sdk\platform-tools\adb.exe" devices
        // MIRAR EL NOMBRE DEL EMULADOR: emulator-XXXX
        // Y USAR EL SIGUIENTE COMANDO CON EL NOMBRE DE EMULADORES QUE HAYA
        // "C:\Users\aleix\AppData\Local\Android\Sdk\platform-tools\adb.exe" -s emulator-XXXX reverse tcp:8080 tcp:8080
        val uri = URI("ws://127.0.0.1:8080")

        // Crear l'instancia del servidor
        wsclient = MyWebSocketClient(uri)

        // Utilitzar el metode .connect() per tractar de conectar al servidor
        wsclient.connect()
    }

    // Funcio per enviar missatge al servidor
    fun enviaMissatge( text : String ) {

        // Comprobar si la conexio amb el servidor esta oberta i si el misatge no esta buit
        if( wsclient.isOpen && text.isNotBlank()) {

            // Enviar el misatge
            wsclient.send( text )
        }
    }
}