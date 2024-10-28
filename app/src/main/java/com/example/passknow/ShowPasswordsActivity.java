package com.example.passknow;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Map;

public class ShowPasswordsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "PasswordPrefs";
    private ArrayAdapter<String> adapter;
    private ArrayList<String> passwordList;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_passwords);

        ListView listView = findViewById(R.id.listViewPasswords);
        passwordList = new ArrayList<>();
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        loadPasswords();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, passwordList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedEntry = passwordList.get(position);
                showOptionsDialog(selectedEntry);
            }
        });
    }

    private void loadPasswords() {
        passwordList.clear();
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            passwordList.add(entry.getKey() + ": " + entry.getValue().toString());
        }
    }

    private void showOptionsDialog(final String selectedEntry) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Opciones");

        String[] options = {"Editar", "Eliminar"};
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String passwordName = selectedEntry.split(":")[0].trim();

                switch (which) {
                    case 0: // Editar
                        showEditPasswordDialog(passwordName);
                        break;
                    case 1: // Eliminar
                        deletePassword(passwordName);
                        break;
                }
            }
        });

        builder.show();
    }

    private void showEditPasswordDialog(final String passwordName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Contraseña");

        final EditText input = new EditText(this);
        input.setHint("Nueva contraseña");
        builder.setView(input);

        builder.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newPassword = input.getText().toString().trim();
                if (!newPassword.isEmpty()) {
                    savePassword(passwordName, newPassword);
                    loadPasswords();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(ShowPasswordsActivity.this, "Contraseña actualizada", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ShowPasswordsActivity.this, "La contraseña no puede estar vacía", Toast.LENGTH_SHORT).show();
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

    private void savePassword(String passwordName, String newPassword) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(passwordName, newPassword);
        editor.apply();
    }

    private void deletePassword(String passwordName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(passwordName);
        editor.apply();

        loadPasswords();
        adapter.notifyDataSetChanged();

        Toast.makeText(this, "Contraseña eliminada", Toast.LENGTH_SHORT).show();
    }
}

