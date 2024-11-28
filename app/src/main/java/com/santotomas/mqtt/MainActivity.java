package com.santotomas.mqtt;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;

public class MainActivity extends AppCompatActivity {

    private Mqtt3AsyncClient clienteMQTT;
    private EditText escribirMensaje;
    private Button enviarMensaje;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        escribirMensaje = findViewById(R.id.escribirMensaje);
        enviarMensaje = findViewById(R.id.enviarMensaje);

        clienteMQTT = MqttClient.builder()
                .useMqttVersion3()
                .identifier("AndroidClient-" + System.currentTimeMillis())
                .serverHost("test.mosquitto.org")
                .serverPort(1883)
                .buildAsync();

        clienteMQTT.connect()
                .whenComplete((ack, throwable) -> {
                    if (throwable == null) {
                        Log.d("MQTT", "Conexión Exitosa");
                        suscribirseATema();
                    } else {
                        Log.e("MQTT", "Falló En La Conexión", throwable);
                    }
                });

        enviarMensaje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mensaje = escribirMensaje.getText().toString();
                publicarMensaje(mensaje);
            }
        });
    }

    private void suscribirseATema() {
        clienteMQTT.subscribeWith()
                .topicFilter("daniel/mensajeriamqtt")
                .qos(MqttQos.AT_LEAST_ONCE)
                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable == null) {
                        Log.d("MQTT", "Suscripción Exitosa Al tema");

                        clienteMQTT.publishes(MqttGlobalPublishFilter.ALL, publish -> {
                            String mensaje = new String(publish.getPayloadAsBytes());
                            Log.d("MQTT", "Mensaje Recibido: " + mensaje);
                        });
                    } else {
                        Log.e("MQTT", "Falló La Suscripción", throwable);
                    }
                });
    }

    private void publicarMensaje(String mensaje) {
        clienteMQTT.publishWith()
                .topic("daniel/mensajeriamqtt")
                .payload(mensaje.getBytes())
                .qos(MqttQos.AT_LEAST_ONCE)
                .send()
                .whenComplete((publishAck, throwable) -> {
                    if (throwable == null) {
                        Log.d("MQTT", "Mensaje Enviado: " + mensaje);
                    } else {
                        Log.e("MQTT", "Error Al Enviar Mensaje", throwable);
                    }
                });
    }
}