/*
#include <SoftwareSerial.h>
#define SBUF_SIZE 1024

int EBimuAsciiParser(float *item, int number_of_item);

int flag = 0;
char Ebimu[6][20];
char sbuf[SBUF_SIZE];
signed int sbuf_cnt=0;

void setup()
{
  Serial.begin(115200);
}

void loop() {
  float euler[6]; 
  
    if(EBimuAsciiParser(euler, 6))
    {
        Serial.print("euler x : ");
        Serial.print(euler[0]);
        Serial.print("euler y : ");
        Serial.print(euler[1]);
        Serial.print("euler z : ");
        Serial.print(euler[2]);
        Serial.print("acc x : ");
        Serial.print(euler[3]);
        Serial.print("acc y : ");
        Serial.print(euler[4]);
        Serial.print("acc z : ");
        Serial.println(euler[5]);

    }
    //delay(20);
}

int EBimuAsciiParser(float *item, int number_of_item) //Ebimu 센서값 받아오는 func
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
              if(i>2 && i<6){
                  if(!(item[i]<10 && item[i]>-10))
                  {    
                      Serial.print(item[i]);       
                      Serial.println("   !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                      //return 0; 
                  } 
              }
              addr = strtok(NULL,",");
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
*/

// use mcp3208, ebimu
#include <SoftwareSerial.h>
#include <CapacitiveSensor.h>
CapacitiveSensor   cs_8_9 = CapacitiveSensor(8,9);     // 8번이 송신핀, 9번이 수신핀 <1번 터치패드>
#define SBUF_SIZE 64

#define BTtx        4 
#define BTrx        7
SoftwareSerial bluetooth(BTtx, BTrx);

#define SELPIN 10 //Selection Pin 
#define DATAOUT 11//MOSI 
#define DATAIN  12//MISO 
#define SPICLOCK  13//Clock 
int readvalue; 
char buf1[40]; char buf2[40]; 
char buf3[40]; char buf4[40]; char buf5[40];
char ebimu1[40]; char ebimu2[40]; char ebimu3[40];
char ebimu4[40]; char ebimu5[40]; char ebimu6[40];
float euler[6];
char sbuf[SBUF_SIZE];
signed int sbuf_cnt=0;
 
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
           }
 
           result = 1;
       }
     else if(sbuf[sbuf_cnt]=='*')
       {   sbuf_cnt=-1;
       }
 
     sbuf_cnt++;
     if(sbuf_cnt>=SBUF_SIZE) sbuf_cnt=0;
  }
  
  return result;
}

void setup(){ 
 //set pin modes
 pinMode(2, OUTPUT); 
 pinMode(SELPIN, OUTPUT); 
 pinMode(DATAOUT, OUTPUT); 
 pinMode(DATAIN, INPUT); 
 pinMode(SPICLOCK, OUTPUT); 
 //disable device to start with 
 digitalWrite(SELPIN,HIGH); 
 digitalWrite(DATAOUT,LOW); 
 digitalWrite(SPICLOCK,LOW); 

 Serial.begin(115200);
 bluetooth.begin(115200); 
} 

int read_adc(int channel){
  int adcvalue = 0;
  byte commandbits = B11000000; //command bits - start, mode, chn (3), dont care (3)

  //allow channel selection
  commandbits|=((channel-1)<<3);

  digitalWrite(SELPIN,LOW); //Select adc
  // setup bits to be written
  for (int i=7; i>=3; i--){
    digitalWrite(DATAOUT,commandbits&1<<i);
    //cycle clock
    digitalWrite(SPICLOCK,HIGH);
    digitalWrite(SPICLOCK,LOW);    
  }

  digitalWrite(SPICLOCK,HIGH);    //ignores 2 null bits
  digitalWrite(SPICLOCK,LOW);
  digitalWrite(SPICLOCK,HIGH);  
  digitalWrite(SPICLOCK,LOW);

  //read bits from adc
  for (int i=11; i>=0; i--){
    adcvalue+=digitalRead(DATAIN)<<i;
    //cycle clock
    digitalWrite(SPICLOCK,HIGH);
    digitalWrite(SPICLOCK,LOW);
  }
  digitalWrite(SELPIN, HIGH); //turn off device
  return adcvalue;
}


void loop() { 
 
 //if(bluetooth.available())
 //{
    if(EBimuAsciiParser(euler,6))
    {
     int a1 = read_adc(1); Serial.print("a1 : "); Serial.println(a1,DEC); 
     int a2 = read_adc(2); Serial.print("a2 : "); Serial.println(a2,DEC);
     int a3 = read_adc(3); Serial.print("a3 : "); Serial.println(a3,DEC);
     int a4 = read_adc(4); Serial.print("a4 : "); Serial.println(a4,DEC);
     int a5 = read_adc(5); Serial.print("a5 : "); Serial.println(a5,DEC); 

     Serial.println(" ");
     dtostrf(a1,7,2,buf1); dtostrf(a2,7,2,buf2); 
     dtostrf(a3,7,2,buf3); dtostrf(a4,7,2,buf4); dtostrf(a5,7,2,buf5);

     
     bluetooth.write(buf1); bluetooth.write(",");
     bluetooth.write(buf2); bluetooth.write(",");
     bluetooth.write(buf3); bluetooth.write(",");
     bluetooth.write(buf4); bluetooth.write(",");
     bluetooth.write(buf5); bluetooth.write(",");

     Serial.print("x : ");
     Serial.print(euler[0]);   
     Serial.print(" y : ");
     Serial.print(euler[1]); 
     Serial.print(" z : ");  
     Serial.println(euler[2]);
     Serial.print("accx : ");
     Serial.print(euler[3]);   
     Serial.print(" accy : ");
     Serial.print(euler[4]); 
     Serial.print(" accz : ");  
     Serial.println(euler[5]);

     float x = euler[0]; float y = euler[1]; float z = euler[2];
     float accx = euler[3]; float accy = euler[4]; float accz = euler[5];
     
     dtostrf(x,7,2,ebimu1); dtostrf(y,7,2,ebimu2); dtostrf(z,7,2,ebimu3);
     dtostrf(accx,7,2,ebimu4); dtostrf(accy,7,2,ebimu5); dtostrf(accz,7,2,ebimu6);
     
     bluetooth.write(ebimu1); bluetooth.write(",");
     bluetooth.write(ebimu2); bluetooth.write(",");
     bluetooth.write(ebimu3); bluetooth.write(",");
     bluetooth.write(ebimu4); bluetooth.write(",");
     bluetooth.write(ebimu5); bluetooth.write(",");
     bluetooth.write(ebimu6); bluetooth.write('\n');
     Serial.println(" ");
    }
     //delay(100);
  //} 
}
