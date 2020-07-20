#include <SoftwareSerial.h>
//define Mcp3208 Pin
#define SBUF_SIZE 64
#define SELPIN 10 //Selection Pin 
#define DATAOUT 11//MOSI 
#define DATAIN  12//MISO 
#define SPICLOCK  13//Clock
int EBimuAsciiParser(float *item, int number_of_item);
int read_adc(int channel);
SoftwareSerial bluetooth(4, 7);
//Flex-------------------------------------------
char leftHandFlex[5][40];  //flexData -> dtostrf
int flexData[5];            //get FlexData
char vcc_buff[40];
char leftHandEbimu[6][40]; //axis_6 -> sprintf
float axis_6[6];            //get 6 axis(euler, acc)
char sbuf[SBUF_SIZE];
signed int sbuf_cnt=0;

int analogInput = 0;

void setup()
{ 
  //mcp3208 + Flex
  pinMode(SELPIN, OUTPUT); 
  pinMode(DATAOUT, OUTPUT); 
  pinMode(DATAIN, INPUT); 
  pinMode(SPICLOCK, OUTPUT); 
  digitalWrite(SELPIN,HIGH); 
  digitalWrite(DATAOUT,LOW); 
  digitalWrite(SPICLOCK,LOW);
  
  //Serial
  Serial.begin(115200);
  //Bluetooth
  bluetooth.begin(115200); 

  pinMode(analogInput, INPUT);
} 
void loop(){
   float vin = analogRead(0);
    vin = (vin * 5.0) / 1024.0;
   if(EBimuAsciiParser(axis_6,6)){
       for(int i = 0; i < 5; i++){
           flexData[i] = read_adc(i+1);
           sprintf(leftHandFlex[i],"%d",flexData[i]);
       }
       Serial.println(" ");
       //Ebimu casting
       Serial.print("  6 axis : ");
       for(int i = 0; i < 6; i++){   
           dtostrf(axis_6[i],7,2,leftHandEbimu[i]);
           Serial.print(axis_6[i]); Serial.print(" ");
       }
       Serial.println(" "); 
       dtostrf(vin,7,2,vcc_buff);
       //send Ebimu
       for(int i = 0; i < 6; i++){
           bluetooth.write(leftHandEbimu[i]);
           bluetooth.write(",");
       }
       //send Flex
       for(int i = 0; i < 5; i++){
           if(i==3)      bluetooth.write(leftHandFlex[4]);
           else if(i==4) bluetooth.write(leftHandFlex[3]);
           else          bluetooth.write(leftHandFlex[i]);
           bluetooth.write(",");

       } 
       bluetooth.write(vcc_buff);
       bluetooth.write("\n");
    }
}

int read_adc(int channel)
{
    int adcvalue = 0;
    byte commandbits = B11000000;
    commandbits|=((channel-1)<<3);
    
    digitalWrite(SELPIN,LOW); 
    for (int i=7; i>=3; i--)
    {
        digitalWrite(DATAOUT,commandbits&1<<i);
        digitalWrite(SPICLOCK,HIGH);
        digitalWrite(SPICLOCK,LOW);    
    }
    digitalWrite(SPICLOCK,HIGH);    
    digitalWrite(SPICLOCK,LOW);
    digitalWrite(SPICLOCK,HIGH);  
    digitalWrite(SPICLOCK,LOW);
    for (int i=11; i>=0; i--)
    {
        adcvalue+=digitalRead(DATAIN)<<i;
        digitalWrite(SPICLOCK,HIGH);
        digitalWrite(SPICLOCK,LOW);
    }
    digitalWrite(SELPIN, HIGH);
    return adcvalue;
}

int EBimuAsciiParser(float *item, int number_of_item)
{
    int n,i;
    int rbytes;
    char *addr; 
    int result = 0;
    
    rbytes = Serial.available();
    for(n=0;n<rbytes;n++)
    {
        sbuf[sbuf_cnt] = Serial.read();
        if(sbuf[sbuf_cnt]==0x0a)
        {
            addr = strtok(sbuf,",");
            for(i=0;i<number_of_item;i++)
            {
                item[i] = atof(addr);
                addr = strtok(NULL,",");
                if(item[3] >= 10 || item[4] >= 10 || item[5] >= 10)
                {
                    return 0;
                }
            }
            result = 1;
        }
        else if(sbuf[sbuf_cnt]=='*')
        {     
            sbuf_cnt=-1;
        }
 
        sbuf_cnt++;
        if(sbuf_cnt>=SBUF_SIZE) sbuf_cnt=0;
    }
    return result;
}
