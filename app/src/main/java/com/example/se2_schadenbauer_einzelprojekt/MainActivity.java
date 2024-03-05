package com.example.se2_schadenbauer_einzelprojekt;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

;

public class MainActivity extends AppCompatActivity {
    private static String host = "se2-submission.aau.at";
    private static int port = 20080;
    //private Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void onClickSendMatriculationNumber (View view)
    {
        TextView textResult = findViewById(R.id.textViewResult);

        getNetworkCall()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    String result = "";
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onNext(@NonNull String s) {
                        result += s;
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                        textResult.setText(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        textResult.setText(result);
                    }
                });
    }

    public Observable<String> getNetworkCall() {
        Observable<String> tcpCallObservable = Observable.create(emitter -> {
            try {
                // Connect to the server
                Socket socket = new Socket(host, port);

                // Send data to the server (if necessary)
                String matriculationNumber = ((EditText) findViewById(R.id.inputMatriculationNumber)).getText().toString();
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                out.write(matriculationNumber + "\n");
                out.flush();

                // Receive response from the server
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line).append("\n");
                }

                // Emit the response to observers
                emitter.onNext(responseBuilder.toString());

                // Close the socket
                out.close();
                reader.close();
                socket.close();
                emitter.onComplete();
            } catch (IOException e) {
                System.out.println(e.getMessage());
                emitter.onError(e);
            }
        });
        return tcpCallObservable;
    }

    public void onClickShowOnlyPrimeNumbers (View view)
    {
        try {
            TextView textResult = findViewById(R.id.textViewResult);
            EditText matriculationNumber = findViewById(R.id.inputMatriculationNumber);
            textResult.setText(getOnlyPrimeNumbersFromMatriculationNumber(matriculationNumber.getText().toString()));
        }
        catch(Exception ex)
        {
            TextView textResult = findViewById(R.id.textViewResult);
            textResult.setText("Exception occured: " + ex.getMessage());
        }
    }

    private String getOnlyPrimeNumbersFromMatriculationNumber(String matriculationNumber) throws Exception
    {
        String primes = "";
        for(char c : matriculationNumber.toCharArray() )
            if(isPrime(c))
                primes += c;

        return primes;
    }

    private boolean isPrime(char c) throws Exception
    {
        int num = Integer.parseInt(String.valueOf(c));

        if(num <= 1)
            return false;

        for(int i = 2; i <= Math.sqrt(num); i++)
        {
            if(num%i == 0)
                return false;
        }
        return true;
    }
}