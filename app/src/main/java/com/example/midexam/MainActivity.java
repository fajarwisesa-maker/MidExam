package com.example.midexam;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText etStudentName, etStudentId;
    private Spinner spinnerMajor;
    private Button btnAdd, btnEdit, btnDelete, btnView;
    private ListView listViewStudents;

    private DatabaseHelper dbHelper;
    private ArrayList<String> studentList;
    private ArrayAdapter<String> listAdapter;

    private final String[] majors = {"Informatics", "Information Systems"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etStudentName = findViewById(R.id.etStudentName);
        etStudentId = findViewById(R.id.etStudentId);
        spinnerMajor = findViewById(R.id.spinnerMajor);
        btnAdd = findViewById(R.id.btnAdd);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        btnView = findViewById(R.id.btnView);
        listViewStudents = findViewById(R.id.listViewStudents);

        dbHelper = new DatabaseHelper(this);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, majors);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMajor.setAdapter(spinnerAdapter);

        studentList = new ArrayList<>();
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, studentList);
        listViewStudents.setAdapter(listAdapter);
        loadStudentData();
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addStudent();
            }
        });
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editStudent();
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteStudent();
            }
        });
        btnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewStudents();
            }
        });
        listViewStudents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = studentList.get(position);
                String[] parts = item.split(" \\| ");
                if (parts.length == 3) {
                    String studentId = parts[0].replace("ID: ", "").trim();
                    String name = parts[1].replace("Name: ", "").trim();
                    String major = parts[2].replace("Major: ", "").trim();

                    etStudentId.setText(studentId);
                    etStudentName.setText(name);

                    if (major.equalsIgnoreCase("Informatics")) {
                        spinnerMajor.setSelection(0);
                    } else if (major.equalsIgnoreCase("Information Systems")) {
                        spinnerMajor.setSelection(1);
                    }
                }
            }
        });
    }
    private void addStudent() {
        String name = etStudentName.getText().toString().trim();
        String studentId = etStudentId.getText().toString().trim();
        String major = spinnerMajor.getSelectedItem().toString();

        if (name.isEmpty() || studentId.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (dbHelper.checkStudentIdExists(studentId)) {
            Toast.makeText(this, "Error: Student ID must be unique! This ID already exists.", Toast.LENGTH_LONG).show();
            return;
        }

        boolean isInserted = dbHelper.insertStudent(name, studentId, major);
        if (isInserted) {
            Toast.makeText(this, "Student Added Successfully!", Toast.LENGTH_SHORT).show();
            clearFields();
            loadStudentData();
        } else {
            Toast.makeText(this, "Failed to Add Student!", Toast.LENGTH_SHORT).show();
        }
    }
    private void editStudent() {
        String name = etStudentName.getText().toString().trim();
        String studentId = etStudentId.getText().toString().trim();
        String major = spinnerMajor.getSelectedItem().toString();

        if (studentId.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "Please enter Student ID and Name to edit!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!dbHelper.checkStudentIdExists(studentId)) {
            Toast.makeText(this, "Error: Student ID not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isUpdated = dbHelper.updateStudent(name, studentId, major);
        if (isUpdated) {
            Toast.makeText(this, "Student Updated Successfully!", Toast.LENGTH_SHORT).show();
            clearFields();
            loadStudentData();
        } else {
            Toast.makeText(this, "Failed to Update Student!", Toast.LENGTH_SHORT).show();
        }
    }
    private void deleteStudent() {
        String studentId = etStudentId.getText().toString().trim();

        if (studentId.isEmpty()) {
            Toast.makeText(this, "Please enter Student ID to delete!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!dbHelper.checkStudentIdExists(studentId)) {
            Toast.makeText(this, "Error: Student ID not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isDeleted = dbHelper.deleteStudent(studentId);
        if (isDeleted) {
            Toast.makeText(this, "Student Deleted Successfully!", Toast.LENGTH_SHORT).show();
            clearFields();
            loadStudentData();
        } else {
            Toast.makeText(this, "Failed to Delete Student!", Toast.LENGTH_SHORT).show();
        }
    }
    private void viewStudents() {
        Cursor cursor = dbHelper.getAllStudents();
        if (cursor.getCount() == 0) {
            showDialog("Student Records", "No student records found in database.");
            return;
        }

        StringBuilder builder = new StringBuilder();
        while (cursor.moveToNext()) {
            String studentId = cursor.getString(1); // COLUMN_STUDENT_ID
            String name = cursor.getString(2);      // COLUMN_NAME
            String major = cursor.getString(3);     // COLUMN_MAJOR

            builder.append("ID: ").append(studentId).append("\n");
            builder.append("Name: ").append(name).append("\n");
            builder.append("Major: ").append(major).append("\n\n");
        }
        cursor.close();

        showDialog("All Student Records", builder.toString());
    }
    private void loadStudentData() {
        studentList.clear();
        Cursor cursor = dbHelper.getAllStudents();
        while (cursor.moveToNext()) {
            String studentId = cursor.getString(1);
            String name = cursor.getString(2);
            String major = cursor.getString(3);

            studentList.add("ID: " + studentId + " | Name: " + name + " | Major: " + major);
        }
        cursor.close();
        listAdapter.notifyDataSetChanged();
    }
    private void clearFields() {
        etStudentName.setText("");
        etStudentId.setText("");
        spinnerMajor.setSelection(0);
    }
    private void showDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", null);
        builder.show();
    }
}
