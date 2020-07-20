/*#include <SoftwareSerial.h>
#include <CapacitiveSensor.h>
#define SBUF_SIZE 64
#define SELPIN 10
#define DATAOUT 11
#define DATAIN  12
#define SPICLOCK  13
#define SBUF_SIZE 64
SoftwareSerial bluetooth(4, 7);


int analogInput = 0;
float vout = 0.0;
float vin = 0.0;
float R1 = 47000.0; // resistance of R1 (100K) -see text!
float R2 = 47000.0; // resistance of R2 (10K) - see text!
int value = 0;
float volt_value = 0;
char buff[SBUF_SIZE];


void setup() {
  pinMode(analogInput, INPUT);
  bluetooth.begin(115200);
}


void loop() {
  // 입력 전압 측정
  value = analogRead(analogInput);
  vout = (value * 5.0) / 1024.0;
  vin = vout / (R2 / (R1 + R2));
  Serial.print("INTPUT V = ");
  Serial.println(vin);
  Serial.println(volt_value);
  
  dtostrf(vin,7,2,buff);
  bluetooth.write(buff);
  bluetooth.write("\n");
  volt_value = 0;
}



int EBimuAsciiParser(float *item, int number_of_item);
int read_adc(int channel);
CapacitiveSensor   cs_4_5 = CapacitiveSensor(3, 4);
//CapacitiveSensor   cs_6_7 = CapacitiveSensor(5, 6); 
SoftwareSerial bluetooth(4, 7);
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
long capacitor_left;
long capacitor_right;
char capacitor_buff[2][40];

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
void loop() 
{
  
   capacitor_left = cs_4_5.capacitiveSensorRaw(30);    // 1번 터치패드 값 수신 <접촉시 55~60의 정수값 출력>
   capacitor_right = cs_6_7.capacitiveSensorRaw(30);    // 2번 터치패드 값 수신 <접촉시 55~60의 정수값 출력> 
   Serial.print(capacitor_left);
   Serial.print(" ");
   Serial.println(capacitor_right); 
   
   if(EBimuAsciiParser(axis_6,6)){
       for(int i = 0; i < 5; i++){
           flexData[i] = read_adc(i+3);
           sprintf(rightHandFlex[i],"%d",flexData[i]);
           Serial.print("  Flex" + String(i+1) + ":  "); Serial.print(flexData[i],DEC);
       }
       Serial.println(" ");
       //Ebimu casting
       Serial.print("  6 axis : ");
       for(int i = 0; i < 6; i++){   
           dtostrf(axis_6[i],7,2,rightHandEbimu[i]);
           Serial.print(axis_6[i]); Serial.print(" ");
       }
       Serial.println(" ");      
       //send Ebimu
       for(int i = 0; i < 6; i++){
           bluetooth.write(rightHandEbimu[i]);
           bluetooth.write(",");
       }
       //send Flex
       for(int i = 0; i < 5; i++){
           bluetooth.write(rightHandFlex[i]);
           if(i<4) bluetooth.write(",");
           else    bluetooth.write("\n");
       }
       
       //send Capacitor
       if(capacitor_left>50){
           sprintf(capacitor_buff[0],"1");
       }
       else{
           sprintf(capacitor_buff[0],"0");
       }
       if(capacitor_right>50){
           sprintf(capacitor_buff[1],"1");
       }
       else{
           sprintf(capacitor_buff[1],"0");
       }
       bluetooth.write(capacitor_buff[0]);
       bluetooth.write(",");
       bluetooth.write(capacitor_buff[1]);
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
    digitalWrite(SELPIN, HIGH); //turn off device
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
}*/

void setup()
{
  pinMode(0,INPUT);
  Serial.begin(115200);
}

void loop()
{
  float value = analogRead(0);
  value = (value * 5.0) / 1024.0;
  Serial.println(value);
  delay(50);
}
