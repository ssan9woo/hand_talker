#include <SoftwareSerial.h>
#define SBUF_SIZE 1024
SoftwareSerial bluetooth(2, 3);

int flag = 0;
char Ebimu[6][20];
char sbuf[SBUF_SIZE];
signed int sbuf_cnt=0;


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
void setup()
{
  Serial.begin(9600);
  bluetooth.begin(9600);
}


void loop() {
  float euler[6]; 


    if(EBimuAsciiParser(euler, 6))
    {
      for(int i = 0 ; i < 6; i++)
      {
        Serial.print(euler[i]);
        dtostrf(euler[i],7,2,Ebimu[i]);
        bluetooth.write(Ebimu[i]);
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
    }
    //delay(50);
    
    
}
