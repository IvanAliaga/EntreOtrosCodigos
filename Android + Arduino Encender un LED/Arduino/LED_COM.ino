const int LED = 13; //LED conectado al pin digital 13
int inByte = 0;
 
void setup(){
    Serial.begin(9600); //Abrimos el el puerto serial
    pinMode(LED, OUTPUT); //Seteamos que el ping digital será de salida
}
 
void loop(){
    if(Serial.available() > 0){
       inByte = Serial.read(); //lee los bytes que ingresan
       if(inByte == '1')
           digitalWrite(LED, HIGH); //Enciende el LED
       else if(inByte == '0')
           digitalWrite(LED, LOW); //Apaga el LED
    }
}