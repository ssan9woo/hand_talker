#include <CapacitiveSensor.h>
#include <SoftwareSerial.h>

#define SBUF_SIZE 64
int EBimuAsciiParser(float *item, int number_of_item);
CapacitiveSensor   cs_2_3 = CapacitiveSensor(2, 3);
CapacitiveSensor   cs_5_6 = CapacitiveSensor(5, 6); 
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
long capacitor_left;
long capacitor_right;
char capacitor_buff[2][40];
char vcc_buff[40];
void setup()
{ 
  //mcp3208 + Flex
  pinMode(SELPIN, OUTPUT); 
  pinMode(DATAOUT, OUTPUT); 
  pinMode(DATAIN, INPUT); 
  pinMode(SPICLOCK, OUTPUT);
  pinMode(0,INPUT); 
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
   float vin = analogRead(0);
   vin = (vin * 5.0) / 1024.0;
   capacitor_left = cs_2_3.capacitiveSensorRaw(30);    // 1번 터치패드 값 수신 <접촉시 55~60의 정수값 출력>
   capacitor_right = cs_5_6.capacitiveSensorRaw(30);    // 2번 터치패드 값 수신 <접촉시 55~60의 정수값 출력> 
   int flag = 1;
   Serial.print(capacitor_left); Serial.print("  ");Serial.print(capacitor_right);
   if(EBimuAsciiParser(axis_6,6))
   if(axis_6[3] >= 10 || axis_6[4] >= 10 || axis_6[5] >= 10) flag=0;
   
   for(int i = 0; i < 6; i++){
       sprintf(rightHandFlex[i],"%d",flexData[i]);
       //Serial.print(flexData[i]); Serial.print(" ");
       if(flexData[i]<1000) flag=0;
   }
   for(int i = 0; i < 6; i++){   
       dtostrf(axis_6[i],7,2,rightHandEbimu[i]);
       //Serial.print(axis_6[i]); Serial.print(" ");
       if(axis_6[i]<-180 || axis_6[i]>180) flag=0;
   }
   Serial.println("");  
   dtostrf(vin,7,2,vcc_buff);    
   //send Ebimu
   if(flag){
       for(int i = 0; i < 6; i++){
           bluetooth.write(rightHandEbimu[i]);
           bluetooth.write(",");
       }
       //send Flex
       for(int i = 0; i < 6; i++){
           bluetooth.write(rightHandFlex[i]);
           bluetooth.write(",");
       }
       
       //send Capacitor
       
       if(capacitor_left>70){
           sprintf(capacitor_buff[0],"true");
       }
       else{
           sprintf(capacitor_buff[0],"false");
       }
       if(capacitor_right>70){
           sprintf(capacitor_buff[1],"true");
       }
       else{
           sprintf(capacitor_buff[1],"false");
       }
       for(int i=0;i<2;i++){
           bluetooth.write(capacitor_buff[i]);
           bluetooth.write(",");
       }
       bluetooth.write(vcc_buff);
       bluetooth.write("\n");
    }
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
