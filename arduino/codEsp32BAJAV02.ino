#include <WiFi.h>
#include <WebServer.h>

const char* ssid = "SSID";
const char* password = "SENHA";

WebServer server(80);

float velocidade = 12.5;
float temperatura = 28.7;
float pressao = 1013.2;

void sendJson() {

  String json = "{";
  json += "\"velocidade\":" + String(velocidade, 1) + ",";
  json += "\"temperatura\":" + String(temperatura, 1) + ",";
  json += "\"pressao\":" + String(pressao, 1);
  json += "}";

  server.send(200, "application/json", json);
}

void setup() {
  Serial.begin(115200);
  delay(1000);

  Serial.println("Conectando ao WiFi...");
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("\nWiFi conectado!");
  Serial.print("IP do ESP32: ");
  Serial.println(WiFi.localIP());

  server.on("/api", sendJson);
  server.begin();

  Serial.println("servidor iniciado");
}

void loop() {

  velocidade += 0.1;
  if (velocidade > 20) velocidade = 10;

  temperatura = 25 + random(0, 5);
  pressao = 1010 + random(0, 10);

  server.handleClient();
}