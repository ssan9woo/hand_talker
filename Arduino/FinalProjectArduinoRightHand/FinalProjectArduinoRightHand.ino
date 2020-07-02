#include <SoftwareSerial.h>
#include <string.h>
#define SBUF_SIZE 512
SoftwareSerial bluetooth(2,3);

int EBimuAsciiParser(float *item, int number_of_item);
char Ebimu[6][20];
char sbuf[SBUF_SIZE];
signed int sbuf_cnt=0;

void setup()
{
  Serial.begin(9600);
  bluetooth.begin(9600);

}

void loop() {
  float euler[6]; 
  char buff[SBUF_SIZE]="";
  String str="";
    if(EBimuAsciiParser(euler, 6))
    {
       for(int i = 0 ; i < 6; i++)
      {
        str+=String(euler[i]);
        if(i < 5 )
          str+=",";
      }    
    }

    str.toCharArray(buff,str.length());
    if(strlen(buff))
    {
      strcat(buff,"\n");
      bluetooth.write(buff);
      Serial.println(buff);
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

              if(3<=i && i <= 5)
              {
                if (item[i]>5)
                {
                  return 0;
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
