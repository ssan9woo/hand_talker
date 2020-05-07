package com.example.ebimu_bluetooth;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 10; // 블루투스 활성화 상태
    private BluetoothAdapter bluetoothAdapter; // 블루투스 어댑터
    private Set<BluetoothDevice> devices; // 블루투스 디바이스 데이터 셋
    private BluetoothDevice bluetoothDevice; // 블루투스 디바이스
    private BluetoothSocket bluetoothSocket = null; // 블루투스 소켓
    private OutputStream outputStream = null; // 블루투스에 데이터를 출력하기 위한 출력 스트림
    private InputStream inputStream = null; // 블루투스에 데이터를 입력하기 위한 입력 스트림
    private Thread workerThread = null; // 문자열 수신에 사용되는 쓰레드
    private byte[] readBuffer; // 수신 된 문자열을 저장하기 위한 버퍼
    private int readBufferPosition; // 버퍼 내 문자 저장 위치

    private TextView textViewReceive; // 수신 된 데이터를 표시하기 위한 텍스트 뷰
    private EditText editTextSend; // 송신 할 데이터를 작성하기 위한 에딧 텍스트
    private Button buttonSend; // 송신하기 위한 버튼
    private int pariedDeviceCount;

    private TextView euler_x;
    private TextView euler_y;
    private TextView euler_z;
    private TextView acc_x;
    private TextView acc_y;
    private TextView acc_z;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        textViewReceive = (TextView) findViewById(R.id.textView_receive);
        editTextSend = (EditText) findViewById(R.id.editText_send);
        buttonSend = (Button) findViewById(R.id.button_send);
        euler_x = (TextView) findViewById(R.id.data2);
        euler_y = (TextView) findViewById(R.id.data3);
        euler_z = (TextView) findViewById(R.id.data4);
        acc_x = (TextView) findViewById(R.id.data5);
        acc_y = (TextView) findViewById(R.id.data6);
        acc_z = (TextView) findViewById(R.id.data7);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData(editTextSend.getText().toString());
            }
        });


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // 블루투스 어댑터를 디폴트 어댑터로 설정
        //bluetoothAdapter.getRemoteDevice(address)
        if (bluetoothAdapter == null)
        { // 디바이스가 블루투스를 지원하지 않을 때
            finish();// 여기에 처리 할 코드를 작성하세요.
        }
        else { // 디바이스가 블루투스를 지원 할 때
            if (bluetoothAdapter.isEnabled())// 블루투스가 활성화 상태 (기기에 블루투스가 켜져있음)
            {
                devices = bluetoothAdapter.getBondedDevices();
                connectDevice("sign");
                //selectBluetoothDevice(); // 블루투스 디바이스 선택 함수 호출
            }
            else{// 블루투스가 비 활성화 상태 (기기에 블루투스가 꺼져있음)
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);// 블루투스를 활성화 하기 위한 다이얼로그 출력
                startActivityForResult(intent, REQUEST_ENABLE_BT);// 선택한 값이 onActivityResult 함수에서 콜백된다.
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (requestCode == RESULT_OK)
                { // '사용'을 눌렀을 때
                    //selectBluetoothDevice(); // 블루투스 디바이스 선택 함수 호출
                }
                else { // '취소'를 눌렀을 때
                    // 여기에 처리 할 코드를 작성하세요.
                }
                break;
        }
    }
    /*
    public void selectBluetoothDevice() {

        devices = bluetoothAdapter.getBondedDevices();// 이미 페어링 되어있는 블루투스 기기를 찾습니다.

        pariedDeviceCount = devices.size();// 페어링 된 디바이스의 크기를 저장

        if (pariedDeviceCount == 0) // 페어링 되어있는 장치가 없는 경우
        {
            // 페어링을 하기위한 함수 호출
        }
        else { // 페어링 되어있는 장치가 있는 경우
            AlertDialog.Builder builder = new AlertDialog.Builder(this);// 디바이스를 선택하기 위한 다이얼로그 생성

            builder.setTitle("페어링 되어있는 블루투스 디바이스 목록");
            List<String> list = new ArrayList<>();  // 페어링 된 각각의 디바이스의 이름과 주소를 저장

            for (BluetoothDevice bluetoothDevice : devices) // 모든 디바이스의 이름을 리스트에 추가
            {
                list.add(bluetoothDevice.getName());
            }
            list.add("취소");

            final CharSequence[] charSequences = list.toArray(new CharSequence[list.size()]);// List를 CharSequence 배열로 변경
            list.toArray(new CharSequence[list.size()]);


            builder.setItems(charSequences, new DialogInterface.OnClickListener() {// 해당 아이템을 눌렀을 때 호출 되는 이벤트 리스너
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    connectDevice(charSequences[which].toString());// 해당 디바이스와 연결하는 함수 호출
                }
            });

            builder.setCancelable(false); // 뒤로가기 버튼 누를 때 창이 안닫히도록 설정
            AlertDialog alertDialog = builder.create(); // 다이얼로그 생성
            alertDialog.show();
        }
    }*/

    public void connectDevice(String deviceName) {
        for (BluetoothDevice tempDevice : devices) { // 페어링 된 디바이스들을 모두 탐색
            if (deviceName.equals(tempDevice.getName())) { // 사용자가 선택한 이름과 같은 디바이스로 설정하고 반복문 종료
                bluetoothDevice = tempDevice;
                break;
            }
        }

        UUID uuid = java.util.UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // UUID 생성

        try { // Rfcomm 채널을 통해 블루투스 디바이스와 통신하는 소켓 생성
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();// 데이터 송,수신 스트림을 얻어옵니다.
            inputStream = bluetoothSocket.getInputStream();

            receiveData(); // 데이터 수신 함수 호출
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void receiveData() {

        final Handler handler = new Handler();
        readBufferPosition = 0; // 데이터를 수신하기 위한 버퍼를 생성
        readBuffer = new byte[1024];

        workerThread = new Thread(new Runnable() { // 데이터를 수신하기 위한 쓰레드 생성
            @Override
            public void run() {
                while (!(Thread.currentThread().isInterrupted())) {
                    try {
                        int byteAvailable = inputStream.available(); // 데이터를 수신했는지 확인합니다.
                        if (byteAvailable > 0) { // 데이터가 수신 된 경우
                            byte[] bytes = new byte[byteAvailable]; // 입력 스트림에서 바이트 단위로 읽어 옵니다.
                            inputStream.read(bytes);

                            for (int i = 0; i < byteAvailable; i++) { // 입력 스트림 바이트를 한 바이트씩 읽어 옵니다.
                                byte tempByte = bytes[i];

                                if (tempByte == '\n') { // 개행문자를 기준으로 받음(한줄)
                                    byte[] encodedBytes = new byte[readBufferPosition]; // readBuffer 배열을 encodedBytes로 복사
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String text = new String(encodedBytes, "US-ASCII");// 인코딩 된 바이트 배열을 문자열로 변환
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        @SuppressLint("SetTextI18n")
                                        @Override
                                        public void run() {
                                            String[] ebimu = new String[6];
                                            for(int i = 0; i < 6; i ++)
                                            {
                                                ebimu[i] = "";
                                            }
                                            int j = 0;
                                            for(int i = 0 ; i < text.length(); i++)
                                            {
                                                if(text.charAt(i) == ',')
                                                {
                                                    j += 1;
                                                    continue;
                                                }
                                                else
                                                {
                                                    ebimu[j] += text.charAt(i);
                                                }
                                            }
                                            euler_x.setText(ebimu[0]);
                                            euler_y.setText(ebimu[1]);
                                            euler_z.setText(ebimu[2]);
                                            acc_x.setText(ebimu[3]);
                                            acc_y.setText(ebimu[4]);
                                            acc_z.setText(ebimu[5]);
                                        }
                                    });
                                }
                                else { // 개행 문자가 아닐 경우
                                    readBuffer[readBufferPosition++] = tempByte;
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        workerThread.start();
    }

    void sendData(String text) {
        text += "\n"; // 문자열에 개행문자("\n")를 추가해줍니다.
        try{
            outputStream.write(text.getBytes()); // 데이터 송신
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected void onDestroy() {
        try{
            workerThread.interrupt();
            inputStream.close();
            outputStream.close();
            bluetoothSocket.close();
        }catch(Exception e){
        }
        super.onDestroy();
    }
}