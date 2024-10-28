package com.example.passknow;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.security.SecureRandom;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMBERS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()-_=+";
    private static final int DEFAULT_PASSWORD_LENGTH = 12;
    private static final String PREFS_NAME = "PasswordPrefs";
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 30;

    private EditText etLength;
    private CheckBox cbUppercase, cbLowercase, cbNumbers, cbSymbols;
    private TextView tvPassword;
    private Button btnGenerate, btnSave, btnCopy, btnShowSavedPasswords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etLength = findViewById(R.id.etLength);
        cbUppercase = findViewById(R.id.cbUppercase);
        cbLowercase = findViewById(R.id.cbLowercase);
        cbNumbers = findViewById(R.id.cbNumbers);
        cbSymbols = findViewById(R.id.cbSymbols);
        tvPassword = findViewById(R.id.tvPassword);
        btnGenerate = findViewById(R.id.btnGenerate);
        btnCopy = findViewById(R.id.btnCopy);
        btnSave = findViewById(R.id.btnSave);
        btnShowSavedPasswords = findViewById(R.id.btnShowSavedPasswords); // Botón para mostrar contraseñas guardadas

        btnGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int length = getPasswordLength();
                String password = generatePassword(length);
                if (!password.isEmpty()) {
                    tvPassword.setText(password);
                }
            }
        });

        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = tvPassword.getText().toString();
                if (!password.isEmpty()) {
                    copyToClipboard(password);
                } else {
                    Toast.makeText(MainActivity.this, "No hay contraseña para copiar", Toast.LENGTH_SHORT).show();
                }

            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = tvPassword.getText().toString();

                if (!password.isEmpty()) {
                    showSavePasswordDialog(password);
                } else {
                    Toast.makeText(MainActivity.this, "No hay contraseña para guardar", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnShowSavedPasswords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ShowPasswordsActivity.class);
                startActivity(intent);
            }
        });
    }

    private int getPasswordLength() {
        String lengthStr = etLength.getText().toString();
        int length;
        try {
            length = Integer.parseInt(lengthStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor, ingresa un número válido", Toast.LENGTH_SHORT).show();
            return DEFAULT_PASSWORD_LENGTH;
        }

        if (length < MIN_PASSWORD_LENGTH) {
            Toast.makeText(this, "La longitud mínima es " + MIN_PASSWORD_LENGTH + " caracteres.", Toast.LENGTH_SHORT).show();
            return MIN_PASSWORD_LENGTH;
        } else if (length > MAX_PASSWORD_LENGTH) {
            Toast.makeText(this, "La longitud máxima es " + MAX_PASSWORD_LENGTH + " caracteres.", Toast.LENGTH_SHORT).show();
            return MAX_PASSWORD_LENGTH;
        }

        return length;
    }

    private String generatePassword(int length) {
        StringBuilder characterSet = new StringBuilder();
        if (cbUppercase.isChecked()) {
            characterSet.append(UPPERCASE);
        }
        if (cbLowercase.isChecked()) {
            characterSet.append(LOWERCASE);
        }
        if (cbNumbers.isChecked()) {
            characterSet.append(NUMBERS);
        }
        if (cbSymbols.isChecked()) {
            characterSet.append(SYMBOLS);
        }

        if (characterSet.length() == 0) {
            Toast.makeText(this, "Debe seleccionar al menos una opción de caracteres", Toast.LENGTH_SHORT).show();
            return "";
        }

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characterSet.length());
            password.append(characterSet.charAt(index));
        }

        return password.toString();
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Contraseña", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Contraseña copiada al portapapeles", Toast.LENGTH_SHORT).show();
    }

    private void showSavePasswordDialog(final String password) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Guardar Contraseña");

        // Crear un campo de entrada para el nombre de la contraseña
        final EditText input = new EditText(this);
        input.setHint("Ingrese un nombre para la contraseña");
        builder.setView(input);

        // Configurar los botones del diálogo
        builder.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String passwordName = input.getText().toString().trim();
                if (!passwordName.isEmpty()) {
                    savePassword(passwordName, password);
                } else {
                    Toast.makeText(MainActivity.this, "El nombre de la contraseña no puede estar vacío", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void savePassword(String passwordName, String password) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(passwordName, password);
        editor.apply();

        Toast.makeText(this, "Contraseña guardada con el nombre: " + passwordName, Toast.LENGTH_SHORT).show();
    }

    private void showSavedPasswords() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();

        if (allEntries.isEmpty()) {
            Toast.makeText(this, "No hay contraseñas guardadas", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder savedPasswords = new StringBuilder();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            savedPasswords.append(entry.getKey()).append(": ").append(entry.getValue().toString()).append("\n");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Contraseñas Guardadas");
        builder.setMessage(savedPasswords.toString());
        builder.setPositiveButton("OK", null);
        builder.show();
    }
}


