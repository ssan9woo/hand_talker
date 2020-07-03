// Ebimu 밀리거나 튀는거 초기 Ebimu Setting과(하이퍼 터미널) delay(ms)의 속도가 둘이 일치하는지,
// 일치하지 않다면 주파수 일치시킨 후 밀리는지 Test 해봐야함.

/*
//#include <CapacitiveSensor.h>
#include <SoftwareSerial.h>
SoftwareSerial bluetooth(4, 7);

//define Mcp3208 Pin
#define SBUF_SIZE 64
#define SELPIN 10 //Selection Pin 
#define DATAOUT 11//MOSI 
#define DATAIN  12//MISO 
#define SPICLOCK  13//Clock 


//Flex-------------------------------------------
char rightHandFlex[6][40];  //flexData -> dtostrf
int flexData[6];            //get FlexData
//-----------------------------------------------


//Ebimu------------------------------------------
char rightHandEbimu[6][40]; //axis_6 -> sprintf
float axis_6[6];            //get 6 axis(euler, acc)
char sbuf[SBUF_SIZE];
signed int sbuf_cnt=0;
//-----------------------------------------------


//Capacitor----------------------------------------
//CapacitiveSensor leftCapacitor = CapacitiveSensor(8,9);     
//CapacitiveSensor rightCapacitor = CapacitiveSensor(10,11); 
//char capacitor[2][40];
//-----------------------------------------------


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
                    Serial.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
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
    digitalWrite(SELPIN, HIGH); //turn off device
    return adcvalue;
}


void loop() 
{   
    if(EBimuAsciiParser(axis_6,6))
    {
        //Flex casting
        for(int i = 0; i < 5; i++)
        {
            flexData[i] = read_adc(i+1);
            sprintf(rightHandFlex[i],"%d",flexData[i]);
            Serial.print("  Flex" + String(i+1) + ":  "); Serial.print(flexData[i],DEC);
        }
        Serial.println(" ");


        //Ebimu casting
        Serial.print("  6 axis : ");
        for(int i = 0; i < 6; i++)
        {   
            dtostrf(axis_6[i],7,2,rightHandEbimu[i]);
            Serial.print(axis_6[i]); Serial.print(" ");
        }
        Serial.println(" ");
  

        //Capacitor casting (int로 되는지 확인)
        //long leftC = leftCapacitor.capacitiveSensorRaw(30);    
        //long rightC = rightCapacitor.capacitiveSensorRaw(30);
        //sprintf(capacitor[0],"%l",leftC);
        //sprintf(capacitor[1],"%l",rightC);

              
        //send Ebimu
        for(int i = 0; i < 6; i++)
        {
            bluetooth.write(rightHandEbimu[i]);
            bluetooth.write(",");
        }
  
  
        //send Flex
        for(int i = 0; i < 5; i++)
        {
            bluetooth.write(rightHandFlex[i]);
            bluetooth.write(",");
        }


        //send Capacitor
        //bluetooth.write(capacitor[0]);
        //bluetooth.write(",");
        //bluetooth.write(capacitor[1]);
        //bluetooth.write("\n");

    }
    delay(50);
}
*/
#include <CapacitiveSensor.h>

CapacitiveSensor   cs_8_9 = CapacitiveSensor(8,9);     // 8번이 송신핀, 9번이 수신핀 <1번 터치패드>


void setup() 
{
  Serial.begin(115200);
  pinMode(2, OUTPUT);
}

void loop() 
{
  long SEN1 = cs_8_9.capacitiveSensorRaw(10);    // 1번 터치패드 값 수신 <접촉시 55~60의 정수값 출력>
  Serial.print( SEN1 );
  Serial.print(" ");
  if(SEN1 > 0)    // 1번 터치패드 접촉시 1번 LED 점등
  {
    digitalWrite(2,HIGH);
  }
  else
  {
    digitalWrite(2,LOW);
  }

}
