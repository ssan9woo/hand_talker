#include <SoftwareSerial.h>
#define SBUF_SIZE 1024
SoftwareSerial bluetooth(2, 3);

float ebimuTest[6] = { 0.00, 10.00, 50.00, 100.00, 150.00, 200.00}; 
char str[6][20];

void setup()
{
  Serial.begin(115200);
  bluetooth.begin(115200);
}

void loop() {
   if(bluetooth.available())
   {
     Serial.println(bluetooth.read());
   }

   for(int i = 0; i < 6; i++)
   {
      Serial.print(ebimuTest[i]);
      dtostrf(ebimuTest[i],7,2,str[i]);
      bluetooth.write(str[i]);
      if(i <= 4)
      {
        bluetooth.write(",");
      }
      else if(i == 5)
      {
        bluetooth.write('\n');
        Serial.println();
      }
   }
   for(int i = 0; i < 6; i++)
   {
     ebimuTest[i] += 1.00;
   }
   delay(50);
}
