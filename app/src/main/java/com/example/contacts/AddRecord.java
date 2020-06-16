package com.example.contacts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.print.PrinterId;
import android.provider.MediaStore;
import android.renderscript.Sampler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class AddRecord extends AppCompatActivity {

    private ImageView personImg;
    private EditText personName, personPhone, personCategory;
    Button addBtn;
    ActionBar actionBar;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 101;

    private static final int IMAGE_PICK_CAMERA_CODE = 102;
    private static final int IMAGE_PICK_GALLERY_CODE = 103;

    private String[] cameraPermissions;
    private String[] storagePermissions;
    private Uri imageUri;
    private String name, phone, category, timeStamp;
    private DatabaseHelper dbHelper;

    AwesomeValidation validation;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_record);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Add contact");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        personImg = findViewById(R.id.personImg);
        personName = findViewById(R.id.personName);
        personPhone = findViewById(R.id.personPhone);
        personCategory = findViewById(R.id.personCategory);
        addBtn = findViewById(R.id.addBtn);

        validation = new AwesomeValidation(ValidationStyle.BASIC);
        validation.addValidation(this,R.id.personName, RegexTemplate.NOT_EMPTY,R.string.invalid_name);
        validation.addValidation(this,R.id.personPhone, RegexTemplate.NOT_EMPTY,R.string.invalide_mobile);

        cameraPermissions = new String[] {
            Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        storagePermissions = new String[] {
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        dbHelper = new DatabaseHelper(this);

        personImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                imagePickDialog();
            }
        });
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               if(validation.validate()){
                     getData();
               }
               else{
                   Toast.makeText(AddRecord.this, "Validation faild.", Toast.LENGTH_SHORT).show();
                }

            }
        });

      }

    private void getData() {

        name = ""+personName.getText().toString().trim();
        phone = ""+personPhone.getText().toString().trim();
        category = ""+personCategory.getText().toString().trim();

        timeStamp = ""+System.currentTimeMillis();

        long id = dbHelper.insertInfo(
                ""+name,
                ""+phone,
                ""+category,
                ""+imageUri,
                ""+timeStamp,
                ""+timeStamp
        );

        startActivity(new Intent(AddRecord.this, MainActivity.class));
    }

    private void imagePickDialog() {

        String[] options = {"Camera", "Gallery"};

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select for image");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(which ==0 ){
                   if(!checkCameraPermission()){
                       requestCameraPermission();
                   }
                   else {
                       pickFromCamera();
                   }
                }
                else if (which == 1){

                    if(! checkStoragePermission()){
                        requestStoragePermission();

                    }
                    else {
                        pickFromStorage();
                    }
                }

            }
        });
        builder.create().show();
    }

    private void pickFromStorage() {
        Intent galleryIntent = new Intent (Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Image title");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
         return  result;
      }
      private void requestStoragePermission() {
          ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
      }
      private boolean checkCameraPermission(){

          boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                  == (PackageManager.PERMISSION_GRANTED);

           boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);

           return result && result1;

      }
      private void requestCameraPermission(){
         ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
      }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case CAMERA_REQUEST_CODE: {
                if(grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    
                    if (cameraAccepted && storageAccepted){
                        pickFromCamera();
                    }
                    else {
                        Toast.makeText(this, "Camera permission required!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if(grantResults.length>0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if(storageAccepted){
                        pickFromStorage();
                    }
                    else {
                        Toast.makeText(this, "Storage permission required!", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

          if(resultCode == RESULT_OK){

              if(requestCode == IMAGE_PICK_GALLERY_CODE){

                  CropImage.activity(data.getData())
                          .setGuidelines(CropImageView.Guidelines.ON)
                          .setAspectRatio(1,1)
                          .start(this);
              }
              else if(requestCode == IMAGE_PICK_CAMERA_CODE){

                  CropImage.activity(imageUri)
                          .setGuidelines(CropImageView.Guidelines.ON)
                          .setAspectRatio(1,1)
                          .start(this);
              }
              else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){

                  CropImage.ActivityResult result = CropImage.getActivityResult(data);

                  if(resultCode == RESULT_OK) {
                       Uri resultUri = result.getUri();
                       imageUri = resultUri;
                       personImg.setImageURI(resultUri);
                  }
                  else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){

                      Exception error = result.getError();
                      Toast.makeText(this, ""+error, Toast.LENGTH_SHORT).show();
                  }
              }
          }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}