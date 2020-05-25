#include <SoftwareSerial.h>
#define SBUF_SIZE 1024
#define FLEX_NUM 5
#define SELPIN 10 //Selection Pin 
#define DATAOUT 11//MOSI 
#define DATAIN  12//MISO 
#define SPICLOCK  13//Clock 
SoftwareSerial bluetooth(4, 7);

int EBimuAsciiParser(float *item, int number_of_item);
int Read_adc(int channel);
void Send_flex_val();
int flag = 0;
char Ebimu[6][20];
char sbuf[SBUF_SIZE];
signed int sbuf_cnt=0;

void setup()
{
  Serial.begin(115200);
  bluetooth.begin(115200);
   //set pin modes 
  pinMode(SELPIN, OUTPUT); 
  pinMode(DATAOUT, OUTPUT); 
  pinMode(DATAIN, INPUT); 
  pinMode(SPICLOCK, OUTPUT); 
  //disable device to start with 
  digitalWrite(SELPIN,HIGH); 
  digitalWrite(DATAOUT,LOW); 
  digitalWrite(SPICLOCK,LOW);
  
}

void loop() {
  float euler[6]; 
  
    if(EBimuAsciiParser(euler, 6))
    {
       for(int i = 0 ; i < 6; i++)
      {
        dtostrf(euler[i],7,2,Ebimu[i]);
        Serial.println(Ebimu[i]);
        bluetooth.write(Ebimu[i]); 
        if(i <= 4)
        {
          bluetooth.write(",");  
        }
        else if(i == 5)
        {
          bluetooth.write("\n");
        }
      }
      Send_flex_val();
    }
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


void Send_flex_val()
{
  for(int i=0;i<5;i++)
  {
    char flex_chr[20];
    int val=read_adc(i+1);
    if(i < 4)
    {
      sprintf(flex_chr,"%d",val);
      bluetooth.write(flex_chr);
      bluetooth.write(",");  
    }
    else
    {
      sprintf(flex_chr,"%d",val);
      bluetooth.write(flex_chr);
      bluetooth.write('\n');
      Serial.println();
    }
  }
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
